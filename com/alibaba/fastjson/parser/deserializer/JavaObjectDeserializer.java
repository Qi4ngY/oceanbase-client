package com.alibaba.fastjson.parser.deserializer;

import java.util.List;
import java.io.Closeable;
import java.io.Serializable;
import java.lang.reflect.Array;
import com.alibaba.fastjson.util.TypeUtils;
import java.util.Collection;
import java.util.ArrayList;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;

public class JavaObjectDeserializer implements ObjectDeserializer
{
    public static final JavaObjectDeserializer instance;
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType)type).getGenericComponentType();
            if (componentType instanceof TypeVariable) {
                final TypeVariable<?> componentVar = (TypeVariable<?>)componentType;
                componentType = componentVar.getBounds()[0];
            }
            final List<Object> list = new ArrayList<Object>();
            parser.parseArray(componentType, list);
            final Class<?> componentClass = TypeUtils.getRawClass(componentType);
            final Object[] array = (Object[])Array.newInstance(componentClass, list.size());
            list.toArray(array);
            return (T)array;
        }
        if (type instanceof Class && type != Object.class && type != Serializable.class && type != Cloneable.class && type != Closeable.class && type != Comparable.class) {
            return parser.parseObject(type);
        }
        return (T)parser.parse(fieldName);
    }
    
    @Override
    public int getFastMatchToken() {
        return 12;
    }
    
    static {
        instance = new JavaObjectDeserializer();
    }
}
