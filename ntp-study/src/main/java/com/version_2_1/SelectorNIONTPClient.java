package com.version_2_1;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class SelectorNIONTPClient {
    private static final String NTP_SERVER = "pool.ntp.org";
    private static final int NTP_PORT = 123;
    private static final int TIMEOUT = 5000; // 毫秒
    private static final int NTP_PACKET_SIZE = 48;
    
    // 复用的DatagramChannel和服务器地址
    private final DatagramChannel channel;
    private final InetSocketAddress serverAddress;
    private final Selector selector;
    
    public SelectorNIONTPClient() throws Exception {
        // 初始化并配置NIO的DatagramChannel
        channel = DatagramChannel.open();
        channel.configureBlocking(false); // 非阻塞模式
        
        // 创建选择器
        selector = Selector.open();
        
        // 将通道注册到选择器，但暂不关注任何事件
        channel.register(selector, 0);
        
        // 解析NTP服务器地址（只解析一次）
        InetAddress address = InetAddress.getByName(NTP_SERVER);
        serverAddress = new InetSocketAddress(address, NTP_PORT);
    }
    
    public Date getNTPTime() throws Exception {
        // 创建NTP请求包
        ByteBuffer buffer = ByteBuffer.allocate(NTP_PACKET_SIZE);
        
        // 设置NTP请求内容 (RFC 2030)
        buffer.put(0, (byte) 0x1B); // LI, Version, Mode (00 011 011)
        for (int i = 1; i < NTP_PACKET_SIZE; i++) {
            buffer.put(i, (byte) 0x0);
        }
        
        // 将buffer准备为发送状态
        buffer.rewind();
        
        // 发送请求
        channel.send(buffer, serverAddress);
        
        // 准备接收响应
        buffer.clear();
        
        // 注册对OP_READ事件的兴趣
        channel.register(selector, SelectionKey.OP_READ);
        
        try {
            long startTime = System.currentTimeMillis();
            
            while (System.currentTimeMillis() - startTime < TIMEOUT) {
                // 阻塞直到有通道就绪或超时
                if (selector.select(TIMEOUT) == 0) {
                    // 如果没有通道就绪，检查是否超时
                    if (System.currentTimeMillis() - startTime >= TIMEOUT) {
                        throw new Exception("NTP request timed out after " + TIMEOUT + " ms");
                    }
                    continue;
                }
                
                // 处理就绪通道
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    
                    if (key.isReadable()) {
                        // 通道已准备好读取数据
                        DatagramChannel readChannel = (DatagramChannel) key.channel();
                        InetSocketAddress sender = (InetSocketAddress) readChannel.receive(buffer);
                        
                        if (sender != null) {
                            // 收到响应，处理数据
                            buffer.flip();
                            return processNTPResponse(buffer);
                        }
                    }
                }
            }
            
            throw new Exception("NTP request timed out after " + TIMEOUT + " ms");
        } finally {
            // 取消通道对OP_READ的兴趣
            channel.register(selector, 0);
        }
    }
    
    private Date processNTPResponse(ByteBuffer buffer) {
        // 从NTP响应中读取时间戳
        // 跳过前40字节，直接读取Transmit Timestamp字段
        buffer.position(40);
        long seconds = ((buffer.get() & 0xFF) << 24) | 
                       ((buffer.get() & 0xFF) << 16) | 
                       ((buffer.get() & 0xFF) << 8) | 
                       (buffer.get() & 0xFF);
                       
        long fraction = ((buffer.get() & 0xFF) << 24) | 
                        ((buffer.get() & 0xFF) << 16) | 
                        ((buffer.get() & 0xFF) << 8) | 
                        (buffer.get() & 0xFF);
        
        // 转换NTP时间到Java时间
        // NTP时间从1900年开始，Java时间从1970年开始
        long ntpTime = seconds * 1000 + (fraction * 1000L) / 0x100000000L;
        long javaTime = ntpTime - 2208988800000L; // (70年的毫秒数)
        
        return new Date(javaTime);
    }
    
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

    
    public static void main(String[] args) {
        SelectorNIONTPClient ntpClient = null;
        
        try {
            // 创建一个NTP客户端实例（连接复用）
            ntpClient = new SelectorNIONTPClient();
            
            // 单次请求测试
            System.out.println("===== 单次请求示例 =====");
            for (int i = 0; i < 5; i++) {
                try {
                    long startTime = System.currentTimeMillis();
                    Date ntpTime = ntpClient.getNTPTime();
                    long endTime = System.currentTimeMillis();
                    
                    System.out.println("服务器时间: " + ntpTime + " (耗时: " + (endTime - startTime) + "ms)");
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    System.err.println("获取NTP时间失败: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 确保资源被释放
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