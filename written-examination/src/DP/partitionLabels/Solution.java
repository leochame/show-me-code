package DP.partitionLabels;

import java.util.ArrayList;
import java.util.List;

class Solution {
/**
 */
    public List<Integer> partitionLabels(String s) {
        int[] index = new int[26];
        char[] chars = s.toCharArray();
        int n = chars.length;
        for(int i = 0; i < chars.length; i++){
            index[chars[i] - 'a'] = i;
        }
        int l = -1;
        int r = 0;
        int i = 0;
        List<Integer> ans = new ArrayList<>();
        while(i < n){
            r = Math.max(index[chars[i] - 'a'], r);
            if(i == r){
                ans.add(r - l);
                l = r;
            }
            i++;   
        }
        return ans;
    }
}