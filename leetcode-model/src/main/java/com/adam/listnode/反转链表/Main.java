package com.adam.listnode.反转链表;
import java.util.*;
public class Main {

    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        int n = scan.nextInt();

        ListNode first = new ListNode();
        ListNode node = first;
        for(int i = 0; i < n; i++){
            int x = scan.nextInt();
            node.next = new ListNode(x);
            node = node.next;
        }
        Solution solution = new Solution();
//        solution.reverseList();

    }
}
class ListNode {
    ListNode next;
    int val;
    ListNode(){}
    ListNode(int val) {
        this.val = val;
    }
}
