package com.adam.concurrent_programming.threadPool.normal_threadPool;

import java.util.concurrent.TimeUnit;

public class TestPool {
    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool(
                2, 1000, TimeUnit.MILLISECONDS, 10
        );

        // 提交5个任务
        for (int i = 0; i < 5; i++) {
            int j = i;
            try {
                threadPool.execute(() -> {
                    System.out.println("执行任务：" + j);
                });
            } catch (RuntimeException e) {
                System.out.println("任务被拒绝：" + j);
            }
        }

        // 关闭线程池
        threadPool.shutdown();
    }
}

