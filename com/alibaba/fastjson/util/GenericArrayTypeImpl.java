package com.alibaba.fastjson.util;

import java.lang.reflect.Type;
import java.lang.reflect.GenericArrayType;

public class GenericArrayTypeImpl implements GenericArrayType
{
    private final Type genericComponentType;
    
    public GenericArrayTypeImpl(final Type genericComponentType) {
        assert genericComponentType != null;
        this.genericComponentType = genericComponentType;
    }
    
    @Override
    public Type getGenericComponentType() {
        return this.genericComponentType;
    }
    
    @Override
    public String toString() {
        final Type genericComponentType = this.getGenericComponentType();
        final StringBuilder builder = new StringBuilder();
        if (genericComponentType instanceof Class) {
            builder.append(((Class)genericComponentType).getName());
        }
        else {
            builder.append(genericComponentType.toString());
        }
        builder.append("[]");
        return builder.toString();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof GenericArrayType) {
            final GenericArrayType that = (GenericArrayType)obj;
            return this.genericComponentType.equals(that.getGenericComponentType());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.genericComponentType.hashCode();
    }
}
