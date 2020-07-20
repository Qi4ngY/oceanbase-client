package com.alibaba.fastjson.parser.deserializer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.TreeMap;
import java.util.SortedMap;
import java.util.IdentityHashMap;
import java.util.Hashtable;
import java.util.Properties;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import java.util.Collection;
import com.alibaba.fastjson.JSONArray;
import java.util.List;
import java.lang.reflect.ParameterizedType;
import com.alibaba.fastjson.parser.ParseContext;
import com.alibaba.fastjson.parser.JSONLexer;
import java.util.Collections;
import java.util.Map;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.JSONObject;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;

public class MapDeserializer implements ObjectDeserializer
{
    public static MapDeserializer instance;
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        if (type == JSONObject.class && parser.getFieldTypeResolver() == null) {
            return (T)parser.parseObject();
        }
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() == 8) {
            lexer.nextToken(16);
            return null;
        }
        final boolean unmodifiableMap = type instanceof Class && "java.util.Collections$UnmodifiableMap".equals(((Class)type).getName());
        final Map<Object, Object> map = ((lexer.getFeatures() & Feature.OrderedField.mask) != 0x0) ? this.createMap(type, lexer.getFeatures()) : this.createMap(type);
        final ParseContext context = parser.getContext();
        try {
            parser.setContext(context, map, fieldName);
            T t = (T)this.deserialze(parser, type, fieldName, map);
            if (unmodifiableMap) {
                t = (T)Collections.unmodifiableMap((Map<?, ?>)t);
            }
            return t;
        }
        finally {
            parser.setContext(context);
        }
    }
    
    protected Object deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName, final Map map) {
        if (!(type instanceof ParameterizedType)) {
            return parser.parseObject(map, fieldName);
        }
        final ParameterizedType parameterizedType = (ParameterizedType)type;
        final Type keyType = parameterizedType.getActualTypeArguments()[0];
        Type valueType = null;
        if (map.getClass().getName().equals("org.springframework.util.LinkedMultiValueMap")) {
            valueType = List.class;
        }
        else {
            valueType = parameterizedType.getActualTypeArguments()[1];
        }
        if (String.class == keyType) {
            return parseMap(parser, map, valueType, fieldName);
        }
        return parseMap(parser, map, keyType, valueType, fieldName);
    }
    
    public static Map parseMap(final DefaultJSONParser parser, final Map<String, Object> map, final Type valueType, final Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        final int token = lexer.token();
        if (token != 12) {
            String msg = "syntax error, expect {, actual " + lexer.tokenName();
            if (fieldName instanceof String) {
                msg += ", fieldName ";
                msg += fieldName;
            }
            msg += ", ";
            msg += lexer.info();
            if (token != 4) {
                final JSONArray array = new JSONArray();
                parser.parseArray(array, fieldName);
                if (array.size() == 1) {
                    final Object first = array.get(0);
                    if (first instanceof JSONObject) {
                        return (JSONObject)first;
                    }
                }
            }
            throw new JSONException(msg);
        }
        final ParseContext context = parser.getContext();
        try {
            int i = 0;
            while (true) {
                lexer.skipWhitespace();
                char ch = lexer.getCurrent();
                if (lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                    while (ch == ',') {
                        lexer.next();
                        lexer.skipWhitespace();
                        ch = lexer.getCurrent();
                    }
                }
                String key;
                if (ch == '\"') {
                    key = lexer.scanSymbol(parser.getSymbolTable(), '\"');
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch != ':') {
                        throw new JSONException("expect ':' at " + lexer.pos());
                    }
                }
                else {
                    if (ch == '}') {
                        lexer.next();
                        lexer.resetStringPosition();
                        lexer.nextToken(16);
                        return map;
                    }
                    if (ch == '\'') {
                        if (!lexer.isEnabled(Feature.AllowSingleQuotes)) {
                            throw new JSONException("syntax error");
                        }
                        key = lexer.scanSymbol(parser.getSymbolTable(), '\'');
                        lexer.skipWhitespace();
                        ch = lexer.getCurrent();
                        if (ch != ':') {
                            throw new JSONException("expect ':' at " + lexer.pos());
                        }
                    }
                    else {
                        if (!lexer.isEnabled(Feature.AllowUnQuotedFieldNames)) {
                            throw new JSONException("syntax error");
                        }
                        key = lexer.scanSymbolUnQuoted(parser.getSymbolTable());
                        lexer.skipWhitespace();
                        ch = lexer.getCurrent();
                        if (ch != ':') {
                            throw new JSONException("expect ':' at " + lexer.pos() + ", actual " + ch);
                        }
                    }
                }
                lexer.next();
                lexer.skipWhitespace();
                ch = lexer.getCurrent();
                lexer.resetStringPosition();
                if (key == JSON.DEFAULT_TYPE_KEY && !lexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                    final String typeName = lexer.scanSymbol(parser.getSymbolTable(), '\"');
                    final ParserConfig config = parser.getConfig();
                    final Class<?> clazz = config.checkAutoType(typeName, null, lexer.getFeatures());
                    if (!Map.class.isAssignableFrom(clazz)) {
                        final ObjectDeserializer deserializer = config.getDeserializer(clazz);
                        lexer.nextToken(16);
                        parser.setResolveStatus(2);
                        if (context != null && !(fieldName instanceof Integer)) {
                            parser.popContext();
                        }
                        return deserializer.deserialze(parser, clazz, fieldName);
                    }
                    lexer.nextToken(16);
                    if (lexer.token() == 13) {
                        lexer.nextToken(16);
                        return map;
                    }
                }
                else {
                    lexer.nextToken();
                    if (i != 0) {
                        parser.setContext(context);
                    }
                    Object value;
                    if (lexer.token() == 8) {
                        value = null;
                        lexer.nextToken();
                    }
                    else {
                        value = parser.parseObject(valueType, key);
                    }
                    map.put(key, value);
                    parser.checkMapResolve(map, key);
                    parser.setContext(context, value, key);
                    parser.setContext(context);
                    final int tok = lexer.token();
                    if (tok == 20 || tok == 15) {
                        return map;
                    }
                    if (tok == 13) {
                        lexer.nextToken();
                        return map;
                    }
                }
                ++i;
            }
        }
        finally {
            parser.setContext(context);
        }
    }
    
    public static Object parseMap(final DefaultJSONParser parser, final Map<Object, Object> map, final Type keyType, final Type valueType, final Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() != 12 && lexer.token() != 16) {
            throw new JSONException("syntax error, expect {, actual " + lexer.tokenName());
        }
        final ObjectDeserializer keyDeserializer = parser.getConfig().getDeserializer(keyType);
        final ObjectDeserializer valueDeserializer = parser.getConfig().getDeserializer(valueType);
        lexer.nextToken(keyDeserializer.getFastMatchToken());
        final ParseContext context = parser.getContext();
        try {
            while (lexer.token() != 13) {
                if (lexer.token() == 4 && lexer.isRef() && !lexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                    Object object = null;
                    lexer.nextTokenWithColon(4);
                    if (lexer.token() != 4) {
                        throw new JSONException("illegal ref, " + JSONToken.name(lexer.token()));
                    }
                    final String ref = lexer.stringVal();
                    if ("..".equals(ref)) {
                        final ParseContext parentContext = context.parent;
                        object = parentContext.object;
                    }
                    else if ("$".equals(ref)) {
                        ParseContext rootContext;
                        for (rootContext = context; rootContext.parent != null; rootContext = rootContext.parent) {}
                        object = rootContext.object;
                    }
                    else {
                        parser.addResolveTask(new DefaultJSONParser.ResolveTask(context, ref));
                        parser.setResolveStatus(1);
                    }
                    lexer.nextToken(13);
                    if (lexer.token() != 13) {
                        throw new JSONException("illegal ref");
                    }
                    lexer.nextToken(16);
                    return object;
                }
                else {
                    if (map.size() == 0 && lexer.token() == 4 && JSON.DEFAULT_TYPE_KEY.equals(lexer.stringVal()) && !lexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                        lexer.nextTokenWithColon(4);
                        lexer.nextToken(16);
                        if (lexer.token() == 13) {
                            lexer.nextToken();
                            return map;
                        }
                        lexer.nextToken(keyDeserializer.getFastMatchToken());
                    }
                    Object key;
                    if (lexer.token() == 4 && keyDeserializer instanceof JavaBeanDeserializer) {
                        final String keyStrValue = lexer.stringVal();
                        lexer.nextToken();
                        final DefaultJSONParser keyParser = new DefaultJSONParser(keyStrValue, parser.getConfig(), parser.getLexer().getFeatures());
                        keyParser.setDateFormat(parser.getDateFomartPattern());
                        key = keyDeserializer.deserialze(keyParser, keyType, null);
                    }
                    else {
                        key = keyDeserializer.deserialze(parser, keyType, null);
                    }
                    if (lexer.token() != 17) {
                        throw new JSONException("syntax error, expect :, actual " + lexer.token());
                    }
                    lexer.nextToken(valueDeserializer.getFastMatchToken());
                    final Object value = valueDeserializer.deserialze(parser, valueType, key);
                    parser.checkMapResolve(map, key);
                    map.put(key, value);
                    if (lexer.token() != 16) {
                        continue;
                    }
                    lexer.nextToken(keyDeserializer.getFastMatchToken());
                }
            }
            lexer.nextToken(16);
        }
        finally {
            parser.setContext(context);
        }
        return map;
    }
    
    public Map<Object, Object> createMap(final Type type) {
        return this.createMap(type, JSON.DEFAULT_GENERATE_FEATURE);
    }
    
    public Map<Object, Object> createMap(final Type type, final int featrues) {
        if (type == Properties.class) {
            return new Properties();
        }
        if (type == Hashtable.class) {
            return new Hashtable<Object, Object>();
        }
        if (type == IdentityHashMap.class) {
            return new IdentityHashMap<Object, Object>();
        }
        if (type == SortedMap.class || type == TreeMap.class) {
            return new TreeMap<Object, Object>();
        }
        if (type == ConcurrentMap.class || type == ConcurrentHashMap.class) {
            return new ConcurrentHashMap<Object, Object>();
        }
        if (type == Map.class) {
            return ((featrues & Feature.OrderedField.mask) != 0x0) ? new LinkedHashMap<Object, Object>() : new HashMap<Object, Object>();
        }
        if (type == HashMap.class) {
            return new HashMap<Object, Object>();
        }
        if (type == LinkedHashMap.class) {
            return new LinkedHashMap<Object, Object>();
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType)type;
            final Type rawType = parameterizedType.getRawType();
            if (EnumMap.class.equals(rawType)) {
                final Type[] actualArgs = parameterizedType.getActualTypeArguments();
                return new EnumMap<Object, Object>((Class<Object>)actualArgs[0]);
            }
            return this.createMap(rawType, featrues);
        }
        else {
            final Class<?> clazz = (Class<?>)type;
            if (clazz.isInterface()) {
                throw new JSONException("unsupport type " + type);
            }
            if ("java.util.Collections$UnmodifiableMap".equals(clazz.getName())) {
                return new HashMap<Object, Object>();
            }
            try {
                return (Map<Object, Object>)clazz.newInstance();
            }
            catch (Exception e) {
                throw new JSONException("unsupport type " + type, e);
            }
        }
    }
    
    @Override
    public int getFastMatchToken() {
        return 12;
    }
    
    static {
        MapDeserializer.instance = new MapDeserializer();
    }
}
