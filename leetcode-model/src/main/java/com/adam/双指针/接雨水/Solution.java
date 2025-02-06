package com.adam.双指针.接雨水;

class Solution {
    public int trap(int[] height) {
        int l = 0;
        int r = height.length - 1;
        int lh = 0;
        int rh = 0;
        int ans = 0;
        while(l < r){
            lh = Math.max(lh,height[l]);
            rh = Math.max(rh,height[r]);
            if(lh < rh){
                ans += lh - height[l++];
            }else{
                ans += rh - height[r--];
            }
        }
        return ans;
    }
}