package com.adam.aliyun.time.core;

import com.adam.aliyun.time.filter.KalmanFilter;
import com.adam.aliyun.time.model.NTPResponse;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TimeOffsetManager {
    private final AtomicLong lastSyncTime = new AtomicLong(0);
    private final AtomicLong currentOffset = new AtomicLong(0);
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final KalmanFilter kalmanFilter;
    private final ServerRegistry serverRegistry;
    
    public TimeOffsetManager(KalmanFilter kalmanFilter, ServerRegistry serverRegistry) {
        this.kalmanFilter = kalmanFilter;
        this.serverRegistry = serverRegistry;
    }
    
    public void updateTimeOffset(List<NTPResponse> responses, long syncStartTime) {
        if (responses.isEmpty()) {
            System.err.println("警告：没有收到任何有效响应");
            return;
        }
        
        // 按层次筛选最优响应
        NTPResponse bestResponse = selectBestResponseByTier(responses);
        
        if (bestResponse == null) {
            System.err.println("警告：无法选择有效响应");
            return;
        }
        
        // 计算时间间隔
        long now = System.currentTimeMillis();
        double dt = (now - lastSyncTime.get()) / 1000.0; // 秒为单位
        
        // 更新卡尔曼滤波器
        kalmanFilter.update(bestResponse.offset, dt);
        
        // 更新当前偏移
        currentOffset.set((long) kalmanFilter.getOffsetEstimate());
        
        // 更新同步时间
        lastSyncTime.set(syncStartTime);
        
        // 标记初始化完成
        isInitialized.set(true);
        
        System.out.println("最佳响应来自: " + bestResponse.config.host + ", 层级: " + bestResponse.config.tier + 
                         ", RTT: " + bestResponse.rtt + "ms, 采用偏移: " + (long) kalmanFilter.getOffsetEstimate() + "ms");
    }
    
    private NTPResponse selectBestResponseByTier(List<NTPResponse> responses) {
        // 首先尝试使用第一层服务器的响应
        List<NTPResponse> tier1Responses = serverRegistry.filterByTier(responses, 1);
        if (!tier1Responses.isEmpty()) {
            return serverRegistry.selectBestResponse(tier1Responses);
        }
        
        // 如果没有第一层响应，尝试第二层
        List<NTPResponse> tier2Responses = serverRegistry.filterByTier(responses, 2);
        if (!tier2Responses.isEmpty()) {
            return serverRegistry.selectBestResponse(tier2Responses);
        }
        
        // 最后尝试第三层
        List<NTPResponse> tier3Responses = serverRegistry.filterByTier(responses, 3);
        if (!tier3Responses.isEmpty()) {
            return serverRegistry.selectBestResponse(tier3Responses);
        }
        
        return null;
    }
    
    public Date getCurrentTime() {
        // 基于上次同步时间和计算的偏移，计算当前补偿后的时间
        long now = System.currentTimeMillis();
        long timeSinceLastSync = now - lastSyncTime.get();
        
        // 加入漂移补偿：当前偏移 + 预估漂移率 * 经过时间
        long adjustedOffset = currentOffset.get() + (long)(kalmanFilter.getDriftEstimate() * timeSinceLastSync);
        
        return new Date(now + adjustedOffset);
    }
    
    public boolean isInitialized() {
        return isInitialized.get();
    }
    
    public long getLastSyncTime() {
        return lastSyncTime.get();
    }
    
    public long getCurrentOffset() {
        return currentOffset.get();
    }
}