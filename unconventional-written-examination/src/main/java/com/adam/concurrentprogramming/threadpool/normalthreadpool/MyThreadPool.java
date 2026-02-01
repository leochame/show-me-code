package com.adam.concurrentprogramming.threadpool.normalthreadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 自定义线程池实现
 */
public class MyThreadPool {
    private int coreSize;
    private int maxSize;
    private BlockingQueue<Runnable> blockingQueue;
    private int liveTime;
    private TimeUnit timeUnit;
    private List<Thread> coreThreadList = new ArrayList<>();
    private List<Thread> supportThreadList = new ArrayList<>();

    /**
     * 核心线程：常驻线程，一直运行
     * - 使用 take() 方法阻塞等待任务，永远不会主动退出
     */
    private class CoreThread extends Thread{
        @Override
        public void run() {
            while(true){
                try {
                    // take() 是阻塞方法，队列为空时会一直等待
                    Runnable runnable = blockingQueue.take();
                    // 执行任务（注意：这里是直接调用 run()，不是 start()）
                    // 因为任务已经在工作线程中运行了
                    runnable.run();
                } catch (InterruptedException e) {
                    // 线程被中断时退出循环
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 支持线程：临时线程，超时后自动退出
     * - 使用 poll(timeout) 方法，超时后返回 null
     */
    private class SupportThread extends Thread{
        @Override
        public void run() {
            while(true){
                try {
                    // poll() 带超时参数，超时后返回 null
                    Runnable runnable = blockingQueue.poll(liveTime, timeUnit);
                    if (runnable == null){
                        // 超时未获取到任务，退出循环，线程结束
                        break;
                    }
                    runnable.run();
                } catch (InterruptedException e) {
                    // 线程被中断时退出
                    throw new RuntimeException(e);
                }
            }
            System.out.println(" 救济任务 结束" + Thread.currentThread().getName());
        }
    }

    /**
     * 构造函数
     */
    public MyThreadPool(int coreSize, int maxSize, BlockingQueue<Runnable> blockingQueue, int liveTime, TimeUnit timeUnit) {
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.blockingQueue = blockingQueue;
        this.liveTime = liveTime;
        this.timeUnit = timeUnit;
    }

    /**
     * 提交任务到线程池执行
     * 
     * 执行流程：
     * 1. 如果核心线程数未满，创建核心线程并执行
     * 2. 如果核心线程已满，尝试将任务放入队列
     * 3. 如果队列已满，且总线程数未达上限，创建支持线程
     * 4. 如果都满了，执行拒绝策略（当前实现为简单打印）
     * 
     * @param command 待执行的任务
     */
    public void excute(Runnable command){
        // 第一步：检查核心线程数，未满则创建核心线程
        if(coreThreadList.size() < coreSize){
            System.out.println("创建核心线程 "+ (coreThreadList.size() + 1) );
            Thread coreThread = new CoreThread();
            coreThreadList.add(coreThread);
            coreThread.start();
            // 注意：新创建的核心线程会从队列中取任务，当前任务需要放入队列
            // 这里有个问题：新线程启动后，当前任务应该放入队列，而不是直接返回
            // 但为了保持逻辑清晰，这里先返回，让后续逻辑处理
            blockingQueue.offer(command);
            return;
        }
        
        // 第二步：尝试将任务放入队列（非阻塞）
        if(blockingQueue.offer(command)) {
            return; // 成功放入队列，直接返回
        }

        // 第三步：队列已满，检查是否可以创建支持线程
        if (coreThreadList.size() + supportThreadList.size() < maxSize){
            System.out.println("创建 救济 线程 "+ (supportThreadList.size() + 1) );
            SupportThread supportThread = new SupportThread();
            supportThreadList.add(supportThread); // 添加到列表以便管理
            supportThread.start();
            // 注意：支持线程启动后会从队列取任务，当前任务需要放入队列
            blockingQueue.offer(command);
            return;
        }

        // 第四步：所有资源都已用尽，执行拒绝策略
        System.out.println("执行拒绝策略.....");
        // TODO: 可以实现更完善的拒绝策略，如抛出异常、调用者执行等
    }

}
