package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

public class ArraySerializer implements ObjectSerializer
{
    private final Class<?> componentType;
    private final ObjectSerializer compObjectSerializer;
    
    public ArraySerializer(final Class<?> componentType, final ObjectSerializer compObjectSerializer) {
        this.componentType = componentType;
        this.compObjectSerializer = compObjectSerializer;
    }
    
    @Override
    public final void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullListAsEmpty);
            return;
        }
        final Object[] array = (Object[])object;
        final int size = array.length;
        final SerialContext context = serializer.context;
        serializer.setContext(context, object, fieldName, 0);
        try {
            out.append('[');
            for (int i = 0; i < size; ++i) {
                if (i != 0) {
                    out.append(',');
                }
                final Object item = array[i];
                if (item == null) {
                    if (out.isEnabled(SerializerFeature.WriteNullStringAsEmpty) && object instanceof String[]) {
                        out.writeString("");
                    }
                    else {
                        out.append("null");
                    }
                }
                else if (item.getClass() == this.componentType) {
                    this.compObjectSerializer.write(serializer, item, i, null, 0);
                }
                else {
                    final ObjectSerializer itemSerializer = serializer.getObjectWriter(item.getClass());
                    itemSerializer.write(serializer, item, i, null, 0);
                }
            }
            out.append(']');
        }
        finally {
            serializer.context = context;
        }
    }
}
