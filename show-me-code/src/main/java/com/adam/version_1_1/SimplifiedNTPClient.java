package com.adam.version_1_1;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.util.Date;

public class SimplifiedNTPClient {
    private static final String NTP_SERVER = "pool.ntp.org";
    private static final int TIMEOUT = 5000; // 毫秒
    
    // 单例NTP客户端
    private final NTPUDPClient client;
    private final InetAddress serverAddress;
    
    public SimplifiedNTPClient() throws Exception {
        // 初始化NTP客户端（只创建一次）
        client = new NTPUDPClient();
        client.setDefaultTimeout(TIMEOUT);
        
        // 解析服务器地址（只解析一次）
        serverAddress = InetAddress.getByName(NTP_SERVER);
    }
    
    public Date getNTPTime() throws Exception {
        // 使用同一个客户端连接获取时间
        TimeInfo timeInfo = client.getTime(serverAddress);
        timeInfo.computeDetails();
        
        // 获取时间戳
        return timeInfo.getMessage().getTransmitTimeStamp().getDate();
    }
    
    public void close() {
        if (client != null) {
            client.close();
        }
    }
    
    public static void main(String[] args) {
        SimplifiedNTPClient ntpClient = null;
        
        try {
            // 创建一个NTP客户端实例（连接复用）
            ntpClient = new SimplifiedNTPClient();
            
            for (int i = 0; i < 100; i++) {
                try {
                    Date ntpTime = ntpClient.getNTPTime();
                    System.out.println("服务器时间: " + ntpTime);
                    
                    // 适当的延迟，避免过于频繁的请求
//                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (Exception e) {
                    System.err.println("获取NTP时间失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 确保资源被释放
            if (ntpClient != null) {
                ntpClient.close();
            }
        }
    }
}