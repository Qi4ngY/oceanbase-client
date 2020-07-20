package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.util.Enumeration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class EnumerationSerializer implements ObjectSerializer
{
    public static EnumerationSerializer instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullListAsEmpty);
            return;
        }
        Type elementType = null;
        if (out.isEnabled(SerializerFeature.WriteClassName) && fieldType instanceof ParameterizedType) {
            final ParameterizedType param = (ParameterizedType)fieldType;
            elementType = param.getActualTypeArguments()[0];
        }
        final Enumeration<?> e = (Enumeration<?>)object;
        final SerialContext context = serializer.context;
        serializer.setContext(context, object, fieldName, 0);
        try {
            int i = 0;
            out.append('[');
            while (e.hasMoreElements()) {
                final Object item = e.nextElement();
                if (i++ != 0) {
                    out.append(',');
                }
                if (item == null) {
                    out.writeNull();
                }
                else {
                    final ObjectSerializer itemSerializer = serializer.getObjectWriter(item.getClass());
                    itemSerializer.write(serializer, item, i - 1, elementType, 0);
                }
            }
            out.append(']');
        }
        finally {
            serializer.context = context;
        }
    }
    
    static {
        EnumerationSerializer.instance = new EnumerationSerializer();
    }
}
