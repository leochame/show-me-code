package statck;

import java.util.*;

class largestRectangleArea {
    public int largestRectangleArea(int[] heights) {
        Deque<Integer> deque = new ArrayDeque<>();
        int ans = 0;
        deque.push(-1);

        // 栈中存放的是以 i 为尾节点的单调递增序列。
        // 例如 1，3，5，1，6，7，4，实际产生的是 1，4。
        for(int i = 0; i <= heights.length; i++){
            int h = -1;
            if(i < heights.length){
                h = heights[i]; 
            }
            // 保持单调递增的特性
            // 如果发现当前值小于栈顶元素，就代表着：以【栈顶元素为高】的右端点已经达到极限。
            // 同时栈顶元素的下一个元素，（因为整个栈保持了单调递增），所以下一个元素是栈顶元素的左节点。
            // 我知道你在思考中的去哪儿了，下一个元素与栈顶元素之间的元素，其实已经被栈顶元素gank完了，完全比栈顶元素大。
            while(deque.size() > 1 && heights[deque.peek()] >= h){
                int hi = heights[deque.pop()];
                int l = deque.peek();
                ans = Math.max(ans,hi * (i - l - 1));
            }
            deque.push(i);
        }
        return ans;
    }
}