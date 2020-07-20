package com.alibaba.fastjson;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentMap;

public class TypeReference<T>
{
    static ConcurrentMap<Type, Type> classTypeCache;
    protected final Type type;
    public static final Type LIST_STRING;
    
    protected TypeReference() {
        final Type superClass = this.getClass().getGenericSuperclass();
        final Type type = ((ParameterizedType)superClass).getActualTypeArguments()[0];
        Type cachedType = TypeReference.classTypeCache.get(type);
        if (cachedType == null) {
            TypeReference.classTypeCache.putIfAbsent(type, type);
            cachedType = TypeReference.classTypeCache.get(type);
        }
        this.type = cachedType;
    }
    
    protected TypeReference(final Type... actualTypeArguments) {
        final Class<?> thisClass = this.getClass();
        final Type superClass = thisClass.getGenericSuperclass();
        final ParameterizedType argType = (ParameterizedType)((ParameterizedType)superClass).getActualTypeArguments()[0];
        final Type rawType = argType.getRawType();
        final Type[] argTypes = argType.getActualTypeArguments();
        int actualIndex = 0;
        for (int i = 0; i < argTypes.length; ++i) {
            if (argTypes[i] instanceof TypeVariable && actualIndex < actualTypeArguments.length) {
                argTypes[i] = actualTypeArguments[actualIndex++];
            }
            if (argTypes[i] instanceof GenericArrayType) {
                argTypes[i] = TypeUtils.checkPrimitiveArray((GenericArrayType)argTypes[i]);
            }
            if (argTypes[i] instanceof ParameterizedType) {
                argTypes[i] = this.handlerParameterizedType((ParameterizedType)argTypes[i], actualTypeArguments, actualIndex);
            }
        }
        final Type key = new ParameterizedTypeImpl(argTypes, thisClass, rawType);
        Type cachedType = TypeReference.classTypeCache.get(key);
        if (cachedType == null) {
            TypeReference.classTypeCache.putIfAbsent(key, key);
            cachedType = TypeReference.classTypeCache.get(key);
        }
        this.type = cachedType;
    }
    
    private Type handlerParameterizedType(final ParameterizedType type, final Type[] actualTypeArguments, int actualIndex) {
        final Class<?> thisClass = this.getClass();
        final Type rawType = type.getRawType();
        final Type[] argTypes = type.getActualTypeArguments();
        for (int i = 0; i < argTypes.length; ++i) {
            if (argTypes[i] instanceof TypeVariable && actualIndex < actualTypeArguments.length) {
                argTypes[i] = actualTypeArguments[actualIndex++];
            }
            if (argTypes[i] instanceof GenericArrayType) {
                argTypes[i] = TypeUtils.checkPrimitiveArray((GenericArrayType)argTypes[i]);
            }
            if (argTypes[i] instanceof ParameterizedType) {
                return this.handlerParameterizedType((ParameterizedType)argTypes[i], actualTypeArguments, actualIndex);
            }
        }
        final Type key = new ParameterizedTypeImpl(argTypes, thisClass, rawType);
        return key;
    }
    
    public Type getType() {
        return this.type;
    }
    
    static {
        TypeReference.classTypeCache = new ConcurrentHashMap<Type, Type>(16, 0.75f, 1);
        LIST_STRING = new TypeReference<List<String>>() {}.getType();
    }
}
