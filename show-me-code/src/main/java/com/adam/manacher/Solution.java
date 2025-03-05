package com.adam.manacher;

class Solution {
    public String longestPalindrome(String s) {
       if (s == null || s.length() < 1) return "";
        
        // 预处理字符串，插入特殊字符#
        StringBuilder processed = new StringBuilder("#");
        for (char c : s.toCharArray()) {
            processed.append(c).append("#");
        }
        String newStr = processed.toString();
        
        int[] p = new int[newStr.length()]; // 回文半径数组
        int maxRight = 0, center = 0;      // 当前最大右边界及其中心
        int maxRadius = 0, maxCenter = 0;   // 记录全局最大值
        
        for (int i = 1; i < newStr.length() - 1; i++) {
            int mirror = 2 * center - i;     // 计算对称镜像位置[1,3](@ref)
            
            // 初始化半径（利用对称性减少计算）
            if (i < maxRight) {
                p[i] = Math.min(maxRight - i, p[mirror]);
            }
            
            // 中心扩展
            while (i - p[i] - 1 >= 0 && i + p[i] + 1 < newStr.length() 
                   && newStr.charAt(i - p[i] - 1) == newStr.charAt(i + p[i] + 1)) {
                p[i]++;
            }
            
            // 更新最大边界
            if (i + p[i] > maxRight) {
                maxRight = i + p[i];
                center = i;
            }
            
            // 更新全局最大值
            if (p[i] > maxRadius) {
                maxRadius = p[i];
                maxCenter = i;
            }
        }
        
        // 转换回原始字符串的索引[1,10](@ref)
        int start = (maxCenter - maxRadius) / 2;
        return s.substring(start, start + maxRadius);

    }
}