package com.adam.lru;

import java.util.*;

public class TTLAndLRU<K, V> {

    class CacheValue {
        V value;
        long expiryTime;

        CacheValue(V value, long ttlMillis) {
            this.value = value;
            this.expiryTime = System.currentTimeMillis() + ttlMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final long ttlMillis;
    private final int capacity;

    private final LinkedHashMap<K, CacheValue> cache;

    public TTLAndLRU(int capacity, long ttlMillis) {
        this.capacity = capacity;
        this.ttlMillis = ttlMillis;
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<K, CacheValue> eldest) {
                return size() > TTLAndLRU.this.capacity;
            }
        };
    }

    public synchronized void put(K key, V value) {
        cache.put(key, new CacheValue(value, ttlMillis));
    }

    public synchronized V get(K key) {
        CacheValue cacheValue = cache.get(key);
        if (cacheValue == null) return null;
        if (cacheValue.isExpired()) {
            cache.remove(key);
            return null;
        }
        return cacheValue.value;
    }

    public synchronized int size() {
        return cache.size();
    }
}
