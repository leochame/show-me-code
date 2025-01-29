package com.adam.树.路径总和;

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
    int targetSum;
    boolean ans;
    public boolean hasPathSum(TreeNode root, int targetSum) {
        this.targetSum = targetSum;
        this.ans = false;
        if(root == null) return false;
        dfs(root,0);
        return ans;
        
    }
    public void dfs(TreeNode root, int sum){
        if(root == null){
            return;
        }
        sum += root.val;
        if(root.left == null && root.right == null){
            if(sum == targetSum) {
                ans = true;
            }
            return;
        }
        dfs(root.left,sum);
        dfs(root.right,sum);
    }

}