package com.adam.test;

import com.adam.concurrentprogramming.threadpool.normalthreadpool.MyThreadPool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        MyThreadPool myThreadPool = new MyThreadPool(2,4,new ArrayBlockingQueue<>(4),1, TimeUnit.SECONDS);
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            myThreadPool.excute(()->{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(" 执行任务 " + Thread.currentThread().getName());
            });
        }
        System.out.println("ok");
    }
}
