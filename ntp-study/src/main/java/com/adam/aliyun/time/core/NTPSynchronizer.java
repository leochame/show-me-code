package com.adam.aliyun.time.core;

import com.adam.aliyun.time.model.NTPPacketBuilder;
import com.adam.aliyun.time.model.NTPResponse;
import com.adam.aliyun.time.model.ServerMetrics;
import com.adam.aliyun.time.model.ServerState;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NTPSynchronizer {
    private final Selector selector;
    private final ServerRegistry serverRegistry;
    private final TimeOffsetManager timeOffsetManager;
    private final int timeout;
    private final int packetSize;
    
    public NTPSynchronizer(Selector selector, ServerRegistry serverRegistry,
                           TimeOffsetManager timeOffsetManager, int timeout, int packetSize) {
        this.selector = selector;
        this.serverRegistry = serverRegistry;
        this.timeOffsetManager = timeOffsetManager;
        this.timeout = timeout;
        this.packetSize = packetSize;
    }

    /**
     *  1. 执行完整的时间同步生命周期管理
     *  2. 向所有可用服务器发送NTP请求包
     *  3. 基于响应数据更新本地时钟偏移
     */
    public void synchronizeTime() {
        // 记录同步开始时间
        long syncStartTime = System.currentTimeMillis();
        boolean syncSuccessful = false;
        
        try {
            // 向所有服务器发送请求
            sendRequests();
            
            // 等待并处理响应
            List<NTPResponse> responses = waitForResponses();
            
            // 基于多源响应更新时间偏移
            timeOffsetManager.updateTimeOffset(responses, syncStartTime);
            
            syncSuccessful = true;
            
        } catch (Exception e) {
            System.err.println("时间同步过程失败: " + e.getMessage());
        }
        
        // 输出同步结果
        if (syncSuccessful) {
            System.out.println("时间同步完成, 偏移量: " + timeOffsetManager.getCurrentOffset() + "ms, 漂移率: " +
                             String.format("%.9f", timeOffsetManager.getCurrentOffset()) + " ms/ms");
        }
    }

    /**
     * 遍历所有服务器通道，发送NTP请求
     */
    private void sendRequests() throws IOException {
        Map<DatagramChannel, ServerState> channelMap = serverRegistry.getChannelMap();
        Map<String, ServerMetrics> serverMetrics = serverRegistry.getServerMetrics();
        
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
            ByteBuffer buffer = NTPPacketBuilder.createNTPRequest();
            channel.send(buffer, state.address);
            
            // 更新发送时间
            state.requestTime = System.currentTimeMillis();
            state.responsePending = true;
            
            System.out.println("已向服务器发送请求: " + state.config.host);
        }
    }

    /**
     * 在超时窗口内异步接收并处理响应
     *
     * 核心逻辑：
     * 1. 非阻塞轮询：每100ms检查一次就绪通道，避免CPU空转
     * 2. 响应解析：对每个就绪通道，读取数据并计算RTT/偏移
     * 3. 状态更新：成功响应更新ServerMetrics，失败触发熔断计数
     */
    private List<NTPResponse> waitForResponses() throws IOException {

        long startTime = System.currentTimeMillis();
        List<NTPResponse> responses = new ArrayList<>();
        Map<DatagramChannel, ServerState> channelMap = serverRegistry.getChannelMap();
        Map<String, ServerMetrics> serverMetrics = serverRegistry.getServerMetrics();
        
        // 设置响应等待超时
        while (System.currentTimeMillis() - startTime < timeout) {
            // 非阻塞选择
            int readyChannels = selector.select(100);
            if (readyChannels == 0) {
                continue;
            }
            // 处理就绪的通道
            processReadyChannels(responses);
            
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
        handleTimeoutResponses();
        
        return responses;
    }
    /**
     * 处理已就绪通道，读取并处理响应数据
     */
    private void processReadyChannels(List<NTPResponse> responses) {
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();

            if (key.isReadable()) {
                processChannelResponse(key, responses);
            }
        }
    }
    /**
     * 处理单个通道的响应，并更新对应的服务器状态
     */
    private void processChannelResponse(SelectionKey key, List<NTPResponse> responses) {
        DatagramChannel channel = (DatagramChannel) key.channel();
        ServerState state = serverRegistry.getChannelMap().get(channel);

        if (state != null && state.responsePending) {
            try {
                // 接收响应
                ByteBuffer buffer = ByteBuffer.allocate(packetSize);
                channel.receive(buffer);
                buffer.flip();

                // 处理响应，计算时间偏移
                long receiveTime = System.currentTimeMillis();
                Date ntpTime = NTPPacketBuilder.processNTPResponse(buffer);
                long rtt = receiveTime - state.requestTime;
                long offset = ntpTime.getTime() - (state.requestTime + rtt / 2);

                // 更新服务器指标
                ServerMetrics metrics = serverRegistry.getServerMetrics().get(state.config.host);
                metrics.recordSuccess(rtt, offset);

                // 收集有效响应
                responses.add(new NTPResponse(state.config, offset, rtt));
                System.out.println("收到来自 " + state.config.host + " 的响应, RTT: " + rtt + "ms, 偏移: " + offset + "ms");

                // 标记已响应
                state.responsePending = false;
            } catch (Exception e) {
                System.err.println("处理 " + state.config.host + " 的响应失败: " + e.getMessage());
                ServerMetrics metrics = serverRegistry.getServerMetrics().get(state.config.host);
                metrics.recordFailure();
            }
        }
    }
    /**
     * 对于超时未响应的服务器进行失败记录
     */
    private void handleTimeoutResponses() {
        for (ServerState state : serverRegistry.getChannelMap().values()) {
            if (state.responsePending) {
                ServerMetrics metrics = serverRegistry.getServerMetrics().get(state.config.host);
                metrics.recordFailure();
                state.responsePending = false;
                System.out.println("服务器 " + state.config.host + " 响应超时");
            }
        }
    }
}
