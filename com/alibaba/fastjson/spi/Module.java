package com.alibaba.fastjson.spi;

import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.parser.ParserConfig;

public interface Module
{
    ObjectDeserializer createDeserializer(final ParserConfig p0, final Class p1);
    
    ObjectSerializer createSerializer(final SerializeConfig p0, final Class p1);
}
