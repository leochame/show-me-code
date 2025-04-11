package com.adam.hashmap;

class SimpleHashMap<K, V> {
    private static final int DEFAULT_CAPACITY = 16;
    private Node<K, V>[] table;
    
    static class Node<K, V> {
        final K key;
        V value;
        Node<K, V> next;
        
        Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
    
    public SimpleHashMap() {
        table = new Node[DEFAULT_CAPACITY];
    }
    
    public void put(K key, V value) {
        int index = getIndex(key);
        Node<K, V> node = table[index];
        
        // 检查是否已存在相同key
        while (node != null) {
            if (node.key.equals(key)) {
                node.value = value;
                return;
            }
            node = node.next;
        }
        
        // 插入新节点
        Node<K, V> newNode = new Node<>(key, value, table[index]);
        table[index] = newNode;
    }
    
    public V get(K key) {
        int index = getIndex(key);
        Node<K, V> node = table[index];
        
        while (node != null) {
            if (node.key.equals(key)) {
                return node.value;
            }
            node = node.next;
        }
        return null;
    }
    
    public void remove(K key) {
        int index = getIndex(key);
        Node<K, V> node = table[index];
        Node<K, V> prev = null;
        
        while (node != null) {
            if (node.key.equals(key)) {
                if (prev == null) {
                    table[index] = node.next;
                } else {
                    prev.next = node.next;
                }
                return;
            }
            prev = node;
            node = node.next;
        }
    }
    
    private int getIndex(K key) {
        if (key == null) return 0;
        return Math.abs(key.hashCode()) % table.length;
    }
}