package com.aliyun.time.model;

import com.aliyun.time.config.ServerConfig;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 监控NTP服务器的健康状态，实现熔断机制和性能指标统计
 */
public class ServerMetrics {
    private final ServerConfig config;
    private int consecutiveFailures;
    private long lastSuccessTime;
    private long totalResponses;
    private long totalRtt;
    private long minRtt;
    private long maxRtt;
    private final AtomicBoolean circuitBroken = new AtomicBoolean(false);
    private final long breakDuration;
    private final int maxFailures;

    public ServerMetrics(ServerConfig config, int maxFailures, long breakDuration) {
        this.config = config;
        this.maxFailures = maxFailures;
        this.breakDuration = breakDuration;
        this.consecutiveFailures = 0;
        this.lastSuccessTime = 0;
        this.totalResponses = 0;
        this.totalRtt = 0;
        this.minRtt = Long.MAX_VALUE;
        this.maxRtt = 0;
    }

    /**
     * 处理成功响应
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
     * 处理失败响应
     */
    public synchronized void recordFailure() {
        consecutiveFailures++;

        // 检查是否需要熔断
        if (consecutiveFailures >= maxFailures) {
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

    public boolean isCircuitBroken() {
        return circuitBroken.get();
    }

    public long getAverageRtt() {
        if (totalResponses == 0) {
            return 0;
        }
        return totalRtt / totalResponses;
    }

    public ServerConfig getConfig() {
        return config;
    }
}