package DP.partitionLabels;

public class twoString {
    public static void main(String[] args) {
        String s1 = "1234567";
        String s2 = "1234567";
        char[] chars1 = s1.toCharArray();
        char[] chars2 = s2.toCharArray();
        int m = chars1.length;
        int n = chars2.length;
        Boolean ans = false;
        int[][] dp = new int[m+1][n+1];
        for(int i = 1; i <=m ; i++){
            for(int j = 1; j <= n; j++){
                if(chars1[i-1] == chars2[j-1]){
                    dp[i][j] = dp[i-1][j-1]+1;
                    if(dp[i][j] == n){
                        ans = true;
                    }
                }
            }
        }
        System.out.println(ans);
    }
}
