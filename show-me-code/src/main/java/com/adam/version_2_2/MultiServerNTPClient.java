package com.adam.version_2_2;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MultiServerNTPClient {
    // 多个NTP服务器
    private static final String[] NTP_SERVERS = {
            "ntp.aliyun.com"
    };
    private static final int NTP_PORT = 123;
    private static final int TIMEOUT = 5000; // 毫秒
    private static final int NTP_PACKET_SIZE = 48;
    
    // 通道和选择器
    private final DatagramChannel channel;
    private final Selector selector;
    
    // 服务器地址映射
    private final Map<String, InetSocketAddress> serverAddresses = new HashMap<>();
    
    // 存储请求和响应的映射关系
    private final Map<InetSocketAddress, Long> requestTimestamps = new ConcurrentHashMap<>();
    private final Map<InetSocketAddress, Date> responseResults = new ConcurrentHashMap<>();
    
    public MultiServerNTPClient() throws Exception {
        // 初始化NIO组件
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.bind(null);
        
        // 创建选择器
        selector = Selector.open();
        
        // 将通道注册到选择器
        channel.register(selector, 0);
        
        // 解析所有NTP服务器地址
        for (String server : NTP_SERVERS) {
            try {
                InetAddress address = InetAddress.getByName(server);
                InetSocketAddress socketAddress = new InetSocketAddress(address, NTP_PORT);
                serverAddresses.put(server, socketAddress);
            } catch (Exception e) {
                System.err.println("无法解析服务器地址: " + server + " - " + e.getMessage());
            }
        }
        
        if (serverAddresses.isEmpty()) {
            throw new Exception("没有可用的NTP服务器地址");
        }
    }
    
    /**
     * 获取多个NTP服务器的平均时间
     */
    public Date getNTPAverageTime() throws Exception {
        // 清除之前的请求状态
        requestTimestamps.clear();
        responseResults.clear();
        
        // 准备接收缓冲区
        ByteBuffer receiveBuffer = ByteBuffer.allocate(NTP_PACKET_SIZE);
        
        // 向所有服务器发送请求
        for (Map.Entry<String, InetSocketAddress> entry : serverAddresses.entrySet()) {
            String serverName = entry.getKey();
            InetSocketAddress serverAddress = entry.getValue();
            
            try {
                // 创建NTP请求包
                ByteBuffer sendBuffer = createNTPRequest();
                
                // 发送请求
                channel.send(sendBuffer, serverAddress);
                channel.register(selector, SelectionKey.OP_READ);

                // 记录请求时间戳
                requestTimestamps.put(serverAddress, System.currentTimeMillis());
                
                System.out.println("已向服务器发送请求: " + serverName);
            } catch (Exception e) {
                System.err.println("向服务器 " + serverName + " 发送请求失败: " + e.getMessage());
            }
        }
        
        // 注册对OP_READ事件的兴趣
        channel.register(selector, SelectionKey.OP_READ);
        
        try {
            long startTime = System.currentTimeMillis();
            int totalServers = serverAddresses.size();
            
            // 等待响应，直到收集到所有响应或超时
            while (responseResults.size() < totalServers && 
                   System.currentTimeMillis() - startTime < TIMEOUT)
            {
                
                // 阻塞直到有通道就绪或超时
                if (selector.select(Math.max(1, TIMEOUT - (System.currentTimeMillis() - startTime))) == 0) {
                    continue; // 超时但还未达到总超时时间
                }
                
                // 处理就绪通道
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    
                    if (key.isReadable()) {
                        // 通道已准备好读取数据
                        receiveBuffer.clear();
                        InetSocketAddress sender = (InetSocketAddress) channel.receive(receiveBuffer);
                        
                        if (sender != null && requestTimestamps.containsKey(sender)) {
                            // 收到响应，处理数据
                            receiveBuffer.flip();
                            
                            try {
                                Date ntpTime = processNTPResponse(receiveBuffer);
                                // 存储结果
                                responseResults.put(sender, ntpTime);
                                
                                // 查找服务器名称
                                String serverName = serverAddresses.entrySet().stream()
                                    .filter(entry -> entry.getValue().equals(sender))
                                    .map(Map.Entry::getKey)
                                    .findFirst()
                                    .orElse("未知服务器");
                                
                                System.out.println("已收到服务器响应: " + serverName + ", 时间: " + ntpTime);
                            } catch (Exception e) {
                                System.err.println("处理来自 " + sender + " 的响应失败: " + e.getMessage());
                            }
                        }
                    }
                }
            }
            
            // 计算平均时间
            return calculateAverageTime();
            
        } finally {
            // 取消通道对OP_READ的兴趣
            channel.register(selector, 0);
        }
    }
    
    /**
     * 创建NTP请求缓冲区
     */
    private ByteBuffer createNTPRequest() {
        ByteBuffer buffer = ByteBuffer.allocate(NTP_PACKET_SIZE);
        
        // 设置NTP请求内容 (RFC 2030)
        buffer.put(0, (byte) 0x1B); // LI, Version, Mode (00 011 011)
        for (int i = 1; i < NTP_PACKET_SIZE; i++) {
            buffer.put(i, (byte) 0x0);
        }
        
        // 将buffer准备为发送状态
        buffer.rewind();
        return buffer;
    }
    
    /**
     * 处理NTP响应，提取时间戳
     */
    private Date processNTPResponse(ByteBuffer buffer) {
        // 从NTP响应中读取时间戳
        buffer.position(40);

        long seconds = ((long)(buffer.get() & 0xFF) << 24) |
                ((buffer.get() & 0xFF) << 16) |
                ((buffer.get() & 0xFF) << 8) |
                (buffer.get() & 0xFF);

        long fraction = ((long)(buffer.get() & 0xFF) << 24) |
                ((buffer.get() & 0xFF) << 16) |
                ((buffer.get() & 0xFF) << 8) |
                (buffer.get() & 0xFF);
        // 转换NTP时间到Java时间
        long ntpTime = seconds * 1000 + (fraction * 1000L) / 0x100000000L;
        long javaTime = ntpTime - 2208988800000L; // (70年的毫秒数)
        
        return new Date(javaTime);
    }
    
    /**
     * 计算收到的时间戳的平均值
     */
    private Date calculateAverageTime() throws Exception {
        if (responseResults.isEmpty()) {
            throw new Exception("没有收到任何NTP服务器响应");
        }
        
        // 收集所有时间戳
        List<Long> timestamps = responseResults.values().stream()
                .map(Date::getTime)
                .collect(Collectors.toList());
        
        // 打印收到的所有时间戳，方便分析
        System.out.println("\n收到的时间戳:");
        int i = 0;
        for (Map.Entry<InetSocketAddress, Date> entry : responseResults.entrySet()) {
            // 查找服务器名称
            String serverName = serverAddresses.entrySet().stream()
                .filter(e -> e.getValue().equals(entry.getKey()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("未知服务器");
            
            System.out.println(serverName + ": " + entry.getValue() + " (" + entry.getValue().getTime() + "ms)");
            i++;
        }
        
        // 计算简单平均值
        double averageTimestamp = timestamps.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElseThrow(() -> new Exception("计算平均时间失败"));
        
        Date averageTime = new Date(Math.round(averageTimestamp));
        System.out.println("\n平均时间: " + averageTime + " (基于 " + timestamps.size() + " 个响应)");
        
        return averageTime;
    }
    
    /**
     * 关闭资源
     */
    public void close() throws Exception {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } finally {
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
        }
    }
    
    /**
     * 主函数
     */
    public static void main(String[] args) {
        MultiServerNTPClient ntpClient = null;
        
        try {
            System.out.println("初始化多服务器NTP客户端...");
            ntpClient = new MultiServerNTPClient();
            
            System.out.println("正在从多个NTP服务器获取时间...");
            long startTime = System.currentTimeMillis();

            Date averageTime = ntpClient.getNTPAverageTime();

            long endTime = System.currentTimeMillis();
            
            System.out.println("\n===== 结果摘要 =====");
            System.out.println("本地系统时间: " + new Date());
            System.out.println("NTP平均时间: " + averageTime);
            System.out.println("时间差异: " + (averageTime.getTime() - System.currentTimeMillis()) + "ms");
            System.out.println("总耗时: " + (endTime - startTime) + "ms");
            
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (ntpClient != null) {
                try {
                    ntpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}