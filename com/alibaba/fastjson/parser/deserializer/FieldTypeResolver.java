package com.alibaba.fastjson.parser.deserializer;

import java.lang.reflect.Type;

public interface FieldTypeResolver extends ParseProcess
{
    Type resolve(final Object p0, final String p1);
}
