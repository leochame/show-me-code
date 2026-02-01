package com.adam.concurrentprogramming.threadpool.normalthreadpool;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
/**
 *  阻塞队列
 * @param <T>
 */
public class BlockingQueue<T> {
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
    public T poll(long timeout, TimeUnit unit) {
        lock.lock();
        try {
            //---------------------
            long nanos = unit.toNanos(timeout);
            while (queue.isEmpty()) {
                if (nanos <= 0) return null;
                nanos = emptyWaitSet.awaitNanos(nanos);
            }
            T t = queue.removeFirst();
            fullWaitSet.signal(); // 唤醒生产者
            return t;
            //----------------------
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
