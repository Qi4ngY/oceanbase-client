package com.alibaba.fastjson.parser.deserializer;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import com.alibaba.fastjson.parser.JSONLexer;
import java.util.Map;
import java.util.HashMap;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.ParserConfig;

public class ThrowableDeserializer extends JavaBeanDeserializer
{
    public ThrowableDeserializer(final ParserConfig mapping, final Class<?> clazz) {
        super(mapping, clazz, clazz);
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() == 8) {
            lexer.nextToken();
            return null;
        }
        if (parser.getResolveStatus() == 2) {
            parser.setResolveStatus(0);
        }
        else if (lexer.token() != 12) {
            throw new JSONException("syntax error");
        }
        Throwable cause = null;
        Class<?> exClass = null;
        if (type != null && type instanceof Class) {
            final Class<?> clazz = (Class<?>)type;
            if (Throwable.class.isAssignableFrom(clazz)) {
                exClass = clazz;
            }
        }
        String message = null;
        StackTraceElement[] stackTrace = null;
        Map<String, Object> otherValues = null;
        while (true) {
            final String key = lexer.scanSymbol(parser.getSymbolTable());
            if (key == null) {
                if (lexer.token() == 13) {
                    lexer.nextToken(16);
                    break;
                }
                if (lexer.token() == 16 && lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                    continue;
                }
            }
            lexer.nextTokenWithColon(4);
            if (JSON.DEFAULT_TYPE_KEY.equals(key)) {
                if (lexer.token() != 4) {
                    throw new JSONException("syntax error");
                }
                final String exClassName = lexer.stringVal();
                exClass = parser.getConfig().checkAutoType(exClassName, Throwable.class, lexer.getFeatures());
                lexer.nextToken(16);
            }
            else if ("message".equals(key)) {
                if (lexer.token() == 8) {
                    message = null;
                }
                else {
                    if (lexer.token() != 4) {
                        throw new JSONException("syntax error");
                    }
                    message = lexer.stringVal();
                }
                lexer.nextToken();
            }
            else if ("cause".equals(key)) {
                cause = this.deserialze(parser, null, "cause");
            }
            else if ("stackTrace".equals(key)) {
                stackTrace = parser.parseObject(StackTraceElement[].class);
            }
            else {
                if (otherValues == null) {
                    otherValues = new HashMap<String, Object>();
                }
                otherValues.put(key, parser.parse());
            }
            if (lexer.token() == 13) {
                lexer.nextToken(16);
                break;
            }
        }
        Throwable ex = null;
        if (exClass == null) {
            ex = new Exception(message, cause);
        }
        else {
            if (!Throwable.class.isAssignableFrom(exClass)) {
                throw new JSONException("type not match, not Throwable. " + exClass.getName());
            }
            try {
                ex = this.createException(message, cause, exClass);
                if (ex == null) {
                    ex = new Exception(message, cause);
                }
            }
            catch (Exception e) {
                throw new JSONException("create instance error", e);
            }
        }
        if (stackTrace != null) {
            ex.setStackTrace(stackTrace);
        }
        if (otherValues != null) {
            JavaBeanDeserializer exBeanDeser = null;
            if (exClass != null) {
                if (exClass == this.clazz) {
                    exBeanDeser = this;
                }
                else {
                    final ObjectDeserializer exDeser = parser.getConfig().getDeserializer(exClass);
                    if (exDeser instanceof JavaBeanDeserializer) {
                        exBeanDeser = (JavaBeanDeserializer)exDeser;
                    }
                }
            }
            if (exBeanDeser != null) {
                for (final Map.Entry<String, Object> entry : otherValues.entrySet()) {
                    final String key2 = entry.getKey();
                    final Object value = entry.getValue();
                    final FieldDeserializer fieldDeserializer = exBeanDeser.getFieldDeserializer(key2);
                    if (fieldDeserializer != null) {
                        fieldDeserializer.setValue(ex, value);
                    }
                }
            }
        }
        return (T)ex;
    }
    
    private Throwable createException(final String message, final Throwable cause, final Class<?> exClass) throws Exception {
        Constructor<?> defaultConstructor = null;
        Constructor<?> messageConstructor = null;
        Constructor<?> causeConstructor = null;
        for (final Constructor<?> constructor : exClass.getConstructors()) {
            final Class<?>[] types = constructor.getParameterTypes();
            if (types.length == 0) {
                defaultConstructor = constructor;
            }
            else if (types.length == 1 && types[0] == String.class) {
                messageConstructor = constructor;
            }
            else if (types.length == 2 && types[0] == String.class && types[1] == Throwable.class) {
                causeConstructor = constructor;
            }
        }
        if (causeConstructor != null) {
            return (Throwable)causeConstructor.newInstance(message, cause);
        }
        if (messageConstructor != null) {
            return (Throwable)messageConstructor.newInstance(message);
        }
        if (defaultConstructor != null) {
            return (Throwable)defaultConstructor.newInstance(new Object[0]);
        }
        return null;
    }
    
    @Override
    public int getFastMatchToken() {
        return 12;
    }
}
