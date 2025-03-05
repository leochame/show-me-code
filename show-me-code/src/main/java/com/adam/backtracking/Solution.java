package com.adam.backtracking;
import java.util.*;
class Solution {
    List<String> ans = new ArrayList<>();
    int n;
    public List<String> generateParenthesis(int n) {
        this.n = n;
        char[] chars = new char[2*n];
        dfs(0,0,0,chars);
        return ans;
    }
    public void dfs(int l,int r,int i,char[] chars){
        if(i == 2 * n ){
            ans.add(new String(chars));
            return;
        }
        if(l != n){
            chars[i] = '(';
            dfs(l+1,r,i+1,chars);
        }
        if(r < l){
            chars[i] = ')';
            dfs(l,r+1,i+1,chars);
        }
    }
}