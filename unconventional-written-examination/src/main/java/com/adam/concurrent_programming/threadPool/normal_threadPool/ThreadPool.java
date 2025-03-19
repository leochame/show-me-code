package com.adam.concurrent_programming.threadPool.normal_threadPool;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private final BlockingQueue<Runnable> taskQueue;
    private final HashSet<Worker> workers = new HashSet<>();
    
    private final int coreSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private volatile boolean isShutdown = false;

    public ThreadPool(int coreSize, long keepAliveTime, TimeUnit timeUnit, int queueCapacity) {
        this.coreSize = coreSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.taskQueue = new BlockingQueue<>(queueCapacity);
    }

    /**
     * 执行任务
     * @param task 任务对象
     */
    public void execute(Runnable task) {
        if (isShutdown) throw new IllegalStateException("ThreadPool is shutdown");

        // 如果任务数量没有超过coreSize，直接交给worker去执行
        // 如果任务数量超过 coreSize，交给任务队列暂存
        synchronized (workers) {
            if (workers.size() < coreSize) {
                Worker worker = new Worker(task);
                workers.add(worker);
                worker.start();
            } else {
                if (!taskQueue.offer(task)) { // 非阻塞式提交
                    throw new RuntimeException("Task queue is full");
                }
            }
        }
    }

    // Worker 是一个 线程对象
    private class Worker extends Thread {
        private Runnable task;

        public Worker(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            /*
              执行任务
               1） 当task 不为空，执行任务
               2） 当task执行完毕，再接着从任务队列获取任务并执行
             */
            while (task != null || (task = taskQueue.poll(keepAliveTime, timeUnit)) != null) {
                try {
                    task.run();
                } finally {
                    task = null;
                }
            }

            synchronized (workers) {
                workers.remove(this);
            }
        }
    }

    // 关闭线程池
    public void shutdown() {
        isShutdown = true;
        synchronized (workers) {
            for (Worker worker : workers) {
                worker.interrupt();
            }
        }
    }
}
