package leetcode_11;

class Solution {
    public int maxArea(int[] height) {
        int lIndex = 0;
        int rIndex = height.length-1;

        int v = 0;
        int ans = 0;

        while(lIndex < rIndex){
            if(height[rIndex] < height[lIndex]){
                ans = Math.max(ans,(rIndex-lIndex)*height[rIndex]);
                int x = rIndex - 1;
                while( x > -1 && height[x] <= height[rIndex]){
                    x--;
                }
                rIndex = x;
            }else{
                ans = Math.max(ans,(rIndex-lIndex)*height[lIndex]);
                
                int x = lIndex + 1;
                while(x < height.length && height[x] <= height[lIndex]){
                    x++;
                }
                lIndex = x;
            }
        }
        return ans;
    }
}