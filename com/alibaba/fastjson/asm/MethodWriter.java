package com.alibaba.fastjson.asm;

public class MethodWriter implements MethodVisitor
{
    MethodWriter next;
    final ClassWriter cw;
    private int access;
    private final int name;
    private final int desc;
    int exceptionCount;
    int[] exceptions;
    private ByteVector code;
    private int maxStack;
    private int maxLocals;
    
    public MethodWriter(final ClassWriter cw, final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        this.code = new ByteVector();
        if (cw.firstMethod == null) {
            cw.firstMethod = this;
        }
        else {
            cw.lastMethod.next = this;
        }
        cw.lastMethod = this;
        this.cw = cw;
        this.access = access;
        this.name = cw.newUTF8(name);
        this.desc = cw.newUTF8(desc);
        if (exceptions != null && exceptions.length > 0) {
            this.exceptionCount = exceptions.length;
            this.exceptions = new int[this.exceptionCount];
            for (int i = 0; i < this.exceptionCount; ++i) {
                this.exceptions[i] = cw.newClassItem(exceptions[i]).index;
            }
        }
    }
    
    @Override
    public void visitInsn(final int opcode) {
        this.code.putByte(opcode);
    }
    
    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        this.code.put11(opcode, operand);
    }
    
    @Override
    public void visitVarInsn(final int opcode, final int var) {
        if (var < 4 && opcode != 169) {
            int opt;
            if (opcode < 54) {
                opt = 26 + (opcode - 21 << 2) + var;
            }
            else {
                opt = 59 + (opcode - 54 << 2) + var;
            }
            this.code.putByte(opt);
        }
        else if (var >= 256) {
            this.code.putByte(196).put12(opcode, var);
        }
        else {
            this.code.put11(opcode, var);
        }
    }
    
    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        final Item i = this.cw.newClassItem(type);
        this.code.put12(opcode, i.index);
    }
    
    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        final Item i = this.cw.newFieldItem(owner, name, desc);
        this.code.put12(opcode, i.index);
    }
    
    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
        final boolean itf = opcode == 185;
        final Item i = this.cw.newMethodItem(owner, name, desc, itf);
        int argSize = i.intVal;
        if (itf) {
            if (argSize == 0) {
                argSize = Type.getArgumentsAndReturnSizes(desc);
                i.intVal = argSize;
            }
            this.code.put12(185, i.index).put11(argSize >> 2, 0);
        }
        else {
            this.code.put12(opcode, i.index);
        }
    }
    
    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        if ((label.status & 0x2) != 0x0 && label.position - this.code.length < -32768) {
            throw new UnsupportedOperationException();
        }
        this.code.putByte(opcode);
        label.put(this, this.code, this.code.length - 1, opcode == 200);
    }
    
    @Override
    public void visitLabel(final Label label) {
        label.resolve(this, this.code.length, this.code.data);
    }
    
    @Override
    public void visitLdcInsn(final Object cst) {
        final Item i = this.cw.newConstItem(cst);
        final int index = i.index;
        if (i.type == 5 || i.type == 6) {
            this.code.put12(20, index);
        }
        else if (index >= 256) {
            this.code.put12(19, index);
        }
        else {
            this.code.put11(18, index);
        }
    }
    
    @Override
    public void visitIincInsn(final int var, final int increment) {
        this.code.putByte(132).put11(var, increment);
    }
    
    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        this.maxStack = maxStack;
        this.maxLocals = maxLocals;
    }
    
    @Override
    public void visitEnd() {
    }
    
    final int getSize() {
        int size = 8;
        if (this.code.length > 0) {
            this.cw.newUTF8("Code");
            size += 18 + this.code.length + 0;
        }
        if (this.exceptionCount > 0) {
            this.cw.newUTF8("Exceptions");
            size += 8 + 2 * this.exceptionCount;
        }
        return size;
    }
    
    final void put(final ByteVector out) {
        final int mask = 393216;
        out.putShort(this.access & 0xFFF9FFFF).putShort(this.name).putShort(this.desc);
        int attributeCount = 0;
        if (this.code.length > 0) {
            ++attributeCount;
        }
        if (this.exceptionCount > 0) {
            ++attributeCount;
        }
        out.putShort(attributeCount);
        if (this.code.length > 0) {
            final int size = 12 + this.code.length + 0;
            out.putShort(this.cw.newUTF8("Code")).putInt(size);
            out.putShort(this.maxStack).putShort(this.maxLocals);
            out.putInt(this.code.length).putByteArray(this.code.data, 0, this.code.length);
            out.putShort(0);
            attributeCount = 0;
            out.putShort(attributeCount);
        }
        if (this.exceptionCount > 0) {
            out.putShort(this.cw.newUTF8("Exceptions")).putInt(2 * this.exceptionCount + 2);
            out.putShort(this.exceptionCount);
            for (int i = 0; i < this.exceptionCount; ++i) {
                out.putShort(this.exceptions[i]);
            }
        }
    }
}
