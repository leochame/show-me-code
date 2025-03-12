package com.adam.aliyun.time.filter;

public class KalmanFilter {
    private double processNoise; // 过程噪声方差
    private double measurementNoise; // 测量噪声方差
    private double estimatedError; // 估计误差协方差
    private double kalmanGain; // 卡尔曼增益
    private double offsetEstimate; // 当前偏移量估计
    private double driftEstimate; // 漂移率估计
    
    public KalmanFilter(double processNoise, double measurementNoise) {
        this.processNoise = processNoise;
        this.measurementNoise = measurementNoise;
        this.estimatedError = 1.0;
        this.kalmanGain = 0.0;
        this.offsetEstimate = 0.0;
        this.driftEstimate = 0.0;
    }
    
    public void update(double measurement, double dt) {
        if (dt > 0) {
            // 预测步骤
            double predictedOffset = offsetEstimate + driftEstimate * dt;
            estimatedError = estimatedError + processNoise * dt * dt;
            
            // 更新步骤
            kalmanGain = estimatedError / (estimatedError + measurementNoise);
            offsetEstimate = predictedOffset + kalmanGain * (measurement - predictedOffset);
            estimatedError = (1 - kalmanGain) * estimatedError;
            
            // 更新漂移估计
            double measuredDrift = (measurement - offsetEstimate) / dt;
            driftEstimate = driftEstimate + 0.1 * (measuredDrift - driftEstimate);
        } else {
            // 初始化估计值
            offsetEstimate = measurement;
        }
    }
    
    public double getOffsetEstimate() {
        return offsetEstimate;
    }
    
    public double getDriftEstimate() {
        return driftEstimate;
    }
}