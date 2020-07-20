package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Collection;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class CollectionCodec implements ObjectSerializer, ObjectDeserializer
{
    public static final CollectionCodec instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullListAsEmpty);
            return;
        }
        Type elementType = null;
        if (out.isEnabled(SerializerFeature.WriteClassName) || SerializerFeature.isEnabled(features, SerializerFeature.WriteClassName)) {
            elementType = TypeUtils.getCollectionItemType(fieldType);
        }
        final Collection<?> collection = (Collection<?>)object;
        final SerialContext context = serializer.context;
        serializer.setContext(context, object, fieldName, 0);
        if (out.isEnabled(SerializerFeature.WriteClassName)) {
            if (HashSet.class == collection.getClass()) {
                out.append("Set");
            }
            else if (TreeSet.class == collection.getClass()) {
                out.append("TreeSet");
            }
        }
        try {
            int i = 0;
            out.append('[');
            for (final Object item : collection) {
                if (i++ != 0) {
                    out.append(',');
                }
                if (item == null) {
                    out.writeNull();
                }
                else {
                    final Class<?> clazz = item.getClass();
                    if (clazz == Integer.class) {
                        out.writeInt((int)item);
                    }
                    else if (clazz == Long.class) {
                        out.writeLong((long)item);
                        if (!out.isEnabled(SerializerFeature.WriteClassName)) {
                            continue;
                        }
                        out.write(76);
                    }
                    else {
                        final ObjectSerializer itemSerializer = serializer.getObjectWriter(clazz);
                        if (SerializerFeature.isEnabled(features, SerializerFeature.WriteClassName) && itemSerializer instanceof JavaBeanSerializer) {
                            final JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer)itemSerializer;
                            javaBeanSerializer.writeNoneASM(serializer, item, i - 1, elementType, features);
                        }
                        else {
                            itemSerializer.write(serializer, item, i - 1, elementType, features);
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
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        if (parser.lexer.token() == 8) {
            parser.lexer.nextToken(16);
            return null;
        }
        if (type == JSONArray.class) {
            final JSONArray array = new JSONArray();
            parser.parseArray(array);
            return (T)array;
        }
        final Collection list = TypeUtils.createCollection(type);
        final Type itemType = TypeUtils.getCollectionItemType(type);
        parser.parseArray(itemType, list, fieldName);
        return (T)list;
    }
    
    @Override
    public int getFastMatchToken() {
        return 14;
    }
    
    static {
        instance = new CollectionCodec();
    }
}
