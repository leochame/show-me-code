package com.adam.滑动窗口

class Solution {
    public List<Integer> findAnagrams(String s, String p) {
        int[] index = new int[26];
        List<Integer> ans = new ArrayList<>();
        for(int i = 0; i < p.length(); i++){
            char c = p.charAt(i);
            index[c-'a']++;

        }
        int left = 0;
        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            index[c-'a'] --;
            while(index[c - 'a'] < 0){
                index[s.charAt(left) - 'a']++;
                left++;
                
            }
            if(i - left + 1 == p.length()){
                ans.add(left);
            }
        }
        return ans;
    }
}