package com.adam.concurrentprogramming.producerconsumer;

import java.sql.ClientInfoStatus;
import java.util.ArrayDeque;
import java.util.Deque;

public class ProducerAndConsumer {
    public static void main(String[] args) {

    }
}

class MessageQueue{

    private final Deque<Message> deque;
    private final int capacity;

    public MessageQueue(int capacity) {
        this.capacity = capacity;
        deque = new ArrayDeque<>();
    }

    public Message take(){
        synchronized (deque) {
            while (deque.isEmpty()) {
                try {
                    deque.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Message message = deque.pollFirst();
            deque.notify();
            return message;
        }
    }
    public void put(Message message){
        synchronized (deque){
            while(deque.size() == capacity){
                try {
                    deque.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            deque.addLast(message);
            deque.notifyAll();
        }
    }
}

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
}
