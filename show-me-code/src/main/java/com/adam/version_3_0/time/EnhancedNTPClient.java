package com.adam.version_3_0.time;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 增强型NTP客户端 - 基于多路复用结合卡尔曼滤波算法优化时钟漂移补偿
 * 采用多源分层时钟源+智能路由熔断机制
 */
public class EnhancedNTPClient {
    // NTP服务器配置
    private static final List<ServerConfig> NTP_SERVERS = new ArrayList<>();
    static {
        // 第一层服务器 - 阿里云主NTP服务器
        NTP_SERVERS.add(new ServerConfig("ntp.aliyun.com", 123, 1));
        NTP_SERVERS.add(new ServerConfig("ntp1.aliyun.com", 123, 1));
        
        // 第二层服务器 - 备用服务器
        NTP_SERVERS.add(new ServerConfig("ntp.ntsc.ac.cn", 123, 2));
        NTP_SERVERS.add(new ServerConfig("ntp.tencent.com", 123, 2));
        
        // 第三层服务器 - 公共NTP服务
        NTP_SERVERS.add(new ServerConfig("cn.pool.ntp.org", 123, 3));
        NTP_SERVERS.add(new ServerConfig("time.windows.com", 123, 3));
    }

    private static final int NTP_PACKET_SIZE = 48;
    private static final int TIMEOUT = 3000; // 毫秒
    private static final long SYNC_INTERVAL = 60000; // 同步间隔，默认1分钟
    private static final int MAX_FAILURES = 3; // 最大连续失败次数，触发熔断

    // NIO组件
    private final Selector selector;
    private final Map<DatagramChannel, ServerState> channelMap = new HashMap<>();
    
    // 卡尔曼滤波相关参数
    private double processNoise = 1e-6; // 过程噪声方差
    private double measurementNoise = 1e-2; // 测量噪声方差
    private double estimatedError = 1.0; // 估计误差协方差
    private double kalmanGain = 0.0; // 卡尔曼增益
    private double offsetEstimate = 0.0; // 当前偏移量估计
    private double driftEstimate = 0.0; // 漂移率估计
    
    // 服务器状态追踪
    private final Map<String, ServerMetrics> serverMetrics = new ConcurrentHashMap<>();
    
