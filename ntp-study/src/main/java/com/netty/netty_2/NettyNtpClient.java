package com.netty.netty_2;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class NettyNtpClient {

    // NTP服务器地址，可以配置为国内的NTP服务器以提高访问速度
    private final String ntpServer;
    private final int ntpPort;
    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private Channel channel;
    
    // 单例实例，确保EventLoopGroup可以重用
    private static NettyNtpClient instance;
    
    // NTP时间起始点 (1900-01-01)与Unix时间起始点 (1970-01-01)的秒数差
    private static final long SECONDS_FROM_1900_TO_1970 = 2208988800L;
    
    private NettyNtpClient(String ntpServer, int ntpPort) {
        this.ntpServer = ntpServer;
        this.ntpPort = ntpPort;
        this.group = new NioEventLoopGroup(1);
        this.bootstrap = new Bootstrap();
        
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) {
                        ch.pipeline().addLast(new NtpClientHandler());
                    }
                });
        
        try {
            this.channel = bootstrap.bind(0).sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to initialize NTP client", e);
        }
    }
    
    public static synchronized NettyNtpClient getInstance() {
        if (instance == null) {
            // 默认使用阿里云的NTP服务器
            instance = new NettyNtpClient("ntp.aliyun.com", 123);
        }
        return instance;
    }
    public static synchronized NettyNtpClient getInstance(String ntpServer, int ntpPort) {
        if (instance == null) {
            instance = new NettyNtpClient(ntpServer, ntpPort);
        }
        return instance;
    }
    
    public CompletableFuture<Instant> getTime() {
        ByteBuf buf = Unpooled.buffer(48);
        // 设置NTP请求的版本和模式
        buf.writeByte((4 << 3) | 3); // 版本4，客户端模式3
        buf.writeZero(47); // 填充剩余字节为0
        
        CompletableFuture<Instant> future = new CompletableFuture<>();
        Promise<Instant> promise = new DefaultPromise<>(group.next());
        promise.addListener(f -> {
            if (f.isSuccess()) {
                future.complete(promise.getNow());
            } else {
                future.completeExceptionally(f.cause());
            }
        });
        
        channel.attr(AttributeKey.valueOf("promise")).set(promise);
        
        channel.writeAndFlush(new DatagramPacket(
                buf,
                new InetSocketAddress(ntpServer, ntpPort)
        )).addListener((ChannelFuture f) -> {
            if (!f.isSuccess()) {
                promise.setFailure(f.cause());
            }
        });
        
        return future;
    }
    
    public void shutdown() {
        group.shutdownGracefully();
    }
    
    @ChannelHandler.Sharable
    private static class NtpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
            ByteBuf buf = packet.content();
            
            if (buf.readableBytes() < 48) {
                return; // NTP响应包至少需要48字节
            }
            
            // 跳过前40个字节，获取时间戳
            buf.skipBytes(40);
            long seconds = buf.readUnsignedInt();
            long fraction = buf.readUnsignedInt();
            
            // 转换为Java Instant
            double ntpTime = seconds - SECONDS_FROM_1900_TO_1970 + (fraction / 4294967296.0);
            long millis = (long) (ntpTime * 1000);
            
            Promise<Instant> promise = (Promise<Instant>) ctx.channel().attr(AttributeKey.valueOf("promise")).get();
            if (promise != null) {
                promise.setSuccess(Instant.ofEpochMilli(millis));
            }
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Promise<Instant> promise = (Promise<Instant>) ctx.channel().attr(AttributeKey.valueOf("promise")).get();
            if (promise != null) {
                promise.setFailure(cause);
            }
        }
    }

    // 简单使用示例
    public static void main(String[] args) throws Exception {
        NettyNtpClient client = NettyNtpClient.getInstance();

        for (int i = 0; i < 100; i++) {
            CompletableFuture<Instant> future = client.getTime();
            Instant time = future.get(2, TimeUnit.SECONDS);
            System.out.println("Current NTP time: " + time);
        }
        client.shutdown();
    }
}