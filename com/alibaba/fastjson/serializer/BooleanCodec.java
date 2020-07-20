package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.parser.JSONLexer;
import java.util.concurrent.atomic.AtomicBoolean;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.io.IOException;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class BooleanCodec implements ObjectSerializer, ObjectDeserializer
{
    public static final BooleanCodec instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        final Boolean value = (Boolean)object;
        if (value == null) {
            out.writeNull(SerializerFeature.WriteNullBooleanAsFalse);
            return;
        }
        if (value) {
            out.write("true");
        }
        else {
            out.write("false");
        }
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type clazz, final Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        Boolean boolObj;
        try {
            if (lexer.token() == 6) {
                lexer.nextToken(16);
                boolObj = Boolean.TRUE;
            }
            else if (lexer.token() == 7) {
                lexer.nextToken(16);
                boolObj = Boolean.FALSE;
            }
            else if (lexer.token() == 2) {
                final int intValue = lexer.intValue();
                lexer.nextToken(16);
                if (intValue == 1) {
                    boolObj = Boolean.TRUE;
                }
                else {
                    boolObj = Boolean.FALSE;
                }
            }
            else {
                final Object value = parser.parse();
                if (value == null) {
                    return null;
                }
                boolObj = TypeUtils.castToBoolean(value);
            }
        }
        catch (Exception ex) {
            throw new JSONException("parseBoolean error, field : " + fieldName, ex);
        }
        if (clazz == AtomicBoolean.class) {
            return (T)new AtomicBoolean(boolObj);
        }
        return (T)boolObj;
    }
    
    @Override
    public int getFastMatchToken() {
        return 6;
    }
    
    static {
        instance = new BooleanCodec();
    }
}
