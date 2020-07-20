package com.alibaba.fastjson.asm;

public class ByteVector
{
    public byte[] data;
    public int length;
    
    public ByteVector() {
        this.data = new byte[64];
    }
    
    public ByteVector(final int initialSize) {
        this.data = new byte[initialSize];
    }
    
    public ByteVector putByte(final int b) {
        int length = this.length;
        if (length + 1 > this.data.length) {
            this.enlarge(1);
        }
        this.data[length++] = (byte)b;
        this.length = length;
        return this;
    }
    
    ByteVector put11(final int b1, final int b2) {
        int length = this.length;
        if (length + 2 > this.data.length) {
            this.enlarge(2);
        }
        final byte[] data = this.data;
        data[length++] = (byte)b1;
        data[length++] = (byte)b2;
        this.length = length;
        return this;
    }
    
    public ByteVector putShort(final int s) {
        int length = this.length;
        if (length + 2 > this.data.length) {
            this.enlarge(2);
        }
        final byte[] data = this.data;
        data[length++] = (byte)(s >>> 8);
        data[length++] = (byte)s;
        this.length = length;
        return this;
    }
    
    public ByteVector put12(final int b, final int s) {
        int length = this.length;
        if (length + 3 > this.data.length) {
            this.enlarge(3);
        }
        final byte[] data = this.data;
        data[length++] = (byte)b;
        data[length++] = (byte)(s >>> 8);
        data[length++] = (byte)s;
        this.length = length;
        return this;
    }
    
    public ByteVector putInt(final int i) {
        int length = this.length;
        if (length + 4 > this.data.length) {
            this.enlarge(4);
        }
        final byte[] data = this.data;
        data[length++] = (byte)(i >>> 24);
        data[length++] = (byte)(i >>> 16);
        data[length++] = (byte)(i >>> 8);
        data[length++] = (byte)i;
        this.length = length;
        return this;
    }
    
    public ByteVector putUTF8(final String s) {
        final int charLength = s.length();
        int len = this.length;
        if (len + 2 + charLength > this.data.length) {
            this.enlarge(2 + charLength);
        }
        final byte[] data = this.data;
        data[len++] = (byte)(charLength >>> 8);
        data[len++] = (byte)charLength;
        for (int i = 0; i < charLength; ++i) {
            final char c = s.charAt(i);
            if (c < '\u0001' || c > '\u007f') {
                throw new UnsupportedOperationException();
            }
            data[len++] = (byte)c;
        }
        this.length = len;
        return this;
    }
    
    public ByteVector putByteArray(final byte[] b, final int off, final int len) {
        if (this.length + len > this.data.length) {
            this.enlarge(len);
        }
        if (b != null) {
            System.arraycopy(b, off, this.data, this.length, len);
        }
        this.length += len;
        return this;
    }
    
    private void enlarge(final int size) {
        final int length1 = 2 * this.data.length;
        final int length2 = this.length + size;
        final byte[] newData = new byte[(length1 > length2) ? length1 : length2];
        System.arraycopy(this.data, 0, newData, 0, this.length);
        this.data = newData;
    }
}
