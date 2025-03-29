package com.adam.concurrent_programming.printRandom;

import java.util.Random;

public class SynchronizedMain {
    static  int i = 0;
    static Object obj = new Object();
    public static void main(String[] args) {
        new Thread(()->{
            while(i < 50){
                if(i%2 == 1) continue;
                synchronized (obj){
                    if(i%2 == 1){
                        try {
                            obj.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println(new Random().nextInt(10)*2);
                    i++;
                    obj.notify();
                }
            }
        }).start();
        new Thread(()->{
            while(i < 50){
                if(i%2 == 0) continue;
                synchronized (obj){
                    if(i%2 == 0){
                        try {
                            obj.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println(new Random().nextInt(10)*2 + 1);
                    i++;
                    obj.notify();
                }
            }
        }).start();
    }
}
