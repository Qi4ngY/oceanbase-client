package com.alibaba.fastjson.parser.deserializer;

import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;

public interface ObjectDeserializer
{
     <T> T deserialze(final DefaultJSONParser p0, final Type p1, final Object p2);
    
    int getFastMatchToken();
}
