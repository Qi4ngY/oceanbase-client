package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.io.IOException;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class CharacterCodec implements ObjectSerializer, ObjectDeserializer
{
    public static final CharacterCodec instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        final Character value = (Character)object;
        if (value == null) {
            out.writeString("");
            return;
        }
        final char c = value;
        if (c == '\0') {
            out.writeString("\u0000");
        }
        else {
            out.writeString(value.toString());
        }
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type clazz, final Object fieldName) {
        final Object value = parser.parse();
        return (T)((value == null) ? null : TypeUtils.castToChar(value));
    }
    
    @Override
    public int getFastMatchToken() {
        return 4;
    }
    
    static {
        instance = new CharacterCodec();
    }
}