    // 时间状态
    private final AtomicLong lastSyncTime = new AtomicLong(0);
    private final AtomicLong currentOffset = new AtomicLong(0);
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    
    // 定时同步任务
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    /**
     * 构造函数 - 初始化NTP客户端
     */
    public EnhancedNTPClient() throws IOException {
        // 初始化NIO选择器
        selector = Selector.open();
        
        // 初始化所有NTP服务器连接
        for (ServerConfig config : NTP_SERVERS) {
            try {
                // 为每个服务器创建一个通道
                DatagramChannel channel = DatagramChannel.open();
                channel.configureBlocking(false);
                channel.bind(null);
                
                // 解析服务器地址
                InetAddress address = InetAddress.getByName(config.host);
                InetSocketAddress serverAddress = new InetSocketAddress(address, config.port);
                
                // 注册到选择器
                channel.register(selector, SelectionKey.OP_READ);
                
                // 保存服务器状态
                ServerState state = new ServerState(serverAddress, config);
                channelMap.put(channel, state);
                
                // 初始化服务器指标
                serverMetrics.put(config.host, new ServerMetrics(config));
                
                System.out.println("已初始化NTP服务器: " + config.host);
            } catch (Exception e) {
                System.err.println("初始化服务器失败: " + config.host + " - " + e.getMessage());
            }
        }
        
        // 启动定时同步任务
        scheduler.scheduleAtFixedRate(this::synchronizeTime, 0, SYNC_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 对外提供的时间获取接口
     * @return 经过时钟漂移补偿后的当前时间
     */
    public Date getCurrentTime() {
        // 如果尚未初始化完成，先执行同步
        if (!isInitialized.get()) {
            try {
                synchronizeTime();
            } catch (Exception e) {
                System.err.println("初始同步失败: " + e.getMessage());
            }
        }
        
        // 基于上次同步时间和计算的偏移，计算当前补偿后的时间
        long now = System.currentTimeMillis();
        long timeSinceLastSync = now - lastSyncTime.get();
        
        // 加入漂移补偿：当前偏移 + 预估漂移率 * 经过时间
        long adjustedOffset = currentOffset.get() + (long)(driftEstimate * timeSinceLastSync);
        
        return new Date(now + adjustedOffset);
    }
    
    /**
     * 多路复用时间同步过程
     */
    private void synchronizeTime() {
        // 记录同步开始时间
        long syncStartTime = System.currentTimeMillis();
        boolean syncSuccessful = false;
        
        try {
            // 向所有服务器发送请求
            sendRequests();
            
            // 等待并处理响应
            waitForResponses();
            
            // 更新同步时间
            lastSyncTime.set(syncStartTime);
            
            // 标记初始化完成
            isInitialized.set(true);
            syncSuccessful = true;
            
        } catch (Exception e) {
            System.err.println("时间同步过程失败: " + e.getMessage());
        }
        
        // 输出同步结果
        if (syncSuccessful) {
            System.out.println("时间同步完成, 偏移量: " + currentOffset.get() + "ms, 漂移率: " + 
                              String.format("%.9f", driftEstimate) + " ms/ms");
        }
    }
    
    /**
     * 向所有服务器发送NTP请求
     */
    private void sendRequests() throws IOException {
        // 为每个通道创建请求
        for (Map.Entry<DatagramChannel, ServerState> entry : channelMap.entrySet()) {
            DatagramChannel channel = entry.getKey();
            ServerState state = entry.getValue();
            
            // 检查服务器是否处于熔断状态
            ServerMetrics metrics = serverMetrics.get(state.config.host);
            if (metrics.isCircuitBroken()) {
                continue; // 跳过已熔断的服务器
            }
            
            // 创建并发送NTP请求
            ByteBuffer buffer = createNTPRequest();
            channel.send(buffer, state.address);
            
            // 更新发送时间
            state.requestTime = System.currentTimeMillis();
            state.responsePending = true;
            
            System.out.println("已向服务器发送请求: " + state.config.host);
        }
    }
    
    /**
     * 等待并处理服务器响应
     */
    private void waitForResponses() throws IOException {
        long startTime = System.currentTimeMillis();
        List<NTPResponse> responses = new ArrayList<>();
        
        // 设置响应等待超时
        while (System.currentTimeMillis() - startTime < TIMEOUT) {
            // 非阻塞选择
            int readyChannels = selector.select(100);
            if (readyChannels == 0) {
                continue;
            }
            
            // 处理就绪的通道
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                
                if (key.isReadable()) {
                    DatagramChannel channel = (DatagramChannel) key.channel();
                    ServerState state = channelMap.get(channel);
                    
                    if (state != null && state.responsePending) {
                        try {
                            // 接收响应
                            ByteBuffer buffer = ByteBuffer.allocate(NTP_PACKET_SIZE);
                            channel.receive(buffer);
                            buffer.flip();
                            
                            // 处理响应
                            long receiveTime = System.currentTimeMillis();
                            Date ntpTime = processNTPResponse(buffer);
                            
                            // 计算往返时间和偏移
                            long rtt = receiveTime - state.requestTime;
                            long offset = ntpTime.getTime() - (state.requestTime + rtt/2);
                            
                            // 更新服务器指标
                            ServerMetrics metrics = serverMetrics.get(state.config.host);
                            metrics.recordSuccess(rtt, offset);
                            
                            // 收集有效响应
                            responses.add(new NTPResponse(state.config, offset, rtt));
                            
                            System.out.println("收到来自 " + state.config.host + " 的响应, RTT: " + rtt + "ms, 偏移: " + offset + "ms");
                            
                            // 标记已响应
                            state.responsePending = false;
                        } catch (Exception e) {
                            System.err.println("处理 " + state.config.host + " 的响应失败: " + e.getMessage());
                            
                            // 更新失败次数
                            ServerMetrics metrics = serverMetrics.get(state.config.host);
                            metrics.recordFailure();
                        }
                    }
                }
            }
            
            // 检查是否所有请求都已得到响应
            boolean allDone = true;
            for (ServerState state : channelMap.values()) {
                if (state.responsePending) {
                    allDone = false;
                    break;
                }
            }
            
            if (allDone) {
                break; // 所有请求都已响应，可以提前退出
            }
        }
        
        // 处理超时未响应的服务器
        for (ServerState state : channelMap.values()) {
            if (state.responsePending) {
                ServerMetrics metrics = serverMetrics.get(state.config.host);
                metrics.recordFailure();
                state.responsePending = false;
                
                System.out.println("服务器 " + state.config.host + " 响应超时");
            }
        }
        
        // 根据多源响应更新时间偏移
        if (!responses.isEmpty()) {
            updateTimeOffset(responses);
        } else {
            System.err.println("警告：没有收到任何有效响应");
        }
    }
    
    /**
     * 基于多源响应更新时间偏移，应用卡尔曼滤波和分层选择逻辑
     */
    private void updateTimeOffset(List<NTPResponse> responses) {
        // 按层次筛选最优响应
        NTPResponse bestResponse = null;
        
        // 首先尝试使用第一层服务器的响应
        List<NTPResponse> tier1Responses = filterByTier(responses, 1);
        if (!tier1Responses.isEmpty()) {
            bestResponse = selectBestResponse(tier1Responses);
        } else {
            // 如果没有第一层响应，尝试第二层
            List<NTPResponse> tier2Responses = filterByTier(responses, 2);
            if (!tier2Responses.isEmpty()) {
                bestResponse = selectBestResponse(tier2Responses);
            } else {
                // 最后尝试第三层
                List<NTPResponse> tier3Responses = filterByTier(responses, 3);
                if (!tier3Responses.isEmpty()) {
                    bestResponse = selectBestResponse(tier3Responses);
                }
            }
        }
        
        if (bestResponse == null) {
            System.err.println("警告：无法选择有效响应");
            return;
        }
        
        // 计算时间间隔
        long now = System.currentTimeMillis();
        double dt = (now - lastSyncTime.get()) / 1000.0; // 秒为单位
        
        if (lastSyncTime.get() > 0 && dt > 0) {
            // 预测步骤
            double predictedOffset = offsetEstimate + driftEstimate * dt;
            estimatedError = estimatedError + processNoise * dt * dt;
            
            // 更新步骤
            kalmanGain = estimatedError / (estimatedError + measurementNoise);
            offsetEstimate = predictedOffset + kalmanGain * (bestResponse.offset - predictedOffset);
            estimatedError = (1 - kalmanGain) * estimatedError;
            
            // 更新漂移估计
            double measuredDrift = (bestResponse.offset - offsetEstimate) / dt;
            driftEstimate = driftEstimate + 0.1 * (measuredDrift - driftEstimate);
        } else {
            // 初始化估计值
            offsetEstimate = bestResponse.offset;
        }
        
        // 更新当前偏移
        currentOffset.set((long) offsetEstimate);
        
        System.out.println("最佳响应来自: " + bestResponse.config.host + ", 层级: " + bestResponse.config.tier + 
                         ", RTT: " + bestResponse.rtt + "ms, 采用偏移: " + (long) offsetEstimate + "ms");
    }
    
    /**
     * 按层级筛选响应
     */
    private List<NTPResponse> filterByTier(List<NTPResponse> responses, int tier) {
        List<NTPResponse> result = new ArrayList<>();
        for (NTPResponse response : responses) {
            if (response.config.tier == tier) {
                result.add(response);
            }
        }
        return result;
    }
    
    /**
     * 从给定响应中选择最佳响应（根据RTT）
     */
    private NTPResponse selectBestResponse(List<NTPResponse> responses) {
        NTPResponse best = responses.get(0);
        for (int i = 1; i < responses.size(); i++) {
            if (responses.get(i).rtt < best.rtt) {
                best = responses.get(i);
            }
        }
        return best;
    }
    
    /**
     * 创建NTP请求缓冲区
     */
    private ByteBuffer createNTPRequest() {
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
    public void close() throws IOException {
        // 关闭调度器
        scheduler.shutdown();
        
        // 关闭所有通道
        for (DatagramChannel channel : channelMap.keySet()) {
            if (channel.isOpen()) {
                channel.close();
            }
        }
        
        // 关闭选择器
        selector.close();
        
        System.out.println("NTP客户端已关闭");
    }
    
    /**
     * 服务器配置类
     */
    private static class ServerConfig {
        public final String host;
        public final int port;
        public final int tier; // 服务器层级
        
        public ServerConfig(String host, int port, int tier) {
            this.host = host;
            this.port = port;
            this.tier = tier;
        }
    }
    
    /**
     * 服务器状态类
     */
    private static class ServerState {
        public final InetSocketAddress address;
        public final ServerConfig config;
        public long requestTime;
        public boolean responsePending;
        
        public ServerState(InetSocketAddress address, ServerConfig config) {
            this.address = address;
            this.config = config;
            this.requestTime = 0;
            this.responsePending = false;
        }
    }
    
    /**
     * NTP响应类
     */
    private static class NTPResponse {
        public final ServerConfig config;
        public final long offset;
        public final long rtt;
        
        public NTPResponse(ServerConfig config, long offset, long rtt) {
            this.config = config;
            this.offset = offset;
            this.rtt = rtt;
        }
    }
    
    /**
     * 服务器指标类 - 用于跟踪服务器健康状态
     */
    private static class ServerMetrics {
        private final ServerConfig config;
        private int consecutiveFailures;
        private long lastSuccessTime;
        private long totalResponses;
        private long totalRtt;
        private long minRtt;
        private long maxRtt;
        private final AtomicBoolean circuitBroken = new AtomicBoolean(false);
        private final long breakDuration = 30000; // 熔断持续30秒
        
        public ServerMetrics(ServerConfig config) {
            this.config = config;
            this.consecutiveFailures = 0;
            this.lastSuccessTime = 0;
            this.totalResponses = 0;
            this.totalRtt = 0;
            this.minRtt = Long.MAX_VALUE;
            this.maxRtt = 0;
        }
        
        /**
         * 记录成功响应
         */
        public synchronized void recordSuccess(long rtt, long offset) {
            consecutiveFailures = 0;
            lastSuccessTime = System.currentTimeMillis();
            totalResponses++;
            totalRtt += rtt;
            minRtt = Math.min(minRtt, rtt);
            maxRtt = Math.max(maxRtt, rtt);
            
            // 重置熔断状态
            circuitBroken.set(false);
        }
        
        /**
         * 记录失败响应
         */
        public synchronized void recordFailure() {
            consecutiveFailures++;
            
            // 检查是否需要熔断
            if (consecutiveFailures >= MAX_FAILURES) {
                circuitBroken.set(true);
                
                // 安排在一段时间后重置熔断器
                new Thread(() -> {
                    try {
                        Thread.sleep(breakDuration);
                        circuitBroken.set(false);
                        consecutiveFailures = 0; // 重置失败计数
                        System.out.println("服务器 " + config.host + " 熔断已重置");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
                
                System.out.println("服务器 " + config.host + " 已熔断, 持续 " + (breakDuration/1000) + " 秒");
            }
        }
        
        /**
         * 检查服务器是否处于熔断状态
         */
        public boolean isCircuitBroken() {
            return circuitBroken.get();
        }
        
        /**
         * 获取平均RTT
         */
        public long getAverageRtt() {
            if (totalResponses == 0) {
                return 0;
            }
            return totalRtt / totalResponses;
        }
    }
    
    /**
     * 主方法 - 示例用法
     */
    public static void main(String[] args) {
        EnhancedNTPClient ntpClient = null;
        
        try {
            System.out.println("初始化增强型NTP客户端...");
            ntpClient = new EnhancedNTPClient();
            
            // 等待初始同步完成
            System.out.println("等待初始时间同步...");
            Thread.sleep(5000);
            
            // 测试时间获取
            for (int i = 0; i < 5; i++) {
                System.out.println("\n===== 测试 #" + (i+1) + " =====");
                System.out.println("本地系统时间: " + new Date());
                System.out.println("补偿后时间: " + ntpClient.getCurrentTime());
                System.out.println("偏差: " + (ntpClient.getCurrentTime().getTime() - System.currentTimeMillis()) + "ms");
                
                if (i < 4) {
                    Thread.sleep(5000);
                }
            }
            
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (ntpClient != null) {
                try {
                    ntpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}