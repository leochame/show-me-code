package com.adam.concurrentprogramming.producerconsumer;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerAndConsumer {
    public static void main(String[] args) {
        MessageQueue queue = new MessageQueue(2);
        for (int i = 0; i < 9; i++) {
            int finalI = i;
            new Thread(()->{
                try {
                    queue.put(new Message(finalI,"value" + finalI));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            },"producer" + i).start();
        }
        new Thread(() -> {
            try {
                while (true) {
                    queue.take();
                    Thread.sleep(100); // 模拟消费延迟
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "consumer").start();



    }
}
class MessageQueue {
    private final LinkedList<Message> list = new LinkedList<>();
    private final int capacity;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();  // 队列未满条件
    private final Condition notEmpty = lock.newCondition(); // 队列非空条件

    public MessageQueue(int capacity) {
        this.capacity = capacity;
    }

    public void put(Message message) throws InterruptedException {
        lock.lock();
        try {
            while (list.size() >= capacity) {
                System.out.println("队列已满，生产者线程陷入等待");
                notFull.await();
            }
            list.addLast(message);
            System.out.println("生产者线程将：" + message + "加入队列");
            notEmpty.signal(); // 唤醒一个消费者
        } finally {
            lock.unlock();
        }
    }

    public Message take() throws InterruptedException {
        lock.lock();
        try {
            while (list.isEmpty()) {
                System.out.println("队列为空，消费者线程陷入等待");
                notEmpty.await();
            }
            Message message = list.removeFirst();
            System.out.println("消费者取走：" + message);
            notFull.signal(); // 唤醒一个生产者
            return message;
        } finally {
            lock.unlock();
        }
    }
}
/**
 * 我们希望内部状态除了构造的时候就不能改变了，这样就是线程安全的了。
 * class 加了 final，就不能有子类或者使用子类覆盖了。
 */
final class Message{
    private final int id;
    private final Object value;

    public Message(int id, Object value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", value=" + value +
                '}';
    }
}