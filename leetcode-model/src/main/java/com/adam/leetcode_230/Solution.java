package com.adam.leetcode_230;

import javax.swing.tree.TreeNode;

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
    int i = 0;
    int k = 0;
    public int kthSmallest(TreeNode root, int k) {
        this.k = k;
        dfs(root);
        return i;
    }
    private void dfs(TreeNode root){
        if(root == null) return;
        dfs(root.left);
        if(k == 0) return;
        k--;
        if(k == 0) {i = root.val; return;}
        dfs(root.right);
    }
}