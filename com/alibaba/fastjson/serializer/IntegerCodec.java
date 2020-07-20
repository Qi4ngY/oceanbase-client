package com.alibaba.fastjson.serializer;

import java.math.BigDecimal;
import com.alibaba.fastjson.parser.JSONLexer;
import java.util.concurrent.atomic.AtomicInteger;
import com.alibaba.fastjson.JSONException;
import java.util.Map;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.io.IOException;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class IntegerCodec implements ObjectSerializer, ObjectDeserializer
{
    public static IntegerCodec instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        final Number value = (Number)object;
        if (value == null) {
            out.writeNull(SerializerFeature.WriteNullNumberAsZero);
            return;
        }
        if (object instanceof Long) {
            out.writeLong(value.longValue());
        }
        else {
            out.writeInt(value.intValue());
        }
        if (out.isEnabled(SerializerFeature.WriteClassName)) {
            final Class<?> clazz = value.getClass();
            if (clazz == Byte.class) {
                out.write(66);
            }
            else if (clazz == Short.class) {
                out.write(83);
            }
        }
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type clazz, final Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        final int token = lexer.token();
        if (token == 8) {
            lexer.nextToken(16);
            return null;
        }
        Integer intObj;
        try {
            if (token == 2) {
                final int val = lexer.intValue();
                lexer.nextToken(16);
                intObj = val;
            }
            else if (token == 3) {
                final BigDecimal number = lexer.decimalValue();
                intObj = TypeUtils.intValue(number);
                lexer.nextToken(16);
            }
            else if (token == 12) {
                final JSONObject jsonObject = new JSONObject(true);
                parser.parseObject(jsonObject);
                intObj = TypeUtils.castToInt(jsonObject);
            }
            else {
                final Object value = parser.parse();
                intObj = TypeUtils.castToInt(value);
            }
        }
        catch (Exception ex) {
            String message = "parseInt error";
            if (fieldName != null) {
                message = message + ", field : " + fieldName;
            }
            throw new JSONException(message, ex);
        }
        if (clazz == AtomicInteger.class) {
            return (T)new AtomicInteger(intObj);
        }
        return (T)intObj;
    }
    
    @Override
    public int getFastMatchToken() {
        return 2;
    }
    
    static {
        IntegerCodec.instance = new IntegerCodec();
    }
}
