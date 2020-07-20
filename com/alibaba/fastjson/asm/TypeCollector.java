package com.alibaba.fastjson.asm;

import java.util.HashMap;
import com.alibaba.fastjson.util.ASMUtils;
import com.alibaba.fastjson.annotation.JSONType;
import java.lang.reflect.Modifier;
import java.util.Map;

public class TypeCollector
{
    private static String JSONType;
    private static final Map<String, String> primitives;
    private final String methodName;
    private final Class<?>[] parameterTypes;
    protected MethodCollector collector;
    protected boolean jsonType;
    
    public TypeCollector(final String methodName, final Class<?>[] parameterTypes) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.collector = null;
    }
    
    protected MethodCollector visitMethod(final int access, final String name, final String desc) {
        if (this.collector != null) {
            return null;
        }
        if (!name.equals(this.methodName)) {
            return null;
        }
        final Type[] argTypes = Type.getArgumentTypes(desc);
        int longOrDoubleQuantity = 0;
        for (final Type t : argTypes) {
            final String className = t.getClassName();
            if (className.equals("long") || className.equals("double")) {
                ++longOrDoubleQuantity;
            }
        }
        if (argTypes.length != this.parameterTypes.length) {
            return null;
        }
        for (int i = 0; i < argTypes.length; ++i) {
            if (!this.correctTypeName(argTypes[i], this.parameterTypes[i].getName())) {
                return null;
            }
        }
        return this.collector = new MethodCollector(Modifier.isStatic(access) ? 0 : 1, argTypes.length + longOrDoubleQuantity);
    }
    
    public void visitAnnotation(final String desc) {
        if (TypeCollector.JSONType.equals(desc)) {
            this.jsonType = true;
        }
    }
    
    private boolean correctTypeName(final Type type, final String paramTypeName) {
        String s = type.getClassName();
        final StringBuilder braces = new StringBuilder();
        while (s.endsWith("[]")) {
            braces.append('[');
            s = s.substring(0, s.length() - 2);
        }
        if (braces.length() != 0) {
            if (TypeCollector.primitives.containsKey(s)) {
                s = braces.append(TypeCollector.primitives.get(s)).toString();
            }
            else {
                s = braces.append('L').append(s).append(';').toString();
            }
        }
        return s.equals(paramTypeName);
    }
    
    public String[] getParameterNamesForMethod() {
        if (this.collector == null || !this.collector.debugInfoPresent) {
            return new String[0];
        }
        return this.collector.getResult().split(",");
    }
    
    public boolean matched() {
        return this.collector != null;
    }
    
    public boolean hasJsonType() {
        return this.jsonType;
    }
    
    static {
        TypeCollector.JSONType = ASMUtils.desc(JSONType.class);
        primitives = new HashMap<String, String>() {
            {
                this.put("int", "I");
                this.put("boolean", "Z");
                this.put("byte", "B");
                this.put("char", "C");
                this.put("short", "S");
                this.put("float", "F");
                this.put("long", "J");
                this.put("double", "D");
            }
        };
    }
}
