package com.alibaba.fastjson.parser.deserializer;

public interface ExtraProcessor extends ParseProcess
{
    void processExtra(final Object p0, final String p1, final Object p2);
}
