package com.alibaba.fastjson.asm;

public interface MethodVisitor
{
    void visitInsn(final int p0);
    
    void visitIntInsn(final int p0, final int p1);
    
    void visitVarInsn(final int p0, final int p1);
    
    void visitTypeInsn(final int p0, final String p1);
    
    void visitFieldInsn(final int p0, final String p1, final String p2, final String p3);
    
    void visitMethodInsn(final int p0, final String p1, final String p2, final String p3);
    
    void visitJumpInsn(final int p0, final Label p1);
    
    void visitLabel(final Label p0);
    
    void visitLdcInsn(final Object p0);
    
    void visitIincInsn(final int p0, final int p1);
    
    void visitMaxs(final int p0, final int p1);
    
    void visitEnd();
}
