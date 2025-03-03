package com.adam.nio.ntp.core;

// 文件3: ClockCompensator.java
public class ClockCompensator {
    private double timeOffset;
    private double timeDrift;
    private double errorCovariance;
    private long lastUpdateTime = System.currentTimeMillis();
    private static final double PROCESS_NOISE = 1e-6;
    private static final double MEASUREMENT_NOISE = 15_000;

    public synchronized long compensate(long ntpTime, long rtt) {

//        System.out.println("before adjust time: " + ntpTime);

        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        long measurement = ntpTime - currentTime - (rtt/2);

        predict(deltaTime);
        update(measurement);

        lastUpdateTime = currentTime;
        return currentTime + (long)timeOffset;
    }

    private void predict(double deltaTime) {
        timeOffset += timeDrift * deltaTime;
        errorCovariance += (deltaTime * deltaTime * PROCESS_NOISE);
    }

    private void update(double measurement) {
        double kalmanGain = errorCovariance / (errorCovariance + MEASUREMENT_NOISE);
        timeOffset += kalmanGain * (measurement - timeOffset);
        timeDrift += kalmanGain * ((measurement - timeOffset) / errorCovariance);
        errorCovariance *= (1 - kalmanGain);
    }
}