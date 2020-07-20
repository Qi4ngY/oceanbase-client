package com.alibaba.fastjson.serializer;

import java.io.IOException;
import com.alibaba.fastjson.JSONAware;
import java.lang.reflect.Type;

public class JSONAwareSerializer implements ObjectSerializer
{
    public static JSONAwareSerializer instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
            return;
        }
        final JSONAware aware = (JSONAware)object;
        out.write(aware.toJSONString());
    }
    
    static {
        JSONAwareSerializer.instance = new JSONAwareSerializer();
    }
}
