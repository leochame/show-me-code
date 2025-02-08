package com.adam.listnode.两数相加;

//import com.adam.listnode.ListNode;

class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode t1 = l1;
        ListNode t2 = l2;
        ListNode ans = new ListNode();
        ListNode cur = ans;
        int a = 0;
        while(t1 != null && t2 != null){
            
            cur.next = new ListNode((a + t1.val + t2.val) % 10);
            a = (a + t1.val + t2.val )/10;
            t1 = t1.next;
            t2 = t2.next;
            cur = cur.next;
        }
        while(t1 != null){
            
            cur.next = new ListNode((a + t1.val) % 10);
            a = (a + t1.val)/10;
            t1 = t1.next;
            cur = cur.next;
        }
        while(t2 != null){
            
            cur.next = new ListNode((a + t2.val) % 10);
            a = (a + t2.val)/10;
            t2 = t2.next;
            cur = cur.next;
        }
        if(a != 0){
            cur.next = new ListNode(a);
        }
        return ans.next;
    }
    public class ListNode {
        int val;
        ListNode next;
        ListNode() {}
        ListNode(int val) { this.val = val; }
        ListNode(int val, ListNode next) { this.val = val; this.next = next; }
    }
}