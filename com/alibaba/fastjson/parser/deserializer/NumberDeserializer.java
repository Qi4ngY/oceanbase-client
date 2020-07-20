package com.alibaba.fastjson.parser.deserializer;

import java.math.BigDecimal;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.JSONException;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;

public class NumberDeserializer implements ObjectDeserializer
{
    public static final NumberDeserializer instance;
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type clazz, final Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() == 2) {
            if (clazz == Double.TYPE || clazz == Double.class) {
                final String val = lexer.numberString();
                lexer.nextToken(16);
                return (T)Double.valueOf(Double.parseDouble(val));
            }
            final long val2 = lexer.longValue();
            lexer.nextToken(16);
            if (clazz == Short.TYPE || clazz == Short.class) {
                if (val2 > 32767L || val2 < -32768L) {
                    throw new JSONException("short overflow : " + val2);
                }
                return (T)Short.valueOf((short)val2);
            }
            else if (clazz == Byte.TYPE || clazz == Byte.class) {
                if (val2 > 127L || val2 < -128L) {
                    throw new JSONException("short overflow : " + val2);
                }
                return (T)Byte.valueOf((byte)val2);
            }
            else {
                if (val2 >= -2147483648L && val2 <= 2147483647L) {
                    return (T)Integer.valueOf((int)val2);
                }
                return (T)Long.valueOf(val2);
            }
        }
        else if (lexer.token() == 3) {
            if (clazz == Double.TYPE || clazz == Double.class) {
                final String val = lexer.numberString();
                lexer.nextToken(16);
                return (T)Double.valueOf(Double.parseDouble(val));
            }
            if (clazz == Short.TYPE || clazz == Short.class) {
                final BigDecimal val3 = lexer.decimalValue();
                lexer.nextToken(16);
                final short shortValue = TypeUtils.shortValue(val3);
                return (T)Short.valueOf(shortValue);
            }
            if (clazz == Byte.TYPE || clazz == Byte.class) {
                final BigDecimal val3 = lexer.decimalValue();
                lexer.nextToken(16);
                final byte byteValue = TypeUtils.byteValue(val3);
                return (T)Byte.valueOf(byteValue);
            }
            final BigDecimal val3 = lexer.decimalValue();
            lexer.nextToken(16);
            return (T)val3;
        }
        else {
            if (lexer.token() == 18 && "NaN".equals(lexer.stringVal())) {
                lexer.nextToken();
                Object nan = null;
                if (clazz == Double.class) {
                    nan = Double.NaN;
                }
                else if (clazz == Float.class) {
                    nan = Float.NaN;
                }
                return (T)nan;
            }
            final Object value = parser.parse();
            if (value == null) {
                return null;
            }
            Label_0533: {
                if (clazz != Double.TYPE) {
                    if (clazz != Double.class) {
                        break Label_0533;
                    }
                }
                try {
                    return (T)TypeUtils.castToDouble(value);
                }
                catch (Exception ex) {
                    throw new JSONException("parseDouble error, field : " + fieldName, ex);
                }
            }
            Label_0583: {
                if (clazz != Short.TYPE) {
                    if (clazz != Short.class) {
                        break Label_0583;
                    }
                }
                try {
                    return (T)TypeUtils.castToShort(value);
                }
                catch (Exception ex) {
                    throw new JSONException("parseShort error, field : " + fieldName, ex);
                }
            }
            if (clazz != Byte.TYPE) {
                if (clazz != Byte.class) {
                    return (T)TypeUtils.castToBigDecimal(value);
                }
            }
            try {
                return (T)TypeUtils.castToByte(value);
            }
            catch (Exception ex) {
                throw new JSONException("parseByte error, field : " + fieldName, ex);
            }
            return (T)TypeUtils.castToBigDecimal(value);
        }
    }
    
    @Override
    public int getFastMatchToken() {
        return 2;
    }
    
    static {
        instance = new NumberDeserializer();
    }
}
