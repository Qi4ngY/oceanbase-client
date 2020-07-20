package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.JSONToken;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;

public class StackTraceElementDeserializer implements ObjectDeserializer
{
    public static final StackTraceElementDeserializer instance;
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() == 8) {
            lexer.nextToken();
            return null;
        }
        if (lexer.token() != 12 && lexer.token() != 16) {
            throw new JSONException("syntax error: " + JSONToken.name(lexer.token()));
        }
        String declaringClass = null;
        String methodName = null;
        String fileName = null;
        int lineNumber = 0;
        String moduleName = null;
        String moduleVersion = null;
        String classLoaderName = null;
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
            if ("className".equals(key)) {
                if (lexer.token() == 8) {
                    declaringClass = null;
                }
                else {
                    if (lexer.token() != 4) {
                        throw new JSONException("syntax error");
                    }
                    declaringClass = lexer.stringVal();
                }
            }
            else if ("methodName".equals(key)) {
                if (lexer.token() == 8) {
                    methodName = null;
                }
                else {
                    if (lexer.token() != 4) {
                        throw new JSONException("syntax error");
                    }
                    methodName = lexer.stringVal();
                }
            }
            else if ("fileName".equals(key)) {
                if (lexer.token() == 8) {
                    fileName = null;
                }
                else {
                    if (lexer.token() != 4) {
                        throw new JSONException("syntax error");
                    }
                    fileName = lexer.stringVal();
                }
            }
            else if ("lineNumber".equals(key)) {
                if (lexer.token() == 8) {
                    lineNumber = 0;
                }
                else {
                    if (lexer.token() != 2) {
                        throw new JSONException("syntax error");
                    }
                    lineNumber = lexer.intValue();
                }
            }
            else if ("nativeMethod".equals(key)) {
                if (lexer.token() == 8) {
                    lexer.nextToken(16);
                }
                else if (lexer.token() == 6) {
                    lexer.nextToken(16);
                }
                else {
                    if (lexer.token() != 7) {
                        throw new JSONException("syntax error");
                    }
                    lexer.nextToken(16);
                }
            }
            else if (key == JSON.DEFAULT_TYPE_KEY) {
                if (lexer.token() == 4) {
                    final String elementType = lexer.stringVal();
                    if (!elementType.equals("java.lang.StackTraceElement")) {
                        throw new JSONException("syntax error : " + elementType);
                    }
                }
                else if (lexer.token() != 8) {
                    throw new JSONException("syntax error");
                }
            }
            else if ("moduleName".equals(key)) {
                if (lexer.token() == 8) {
                    moduleName = null;
                }
                else {
                    if (lexer.token() != 4) {
                        throw new JSONException("syntax error");
                    }
                    moduleName = lexer.stringVal();
                }
            }
            else if ("moduleVersion".equals(key)) {
                if (lexer.token() == 8) {
                    moduleVersion = null;
                }
                else {
                    if (lexer.token() != 4) {
                        throw new JSONException("syntax error");
                    }
                    moduleVersion = lexer.stringVal();
                }
            }
            else {
                if (!"classLoaderName".equals(key)) {
                    throw new JSONException("syntax error : " + key);
                }
                if (lexer.token() == 8) {
                    classLoaderName = null;
                }
                else {
                    if (lexer.token() != 4) {
                        throw new JSONException("syntax error");
                    }
                    classLoaderName = lexer.stringVal();
                }
            }
            if (lexer.token() == 13) {
                lexer.nextToken(16);
                break;
            }
        }
        return (T)new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
    }
    
    @Override
    public int getFastMatchToken() {
        return 12;
    }
    
    static {
        instance = new StackTraceElementDeserializer();
    }
}
