package com.alibaba.fastjson.serializer;

import java.io.IOException;

public interface ContextObjectSerializer extends ObjectSerializer
{
    void write(final JSONSerializer p0, final Object p1, final BeanContext p2) throws IOException;
}
