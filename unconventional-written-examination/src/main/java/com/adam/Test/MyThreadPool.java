package com.adam.Test;

import java.util.ArrayList;
import java.util.List;
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

    public MyThreadPool(int coreSize, int maxSize, BlockingQueue<Runnable> blockingQueue, int liveTime, TimeUnit timeUnit) {
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.blockingQueue = blockingQueue;
        this.liveTime = liveTime;
        this.timeUnit = timeUnit;
    }

    public void excute(Runnable command){
        if(coreThreadList.size() < coreSize){
            System.out.println("创建核心线程 "+ coreThreadList.size() + 1 );
            Thread coreThread = new CoreThread();
            coreThreadList.add(coreThread);
            coreThread.start();
            return;
        }
        if(blockingQueue.offer(command)) return;

        if (coreThreadList.size() + supportThreadList.size() < maxSize){
            System.out.println("创建 救济 线程 "+ supportThreadList.size() + 1 );
                SupportThread supportThread = new SupportThread();
                supportThread.start();
                return;
        }

        System.out.println("执行拒绝策略.....");
    }

    private class CoreThread extends Thread{
        @Override
        public void run() {
            while(true){
                try {
                    Runnable runnable = blockingQueue.take();
                    runnable.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    private class SupportThread extends Thread{
        @Override
        public void run() {
            while(true){
                try {
                    Runnable runnable = blockingQueue.poll(liveTime,timeUnit);
                    if (runnable == null){
                        break;
                    }
                    runnable.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(" 救济任务 结束" + Thread.currentThread().getName());
        }
    }
}
