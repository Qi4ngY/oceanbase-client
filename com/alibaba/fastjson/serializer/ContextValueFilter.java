package com.alibaba.fastjson.serializer;

public interface ContextValueFilter extends SerializeFilter
{
    Object process(final BeanContext p0, final Object p1, final String p2, final Object p3);
}
