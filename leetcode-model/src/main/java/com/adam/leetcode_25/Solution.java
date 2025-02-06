package com.adam.leetcode_25;

import com.adam.listnode.ListNode;

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
class Solution {
    public ListNode reverseKGroup(ListNode head, int k) {
        ListNode node = head;
        int n = 0;
        while(node != null){
            n++;
            node = node.next;
        }
        node = new ListNode(0,head);
        ListNode ini = node;
        ListNode pre = null;
        ListNode cur = head;
        for(int i = k-1; i < n; i+=k){
            for(int j = 0;j < k; j++){
                ListNode temp = cur.next;
                cur.next = pre;
                pre = cur;
                cur = temp;
            }
            ListNode temp = ini.next;
            temp.next = cur;
            ini.next = pre;
            ini = temp; 
        }
        return node.next;
    }
}