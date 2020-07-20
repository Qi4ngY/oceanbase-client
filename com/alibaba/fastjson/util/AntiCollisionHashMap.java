package com.alibaba.fastjson.util;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.NoSuchElementException;
import java.util.ConcurrentModificationException;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Random;
import java.util.Collection;
import java.util.Set;
import java.io.Serializable;
import java.util.Map;
import java.util.AbstractMap;

public class AntiCollisionHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable
{
    transient volatile Set<K> keySet;
    transient volatile Collection<V> values;
    static final int DEFAULT_INITIAL_CAPACITY = 16;
    static final int MAXIMUM_CAPACITY = 1073741824;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    transient Entry<K, V>[] table;
    transient int size;
    int threshold;
    final float loadFactor;
    transient volatile int modCount;
    static final int M_MASK = -2023358765;
    static final int SEED = -2128831035;
    static final int KEY = 16777619;
    final int random;
    private transient Set<Map.Entry<K, V>> entrySet;
    private static final long serialVersionUID = 362498820763181265L;
    
    private int hashString(final String key) {
        int hash = -2128831035 * this.random;
        for (int i = 0; i < key.length(); ++i) {
            hash = (hash * 16777619 ^ key.charAt(i));
        }
        return (hash ^ hash >> 1) & 0x8765FED3;
    }
    
    public AntiCollisionHashMap(int initialCapacity, final float loadFactor) {
        this.keySet = null;
        this.values = null;
        this.random = new Random().nextInt(99999);
        this.entrySet = null;
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }
        if (initialCapacity > 1073741824) {
            initialCapacity = 1073741824;
        }
        if (loadFactor <= 0.0f || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        int capacity;
        for (capacity = 1; capacity < initialCapacity; capacity <<= 1) {}
        this.loadFactor = loadFactor;
        this.threshold = (int)(capacity * loadFactor);
        this.table = (Entry<K, V>[])new Entry[capacity];
        this.init();
    }
    
    public AntiCollisionHashMap(final int initialCapacity) {
        this(initialCapacity, 0.75f);
    }
    
    public AntiCollisionHashMap() {
        this.keySet = null;
        this.values = null;
        this.random = new Random().nextInt(99999);
        this.entrySet = null;
        this.loadFactor = 0.75f;
        this.threshold = 12;
        this.table = (Entry<K, V>[])new Entry[16];
        this.init();
    }
    
    public AntiCollisionHashMap(final Map<? extends K, ? extends V> m) {
        this(Math.max((int)(m.size() / 0.75f) + 1, 16), 0.75f);
        this.putAllForCreate(m);
    }
    
    void init() {
    }
    
    static int hash(int h) {
        h *= h;
        h ^= (h >>> 20 ^ h >>> 12);
        return h ^ h >>> 7 ^ h >>> 4;
    }
    
    static int indexFor(final int h, final int length) {
        return h & length - 1;
    }
    
