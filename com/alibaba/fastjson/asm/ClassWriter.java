package com.alibaba.fastjson.asm;

public class ClassWriter
{
    int version;
    int index;
    final ByteVector pool;
    Item[] items;
    int threshold;
    final Item key;
    final Item key2;
    final Item key3;
    Item[] typeTable;
    private int access;
    private int name;
    String thisName;
    private int superName;
    private int interfaceCount;
    private int[] interfaces;
    FieldWriter firstField;
    FieldWriter lastField;
    MethodWriter firstMethod;
    MethodWriter lastMethod;
    
    public ClassWriter() {
        this(0);
    }
    
    private ClassWriter(final int flags) {
        this.index = 1;
        this.pool = new ByteVector();
        this.items = new Item[256];
        this.threshold = (int)(0.75 * this.items.length);
        this.key = new Item();
        this.key2 = new Item();
        this.key3 = new Item();
    }
    
    public void visit(final int version, final int access, final String name, final String superName, final String[] interfaces) {
        this.version = version;
        this.access = access;
        this.name = this.newClassItem(name).index;
        this.thisName = name;
        this.superName = ((superName == null) ? 0 : this.newClassItem(superName).index);
        if (interfaces != null && interfaces.length > 0) {
            this.interfaceCount = interfaces.length;
            this.interfaces = new int[this.interfaceCount];
            for (int i = 0; i < this.interfaceCount; ++i) {
                this.interfaces[i] = this.newClassItem(interfaces[i]).index;
            }
        }
    }
    
    public byte[] toByteArray() {
        int size = 24 + 2 * this.interfaceCount;
        int nbFields = 0;
        for (FieldWriter fb = this.firstField; fb != null; fb = fb.next) {
            ++nbFields;
            size += fb.getSize();
        }
        int nbMethods = 0;
        for (MethodWriter mb = this.firstMethod; mb != null; mb = mb.next) {
            ++nbMethods;
            size += mb.getSize();
        }
        final int attributeCount = 0;
        size += this.pool.length;
        final ByteVector out = new ByteVector(size);
        out.putInt(-889275714).putInt(this.version);
        out.putShort(this.index).putByteArray(this.pool.data, 0, this.pool.length);
        final int mask = 393216;
        out.putShort(this.access & ~mask).putShort(this.name).putShort(this.superName);
        out.putShort(this.interfaceCount);
        for (int i = 0; i < this.interfaceCount; ++i) {
            out.putShort(this.interfaces[i]);
        }
        out.putShort(nbFields);
        for (FieldWriter fb = this.firstField; fb != null; fb = fb.next) {
            fb.put(out);
        }
        out.putShort(nbMethods);
        for (MethodWriter mb = this.firstMethod; mb != null; mb = mb.next) {
            mb.put(out);
        }
        out.putShort(attributeCount);
        return out.data;
    }
    
    Item newConstItem(final Object cst) {
        if (cst instanceof Integer) {
            final int val = (int)cst;
            this.key.set(val);
            Item result = this.get(this.key);
            if (result == null) {
                this.pool.putByte(3).putInt(val);
                result = new Item(this.index++, this.key);
                this.put(result);
            }
            return result;
        }
        if (cst instanceof String) {
            return this.newString((String)cst);
        }
        if (cst instanceof Type) {
            final Type t = (Type)cst;
            return this.newClassItem((t.sort == 10) ? t.getInternalName() : t.getDescriptor());
        }
        throw new IllegalArgumentException("value " + cst);
    }
    
    public int newUTF8(final String value) {
        this.key.set(1, value, null, null);
        Item result = this.get(this.key);
        if (result == null) {
            this.pool.putByte(1).putUTF8(value);
            result = new Item(this.index++, this.key);
            this.put(result);
        }
        return result.index;
    }
    
    public Item newClassItem(final String value) {
        this.key2.set(7, value, null, null);
        Item result = this.get(this.key2);
        if (result == null) {
            this.pool.put12(7, this.newUTF8(value));
            result = new Item(this.index++, this.key2);
            this.put(result);
        }
        return result;
    }
    
    Item newFieldItem(final String owner, final String name, final String desc) {
        this.key3.set(9, owner, name, desc);
        Item result = this.get(this.key3);
        if (result == null) {
            final int s1 = this.newClassItem(owner).index;
            final int s2 = this.newNameTypeItem(name, desc).index;
            this.pool.put12(9, s1).putShort(s2);
            result = new Item(this.index++, this.key3);
            this.put(result);
        }
        return result;
    }
    
    Item newMethodItem(final String owner, final String name, final String desc, final boolean itf) {
        final int type = itf ? 11 : 10;
        this.key3.set(type, owner, name, desc);
        Item result = this.get(this.key3);
        if (result == null) {
            final int s1 = this.newClassItem(owner).index;
            final int s2 = this.newNameTypeItem(name, desc).index;
            this.pool.put12(type, s1).putShort(s2);
            result = new Item(this.index++, this.key3);
            this.put(result);
        }
        return result;
    }
    
    private Item newString(final String value) {
        this.key2.set(8, value, null, null);
        Item result = this.get(this.key2);
        if (result == null) {
            this.pool.put12(8, this.newUTF8(value));
            result = new Item(this.index++, this.key2);
            this.put(result);
        }
        return result;
    }
    
    public Item newNameTypeItem(final String name, final String desc) {
        this.key2.set(12, name, desc, null);
        Item result = this.get(this.key2);
        if (result == null) {
            final int s1 = this.newUTF8(name);
            final int s2 = this.newUTF8(desc);
            this.pool.put12(12, s1).putShort(s2);
            result = new Item(this.index++, this.key2);
            this.put(result);
        }
        return result;
    }
    
    private Item get(final Item key) {
        Item i;
        for (i = this.items[key.hashCode % this.items.length]; i != null && (i.type != key.type || !key.isEqualTo(i)); i = i.next) {}
        return i;
    }
    
    private void put(final Item i) {
        if (this.index > this.threshold) {
            final int ll = this.items.length;
            final int nl = ll * 2 + 1;
            final Item[] newItems = new Item[nl];
            for (int l = ll - 1; l >= 0; --l) {
                Item k;
                for (Item j = this.items[l]; j != null; j = k) {
                    final int index = j.hashCode % newItems.length;
                    k = j.next;
                    j.next = newItems[index];
                    newItems[index] = j;
                }
            }
            this.items = newItems;
            this.threshold = (int)(nl * 0.75);
        }
        final int index2 = i.hashCode % this.items.length;
        i.next = this.items[index2];
        this.items[index2] = i;
    }
}
