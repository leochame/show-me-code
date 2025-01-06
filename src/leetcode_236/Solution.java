package leetcode_236;

/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 */
class Solution {

    private TreeNode p;
    private TreeNode q;
    private TreeNode ans;
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        this.p = p;
        this.q = q;
        dfs(root);
        return ans;
    }

    private int dfs(TreeNode root){
        if(root == null){
            return 0;
        }
        int t = dfs(root.left);
        t += dfs(root.right);
        if(root == p || root == q){
            t++;
        }
        if(t == 2){
            ans = root;
            return 0;
        }
        return t;
    }


}