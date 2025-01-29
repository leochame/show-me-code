package com.adam.并发.leetcode_1114.交替打印;

// 使用synchronized锁
class FooBar2 {
    private int n;

    private int m = 0;
    private final Object lock = new Object();

    public FooBar(int n) {
        this.n = n;
    }

    public void foo(Runnable printFoo) throws InterruptedException {
        
        for (int i = 0; i < n; i++) {
            synchronized(lock){
                while(m % 2 != 0){
                    lock.wait();
                }
                printFoo.run();
                m++;
                lock.notifyAll();
            }
        	// printFoo.run() outputs "foo". Do not change or remove this line.
        	
        }
    }

    public void bar(Runnable printBar) throws InterruptedException {
        
        for (int i = 0; i < n; i++) {
            synchronized(lock){
                while(m % 2 != 1){
                    lock.wait();
                }
                printBar.run();
                m++;
                lock.notifyAll();
            }
            // printBar.run() outputs "bar". Do not change or remove this line.

        }
    }
}