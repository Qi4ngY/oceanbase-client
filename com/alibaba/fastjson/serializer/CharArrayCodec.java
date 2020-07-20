package com.alibaba.fastjson.serializer;

import java.util.Iterator;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSON;
import java.util.Collection;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class CharArrayCodec implements ObjectDeserializer
{
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type clazz, final Object fieldName) {
        return deserialze(parser);
    }
    
    public static <T> T deserialze(final DefaultJSONParser parser) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() == 4) {
            final String val = lexer.stringVal();
            lexer.nextToken(16);
            return (T)(Object)val.toCharArray();
        }
        if (lexer.token() == 2) {
            final Number val2 = lexer.integerValue();
            lexer.nextToken(16);
            return (T)(Object)val2.toString().toCharArray();
        }
        final Object value = parser.parse();
        if (value instanceof String) {
            return (T)(Object)((String)value).toCharArray();
        }
        if (!(value instanceof Collection)) {
            return (T)((value == null) ? null : JSON.toJSONString(value).toCharArray());
        }
        final Collection<?> collection = (Collection<?>)value;
        boolean accept = true;
        for (final Object item : collection) {
            if (item instanceof String) {
                final int itemLength = ((String)item).length();
                if (itemLength != 1) {
                    accept = false;
                    break;
                }
                continue;
            }
        }
        if (!accept) {
            throw new JSONException("can not cast to char[]");
        }
        final char[] chars = new char[collection.size()];
        int pos = 0;
        for (final Object item2 : collection) {
            chars[pos++] = ((String)item2).charAt(0);
        }
        return (T)(Object)chars;
    }
    
    @Override
    public int getFastMatchToken() {
        return 4;
    }
}
