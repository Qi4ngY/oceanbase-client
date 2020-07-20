package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

public class AppendableSerializer implements ObjectSerializer
{
    public static final AppendableSerializer instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        if (object == null) {
            final SerializeWriter out = serializer.out;
            out.writeNull(SerializerFeature.WriteNullStringAsEmpty);
            return;
        }
        serializer.write(object.toString());
    }
    
    static {
        instance = new AppendableSerializer();
    }
}
