package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

public class JSONObjectCodec implements ObjectSerializer
{
    public static final JSONObjectCodec instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        final MapSerializer mapSerializer = MapSerializer.instance;
        try {
            final Field mapField = object.getClass().getDeclaredField("map");
            if (Modifier.isPrivate(mapField.getModifiers())) {
                mapField.setAccessible(true);
            }
            final Object map = mapField.get(object);
            mapSerializer.write(serializer, map, fieldName, fieldType, features);
        }
        catch (Exception e) {
            out.writeNull();
        }
    }
    
    static {
        instance = new JSONObjectCodec();
    }
}
