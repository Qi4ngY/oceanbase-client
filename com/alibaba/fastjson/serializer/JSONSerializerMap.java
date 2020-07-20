package com.alibaba.fastjson.serializer;

import java.lang.reflect.Type;

@Deprecated
public class JSONSerializerMap extends SerializeConfig
{
    public final boolean put(final Class<?> clazz, final ObjectSerializer serializer) {
        return super.put(clazz, serializer);
    }
}
