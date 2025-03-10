package com.aliyun.time.core;

import com.aliyun.time.model.NTPResponse;
import com.aliyun.time.config.ServerConfig;
import com.aliyun.time.model.ServerMetrics;
import com.aliyun.time.model.ServerState;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分布式NTP服务器注册中心
 * 1. 连接管理：维护所有NTP服务器通道的创建/销毁（initialize()/closeAll()）
 * 2. 健康监控：通过ServerMetrics实现熔断机制（连续失败次数阈值与恢复周期）
 * 3. 负载均衡：基于RTT（往返时间）的最优服务器选择（selectBestResponse()）
 */
public class ServerRegistry {
    private final Map<DatagramChannel, ServerState> channelMap = new HashMap<>();
    private final Map<String, ServerMetrics> serverMetrics = new ConcurrentHashMap<>();
    private final List<ServerConfig> serverConfigs;
    private final Selector selector;
    private final int maxFailures;
    private final long breakDuration;
    
    public ServerRegistry(List<ServerConfig> serverConfigs, Selector selector, int maxFailures, long breakDuration) {
        this.serverConfigs = serverConfigs;
        this.selector = selector;
        this.maxFailures = maxFailures;
        this.breakDuration = breakDuration;
    }

    /**
     * 服务器初始化
     */
    public void initialize() throws IOException {
        for (ServerConfig config : serverConfigs) {
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
                serverMetrics.put(config.host, new ServerMetrics(config, maxFailures, breakDuration));
                
                System.out.println("已初始化NTP服务器: " + config.host);
            } catch (Exception e) {
                System.err.println("初始化服务器失败: " + config.host + " - " + e.getMessage());
            }
        }
    }
    
    public Map<DatagramChannel, ServerState> getChannelMap() {
        return channelMap;
    }
    
    public Map<String, ServerMetrics> getServerMetrics() {
        return serverMetrics;
    }

    /**
     * 分层筛选
     * 优先选择高可用层级
     */
    public List<NTPResponse> filterByTier(List<NTPResponse> responses, int tier) {
        List<NTPResponse> result = new ArrayList<>();
        for (NTPResponse response : responses) {
            if (response.config.tier == tier) {
                result.add(response);
            }
        }
        return result;
    }


    /**
     * 最小RTT原则降低网络延迟影响
     * 负载均衡策略
     */
    public NTPResponse selectBestResponse(List<NTPResponse> responses) {
        NTPResponse best = responses.get(0);
        for (int i = 1; i < responses.size(); i++) {
            if (responses.get(i).rtt < best.rtt) {
                best = responses.get(i);
            }
        }
        return best;
    }
    
    public void closeAll() throws IOException {
        for (DatagramChannel channel : channelMap.keySet()) {
            if (channel.isOpen()) {
                channel.close();
            }
        }
    }
}