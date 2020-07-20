package com.alibaba.fastjson.serializer;

public interface NameFilter extends SerializeFilter
{
    String process(final Object p0, final String p1, final Object p2);
}
