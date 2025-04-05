package greedy.best_time_stock.best_time_stock_4;

class Solution {
    public int maxProfit(int k, int[] prices) {
        if (prices.length == 0) return 0;
        // 优化：当k足够大时转为贪心
        if (k >= prices.length/2) { 
            int profit = 0;
            for (int i = 1; i < prices.length; i++)
                profit += Math.max(0, prices[i] - prices[i-1]);
            return profit;
        }
        
        int[][] dp = new int[k+1][2];
        // 初始化
        for (int j = 0; j <= k; j++) {
            dp[j][1] = -prices[0]; 
            dp[j][0] = 0;
        }

        for (int price : prices) {
            for (int j = k; j > 0; j--) { // 倒序处理防止覆盖
                // 先处理卖出，再处理买入

                //j表示的是完成j次交易后的未持有状态，当前次数j的未持有状态
                dp[j][0] = Math.max(dp[j][0], dp[j][1] + price);
                dp[j][1] = Math.max(dp[j][1], dp[j-1][0] - price);
            }
        }
        return dp[k][0];
    }
}