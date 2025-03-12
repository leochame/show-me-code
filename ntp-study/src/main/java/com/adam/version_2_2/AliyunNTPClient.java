package com.adam.version_2_2;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Date;

public class AliyunNTPClient {
    // 阿里云NTP服务器
    private static final String NTP_SERVER = "ntp.aliyun.com";
    private static final int NTP_PORT = 123;
    private static final int TIMEOUT = 5000; // 毫秒
    private static final int NTP_PACKET_SIZE = 48;

    // 通道
    private final DatagramChannel channel;

    // 服务器地址
    private final InetSocketAddress serverAddress;

    public AliyunNTPClient() throws Exception {
        // 初始化NIO组件
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.bind(null);

        // 解析NTP服务器地址
        try {
            InetAddress address = InetAddress.getByName(NTP_SERVER);
            serverAddress = new InetSocketAddress(address, NTP_PORT);
        } catch (Exception e) {
            System.err.println("无法解析服务器地址: " + NTP_SERVER + " - " + e.getMessage());
            throw new Exception("无法解析NTP服务器地址");
        }
    }

    /**
     * 获取NTP时间
     */
    public Date getNTPTime() throws Exception {
        // 准备接收缓冲区
        ByteBuffer receiveBuffer = ByteBuffer.allocate(NTP_PACKET_SIZE);

        try {
            // 创建NTP请求包
            ByteBuffer sendBuffer = createNTPRequest();

            // 发送请求
            channel.send(sendBuffer, serverAddress);
            System.out.println("已向服务器发送请求: " + NTP_SERVER);

            // 记录请求时间
            long requestTime = System.currentTimeMillis();

            // 等待响应或超时
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < TIMEOUT) {
                receiveBuffer.clear();
                InetSocketAddress sender = (InetSocketAddress) channel.receive(receiveBuffer);

                if (sender != null) {
                    // 收到响应，处理数据
                    receiveBuffer.flip();

                    try {
                        Date ntpTime = processNTPResponse(receiveBuffer);
                        System.out.println("已收到服务器响应: " + NTP_SERVER + ", 时间: " + ntpTime);
                        return ntpTime;
                    } catch (Exception e) {
                        System.err.println("处理来自 " + sender + " 的响应失败: " + e.getMessage());
                        throw e;
                    }
                }

                // 简单的非阻塞等待
                Thread.sleep(10);
            }

            throw new Exception("从NTP服务器获取响应超时");
        } catch (Exception e) {
            System.err.println("向服务器 " + NTP_SERVER + " 请求失败: " + e.getMessage());
            throw e;
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
     * 关闭资源
     */
    public void close() throws Exception {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }

    /**
     * 主函数
     */
    public static void main(String[] args) {
        AliyunNTPClient ntpClient = null;

        try {
            System.out.println("初始化阿里云NTP客户端...");
            ntpClient = new AliyunNTPClient();

            System.out.println("正在从阿里云NTP服务器获取时间...");
            long startTime = System.currentTimeMillis();

            Date ntpTime = ntpClient.getNTPTime();

            long endTime = System.currentTimeMillis();

            System.out.println("\n===== 结果摘要 =====");
            System.out.println("本地系统时间: " + new Date());
            System.out.println("阿里云NTP时间: " + ntpTime);
            System.out.println("时间差异: " + (ntpTime.getTime() - System.currentTimeMillis()) + "ms");
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