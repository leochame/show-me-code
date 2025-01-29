package com.adam.树.二叉树中的最大路径和;

/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode() {}
 *     TreeNode(int val) { this.val = val; }
 *     TreeNode(int val, TreeNode left, TreeNode right) {
 *         this.val = val;
 *         this.left = left;
 *         this.right = right;
 *     }
 * }
 */
class Solution {

    int ans;

    public int maxPathSum(TreeNode root) {
        ans = Integer.MIN_VALUE;
        dfs(root);
        return ans;
    }
    private int dfs(TreeNode root){
        if(root == null) return 0;
       
       int left = dfs(root.left);
       int right = dfs(root.right);

       int max = Math.max(left ,right);

       int f = root.val;
       if(right > 0) f += right;
       if(left > 0) f += left;

       ans = Math.max(ans,f);

       if(max <= 0) return root.val;

       return root.val + max;

    }
}