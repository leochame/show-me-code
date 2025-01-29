package com.adam.并发.leetcode_1114.交替打印;

//使用信号量打印
class FooBar1 {
    private int n;
    final static Semaphore sa = new Semaphore(1);
    final static Semaphore sb = new Semaphore(0);

    public FooBar(int n) {
        this.n = n;
    }

    public void foo(Runnable printFoo) throws InterruptedException {
        
        for (int i = 0; i < n; i++) {
            sa.acquire();
        	// printFoo.run() outputs "foo". Do not change or remove this line.
        	printFoo.run();
            sb.release();
        }
    }

    public void bar(Runnable printBar) throws InterruptedException {
        
        for (int i = 0; i < n; i++) {
            sb.acquire();
            // printBar.run() outputs "bar". Do not change or remove this line.
        	printBar.run();
            sa.release();
        }
    }
}