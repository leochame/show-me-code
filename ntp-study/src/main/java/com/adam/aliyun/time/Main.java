package com.adam.aliyun.time;

import java.util.Date;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("初始化增强型NTP客户端...");
            // 程序启动时初始化客户端
            EnhancedNTPClient.initialize();

            // 等待初始同步完成
            System.out.println("等待初始时间同步...");
            Thread.sleep(5000);

            // 测试时间获取 - 使用静态方法
            for (int i = 0; i < 5; i++) {
                System.out.println("\n===== 测试 #" + (i+1) + " =====");
                System.out.println("本地系统时间: " + new Date());
                System.out.println("补偿后时间: " + EnhancedNTPClient.getInstance().getCurrentTime());
                System.out.println("偏差: " + (EnhancedNTPClient.getInstance().getCurrentTime().getTime() - System.currentTimeMillis()) + "ms");

                if (i < 4) {
                    Thread.sleep(5000);
                }
            }

            // 注: 正常情况下不需要显式调用shutdown，因为已添加ShutdownHook
            // 但在某些场景下可能需要提前关闭
            // EnhancedNTPClient.shutdown();

        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}