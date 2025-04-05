package greedy.best_time_stock.best_time_stock_2;

class Solution {
    public int maxProfit(int[] prices) {
        int ans = 0;
        int have = prices[0];
        for(int i = 1; i < prices.length; i++){
            if(prices[i] > have){
                ans += (prices[i] - have);
            }
            have = prices[i];
        }
        return ans;
    }
}