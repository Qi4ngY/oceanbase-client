package com.alibaba.fastjson.serializer;

public interface PropertyPreFilter extends SerializeFilter
{
    boolean apply(final JSONSerializer p0, final Object p1, final String p2);
}
