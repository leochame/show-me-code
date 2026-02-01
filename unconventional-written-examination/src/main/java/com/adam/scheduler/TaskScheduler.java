package com.adam.scheduler;

import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TaskScheduler {
    private final PriorityQueue<ScheduledTask> taskQueue;
    private final ReentrantLock lock;
    private final Condition condition;
    private final Thread workerThread;
    private volatile boolean running;
    
    public TaskScheduler() {
        this.taskQueue = new PriorityQueue<>((a, b) -> Long.compare(a.executeTime, b.executeTime));
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
        this.running = true;
        this.workerThread = new Thread(this::run);
        this.workerThread.start();
    }
    
    public void schedule(Runnable task, long delayMs) {
        lock.lock();
        try {
            taskQueue.offer(new ScheduledTask(task, System.currentTimeMillis() + delayMs));
            condition.signal();
        } finally {
            lock.unlock();
        }
    }
    
    public void scheduleAtFixedRate(Runnable task, long initialDelayMs, long periodMs) {
        schedule(() -> {
            task.run();
            scheduleAtFixedRate(task, periodMs, periodMs);
        }, initialDelayMs);
    }
    
    private void run() {
        while (running) {
            lock.lock();
            try {
                while (taskQueue.isEmpty() && running) {
                    condition.await();
                }
                if (!running) break;
                
                long currentTime = System.currentTimeMillis();
                ScheduledTask task = taskQueue.peek();
                
                if (task != null && task.executeTime <= currentTime) {
                    taskQueue.poll();
                    lock.unlock();
                    try {
                        task.task.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    lock.lock();
                } else if (task != null) {
                    condition.awaitNanos((task.executeTime - currentTime) * 1_000_000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } finally {
                lock.unlock();
            }
        }
    }
    
    public void shutdown() {
        running = false;
        lock.lock();
        try {
            condition.signal();
        } finally {
            lock.unlock();
        }
        try {
            workerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    static class ScheduledTask {
        Runnable task;
        long executeTime;
        
        ScheduledTask(Runnable task, long executeTime) {
            this.task = task;
            this.executeTime = executeTime;
        }
    }
}

