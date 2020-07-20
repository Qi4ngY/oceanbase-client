package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

public class ToStringSerializer implements ObjectSerializer
{
    public static final ToStringSerializer instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
            return;
        }
        final String strVal = object.toString();
        out.writeString(strVal);
    }
    
    static {
        instance = new ToStringSerializer();
    }
}
