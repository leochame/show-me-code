package leetcode_42;

class Solution {
    public int trap(int[] height) {
        int n = height.length;
        Stack<Integer> stack = new Stack<>(); 
        
        for(int i = 0; i < n; i++){
            if(stack.isEmpty() || height[stack.peek()] <= height[i]){
                stack.push(i);
            }
        }
        Stack<Integer> stack1 = new Stack<>(); 

        for(int i = n-1; i > -1; i--){
            if(stack1.isEmpty() || height[stack1.peek()] < height[i]){
                stack1.push(i);
            }
        }
        int ans = 0;
        while(true){
            int q = stack.pop();
            if(stack.isEmpty()){
                break;
            }
            int m = stack.peek();
            ans+=height[m]*(q-m-1);
            for(int i = m+1;i < q; i++){
                ans -= height[i];
            }
        }
        while(true){
            int q = stack1.pop();
            if(stack1.isEmpty()){
                break;
            }
            int m = stack1.peek();
            ans+=height[m]*(m-q-1);
            for(int i = q+1;i < m;i++){
                ans -= height[i];
            }
        }
        return ans;
    }
}