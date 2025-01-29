package com.adam.leetcode_146;

class LRUCache {
    public class TreeNode{
        int key;
        int val;
        TreeNode next;
        TreeNode prev;
        TreeNode(){}
    }
    int capacity;
    int size;
    TreeNode root;
    TreeNode tail;
    Map<Integer,TreeNode> map = new HashMap<>();
    List<Integer> list = new ArrayList<>();

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.map = new HashMap<>();
        this.list = new ArrayList<>();
        root = new TreeNode();
        tail = new TreeNode();
        root.next = tail;
        tail.prev = root;
    }
    
    public int get(int key) {
        TreeNode  node = map.get(key);
        if(node != null){
            node.prev.next = node.next;
            node.next.prev = node.prev;
            node.next = root.next;
            root.next.prev = node;
            node.prev = root;
            root.next = node;
            return node.val;
        }
        return -1;
    }
    
    public void put(int key, int value) {
        TreeNode  node = map.get(key);
        if(node != null){
            node.prev.next = node.next;
            node.next.prev = node.prev;
            node.next = root.next;
            root.next.prev = node;
            node.prev = root;
            root.next = node;
            node.val = value;
            return;
        }
        size++;
        node = new TreeNode();
        node.next = root.next;
        node.prev = root;
        root.next.prev = node;
        root.next = node;
        node.key = key;
        node.val = value;
        map.put(key,node);
        if(size > capacity){
            size--;
            TreeNode node1 = tail.prev;
            tail.prev = node1.prev;
            node1.prev.next = tail;
            map.remove(node1.key);
        }
    }
}

/**
 * Your LRUCache object will be instantiated and called as such:
 * LRUCache obj = new LRUCache(capacity);
 * int param_1 = obj.get(key);
 * obj.put(key,value);
 */