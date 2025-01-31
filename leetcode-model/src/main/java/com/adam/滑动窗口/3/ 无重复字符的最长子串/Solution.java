package com.adam.滑动窗口

class Solution {
    public int lengthOfLongestSubstring(String s) {
        char[] chars = s.toCharArray();
        int[] in = new int[200];
        int l = 0;
        // int r = 0;
        int ans = 0;
        for(int i = 0; i < chars.length; i++){
            int x = chars[i];
            while(in[x] == 1){
                in[chars[l++]]--;
            }
            in[x]++;
            ans = Math.max(ans,i-l+1);
        }
        return ans;
    }
}