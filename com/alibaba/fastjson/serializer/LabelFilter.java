package com.alibaba.fastjson.serializer;

public interface LabelFilter extends SerializeFilter
{
    boolean apply(final String p0);
}
