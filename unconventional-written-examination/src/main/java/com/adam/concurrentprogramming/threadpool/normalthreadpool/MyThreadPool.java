package com.adam.concurrentprogramming.threadpool.normalthreadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MyThreadPool {
    private int coreSize;
    private int maxSize;
    private BlockingQueue<Runnable> blockingQueue;
    private int liveTime;
    private TimeUnit timeUnit;
    private List<Thread> coreThreadList = new ArrayList<>();
    private List<Thread> supportThreadList = new ArrayList<>();

    private class CoreThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Runnable runnable = blockingQueue.take();
                    runnable.run();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private class SupportThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Runnable runnable = blockingQueue.poll(liveTime, timeUnit);
                    if (runnable == null) {
                        break;
                    }
                    runnable.run();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    public MyThreadPool(int coreSize, int maxSize, BlockingQueue<Runnable> blockingQueue, int liveTime, TimeUnit timeUnit) {
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.blockingQueue = blockingQueue;
        this.liveTime = liveTime;
        this.timeUnit = timeUnit;
    }

    public void excute(Runnable command) {
        if (coreThreadList.size() < coreSize) {
            Thread coreThread = new CoreThread();
            coreThreadList.add(coreThread);
            coreThread.start();
            blockingQueue.offer(command);
            return;
        }

        if (blockingQueue.offer(command)) {
            return;
        }

        if (coreThreadList.size() + supportThreadList.size() < maxSize) {
            SupportThread supportThread = new SupportThread();
            supportThreadList.add(supportThread);
            supportThread.start();
            blockingQueue.offer(command);
            return;
        }

        System.out.println("拒绝策略");
    }

    public static void main(String[] args) {
        // 创建线程池：核心线程2，最大线程4，队列容量4，支持线程存活1秒
        MyThreadPool pool = new MyThreadPool(
            2,
            4,
            new ArrayBlockingQueue<>(4),
            1,
            TimeUnit.SECONDS
        );

        // 提交10个任务
        for (int i = 1; i <= 10; i++) {
            int taskId = i;
            pool.excute(() -> {
                try {
                    Thread.sleep(500);
                    System.out.println("任务 " + taskId + " 执行完成，线程: " + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        // 等待任务执行完成
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
