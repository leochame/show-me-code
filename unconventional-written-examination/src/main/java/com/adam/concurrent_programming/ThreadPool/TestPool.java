package com.adam.concurrent_programming.ThreadPool;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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

class ThreadPool {
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

    // 提交任务（添加拒绝策略）
    public void execute(Runnable task) {
        if (isShutdown) throw new IllegalStateException("ThreadPool is shutdown");

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

    // Worker线程实现
    private class Worker extends Thread {
        private Runnable task;

        public Worker(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            try {
                while (task != null || (task = getTask()) != null) {
                    try {
                        task.run();
                    } finally {
                        task = null;
                    }
                }
            } finally {
                synchronized (workers) {
                    workers.remove(this);
                }
            }
        }

        // 获取任务（支持超时回收）
        private Runnable getTask() {
            try {
                return taskQueue.poll(keepAliveTime, timeUnit);
            } catch (InterruptedException e) {
                return null;
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
class BlockingQueue<T> {
    private Deque<T> queue = new ArrayDeque<>();
    private ReentrantLock lock = new ReentrantLock();
    private int capacity; // 修复1：构造函数初始化容量

    // 生产者条件变量（队列满时阻塞）
    private Condition fullWaitSet = lock.newCondition();
    // 消费者条件变量（队列空时阻塞）
    private Condition emptyWaitSet = lock.newCondition();

    public BlockingQueue(int capacity) {
        this.capacity = capacity; // 修复2：初始化容量
    }

    // 取任务（支持超时）
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        lock.lock();
        try {
            long nanos = unit.toNanos(timeout);
            while (queue.isEmpty()) {
                if (nanos <= 0) return null;
                nanos = emptyWaitSet.awaitNanos(nanos);
            }
            T t = queue.removeFirst();
            fullWaitSet.signal(); // 唤醒生产者
            return t;
        } finally {
            lock.unlock();
        }
    }

    // 添加任务
    public boolean offer(T element) {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                try {
                    fullWaitSet.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            queue.addLast(element);
            emptyWaitSet.signal(); // 唤醒消费者
            return true;
        } finally {
            lock.unlock();
        }
    }
}