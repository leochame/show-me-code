package Tree;

import java.util.HashMap;
import java.util.Map;


class Solution {

    Map<Integer,Integer> inMap = new HashMap<>();
    int[] preorder;
    
    public TreeNode buildTree(int[] preorder, int[] inorder) {
        this.preorder = preorder;
        for(int i = 0; i < preorder.length ; i++){
            inMap.put(inorder[i],i);
        }
        return dfs(0,0,inorder.length-1);
    }

    /**
     * 调用 dfs 递归构造树
     *
     * @param root 初始根节点是前序遍历的第一个元素
     * @param left right： 中序遍历数组的范围
     * @return TreeNode
     */
    private TreeNode dfs(int root, int left ,int right) {
         if (left > right) return null;  
         TreeNode node = new TreeNode(preorder[root]);
         int i = inMap.get(preorder[root]);

        /*
         *  左子树的根节点在 preorder 中是 root + 1；
         *  中序遍历的范围是 [left, i-1]。
         */
         node.left = dfs(root+1,left,i-1);

        /*
         * 右子树的根节点在 preorder 中的位置是： root + 左子树长度 + 1 = root + (i - left) + 1
         * 中序遍历的范围是 [i+1, right]。
         */
        node.right = dfs(root + (i -left) + 1, i+ 1 ,right);
         return node;
    }
}