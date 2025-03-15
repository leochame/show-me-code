package com.version_2_0;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NIONTPClient {
    private static final String NTP_SERVER = "pool.ntp.org";
    private static final int NTP_PORT = 123;
    private static final int TIMEOUT = 5000; // 毫秒
    private static final int NTP_PACKET_SIZE = 48;
    
    // 复用的DatagramChannel和服务器地址
    private final DatagramChannel channel;
    private final InetSocketAddress serverAddress;
    
    public NIONTPClient() throws Exception {
        // 初始化并配置NIO的DatagramChannel
        channel = DatagramChannel.open();
        channel.configureBlocking(false); // 非阻塞模式
        
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
        
        // 非阻塞轮询，等待响应或超时
        long startTime = System.currentTimeMillis();
        InetSocketAddress responseAddress = null;
        
        while (System.currentTimeMillis() - startTime < TIMEOUT) {
            responseAddress = (InetSocketAddress) channel.receive(buffer);
            
            if (responseAddress != null) {
                // 收到响应，处理数据
                buffer.flip();
                return processNTPResponse(buffer);
            }
            
            // 短暂休眠，避免CPU空转
            Thread.sleep(10);
        }
        
        throw new Exception("NTP request timed out after " + TIMEOUT + " ms");
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
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }
    
    public static void main(String[] args) {
        NIONTPClient ntpClient = null;
        
        try {
            // 创建一个NTP客户端实例（连接复用）
            ntpClient = new NIONTPClient();
            
            for (int i = 0; i < 100; i++) {
                try {
                    long startTime = System.currentTimeMillis();
                    Date ntpTime = ntpClient.getNTPTime();
                    long endTime = System.currentTimeMillis();
                    
                    System.out.println("服务器时间: " + ntpTime + " (耗时: " + (endTime - startTime) + "ms)");
                    
                    // 适当的延迟，避免过于频繁的请求
                    TimeUnit.MILLISECONDS.sleep(100);
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