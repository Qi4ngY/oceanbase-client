package com.alibaba.fastjson.support.springfox;

import java.io.IOException;
import com.alibaba.fastjson.serializer.SerializeWriter;
import springfox.documentation.spring.web.json.Json;
import java.lang.reflect.Type;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

public class SwaggerJsonSerializer implements ObjectSerializer
{
    public static final SwaggerJsonSerializer instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.getWriter();
        final Json json = (Json)object;
        final String value = json.value();
        out.write(value);
    }
    
    static {
        instance = new SwaggerJsonSerializer();
    }
}
