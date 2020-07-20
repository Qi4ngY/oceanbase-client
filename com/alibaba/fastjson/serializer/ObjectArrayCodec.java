package com.alibaba.fastjson.serializer;

import java.lang.reflect.Array;
import com.alibaba.fastjson.parser.JSONLexer;
import java.util.Collection;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.GenericArrayType;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.io.IOException;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class ObjectArrayCodec implements ObjectSerializer, ObjectDeserializer
{
    public static final ObjectArrayCodec instance;
    
    @Override
    public final void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        final Object[] array = (Object[])object;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullListAsEmpty);
            return;
        }
        final int size = array.length;
        final int end = size - 1;
        if (end == -1) {
            out.append("[]");
            return;
        }
        final SerialContext context = serializer.context;
        serializer.setContext(context, object, fieldName, 0);
        try {
            Class<?> preClazz = null;
            ObjectSerializer preWriter = null;
            out.append('[');
            if (out.isEnabled(SerializerFeature.PrettyFormat)) {
                serializer.incrementIndent();
                serializer.println();
                for (int i = 0; i < size; ++i) {
                    if (i != 0) {
                        out.write(44);
                        serializer.println();
                    }
                    serializer.write(array[i]);
                }
                serializer.decrementIdent();
                serializer.println();
                out.write(93);
                return;
            }
            for (int i = 0; i < end; ++i) {
                final Object item = array[i];
                if (item == null) {
                    out.append("null,");
                }
                else {
                    if (serializer.containsReference(item)) {
                        serializer.writeReference(item);
                    }
                    else {
                        final Class<?> clazz = item.getClass();
                        if (clazz == preClazz) {
                            preWriter.write(serializer, item, i, null, 0);
                        }
                        else {
                            preClazz = clazz;
                            preWriter = serializer.getObjectWriter(clazz);
                            preWriter.write(serializer, item, i, null, 0);
                        }
                    }
                    out.append(',');
                }
            }
            final Object item2 = array[end];
            if (item2 == null) {
                out.append("null]");
            }
            else {
                if (serializer.containsReference(item2)) {
                    serializer.writeReference(item2);
                }
                else {
                    serializer.writeWithFieldName(item2, end);
                }
                out.append(']');
            }
        }
        finally {
            serializer.context = context;
        }
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        final int token = lexer.token();
        if (token == 8) {
            lexer.nextToken(16);
            return null;
        }
        if (token != 4 && token != 26) {
            Type componentType;
            Class componentClass;
            if (type instanceof GenericArrayType) {
                final GenericArrayType clazz = (GenericArrayType)type;
                componentType = clazz.getGenericComponentType();
                if (componentType instanceof TypeVariable) {
                    final TypeVariable typeVar = (TypeVariable)componentType;
                    final Type objType = parser.getContext().type;
                    if (objType instanceof ParameterizedType) {
                        final ParameterizedType objParamType = (ParameterizedType)objType;
                        final Type objRawType = objParamType.getRawType();
                        Type actualType = null;
                        if (objRawType instanceof Class) {
                            final TypeVariable[] objTypeParams = ((Class)objRawType).getTypeParameters();
                            for (int i = 0; i < objTypeParams.length; ++i) {
                                if (objTypeParams[i].getName().equals(typeVar.getName())) {
                                    actualType = objParamType.getActualTypeArguments()[i];
                                }
                            }
                        }
                        if (actualType instanceof Class) {
                            componentClass = (Class)actualType;
                        }
                        else {
                            componentClass = Object.class;
                        }
                    }
                    else {
                        componentClass = TypeUtils.getClass(typeVar.getBounds()[0]);
                    }
                }
                else {
                    componentClass = TypeUtils.getClass(componentType);
                }
            }
            else {
                final Class clazz2 = (Class)type;
                componentClass = (Class)(componentType = clazz2.getComponentType());
            }
            final JSONArray array = new JSONArray();
            parser.parseArray(componentType, array, fieldName);
            return this.toObjectArray(parser, componentClass, array);
        }
        final byte[] bytes = lexer.bytesValue();
        lexer.nextToken(16);
        if (bytes.length == 0 && type != byte[].class) {
            return null;
        }
        return (T)(Object)bytes;
    }
    
    private <T> T toObjectArray(final DefaultJSONParser parser, final Class<?> componentType, final JSONArray array) {
        if (array == null) {
            return null;
        }
        final int size = array.size();
        final Object objArray = Array.newInstance(componentType, size);
        for (int i = 0; i < size; ++i) {
            final Object value = array.get(i);
            if (value == array) {
                Array.set(objArray, i, objArray);
            }
            else if (componentType.isArray()) {
                Object element;
                if (componentType.isInstance(value)) {
                    element = value;
                }
                else {
                    element = this.toObjectArray(parser, componentType, (JSONArray)value);
                }
                Array.set(objArray, i, element);
            }
            else {
                Object element = null;
                if (value instanceof JSONArray) {
                    boolean contains = false;
                    final JSONArray valueArray = (JSONArray)value;
                    for (int valueArraySize = valueArray.size(), y = 0; y < valueArraySize; ++y) {
                        final Object valueItem = valueArray.get(y);
                        if (valueItem == array) {
                            valueArray.set(i, objArray);
                            contains = true;
                        }
                    }
                    if (contains) {
                        element = valueArray.toArray();
                    }
                }
                if (element == null) {
                    element = TypeUtils.cast(value, componentType, parser.getConfig());
                }
                Array.set(objArray, i, element);
            }
        }
        array.setRelatedArray(objArray);
        array.setComponentType(componentType);
        return (T)objArray;
    }
    
    @Override
    public int getFastMatchToken() {
        return 14;
    }
    
    static {
        instance = new ObjectArrayCodec();
    }
}
