package listNode.reverseKGroup;

import listNode.ListNode;

public class Solution {
}
/**
 * Definition for singly-linked list.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 * }
 */
class Solution1 {
    public ListNode reverseKGroup(ListNode head, int k) {
        int n = 0;
        ListNode node = head;
        while(node != null){
            n++;
            node = node.next;
        }
        ListNode pre = null;
        ListNode cur = head;
        ListNode dum = new ListNode(0,head);
        ListNode p1 = dum;
        for(int i = k-1; i < n; i += k){
            for(int j = 0; j < k; j++){
                ListNode temp = cur.next;
                cur.next = pre;
                pre = cur;
                cur = temp;
            }
            ListNode temp = p1.next;
            p1.next.next = cur;
            p1.next = pre;
            p1 = temp;
        }
        return dum.next;
    }
}
