package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

public interface ObjectSerializer
{
    void write(final JSONSerializer p0, final Object p1, final Object p2, final Type p3, final int p4) throws IOException;
}
