package com.alibaba.fastjson.parser.deserializer;

import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;

public abstract class ContextObjectDeserializer implements ObjectDeserializer
{
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        return this.deserialze(parser, type, fieldName, null, 0);
    }
    
    public abstract <T> T deserialze(final DefaultJSONParser p0, final Type p1, final Object p2, final String p3, final int p4);
}
