package com.alibaba.fastjson.parser.deserializer;

import java.lang.reflect.Type;

public interface PropertyProcessable extends ParseProcess
{
    Type getType(final String p0);
    
    void apply(final String p0, final Object p1);
}
