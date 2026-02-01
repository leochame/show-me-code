package com.adam.ratelimiter;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 滑动时间窗口
 *
 */
public class SlidingTimeWindow {
    static Deque<Long> deque;
    public static void main(String[] args) throws InterruptedException {
        solve();
    }
    public static void solve() throws InterruptedException {
        deque = new ArrayDeque<>();
        for (int i = 0; i < 100; i++) {
            if(judge(5,1)){
                System.out.println("put");
            }else{
                System.out.println("failed");
            }
            Thread.sleep(100);
        }
    }
    public  static Boolean judge(Integer i, long  time){
        long currentTime = System.currentTimeMillis();
        if(deque.isEmpty()){
            deque.addLast(currentTime);
        }else{
            while(!deque.isEmpty() && currentTime - deque.peekFirst() > time*1000 ){
                deque.pollFirst();
            }
            if(deque.size() >= i){
                return false;
            }
            deque.push(currentTime);
        }
        return true;
    }

}
