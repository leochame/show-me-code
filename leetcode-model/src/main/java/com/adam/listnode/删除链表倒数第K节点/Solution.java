package com.adam.listnode.删除链表倒数第K节点;

import com.adam.listnode.ListNode;

class Solution {
    public ListNode removeNthFromEnd(ListNode head, int x) {
        int n = 0;
        ListNode node = head;
        while(node != null){
            n++;
            node = node.next;
        }
        if ( n == 1){
            return null;
        }

        node = head;
        n = n - x; 
        if( n == 0) return head.next;
        while(n != 1 && node != null){
            n -- ;
            node = node.next;
        }
        node.next = node.next.next;
        return head;
    }
}