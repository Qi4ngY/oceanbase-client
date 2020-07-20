package com.alibaba.fastjson.serializer;

import java.math.BigDecimal;
import com.alibaba.fastjson.parser.JSONLexer;
import java.util.concurrent.atomic.AtomicLong;
import com.alibaba.fastjson.JSONException;
import java.util.Map;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.io.IOException;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class LongCodec implements ObjectSerializer, ObjectDeserializer
{
    public static LongCodec instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullNumberAsZero);
        }
        else {
            final long value = (long)object;
            out.writeLong(value);
            if (out.isEnabled(SerializerFeature.WriteClassName) && value <= 2147483647L && value >= -2147483648L && fieldType != Long.class && fieldType != Long.TYPE) {
                out.write(76);
            }
        }
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type clazz, final Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        Long longObject;
        try {
            final int token = lexer.token();
            if (token == 2) {
                final long longValue = lexer.longValue();
                lexer.nextToken(16);
                longObject = longValue;
            }
            else if (token == 3) {
                final BigDecimal number = lexer.decimalValue();
                longObject = TypeUtils.longValue(number);
                lexer.nextToken(16);
            }
            else {
                if (token == 12) {
                    final JSONObject jsonObject = new JSONObject(true);
                    parser.parseObject(jsonObject);
                    longObject = TypeUtils.castToLong(jsonObject);
                }
                else {
                    final Object value = parser.parse();
                    longObject = TypeUtils.castToLong(value);
                }
                if (longObject == null) {
                    return null;
                }
            }
        }
        catch (Exception ex) {
            throw new JSONException("parseLong error, field : " + fieldName, ex);
        }
        return (T)((clazz == AtomicLong.class) ? new AtomicLong(longObject) : longObject);
    }
    
    @Override
    public int getFastMatchToken() {
        return 2;
    }
    
    static {
        LongCodec.instance = new LongCodec();
    }
}
