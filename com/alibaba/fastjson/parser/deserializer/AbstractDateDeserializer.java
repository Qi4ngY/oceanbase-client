package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.JSONScanner;
import com.alibaba.fastjson.parser.Feature;
import java.util.Locale;
import java.text.ParseException;
import com.alibaba.fastjson.JSON;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;

public abstract class AbstractDateDeserializer extends ContextObjectDeserializer implements ObjectDeserializer
{
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type clazz, final Object fieldName) {
        return this.deserialze(parser, clazz, fieldName, null, 0);
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, Type clazz, final Object fieldName, final String format, final int features) {
        final JSONLexer lexer = parser.lexer;
        Object val;
        if (lexer.token() == 2) {
            long millis = lexer.longValue();
            lexer.nextToken(16);
            if ("unixtime".equals(format)) {
                millis *= 1000L;
            }
            val = millis;
        }
        else if (lexer.token() == 4) {
            final String strVal = lexer.stringVal();
            if (format != null) {
                if ("yyyy-MM-dd HH:mm:ss.SSSSSSSSS".equals(format) && clazz instanceof Class && ((Class)clazz).getName().equals("java.sql.Timestamp")) {
                    return (T)TypeUtils.castToTimestamp(strVal);
                }
                SimpleDateFormat simpleDateFormat = null;
                try {
                    simpleDateFormat = new SimpleDateFormat(format, parser.lexer.getLocale());
                }
                catch (IllegalArgumentException ex) {
                    if (format.contains("T")) {
                        final String fromat2 = format.replaceAll("T", "'T'");
                        try {
                            simpleDateFormat = new SimpleDateFormat(fromat2, parser.lexer.getLocale());
                        }
                        catch (IllegalArgumentException e2) {
                            throw ex;
                        }
                    }
                }
                if (JSON.defaultTimeZone != null) {
                    simpleDateFormat.setTimeZone(parser.lexer.getTimeZone());
                }
                try {
                    val = simpleDateFormat.parse(strVal);
                }
                catch (ParseException ex2) {
                    val = null;
                }
                if (val == null && JSON.defaultLocale == Locale.CHINA) {
                    try {
                        simpleDateFormat = new SimpleDateFormat(format, Locale.US);
                    }
                    catch (IllegalArgumentException ex) {
                        if (format.contains("T")) {
                            final String fromat2 = format.replaceAll("T", "'T'");
                            try {
                                simpleDateFormat = new SimpleDateFormat(fromat2, parser.lexer.getLocale());
                            }
                            catch (IllegalArgumentException e2) {
                                throw ex;
                            }
                        }
                    }
                    simpleDateFormat.setTimeZone(parser.lexer.getTimeZone());
                    try {
                        val = simpleDateFormat.parse(strVal);
                    }
                    catch (ParseException ex2) {
                        val = null;
                    }
                }
                if (val == null) {
                    if (format.equals("yyyy-MM-dd'T'HH:mm:ss.SSS") && strVal.length() == 19) {
                        try {
                            final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", JSON.defaultLocale);
                            df.setTimeZone(JSON.defaultTimeZone);
                            val = df.parse(strVal);
                        }
                        catch (ParseException ex3) {
                            val = null;
                        }
                    }
                    else {
                        val = null;
                    }
                }
            }
            else {
                val = null;
            }
            if (val == null) {
                val = strVal;
                lexer.nextToken(16);
                if (lexer.isEnabled(Feature.AllowISO8601DateFormat)) {
                    final JSONScanner iso8601Lexer = new JSONScanner(strVal);
                    if (iso8601Lexer.scanISO8601DateIfMatch()) {
                        val = iso8601Lexer.getCalendar().getTime();
                    }
                    iso8601Lexer.close();
                }
            }
        }
        else if (lexer.token() == 8) {
            lexer.nextToken();
            val = null;
        }
        else if (lexer.token() == 12) {
            lexer.nextToken();
            if (lexer.token() != 4) {
                throw new JSONException("syntax error");
            }
            final String key = lexer.stringVal();
            if (JSON.DEFAULT_TYPE_KEY.equals(key)) {
                lexer.nextToken();
                parser.accept(17);
                final String typeName = lexer.stringVal();
                final Class<?> type = parser.getConfig().checkAutoType(typeName, null, lexer.getFeatures());
                if (type != null) {
                    clazz = type;
                }
                parser.accept(4);
                parser.accept(16);
            }
            lexer.nextTokenWithColon(2);
            if (lexer.token() != 2) {
                throw new JSONException("syntax error : " + lexer.tokenName());
            }
            final long timeMillis = lexer.longValue();
            lexer.nextToken();
            val = timeMillis;
            parser.accept(13);
        }
        else if (parser.getResolveStatus() == 2) {
            parser.setResolveStatus(0);
            parser.accept(16);
            if (lexer.token() != 4) {
                throw new JSONException("syntax error");
            }
            if (!"val".equals(lexer.stringVal())) {
                throw new JSONException("syntax error");
            }
            lexer.nextToken();
            parser.accept(17);
            val = parser.parse();
            parser.accept(13);
        }
        else {
            val = parser.parse();
        }
        return this.cast(parser, clazz, fieldName, val);
    }
    
    protected abstract <T> T cast(final DefaultJSONParser p0, final Type p1, final Object p2, final Object p3);
}
