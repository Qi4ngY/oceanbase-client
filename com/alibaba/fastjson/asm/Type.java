package com.alibaba.fastjson.asm;

public class Type
{
    public static final Type VOID_TYPE;
    public static final Type BOOLEAN_TYPE;
    public static final Type CHAR_TYPE;
    public static final Type BYTE_TYPE;
    public static final Type SHORT_TYPE;
    public static final Type INT_TYPE;
    public static final Type FLOAT_TYPE;
    public static final Type LONG_TYPE;
    public static final Type DOUBLE_TYPE;
    protected final int sort;
    private final char[] buf;
    private final int off;
    private final int len;
    
    private Type(final int sort, final char[] buf, final int off, final int len) {
        this.sort = sort;
        this.buf = buf;
        this.off = off;
        this.len = len;
    }
    
    public static Type getType(final String typeDescriptor) {
        return getType(typeDescriptor.toCharArray(), 0);
    }
    
    public static int getArgumentsAndReturnSizes(final String desc) {
        int n = 1;
        int c = 1;
        while (true) {
            final char car = desc.charAt(c++);
            if (car == ')') {
                break;
            }
            if (car == 'L') {
                while (desc.charAt(c++) != ';') {}
                ++n;
            }
            else if (car == 'D' || car == 'J') {
                n += 2;
            }
            else {
                ++n;
            }
        }
        final char car = desc.charAt(c);
        return n << 2 | ((car == 'V') ? 0 : ((car == 'D' || car == 'J') ? 2 : 1));
    }
    
    private static Type getType(final char[] buf, final int off) {
        switch (buf[off]) {
            case 'V': {
                return Type.VOID_TYPE;
            }
            case 'Z': {
                return Type.BOOLEAN_TYPE;
            }
            case 'C': {
                return Type.CHAR_TYPE;
            }
            case 'B': {
                return Type.BYTE_TYPE;
            }
            case 'S': {
                return Type.SHORT_TYPE;
            }
            case 'I': {
                return Type.INT_TYPE;
            }
            case 'F': {
                return Type.FLOAT_TYPE;
            }
            case 'J': {
                return Type.LONG_TYPE;
            }
            case 'D': {
                return Type.DOUBLE_TYPE;
            }
            case '[': {
                int len;
                for (len = 1; buf[off + len] == '['; ++len) {}
                if (buf[off + len] == 'L') {
                    ++len;
                    while (buf[off + len] != ';') {
                        ++len;
                    }
                }
                return new Type(9, buf, off, len + 1);
            }
            default: {
                int len;
                for (len = 1; buf[off + len] != ';'; ++len) {}
                return new Type(10, buf, off + 1, len - 1);
            }
        }
    }
    
    public String getInternalName() {
        return new String(this.buf, this.off, this.len);
    }
    
    String getDescriptor() {
        return new String(this.buf, this.off, this.len);
    }
    
    private int getDimensions() {
        int i;
        for (i = 1; this.buf[this.off + i] == '['; ++i) {}
        return i;
    }
    
    static Type[] getArgumentTypes(final String methodDescriptor) {
        final char[] buf = methodDescriptor.toCharArray();
        int off = 1;
        int size = 0;
        while (true) {
            final char car = buf[off++];
            if (car == ')') {
                break;
            }
            if (car == 'L') {
                while (buf[off++] != ';') {}
                ++size;
            }
            else {
                if (car == '[') {
                    continue;
                }
                ++size;
            }
        }
        Type[] args;
        for (args = new Type[size], off = 1, size = 0; buf[off] != ')'; off += args[size].len + ((args[size].sort == 10) ? 2 : 0), ++size) {
            args[size] = getType(buf, off);
        }
        return args;
    }
    
    protected String getClassName() {
        switch (this.sort) {
            case 0: {
                return "void";
            }
            case 1: {
                return "boolean";
            }
            case 2: {
                return "char";
            }
            case 3: {
                return "byte";
            }
            case 4: {
                return "short";
            }
            case 5: {
                return "int";
            }
            case 6: {
                return "float";
            }
            case 7: {
                return "long";
            }
            case 8: {
                return "double";
            }
            case 9: {
                final Type elementType = getType(this.buf, this.off + this.getDimensions());
                final StringBuilder b = new StringBuilder(elementType.getClassName());
                for (int i = this.getDimensions(); i > 0; --i) {
                    b.append("[]");
                }
                return b.toString();
            }
            default: {
                return new String(this.buf, this.off, this.len).replace('/', '.');
            }
        }
    }
    
    static {
        VOID_TYPE = new Type(0, null, 1443168256, 1);
        BOOLEAN_TYPE = new Type(1, null, 1509950721, 1);
        CHAR_TYPE = new Type(2, null, 1124075009, 1);
        BYTE_TYPE = new Type(3, null, 1107297537, 1);
        SHORT_TYPE = new Type(4, null, 1392510721, 1);
        INT_TYPE = new Type(5, null, 1224736769, 1);
        FLOAT_TYPE = new Type(6, null, 1174536705, 1);
        LONG_TYPE = new Type(7, null, 1241579778, 1);
        DOUBLE_TYPE = new Type(8, null, 1141048066, 1);
    }
}
