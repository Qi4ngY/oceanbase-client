package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONScanner;
import com.alibaba.fastjson.util.TypeUtils;
import java.math.BigDecimal;
import java.sql.Time;
import com.alibaba.fastjson.JSONException;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;

public class TimeDeserializer implements ObjectDeserializer
{
    public static final TimeDeserializer instance;
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type clazz, final Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() == 16) {
            lexer.nextToken(4);
            if (lexer.token() != 4) {
                throw new JSONException("syntax error");
            }
            lexer.nextTokenWithColon(2);
            if (lexer.token() != 2) {
                throw new JSONException("syntax error");
            }
            final long time = lexer.longValue();
            lexer.nextToken(13);
            if (lexer.token() != 13) {
                throw new JSONException("syntax error");
            }
            lexer.nextToken(16);
            return (T)new Time(time);
        }
        else {
            final Object val = parser.parse();
            if (val == null) {
                return null;
            }
            if (val instanceof Time) {
                return (T)val;
            }
            if (val instanceof BigDecimal) {
                return (T)new Time(TypeUtils.longValue((BigDecimal)val));
            }
            if (val instanceof Number) {
                return (T)new Time(((Number)val).longValue());
            }
            if (!(val instanceof String)) {
                throw new JSONException("parse error");
            }
            final String strVal = (String)val;
            if (strVal.length() == 0) {
                return null;
            }
            final JSONScanner dateLexer = new JSONScanner(strVal);
            long longVal;
            if (dateLexer.scanISO8601DateIfMatch()) {
                longVal = dateLexer.getCalendar().getTimeInMillis();
            }
            else {
                boolean isDigit = true;
                for (int i = 0; i < strVal.length(); ++i) {
                    final char ch = strVal.charAt(i);
                    if (ch < '0' || ch > '9') {
                        isDigit = false;
                        break;
                    }
                }
                if (!isDigit) {
                    dateLexer.close();
                    return (T)Time.valueOf(strVal);
                }
                longVal = Long.parseLong(strVal);
            }
            dateLexer.close();
            return (T)new Time(longVal);
        }
    }
    
    @Override
    public int getFastMatchToken() {
        return 2;
    }
    
    static {
        instance = new TimeDeserializer();
    }
}
