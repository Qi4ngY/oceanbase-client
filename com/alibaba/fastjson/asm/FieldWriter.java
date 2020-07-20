package com.alibaba.fastjson.asm;

public final class FieldWriter
{
    FieldWriter next;
    private final int access;
    private final int name;
    private final int desc;
    
    public FieldWriter(final ClassWriter cw, final int access, final String name, final String desc) {
        if (cw.firstField == null) {
            cw.firstField = this;
        }
        else {
            cw.lastField.next = this;
        }
        cw.lastField = this;
        this.access = access;
        this.name = cw.newUTF8(name);
        this.desc = cw.newUTF8(desc);
    }
    
    public void visitEnd() {
    }
    
    int getSize() {
        return 8;
    }
    
    void put(final ByteVector out) {
        final int mask = 393216;
        out.putShort(this.access & 0xFFF9FFFF).putShort(this.name).putShort(this.desc);
        final int attributeCount = 0;
        out.putShort(attributeCount);
    }
}
