package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.io.IOException;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class StringCodec implements ObjectSerializer, ObjectDeserializer
{
    public static StringCodec instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        this.write(serializer, (String)object);
    }
    
    public void write(final JSONSerializer serializer, final String value) {
        final SerializeWriter out = serializer.out;
        if (value == null) {
            out.writeNull(SerializerFeature.WriteNullStringAsEmpty);
            return;
        }
        out.writeString(value);
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type clazz, final Object fieldName) {
        if (clazz == StringBuffer.class) {
            final JSONLexer lexer = parser.lexer;
            if (lexer.token() == 4) {
                final String val = lexer.stringVal();
                lexer.nextToken(16);
                return (T)new StringBuffer(val);
            }
            final Object value = parser.parse();
            if (value == null) {
                return null;
            }
            return (T)new StringBuffer(value.toString());
        }
        else {
            if (clazz != StringBuilder.class) {
                return deserialze(parser);
            }
            final JSONLexer lexer = parser.lexer;
            if (lexer.token() == 4) {
                final String val = lexer.stringVal();
                lexer.nextToken(16);
                return (T)new StringBuilder(val);
            }
            final Object value = parser.parse();
            if (value == null) {
                return null;
            }
            return (T)new StringBuilder(value.toString());
        }
    }
    
    public static <T> T deserialze(final DefaultJSONParser parser) {
        final JSONLexer lexer = parser.getLexer();
        if (lexer.token() == 4) {
            final String val = lexer.stringVal();
            lexer.nextToken(16);
            return (T)val;
        }
        if (lexer.token() == 2) {
            final String val = lexer.numberString();
            lexer.nextToken(16);
            return (T)val;
        }
        final Object value = parser.parse();
        if (value == null) {
            return null;
        }
        return (T)value.toString();
    }
    
    @Override
    public int getFastMatchToken() {
        return 4;
    }
    
    static {
        StringCodec.instance = new StringCodec();
    }
}