    @Override
    public int size() {
        return this.size;
    }
    
    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }
    
    @Override
    public V get(final Object key) {
        if (key == null) {
            return this.getForNullKey();
        }
        int hash = 0;
        if (key instanceof String) {
            hash = hash(this.hashString((String)key));
        }
        else {
            hash = hash(key.hashCode());
        }
        for (Entry<K, V> e = this.table[indexFor(hash, this.table.length)]; e != null; e = e.next) {
            final Object k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                return e.value;
            }
        }
        return null;
    }
    
    private V getForNullKey() {
        for (Entry<K, V> e = this.table[0]; e != null; e = e.next) {
            if (e.key == null) {
                return e.value;
            }
        }
        return null;
    }
    
    @Override
    public boolean containsKey(final Object key) {
        return this.getEntry(key) != null;
    }
    
    final Entry<K, V> getEntry(final Object key) {
        final int hash = (key == null) ? 0 : ((key instanceof String) ? hash(this.hashString((String)key)) : hash(key.hashCode()));
        for (Entry<K, V> e = this.table[indexFor(hash, this.table.length)]; e != null; e = e.next) {
            final Object k;
            if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                return e;
            }
        }
        return null;
    }
    
    @Override
    public V put(final K key, final V value) {
        if (key == null) {
            return this.putForNullKey(value);
        }
        int hash = 0;
        if (key instanceof String) {
            hash = hash(this.hashString((String)key));
        }
        else {
            hash = hash(key.hashCode());
        }
        final int i = indexFor(hash, this.table.length);
        for (Entry<K, V> e = this.table[i]; e != null; e = e.next) {
            final Object k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                final V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        ++this.modCount;
        this.addEntry(hash, key, value, i);
        return null;
    }
    
    private V putForNullKey(final V value) {
        for (Entry<K, V> e = this.table[0]; e != null; e = e.next) {
            if (e.key == null) {
                final V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        ++this.modCount;
        this.addEntry(0, null, value, 0);
        return null;
    }
    
    private void putForCreate(final K key, final V value) {
        final int hash = (key == null) ? 0 : ((key instanceof String) ? hash(this.hashString((String)key)) : hash(key.hashCode()));
        final int i = indexFor(hash, this.table.length);
        for (Entry<K, V> e = this.table[i]; e != null; e = e.next) {
            final Object k;
            if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                e.value = value;
                return;
            }
        }
        this.createEntry(hash, key, value, i);
    }
    
    private void putAllForCreate(final Map<? extends K, ? extends V> m) {
        for (final Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            this.putForCreate(e.getKey(), e.getValue());
        }
    }
    
    void resize(final int newCapacity) {
        final Entry<K, V>[] oldTable = this.table;
        final int oldCapacity = oldTable.length;
        if (oldCapacity == 1073741824) {
            this.threshold = Integer.MAX_VALUE;
            return;
        }
        final Entry<K, V>[] newTable = (Entry<K, V>[])new Entry[newCapacity];
        this.transfer(newTable);
        this.table = newTable;
        this.threshold = (int)(newCapacity * this.loadFactor);
    }
    
    void transfer(final Entry[] newTable) {
        final Entry[] src = this.table;
        final int newCapacity = newTable.length;
        for (int j = 0; j < src.length; ++j) {
            Entry<K, V> e = (Entry<K, V>)src[j];
            if (e != null) {
                src[j] = null;
                do {
                    final Entry<K, V> next = e.next;
                    final int i = indexFor(e.hash, newCapacity);
                    e.next = (Entry<K, V>)newTable[i];
                    newTable[i] = e;
                    e = next;
                } while (e != null);
            }
        }
    }
    
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        final int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded == 0) {
            return;
        }
        if (numKeysToBeAdded > this.threshold) {
            int targetCapacity = (int)(numKeysToBeAdded / this.loadFactor + 1.0f);
            if (targetCapacity > 1073741824) {
                targetCapacity = 1073741824;
            }
            int newCapacity;
            for (newCapacity = this.table.length; newCapacity < targetCapacity; newCapacity <<= 1) {}
            if (newCapacity > this.table.length) {
                this.resize(newCapacity);
            }
        }
        for (final Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }
    
    @Override
    public V remove(final Object key) {
        final Entry<K, V> e = this.removeEntryForKey(key);
        return (e == null) ? null : e.value;
    }
    
    final Entry<K, V> removeEntryForKey(final Object key) {
        final int hash = (key == null) ? 0 : ((key instanceof String) ? hash(this.hashString((String)key)) : hash(key.hashCode()));
        final int i = indexFor(hash, this.table.length);
        Entry<K, V> e;
        Entry<K, V> next;
        for (Entry<K, V> prev = e = this.table[i]; e != null; e = next) {
            next = e.next;
            final Object k;
            if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                ++this.modCount;
                --this.size;
                if (prev == e) {
                    this.table[i] = next;
                }
                else {
                    prev.next = next;
                }
                return e;
            }
            prev = e;
        }
        return e;
    }
    
    final Entry<K, V> removeMapping(final Object o) {
        if (!(o instanceof Map.Entry)) {
            return null;
        }
        final Map.Entry<K, V> entry = (Map.Entry<K, V>)o;
        final Object key = entry.getKey();
        final int hash = (key == null) ? 0 : ((key instanceof String) ? hash(this.hashString((String)key)) : hash(key.hashCode()));
        final int i = indexFor(hash, this.table.length);
        Entry<K, V> e;
        Entry<K, V> next;
        for (Entry<K, V> prev = e = this.table[i]; e != null; e = next) {
            next = e.next;
            if (e.hash == hash && e.equals(entry)) {
                ++this.modCount;
                --this.size;
                if (prev == e) {
                    this.table[i] = next;
                }
                else {
                    prev.next = next;
                }
                return e;
            }
            prev = e;
        }
        return e;
    }
    
    @Override
    public void clear() {
        ++this.modCount;
        final Entry[] tab = this.table;
        for (int i = 0; i < tab.length; ++i) {
            tab[i] = null;
        }
        this.size = 0;
    }
    
    @Override
    public boolean containsValue(final Object value) {
        if (value == null) {
            return this.containsNullValue();
        }
        final Entry[] tab = this.table;
        for (int i = 0; i < tab.length; ++i) {
            for (Entry e = tab[i]; e != null; e = e.next) {
                if (value.equals(e.value)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean containsNullValue() {
        final Entry[] tab = this.table;
        for (int i = 0; i < tab.length; ++i) {
            for (Entry e = tab[i]; e != null; e = e.next) {
                if (e.value == null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public Object clone() {
        AntiCollisionHashMap<K, V> result = null;
        try {
            result = (AntiCollisionHashMap)super.clone();
        }
        catch (CloneNotSupportedException ex) {}
        result.table = (Entry<K, V>[])new Entry[this.table.length];
        result.entrySet = null;
        result.modCount = 0;
        result.size = 0;
        result.init();
        result.putAllForCreate((Map<? extends K, ? extends V>)this);
        return result;
    }
    
    void addEntry(final int hash, final K key, final V value, final int bucketIndex) {
        final Entry<K, V> e = this.table[bucketIndex];
        this.table[bucketIndex] = new Entry<K, V>(hash, key, value, e);
        if (this.size++ >= this.threshold) {
            this.resize(2 * this.table.length);
        }
    }
    
    void createEntry(final int hash, final K key, final V value, final int bucketIndex) {
        final Entry<K, V> e = this.table[bucketIndex];
        this.table[bucketIndex] = new Entry<K, V>(hash, key, value, e);
        ++this.size;
    }
    
    Iterator<K> newKeyIterator() {
        return new KeyIterator();
    }
    
    Iterator<V> newValueIterator() {
        return new ValueIterator();
    }
    
    Iterator<Map.Entry<K, V>> newEntryIterator() {
        return new EntryIterator();
    }
    
    @Override
    public Set<K> keySet() {
        final Set<K> ks = this.keySet;
        return (ks != null) ? ks : (this.keySet = new KeySet());
    }
    
    @Override
    public Collection<V> values() {
        final Collection<V> vs = this.values;
        return (vs != null) ? vs : (this.values = new Values());
    }
    
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return this.entrySet0();
    }
    
    private Set<Map.Entry<K, V>> entrySet0() {
        final Set<Map.Entry<K, V>> es = this.entrySet;
        return (es != null) ? es : (this.entrySet = new EntrySet());
    }
    
    private void writeObject(final ObjectOutputStream s) throws IOException {
        final Iterator<Map.Entry<K, V>> i = (this.size > 0) ? this.entrySet0().iterator() : null;
        s.defaultWriteObject();
        s.writeInt(this.table.length);
        s.writeInt(this.size);
        if (i != null) {
            while (i.hasNext()) {
                final Map.Entry<K, V> e = i.next();
                s.writeObject(e.getKey());
                s.writeObject(e.getValue());
            }
        }
    }
    
    private void readObject(final ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        final int numBuckets = s.readInt();
        this.table = (Entry<K, V>[])new Entry[numBuckets];
        this.init();
        for (int size = s.readInt(), i = 0; i < size; ++i) {
            final K key = (K)s.readObject();
            final V value = (V)s.readObject();
            this.putForCreate(key, value);
        }
    }
    
    static class Entry<K, V> implements Map.Entry<K, V>
    {
        final K key;
        V value;
        Entry<K, V> next;
        final int hash;
        
        Entry(final int h, final K k, final V v, final Entry<K, V> n) {
            this.value = v;
            this.next = n;
            this.key = k;
            this.hash = h;
        }
        
        @Override
        public final K getKey() {
            return this.key;
        }
        
        @Override
        public final V getValue() {
            return this.value;
        }
        
        @Override
        public final V setValue(final V newValue) {
            final V oldValue = this.value;
            this.value = newValue;
            return oldValue;
        }
        
        @Override
        public final boolean equals(final Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            final Map.Entry e = (Map.Entry)o;
            final Object k1 = this.getKey();
            final Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                final Object v1 = this.getValue();
                final Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2))) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public final int hashCode() {
            return ((this.key == null) ? 0 : this.key.hashCode()) ^ ((this.value == null) ? 0 : this.value.hashCode());
        }
        
        @Override
        public final String toString() {
            return this.getKey() + "=" + this.getValue();
        }
    }
    
    private abstract class HashIterator<E> implements Iterator<E>
    {
        Entry<K, V> next;
        int expectedModCount;
        int index;
        Entry<K, V> current;
        
        HashIterator() {
            this.expectedModCount = AntiCollisionHashMap.this.modCount;
            if (AntiCollisionHashMap.this.size > 0) {
                final Entry[] t = AntiCollisionHashMap.this.table;
                while (this.index < t.length && (this.next = (Entry<K, V>)t[this.index++]) == null) {}
            }
        }
        
        @Override
        public final boolean hasNext() {
            return this.next != null;
        }
        
        final Entry<K, V> nextEntry() {
            if (AntiCollisionHashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
            final Entry<K, V> e = this.next;
            if (e == null) {
                throw new NoSuchElementException();
            }
            if ((this.next = e.next) == null) {
                final Entry[] t = AntiCollisionHashMap.this.table;
                while (this.index < t.length && (this.next = (Entry<K, V>)t[this.index++]) == null) {}
            }
            return this.current = e;
        }
        
        @Override
        public void remove() {
            if (this.current == null) {
                throw new IllegalStateException();
            }
            if (AntiCollisionHashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
            final Object k = this.current.key;
            this.current = null;
            AntiCollisionHashMap.this.removeEntryForKey(k);
            this.expectedModCount = AntiCollisionHashMap.this.modCount;
        }
    }
    
    private final class ValueIterator extends HashIterator<V>
    {
        @Override
        public V next() {
            return this.nextEntry().value;
        }
    }
    
    private final class KeyIterator extends HashIterator<K>
    {
        @Override
        public K next() {
            return this.nextEntry().getKey();
        }
    }
    
    private final class EntryIterator extends HashIterator<Map.Entry<K, V>>
    {
        @Override
        public Map.Entry<K, V> next() {
            return this.nextEntry();
        }
    }
    
    private final class KeySet extends AbstractSet<K>
    {
        @Override
        public Iterator<K> iterator() {
            return AntiCollisionHashMap.this.newKeyIterator();
        }
        
        @Override
        public int size() {
            return AntiCollisionHashMap.this.size;
        }
        
        @Override
        public boolean contains(final Object o) {
            return AntiCollisionHashMap.this.containsKey(o);
        }
        
        @Override
        public boolean remove(final Object o) {
            return AntiCollisionHashMap.this.removeEntryForKey(o) != null;
        }
        
        @Override
        public void clear() {
            AntiCollisionHashMap.this.clear();
        }
    }
    
    private final class Values extends AbstractCollection<V>
    {
        @Override
        public Iterator<V> iterator() {
            return AntiCollisionHashMap.this.newValueIterator();
        }
        
        @Override
        public int size() {
            return AntiCollisionHashMap.this.size;
        }
        
        @Override
        public boolean contains(final Object o) {
            return AntiCollisionHashMap.this.containsValue(o);
        }
        
        @Override
        public void clear() {
            AntiCollisionHashMap.this.clear();
        }
    }
    
    private final class EntrySet extends AbstractSet<Map.Entry<K, V>>
    {
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return AntiCollisionHashMap.this.newEntryIterator();
        }
        
        @Override
        public boolean contains(final Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            final Map.Entry<K, V> e = (Map.Entry<K, V>)o;
            final Entry<K, V> candidate = AntiCollisionHashMap.this.getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }
        
        @Override
        public boolean remove(final Object o) {
            return AntiCollisionHashMap.this.removeMapping(o) != null;
        }
        
        @Override
        public int size() {
            return AntiCollisionHashMap.this.size;
        }
        
        @Override
        public void clear() {
            AntiCollisionHashMap.this.clear();
        }
    }
}
