package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Type;

public final class ListSerializer implements ObjectSerializer
{
    public static final ListSerializer instance;
    
    @Override
    public final void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final boolean writeClassName = serializer.out.isEnabled(SerializerFeature.WriteClassName) || SerializerFeature.isEnabled(features, SerializerFeature.WriteClassName);
        final SerializeWriter out = serializer.out;
        Type elementType = null;
        if (writeClassName) {
            elementType = TypeUtils.getCollectionItemType(fieldType);
        }
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullListAsEmpty);
            return;
        }
        final List<?> list = (List<?>)object;
        if (list.size() == 0) {
            out.append("[]");
            return;
        }
        final SerialContext context = serializer.context;
        serializer.setContext(context, object, fieldName, 0);
        ObjectSerializer itemSerializer = null;
        try {
            if (out.isEnabled(SerializerFeature.PrettyFormat)) {
                out.append('[');
                serializer.incrementIndent();
                int i = 0;
                for (final Object item : list) {
                    if (i != 0) {
                        out.append(',');
                    }
                    serializer.println();
                    if (item != null) {
                        if (serializer.containsReference(item)) {
                            serializer.writeReference(item);
                        }
                        else {
                            itemSerializer = serializer.getObjectWriter(item.getClass());
                            final SerialContext itemContext = new SerialContext(context, object, fieldName, 0, 0);
                            serializer.context = itemContext;
                            itemSerializer.write(serializer, item, i, elementType, features);
                        }
                    }
                    else {
                        serializer.out.writeNull();
                    }
                    ++i;
                }
                serializer.decrementIdent();
                serializer.println();
                out.append(']');
                return;
            }
            out.append('[');
            int i = 0;
            for (int size = list.size(); i < size; ++i) {
                final Object item = list.get(i);
                if (i != 0) {
                    out.append(',');
                }
                if (item == null) {
                    out.append("null");
                }
                else {
                    final Class<?> clazz = item.getClass();
                    if (clazz == Integer.class) {
                        out.writeInt((int)item);
                    }
                    else if (clazz == Long.class) {
                        final long val = (long)item;
                        if (writeClassName) {
                            out.writeLong(val);
                            out.write(76);
                        }
                        else {
                            out.writeLong(val);
                        }
                    }
                    else if ((SerializerFeature.DisableCircularReferenceDetect.mask & features) != 0x0) {
                        itemSerializer = serializer.getObjectWriter(item.getClass());
                        itemSerializer.write(serializer, item, i, elementType, features);
                    }
                    else {
                        if (!out.disableCircularReferenceDetect) {
                            final SerialContext itemContext2 = new SerialContext(context, object, fieldName, 0, 0);
                            serializer.context = itemContext2;
                        }
                        if (serializer.containsReference(item)) {
                            serializer.writeReference(item);
                        }
                        else {
                            itemSerializer = serializer.getObjectWriter(item.getClass());
                            if ((SerializerFeature.WriteClassName.mask & features) != 0x0 && itemSerializer instanceof JavaBeanSerializer) {
                                final JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer)itemSerializer;
                                javaBeanSerializer.writeNoneASM(serializer, item, i, elementType, features);
                            }
                            else {
                                itemSerializer.write(serializer, item, i, elementType, features);
                            }
                        }
                    }
                }
            }
            out.append(']');
        }
        finally {
            serializer.context = context;
        }
    }
    
    static {
        instance = new ListSerializer();
    }
}
