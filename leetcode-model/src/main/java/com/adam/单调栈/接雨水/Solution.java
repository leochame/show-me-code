package com.adam.单调栈.接雨水;

import java.util.ArrayDeque;
import java.util.Deque;

class Solution {
    Deque<Integer> stack = new ArrayDeque<>();
    public int trap(int[] height) {
        int ans = 0;
        for(int i = 0; i < height.length; i++){
            while(!stack.isEmpty() && height[i] >= height[stack.peek()]){
                int d = height[stack.pop()];
                
                if(stack.isEmpty()) break;

                ans += (Math.min(height[i],height[stack.peek()]) - d) * (i - stack.peek() - 1);
            }
            stack.push(i);
        }
        return ans;
    }
}