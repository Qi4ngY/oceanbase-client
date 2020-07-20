package com.alibaba.fastjson.serializer;

public interface ValueFilter extends SerializeFilter
{
    Object process(final Object p0, final String p1, final Object p2);
}
