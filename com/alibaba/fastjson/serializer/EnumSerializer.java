package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

public class EnumSerializer implements ObjectSerializer
{
    public static final EnumSerializer instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        out.writeEnum((Enum<?>)object);
    }
    
    static {
        instance = new EnumSerializer();
    }
}
