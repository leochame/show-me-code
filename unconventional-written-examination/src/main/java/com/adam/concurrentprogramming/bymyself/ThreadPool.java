package com.adam.concurrentprogramming.bymyself;

import java.util.HashSet;
import java.util.concurrent.PriorityBlockingQueue;

public class ThreadPool {
    int coreSize;
    HashSet<Worker> workers = new HashSet<>();
    PriorityBlockingQueue<Worker> pbQueue = new PriorityBlockingQueue<>();

    public void execute(Runnable task){
        if(workers.size() < coreSize){
            Worker worker = new Worker(task);
            workers.add(worker);
            worker.start();

        }
    }

    private class Worker extends Thread{
        private Runnable task;
        public Worker(Runnable task){
            this.task = task;
        }

        @Override
        public void run() {
            while(task != null && (task = pbQueue.poll()) != null){
                try{
                    task.run();
                }finally {
                    task = null;
                }
            }
            workers.remove(task);
        }
    }
}
