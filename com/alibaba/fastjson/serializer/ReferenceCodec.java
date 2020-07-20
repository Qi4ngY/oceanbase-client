package com.alibaba.fastjson.serializer;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.io.IOException;
import java.lang.ref.Reference;
import java.util.concurrent.atomic.AtomicReference;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class ReferenceCodec implements ObjectSerializer, ObjectDeserializer
{
    public static final ReferenceCodec instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        Object item;
        if (object instanceof AtomicReference) {
            final AtomicReference val = (AtomicReference)object;
            item = val.get();
        }
        else {
            item = ((Reference)object).get();
        }
        serializer.write(item);
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        final ParameterizedType paramType = (ParameterizedType)type;
        final Type itemType = paramType.getActualTypeArguments()[0];
        final Object itemObject = parser.parseObject(itemType);
        final Type rawType = paramType.getRawType();
        if (rawType == AtomicReference.class) {
            return (T)new AtomicReference((V)itemObject);
        }
        if (rawType == WeakReference.class) {
            return (T)new WeakReference((T)itemObject);
        }
        if (rawType == SoftReference.class) {
            return (T)new SoftReference((T)itemObject);
        }
        throw new UnsupportedOperationException(rawType.toString());
    }
    
    @Override
    public int getFastMatchToken() {
        return 12;
    }
    
    static {
        instance = new ReferenceCodec();
    }
}
