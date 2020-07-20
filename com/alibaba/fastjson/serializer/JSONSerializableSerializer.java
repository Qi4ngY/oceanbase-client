package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

public class JSONSerializableSerializer implements ObjectSerializer
{
    public static JSONSerializableSerializer instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final JSONSerializable jsonSerializable = (JSONSerializable)object;
        if (jsonSerializable == null) {
            serializer.writeNull();
            return;
        }
        jsonSerializable.write(serializer, fieldName, fieldType, features);
    }
    
    static {
        JSONSerializableSerializer.instance = new JSONSerializableSerializer();
    }
}
