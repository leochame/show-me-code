package com.adam.aliyun.time.model;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * NTP 包的传递与接收处理
 */
public class NTPPacketBuilder {
    private static final int NTP_PACKET_SIZE = 48;
    
    public static ByteBuffer createNTPRequest() {
        ByteBuffer buffer = ByteBuffer.allocate(NTP_PACKET_SIZE);
        
        // 设置NTP请求内容 (RFC 2030)
        buffer.put(0, (byte) 0x1B); // LI = 0, Version = 3, Mode = 3 (client)
        for (int i = 1; i < NTP_PACKET_SIZE; i++) {
            buffer.put(i, (byte) 0x0);
        }
        
        // 将buffer准备为发送状态
        buffer.rewind();
        return buffer;
    }
    
    public static Date processNTPResponse(ByteBuffer buffer) {
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
}
