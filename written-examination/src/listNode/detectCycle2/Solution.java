package listNode.detectCycle2;

import listNode.ListNode;

import java.util.concurrent.ThreadPoolExecutor;

public class Solution {
    public ListNode detectCycle(ListNode head) {
        if(head == null || head.next == null || head.next.next == null)
             return null;
        ListNode fast = head.next.next;
        ListNode slow = head.next;
        while(fast !=  slow ){
            if(fast.next == null || fast.next.next == null)
             return null;
            fast = fast.next.next;
            slow = slow.next;
        }


        while(slow != head){
            slow = slow.next;
            head = head.next;
        }
        return head;
    }
}