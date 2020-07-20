package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

public interface JSONSerializable
{
    void write(final JSONSerializer p0, final Object p1, final Type p2, final int p3) throws IOException;
}
