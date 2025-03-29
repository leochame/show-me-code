package com.adam.concurrent_programming.printRandom;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerMain {
    static AtomicInteger atomicInteger = new AtomicInteger(0);
    public static void main(String[] args) {
        Thread thread1 = new Thread(()->{
            while(atomicInteger.get() <= 50){
                if(atomicInteger.get()%2 == 0){
                    int i = new Random().nextInt(10);
                    System.out.println( 2*i);
                    atomicInteger.addAndGet(1);
                }
            }
        });
        Thread thread2 = new Thread(()->{
            while(atomicInteger.get() <= 50){
                if(atomicInteger.get()%2 == 1){
                    int i = new Random().nextInt(10);
                    System.out.println( 2*i + 1);
                    atomicInteger.addAndGet(1);
                }
            }
        });
        thread1.start();
        thread2.start();
    }
}
