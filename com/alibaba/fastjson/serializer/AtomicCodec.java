package com.alibaba.fastjson.serializer;

import java.util.Collection;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class AtomicCodec implements ObjectSerializer, ObjectDeserializer
{
    public static final AtomicCodec instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object instanceof AtomicInteger) {
            final AtomicInteger val = (AtomicInteger)object;
            out.writeInt(val.get());
            return;
        }
        if (object instanceof AtomicLong) {
            final AtomicLong val2 = (AtomicLong)object;
            out.writeLong(val2.get());
            return;
        }
        if (object instanceof AtomicBoolean) {
            final AtomicBoolean val3 = (AtomicBoolean)object;
            out.append(val3.get() ? "true" : "false");
            return;
        }
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullListAsEmpty);
            return;
        }
        if (object instanceof AtomicIntegerArray) {
            final AtomicIntegerArray array = (AtomicIntegerArray)object;
            final int len = array.length();
            out.write(91);
            for (int i = 0; i < len; ++i) {
                final int val4 = array.get(i);
                if (i != 0) {
                    out.write(44);
                }
                out.writeInt(val4);
            }
            out.write(93);
            return;
        }
        final AtomicLongArray array2 = (AtomicLongArray)object;
        final int len = array2.length();
        out.write(91);
        for (int i = 0; i < len; ++i) {
            final long val5 = array2.get(i);
            if (i != 0) {
                out.write(44);
            }
            out.writeLong(val5);
        }
        out.write(93);
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type clazz, final Object fieldName) {
        if (parser.lexer.token() == 8) {
            parser.lexer.nextToken(16);
            return null;
        }
        final JSONArray array = new JSONArray();
        parser.parseArray(array);
        if (clazz == AtomicIntegerArray.class) {
            final AtomicIntegerArray atomicArray = new AtomicIntegerArray(array.size());
            for (int i = 0; i < array.size(); ++i) {
                atomicArray.set(i, array.getInteger(i));
            }
            return (T)atomicArray;
        }
        final AtomicLongArray atomicArray2 = new AtomicLongArray(array.size());
        for (int i = 0; i < array.size(); ++i) {
            atomicArray2.set(i, array.getLong(i));
        }
        return (T)atomicArray2;
    }
    
    @Override
    public int getFastMatchToken() {
        return 14;
    }
    
    static {
        instance = new AtomicCodec();
    }
}
