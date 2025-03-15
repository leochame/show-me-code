package com.netty.netty_1;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;

/**
 * 使用 Netty 获取 NTP 时间的客户端
 * 一个简易的版本
 */
public class NettyNtpClient {
    // NTP服务器地址，可以根据需要更改
    private static final String NTP_SERVER = "pool.ntp.org";
    private static final int NTP_PORT = 123;
    
    // NTP报文中时间戳偏移量
    private static final int ORIGINATE_TIME_OFFSET = 24;
    private static final int RECEIVE_TIME_OFFSET = 32;
    private static final int TRANSMIT_TIME_OFFSET = 40;
    
    // NTP时间和Unix时间的偏移(1900年到1970年的秒数)
    private static final long NTP_EPOCH_OFFSET = 2208988800L;

    public static LocalDateTime getNtpTime() throws InterruptedException, ExecutionException, TimeoutException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            final Promise<Long> timePromise = new DefaultPromise<>(group.next());
            
            bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<DatagramPacket>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
                                ByteBuf buf = msg.content();
                                
                                // 读取传输时间戳(从NTP服务器发送回来的时间)
                                long seconds = buf.getUnsignedInt(TRANSMIT_TIME_OFFSET);
                                long fraction = buf.getUnsignedInt(TRANSMIT_TIME_OFFSET + 4);
                                
                                // 将NTP时间转换为UNIX时间(毫秒)
                                long ntpTime = seconds - NTP_EPOCH_OFFSET;
                                long milliseconds = (fraction * 1000L) / 0x100000000L;
                                long timeValue = (ntpTime * 1000) + milliseconds;
                                
                                timePromise.setSuccess(timeValue);
                            }
                            
                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                timePromise.setFailure(cause);
                                ctx.close();
                            }
                        });
                    }
                });
            
            // 创建NTP请求数据包
            ByteBuf buffer = createNtpRequest();
            
            // 发送NTP请求
            Channel ch = bootstrap.bind(0).sync().channel();
            ch.writeAndFlush(new DatagramPacket(
                buffer,
                new InetSocketAddress(NTP_SERVER, NTP_PORT)
            )).sync();
            
            // 等待接收NTP响应，设置10秒超时
            Long timeMillis = timePromise.get(10, TimeUnit.SECONDS);
            
            // 转换为LocalDateTime
            return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timeMillis), 
                ZoneId.systemDefault()
            );
            
        } finally {
            group.shutdownGracefully();
        }
    }
    
    private static ByteBuf createNtpRequest() {
        ByteBuf buffer = io.netty.buffer.Unpooled.buffer(48);
        
        // 设置LI, Version, Mode
        // LI = 0 (无闰秒警告), VN = 4 (NTP版本4), Mode = 3 (客户端)
        buffer.writeByte((0 << 6) | (4 << 3) | (3));
        
        // 其他字段设置为0
        buffer.writeByte(0);      // Stratum
        buffer.writeByte(0);      // Poll Interval
        buffer.writeByte(0);      // Precision
        buffer.writeInt(0);       // Root Delay
        buffer.writeInt(0);       // Root Dispersion
        buffer.writeInt(0);       // Reference Identifier
        buffer.writeLong(0);      // Reference Timestamp
        buffer.writeLong(0);      // Originate Timestamp
        buffer.writeLong(0);      // Receive Timestamp
        
        // 设置传输时间戳为当前时间
        long now = System.currentTimeMillis();
        long seconds = (now / 1000) + NTP_EPOCH_OFFSET;
        long fraction = ((now % 1000) * 0x100000000L) / 1000;
        
        buffer.writeInt((int) seconds);
        buffer.writeInt((int) fraction);
        
        return buffer;
    }
    
    // 示例用法
    public static void main(String[] args) throws InterruptedException {
        for (int j = 0; j < 100; j++) {
            try {
                for (int i = 0; i < 10; i++) {
                    LocalDateTime ntpTime = getNtpTime();
                    System.out.println("NTP时间: " + ntpTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            sleep(100);
        }

    }
}