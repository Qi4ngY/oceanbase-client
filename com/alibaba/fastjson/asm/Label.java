package com.alibaba.fastjson.asm;

public class Label
{
    int status;
    int position;
    private int referenceCount;
    private int[] srcAndRefPositions;
    static final int FORWARD_REFERENCE_TYPE_MASK = -268435456;
    static final int FORWARD_REFERENCE_HANDLE_MASK = 268435455;
    static final int FORWARD_REFERENCE_TYPE_SHORT = 268435456;
    static final int FORWARD_REFERENCE_TYPE_WIDE = 536870912;
    int inputStackTop;
    int outputStackMax;
    Label successor;
    Label next;
    
    void put(final MethodWriter owner, final ByteVector out, final int source, final boolean wideOffset) {
        if ((this.status & 0x2) == 0x0) {
            if (wideOffset) {
                this.addReference(source, out.length, 536870912);
                out.putInt(-1);
            }
            else {
                this.addReference(source, out.length, 268435456);
                out.putShort(-1);
            }
        }
        else if (wideOffset) {
            out.putInt(this.position - source);
        }
        else {
            out.putShort(this.position - source);
        }
    }
    
    private void addReference(final int sourcePosition, final int referencePosition, final int referenceType) {
        if (this.srcAndRefPositions == null) {
            this.srcAndRefPositions = new int[6];
        }
        if (this.referenceCount >= this.srcAndRefPositions.length) {
            final int[] a = new int[this.srcAndRefPositions.length + 6];
            System.arraycopy(this.srcAndRefPositions, 0, a, 0, this.srcAndRefPositions.length);
            this.srcAndRefPositions = a;
        }
        this.srcAndRefPositions[this.referenceCount++] = sourcePosition;
        this.srcAndRefPositions[this.referenceCount++] = (referencePosition | referenceType);
    }
    
    void resolve(final MethodWriter owner, final int position, final byte[] data) {
        this.status |= 0x2;
        this.position = position;
        int i = 0;
        while (i < this.referenceCount) {
            final int source = this.srcAndRefPositions[i++];
            final int reference = this.srcAndRefPositions[i++];
            int handle = reference & 0xFFFFFFF;
            final int offset = position - source;
            if ((reference & 0xF0000000) == 0x10000000) {
                data[handle++] = (byte)(offset >>> 8);
                data[handle] = (byte)offset;
            }
            else {
                data[handle++] = (byte)(offset >>> 24);
                data[handle++] = (byte)(offset >>> 16);
                data[handle++] = (byte)(offset >>> 8);
                data[handle] = (byte)offset;
            }
        }
    }
}
