package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.lang.reflect.Type;

public class AdderSerializer implements ObjectSerializer
{
    public static final AdderSerializer instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object instanceof LongAdder) {
            out.writeFieldValue('{', "value", ((LongAdder)object).longValue());
            out.write(125);
        }
        else if (object instanceof DoubleAdder) {
            out.writeFieldValue('{', "value", ((DoubleAdder)object).doubleValue());
            out.write(125);
        }
    }
    
    static {
        instance = new AdderSerializer();
    }
}
