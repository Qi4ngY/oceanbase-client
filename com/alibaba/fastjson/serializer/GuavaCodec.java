package com.alibaba.fastjson.serializer;

import java.util.Iterator;
import com.alibaba.fastjson.JSONObject;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import com.google.common.collect.ArrayListMultimap;
import java.lang.reflect.ParameterizedType;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.io.IOException;
import com.google.common.collect.Multimap;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class GuavaCodec implements ObjectSerializer, ObjectDeserializer
{
    public static GuavaCodec instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object instanceof Multimap) {
            final Multimap multimap = (Multimap)object;
            serializer.write(multimap.asMap());
        }
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        Type rawType = type;
        if (type instanceof ParameterizedType) {
            rawType = ((ParameterizedType)type).getRawType();
        }
        if (rawType == ArrayListMultimap.class) {
            final ArrayListMultimap multimap = ArrayListMultimap.create();
            final JSONObject object = parser.parseObject();
            for (final Map.Entry entry : object.entrySet()) {
                final Object value = entry.getValue();
                if (value instanceof Collection) {
                    multimap.putAll(entry.getKey(), (Iterable)value);
                }
                else {
                    multimap.put(entry.getKey(), value);
                }
            }
            return (T)multimap;
        }
        return null;
    }
    
    @Override
    public int getFastMatchToken() {
        return 0;
    }
    
    static {
        GuavaCodec.instance = new GuavaCodec();
    }
}
