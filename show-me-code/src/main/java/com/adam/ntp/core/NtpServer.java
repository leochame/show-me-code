package com.adam.ntp.core;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// 文件1: NtpServer.java
public class NtpServer {
    private final String host;
    private final int stratum;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private volatile long lastFailureTime;
    private volatile boolean active = true;

    public NtpServer(String host, int stratum) {
        this.host = host;
        this.stratum = stratum;
    }

    public boolean isAvailable() {
        return active && (failureCount.get() < 3) &&
                (System.currentTimeMillis() - lastFailureTime > 300_000);
    }

    public void recordFailure() {
        failureCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();
        if(failureCount.get() >= 3) {
            active = false;
            Executors.newSingleThreadScheduledExecutor()
                    .schedule(() -> active = true, 5, TimeUnit.MINUTES);
        }
    }

    public int getStratum() {
        return stratum;
    }

    public String getHost() {
        return host;
    }
}