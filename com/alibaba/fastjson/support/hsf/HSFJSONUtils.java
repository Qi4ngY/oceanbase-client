package com.alibaba.fastjson.support.hsf;

import java.lang.reflect.Type;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.lang.reflect.Method;
import com.alibaba.fastjson.parser.ParseContext;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.JSONLexerBase;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.SymbolTable;

public class HSFJSONUtils
{
    static final SymbolTable typeSymbolTable;
    static final char[] fieldName_argsTypes;
    static final char[] fieldName_argsObjs;
    static final char[] fieldName_type;
    
    public static Object[] parseInvocationArguments(final String json, final MethodLocator methodLocator) {
        final DefaultJSONParser parser = new DefaultJSONParser(json);
        final JSONLexerBase lexer = (JSONLexerBase)parser.getLexer();
        final ParseContext rootContext = parser.setContext(null, null);
        final int token = lexer.token();
        Object[] values;
        if (token == 12) {
            String[] typeNames = lexer.scanFieldStringArray(HSFJSONUtils.fieldName_argsTypes, -1, HSFJSONUtils.typeSymbolTable);
            if (typeNames == null && lexer.matchStat == -2) {
                final String type = lexer.scanFieldString(HSFJSONUtils.fieldName_type);
                if ("com.alibaba.fastjson.JSONObject".equals(type)) {
                    typeNames = lexer.scanFieldStringArray(HSFJSONUtils.fieldName_argsTypes, -1, HSFJSONUtils.typeSymbolTable);
                }
            }
            Method method = methodLocator.findMethod(typeNames);
            if (method == null) {
                lexer.close();
                final JSONObject jsonObject = JSON.parseObject(json);
                typeNames = jsonObject.getObject("argsTypes", String[].class);
                method = methodLocator.findMethod(typeNames);
                final JSONArray argsObjs = jsonObject.getJSONArray("argsObjs");
                if (argsObjs == null) {
                    values = null;
                }
                else {
                    final Type[] argTypes = method.getGenericParameterTypes();
                    values = new Object[argTypes.length];
                    for (int i = 0; i < argTypes.length; ++i) {
                        final Type type2 = argTypes[i];
                        values[i] = argsObjs.getObject(i, type2);
                    }
                }
            }
            else {
                final Type[] argTypes2 = method.getGenericParameterTypes();
                lexer.skipWhitespace();
                if (lexer.getCurrent() == ',') {
                    lexer.next();
                }
                if (lexer.matchField2(HSFJSONUtils.fieldName_argsObjs)) {
                    lexer.nextToken();
                    final ParseContext context = parser.setContext(rootContext, null, "argsObjs");
                    values = parser.parseArray(argTypes2);
                    context.object = values;
                    parser.accept(13);
                    parser.handleResovleTask(null);
                }
                else {
                    values = null;
                }
                parser.close();
            }
        }
        else if (token == 14) {
            final String[] typeNames = lexer.scanFieldStringArray(null, -1, HSFJSONUtils.typeSymbolTable);
            lexer.skipWhitespace();
            final char ch = lexer.getCurrent();
            if (ch == ']') {
                final Method method2 = methodLocator.findMethod(null);
                final Type[] argTypes3 = method2.getGenericParameterTypes();
                values = new Object[typeNames.length];
                for (int j = 0; j < typeNames.length; ++j) {
                    final Type argType = argTypes3[j];
                    final String typeName = typeNames[j];
                    if (argType != String.class) {
                        values[j] = TypeUtils.cast(typeName, argType, parser.getConfig());
                    }
                    else {
                        values[j] = typeName;
                    }
                }
                return values;
            }
            if (ch == ',') {
                lexer.next();
                lexer.skipWhitespace();
            }
            lexer.nextToken(14);
            final Method method2 = methodLocator.findMethod(typeNames);
            final Type[] argTypes3 = method2.getGenericParameterTypes();
            values = parser.parseArray(argTypes3);
            lexer.close();
        }
        else {
            values = null;
        }
        return values;
    }
    
    static {
        typeSymbolTable = new SymbolTable(1024);
        fieldName_argsTypes = "\"argsTypes\"".toCharArray();
        fieldName_argsObjs = "\"argsObjs\"".toCharArray();
        fieldName_type = "\"@type\":".toCharArray();
    }
}
