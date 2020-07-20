package com.alibaba.fastjson.util;

import java.util.Arrays;

public class IdentityHashMap<K, V>
{
    private final Entry<K, V>[] buckets;
    private final int indexMask;
    public static final int DEFAULT_SIZE = 8192;
    
    public IdentityHashMap() {
        this(8192);
    }
    
    public IdentityHashMap(final int tableSize) {
        this.indexMask = tableSize - 1;
        this.buckets = (Entry<K, V>[])new Entry[tableSize];
    }
    
    public final V get(final K key) {
        final int hash = System.identityHashCode(key);
        final int bucket = hash & this.indexMask;
        for (Entry<K, V> entry = this.buckets[bucket]; entry != null; entry = entry.next) {
            if (key == entry.key) {
                return entry.value;
            }
        }
        return null;
    }
    
    public Class findClass(final String keyString) {
        for (int i = 0; i < this.buckets.length; ++i) {
            final Entry bucket = this.buckets[i];
            if (bucket != null) {
                for (Entry<K, V> entry = (Entry<K, V>)bucket; entry != null; entry = entry.next) {
                    final Object key = bucket.key;
                    if (key instanceof Class) {
                        final Class clazz = (Class)key;
                        final String className = clazz.getName();
                        if (className.equals(keyString)) {
                            return clazz;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public boolean put(final K key, final V value) {
        final int hash = System.identityHashCode(key);
        final int bucket = hash & this.indexMask;
        for (Entry<K, V> entry = this.buckets[bucket]; entry != null; entry = entry.next) {
            if (key == entry.key) {
                entry.value = value;
                return true;
            }
        }
        Entry<K, V> entry = new Entry<K, V>(key, value, hash, this.buckets[bucket]);
        this.buckets[bucket] = entry;
        return false;
    }
    
    public void clear() {
        Arrays.fill(this.buckets, null);
    }
    
    protected static final class Entry<K, V>
    {
        public final int hashCode;
        public final K key;
        public V value;
        public final Entry<K, V> next;
        
        public Entry(final K key, final V value, final int hash, final Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
            this.hashCode = hash;
        }
    }
}
