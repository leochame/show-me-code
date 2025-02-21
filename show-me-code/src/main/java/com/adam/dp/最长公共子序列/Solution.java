package com.adam.dp.最长公共子序列;

class Solution {
    public int longestCommonSubsequence(String text1, String text2) {
        char[] chars1 = text1.toCharArray();
        char[] chars2 = text2.toCharArray();
        int n = chars1.length;
        int m = chars2.length;
        int[][] dp = new int[n+1][m+1];
        int ans = 0;
        for(int i = 1; i <= n; i++){
            for(int j = 1; j <= m; j++){
                if(chars1[i-1] == chars2[j-1]){
                    dp[i][j] = dp[i-1][j-1]+1;
                }else{
                    dp[i][j] = Math.max(dp[i-1][j],dp[i][j-1]);
                }
                ans = Math.max(ans, dp[i][j]);
            }
        }
        return ans;
    }
}