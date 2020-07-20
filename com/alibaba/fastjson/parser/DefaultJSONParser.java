package com.alibaba.fastjson.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import com.alibaba.fastjson.parser.deserializer.PropertyProcessable;
import java.util.Iterator;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessable;
import com.alibaba.fastjson.JSONPathException;
import com.alibaba.fastjson.JSONPath;
import java.util.Date;
import java.util.TreeSet;
import java.util.HashSet;
import com.alibaba.fastjson.parser.deserializer.ResolveFieldDeserializer;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.lang.reflect.ParameterizedType;
import com.alibaba.fastjson.parser.deserializer.FieldDeserializer;
import com.alibaba.fastjson.serializer.LongCodec;
import com.alibaba.fastjson.serializer.StringCodec;
import com.alibaba.fastjson.serializer.IntegerCodec;
import java.util.ArrayList;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.JSONArray;
import java.util.Collection;
import com.alibaba.fastjson.parser.deserializer.MapDeserializer;
import com.alibaba.fastjson.parser.deserializer.ThrowableDeserializer;
import java.util.Collections;
import java.util.HashMap;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import java.lang.reflect.Type;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONException;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.serializer.BeanContext;
import com.alibaba.fastjson.parser.deserializer.FieldTypeResolver;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessor;
import com.alibaba.fastjson.parser.deserializer.ExtraTypeProvider;
import java.util.List;
import java.text.DateFormat;
import java.util.Set;
import java.io.Closeable;

public class DefaultJSONParser implements Closeable
{
    public final Object input;
    public final SymbolTable symbolTable;
    protected ParserConfig config;
    private static final Set<Class<?>> primitiveClasses;
    private String dateFormatPattern;
    private DateFormat dateFormat;
    public final JSONLexer lexer;
    protected ParseContext context;
    private ParseContext[] contextArray;
    private int contextArrayIndex;
    private List<ResolveTask> resolveTaskList;
    public static final int NONE = 0;
    public static final int NeedToResolve = 1;
    public static final int TypeNameRedirect = 2;
    public int resolveStatus;
    private List<ExtraTypeProvider> extraTypeProviders;
    private List<ExtraProcessor> extraProcessors;
    protected FieldTypeResolver fieldTypeResolver;
    private int objectKeyLevel;
    private boolean autoTypeEnable;
    private String[] autoTypeAccept;
    protected transient BeanContext lastBeanContext;
    
    public String getDateFomartPattern() {
        return this.dateFormatPattern;
    }
    
    public DateFormat getDateFormat() {
        if (this.dateFormat == null) {
            (this.dateFormat = new SimpleDateFormat(this.dateFormatPattern, this.lexer.getLocale())).setTimeZone(this.lexer.getTimeZone());
        }
        return this.dateFormat;
    }
    
    public void setDateFormat(final String dateFormat) {
        this.dateFormatPattern = dateFormat;
        this.dateFormat = null;
    }
    
    public void setDateFomrat(final DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }
    
    public DefaultJSONParser(final String input) {
        this(input, ParserConfig.getGlobalInstance(), JSON.DEFAULT_PARSER_FEATURE);
    }
    
    public DefaultJSONParser(final String input, final ParserConfig config) {
        this(input, new JSONScanner(input, JSON.DEFAULT_PARSER_FEATURE), config);
    }
    
    public DefaultJSONParser(final String input, final ParserConfig config, final int features) {
        this(input, new JSONScanner(input, features), config);
    }
    
    public DefaultJSONParser(final char[] input, final int length, final ParserConfig config, final int features) {
        this(input, new JSONScanner(input, length, features), config);
    }
    
    public DefaultJSONParser(final JSONLexer lexer) {
        this(lexer, ParserConfig.getGlobalInstance());
    }
    
    public DefaultJSONParser(final JSONLexer lexer, final ParserConfig config) {
        this(null, lexer, config);
    }
    
    public DefaultJSONParser(final Object input, final JSONLexer lexer, final ParserConfig config) {
        this.dateFormatPattern = JSON.DEFFAULT_DATE_FORMAT;
        this.contextArrayIndex = 0;
        this.resolveStatus = 0;
        this.extraTypeProviders = null;
        this.extraProcessors = null;
        this.fieldTypeResolver = null;
        this.objectKeyLevel = 0;
        this.autoTypeAccept = null;
        this.lexer = lexer;
        this.input = input;
        this.config = config;
        this.symbolTable = config.symbolTable;
        final int ch = lexer.getCurrent();
        if (ch == 123) {
            lexer.next();
            ((JSONLexerBase)lexer).token = 12;
        }
        else if (ch == 91) {
            lexer.next();
            ((JSONLexerBase)lexer).token = 14;
        }
        else {
            lexer.nextToken();
        }
    }
    
    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }
    
    public String getInput() {
        if (this.input instanceof char[]) {
            return new String((char[])this.input);
        }
        return this.input.toString();
    }
    
    public final Object parseObject(final Map object, final Object fieldName) {
        final JSONLexer lexer = this.lexer;
        if (lexer.token() == 8) {
            lexer.nextToken();
            return null;
        }
        if (lexer.token() == 13) {
            lexer.nextToken();
            return object;
        }
        if (lexer.token() == 4 && lexer.stringVal().length() == 0) {
            lexer.nextToken();
            return object;
        }
        if (lexer.token() != 12 && lexer.token() != 16) {
            throw new JSONException("syntax error, expect {, actual " + lexer.tokenName() + ", " + lexer.info());
        }
        ParseContext context = this.context;
        try {
            final boolean isJsonObjectMap = object instanceof JSONObject;
            final Map map = isJsonObjectMap ? ((JSONObject)object).getInnerMap() : object;
            boolean setContextFlag = false;
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
                boolean isObjectKey = false;
                Object key;
                if (ch == '\"') {
                    key = lexer.scanSymbol(this.symbolTable, '\"');
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch != ':') {
                        throw new JSONException("expect ':' at " + lexer.pos() + ", name " + key);
                    }
                }
                else {
                    if (ch == '}') {
                        lexer.next();
                        lexer.resetStringPosition();
                        lexer.nextToken();
                        if (!setContextFlag) {
                            if (this.context != null && fieldName == this.context.fieldName && object == this.context.object) {
                                context = this.context;
                            }
                            else {
                                final ParseContext contextR = this.setContext(object, fieldName);
                                if (context == null) {
                                    context = contextR;
                                }
                                setContextFlag = true;
                            }
                        }
                        return object;
                    }
                    if (ch == '\'') {
                        if (!lexer.isEnabled(Feature.AllowSingleQuotes)) {
                            throw new JSONException("syntax error");
                        }
                        key = lexer.scanSymbol(this.symbolTable, '\'');
                        lexer.skipWhitespace();
                        ch = lexer.getCurrent();
                        if (ch != ':') {
                            throw new JSONException("expect ':' at " + lexer.pos());
                        }
                    }
                    else {
                        if (ch == '\u001a') {
                            throw new JSONException("syntax error");
                        }
                        if (ch == ',') {
                            throw new JSONException("syntax error");
                        }
                        if ((ch >= '0' && ch <= '9') || ch == '-') {
                            lexer.resetStringPosition();
                            lexer.scanNumber();
                            try {
                                if (lexer.token() == 2) {
                                    key = lexer.integerValue();
                                }
                                else {
                                    key = lexer.decimalValue(true);
                                }
                                if (lexer.isEnabled(Feature.NonStringKeyAsString) || isJsonObjectMap) {
                                    key = key.toString();
                                }
                            }
                            catch (NumberFormatException e2) {
                                throw new JSONException("parse number key error" + lexer.info());
                            }
                            ch = lexer.getCurrent();
                            if (ch != ':') {
                                throw new JSONException("parse number key error" + lexer.info());
                            }
                        }
                        else if (ch == '{' || ch == '[') {
                            if (this.objectKeyLevel++ > 512) {
                                throw new JSONException("object key level > 512");
                            }
                            lexer.nextToken();
                            key = this.parse();
                            isObjectKey = true;
                        }
                        else {
                            if (!lexer.isEnabled(Feature.AllowUnQuotedFieldNames)) {
                                throw new JSONException("syntax error");
                            }
                            key = lexer.scanSymbolUnQuoted(this.symbolTable);
                            lexer.skipWhitespace();
                            ch = lexer.getCurrent();
                            if (ch != ':') {
                                throw new JSONException("expect ':' at " + lexer.pos() + ", actual " + ch);
                            }
                        }
                    }
                }
                if (!isObjectKey) {
                    lexer.next();
                    lexer.skipWhitespace();
                }
                ch = lexer.getCurrent();
                lexer.resetStringPosition();
                if (key == JSON.DEFAULT_TYPE_KEY && !lexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                    final String typeName = lexer.scanSymbol(this.symbolTable, '\"');
                    if (lexer.isEnabled(Feature.IgnoreAutoType)) {
                        continue;
                    }
                    Class<?> clazz = null;
                    if (object != null && object.getClass().getName().equals(typeName)) {
                        clazz = object.getClass();
                    }
                    else {
                        boolean allDigits = true;
                        for (int i = 0; i < typeName.length(); ++i) {
                            final char c = typeName.charAt(i);
                            if (c < '0' || c > '9') {
                                allDigits = false;
                                break;
                            }
                        }
                        if (!allDigits) {
                            clazz = this.config.checkAutoType(typeName, null, lexer.getFeatures());
                        }
                    }
                    if (clazz == null) {
                        map.put(JSON.DEFAULT_TYPE_KEY, typeName);
                    }
                    else {
                        lexer.nextToken(16);
                        if (lexer.token() == 13) {
                            lexer.nextToken(16);
                            try {
                                Object instance = null;
                                final ObjectDeserializer deserializer = this.config.getDeserializer(clazz);
                                if (deserializer instanceof JavaBeanDeserializer) {
                                    instance = TypeUtils.cast(object, clazz, this.config);
                                }
                                if (instance == null) {
                                    if (clazz == Cloneable.class) {
                                        instance = new HashMap();
                                    }
                                    else if ("java.util.Collections$EmptyMap".equals(typeName)) {
                                        instance = Collections.emptyMap();
                                    }
                                    else if ("java.util.Collections$UnmodifiableMap".equals(typeName)) {
                                        instance = Collections.unmodifiableMap((Map<?, ?>)new HashMap<Object, Object>());
                                    }
                                    else {
                                        instance = clazz.newInstance();
                                    }
                                }
                                return instance;
                            }
                            catch (Exception e) {
                                throw new JSONException("create instance error", e);
                            }
                        }
                        this.setResolveStatus(2);
                        if (this.context != null && fieldName != null && !(fieldName instanceof Integer) && !(this.context.fieldName instanceof Integer)) {
                            this.popContext();
                        }
                        if (object.size() > 0) {
                            final Object newObj = TypeUtils.cast(object, clazz, this.config);
                            this.setResolveStatus(0);
                            this.parseObject(newObj);
                            return newObj;
                        }
                        final ObjectDeserializer deserializer2 = this.config.getDeserializer(clazz);
                        final Class deserClass = deserializer2.getClass();
                        if (JavaBeanDeserializer.class.isAssignableFrom(deserClass) && deserClass != JavaBeanDeserializer.class && deserClass != ThrowableDeserializer.class) {
                            this.setResolveStatus(0);
                        }
                        else if (deserializer2 instanceof MapDeserializer) {
                            this.setResolveStatus(0);
                        }
                        final Object obj = deserializer2.deserialze(this, clazz, fieldName);
                        return obj;
                    }
                }
                else if (key == "$ref" && context != null && (object == null || object.size() == 0) && !lexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                    lexer.nextToken(4);
                    if (lexer.token() != 4) {
                        throw new JSONException("illegal ref, " + JSONToken.name(lexer.token()));
                    }
                    final String ref = lexer.stringVal();
                    lexer.nextToken(13);
                    if (lexer.token() == 16) {
                        map.put(key, ref);
                    }
                    else {
                        Object refValue = null;
                        if ("@".equals(ref)) {
                            if (this.context != null) {
                                final ParseContext thisContext = this.context;
                                final Object thisObj = thisContext.object;
                                if (thisObj instanceof Object[] || thisObj instanceof Collection) {
                                    refValue = thisObj;
                                }
                                else if (thisContext.parent != null) {
                                    refValue = thisContext.parent.object;
                                }
                            }
                        }
                        else if ("..".equals(ref)) {
                            if (context.object != null) {
                                refValue = context.object;
                            }
                            else {
                                this.addResolveTask(new ResolveTask(context, ref));
                                this.setResolveStatus(1);
                            }
                        }
                        else if ("$".equals(ref)) {
                            ParseContext rootContext;
                            for (rootContext = context; rootContext.parent != null; rootContext = rootContext.parent) {}
                            if (rootContext.object != null) {
                                refValue = rootContext.object;
                            }
                            else {
                                this.addResolveTask(new ResolveTask(rootContext, ref));
                                this.setResolveStatus(1);
                            }
                        }
                        else {
                            this.addResolveTask(new ResolveTask(context, ref));
                            this.setResolveStatus(1);
                        }
                        if (lexer.token() != 13) {
                            throw new JSONException("syntax error, " + lexer.info());
                        }
                        lexer.nextToken(16);
                        return refValue;
                    }
                }
                else {
                    if (!setContextFlag) {
                        if (this.context != null && fieldName == this.context.fieldName && object == this.context.object) {
                            context = this.context;
                        }
                        else {
                            final ParseContext contextR = this.setContext(object, fieldName);
                            if (context == null) {
                                context = contextR;
                            }
                            setContextFlag = true;
                        }
                    }
                    if (object.getClass() == JSONObject.class && key == null) {
                        key = "null";
                    }
                    Object value;
                    if (ch == '\"') {
                        lexer.scanString();
                        final String strValue = (String)(value = lexer.stringVal());
                        if (lexer.isEnabled(Feature.AllowISO8601DateFormat)) {
                            final JSONScanner iso8601Lexer = new JSONScanner(strValue);
                            if (iso8601Lexer.scanISO8601DateIfMatch()) {
                                value = iso8601Lexer.getCalendar().getTime();
                            }
                            iso8601Lexer.close();
                        }
                        map.put(key, value);
                    }
                    else if ((ch >= '0' && ch <= '9') || ch == '-') {
                        lexer.scanNumber();
                        if (lexer.token() == 2) {
                            value = lexer.integerValue();
                        }
                        else {
                            value = lexer.decimalValue(lexer.isEnabled(Feature.UseBigDecimal));
                        }
                        map.put(key, value);
                    }
                    else if (ch == '[') {
                        lexer.nextToken();
                        final JSONArray list = new JSONArray();
                        final boolean parentIsArray = fieldName != null && fieldName.getClass() == Integer.class;
                        if (fieldName == null) {
                            this.setContext(context);
                        }
                        this.parseArray(list, key);
                        if (lexer.isEnabled(Feature.UseObjectArray)) {
                            value = list.toArray();
                        }
                        else {
                            value = list;
                        }
                        map.put(key, value);
                        if (lexer.token() == 13) {
                            lexer.nextToken();
                            return object;
                        }
                        if (lexer.token() == 16) {
                            continue;
                        }
                        throw new JSONException("syntax error");
                    }
                    else if (ch == '{') {
                        lexer.nextToken();
                        final boolean parentIsArray2 = fieldName != null && fieldName.getClass() == Integer.class;
                        Map input;
                        if (lexer.isEnabled(Feature.CustomMapDeserializer)) {
                            final MapDeserializer mapDeserializer = (MapDeserializer)this.config.getDeserializer(Map.class);
                            input = (((lexer.getFeatures() & Feature.OrderedField.mask) != 0x0) ? mapDeserializer.createMap(Map.class, lexer.getFeatures()) : mapDeserializer.createMap(Map.class));
                        }
                        else {
                            input = new JSONObject(lexer.isEnabled(Feature.OrderedField));
                        }
                        ParseContext ctxLocal = null;
                        if (!parentIsArray2) {
                            ctxLocal = this.setContext(context, input, key);
                        }
                        Object obj = null;
                        boolean objParsed = false;
                        if (this.fieldTypeResolver != null) {
                            final String resolveFieldName = (key != null) ? key.toString() : null;
                            final Type fieldType = this.fieldTypeResolver.resolve(object, resolveFieldName);
                            if (fieldType != null) {
                                final ObjectDeserializer fieldDeser = this.config.getDeserializer(fieldType);
                                obj = fieldDeser.deserialze(this, fieldType, key);
                                objParsed = true;
                            }
                        }
                        if (!objParsed) {
                            obj = this.parseObject(input, key);
                        }
                        if (ctxLocal != null && input != obj) {
                            ctxLocal.object = object;
                        }
                        if (key != null) {
                            this.checkMapResolve(object, key.toString());
                        }
                        map.put(key, obj);
                        if (parentIsArray2) {
                            this.setContext(obj, key);
                        }
                        if (lexer.token() == 13) {
                            lexer.nextToken();
                            this.setContext(context);
                            return object;
                        }
                        if (lexer.token() != 16) {
                            throw new JSONException("syntax error, " + lexer.tokenName());
                        }
                        if (parentIsArray2) {
                            this.popContext();
                            continue;
                        }
                        this.setContext(context);
                        continue;
                    }
                    else {
                        lexer.nextToken();
                        value = this.parse();
                        map.put(key, value);
                        if (lexer.token() == 13) {
                            lexer.nextToken();
                            return object;
                        }
                        if (lexer.token() == 16) {
                            continue;
                        }
                        throw new JSONException("syntax error, position at " + lexer.pos() + ", name " + key);
                    }
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch == ',') {
                        lexer.next();
                    }
                    else {
                        if (ch == '}') {
                            lexer.next();
                            lexer.resetStringPosition();
                            lexer.nextToken();
                            this.setContext(value, key);
                            return object;
                        }
                        throw new JSONException("syntax error, position at " + lexer.pos() + ", name " + key);
                    }
                }
            }
        }
        finally {
            this.setContext(context);
        }
    }
    
    public ParserConfig getConfig() {
        return this.config;
    }
    
    public void setConfig(final ParserConfig config) {
        this.config = config;
    }
    
    public <T> T parseObject(final Class<T> clazz) {
        return this.parseObject(clazz, null);
    }
    
    public <T> T parseObject(final Type type) {
        return this.parseObject(type, null);
    }
    
    public <T> T parseObject(final Type type, final Object fieldName) {
        final int token = this.lexer.token();
        if (token == 8) {
            this.lexer.nextToken();
            return null;
        }
        if (token == 4) {
            if (type == byte[].class) {
                final byte[] bytes = this.lexer.bytesValue();
                this.lexer.nextToken();
                return (T)(Object)bytes;
            }
            if (type == char[].class) {
                final String strVal = this.lexer.stringVal();
                this.lexer.nextToken();
                return (T)(Object)strVal.toCharArray();
            }
        }
        final ObjectDeserializer deserializer = this.config.getDeserializer(type);
        try {
            if (deserializer.getClass() != JavaBeanDeserializer.class) {
                return deserializer.deserialze(this, type, fieldName);
            }
            if (this.lexer.token() != 12 && this.lexer.token() != 14) {
                throw new JSONException("syntax error,except start with { or [,but actually start with " + this.lexer.tokenName());
            }
            return ((JavaBeanDeserializer)deserializer).deserialze(this, type, fieldName, 0);
        }
        catch (JSONException e) {
            throw e;
        }
        catch (Throwable e2) {
            throw new JSONException(e2.getMessage(), e2);
        }
    }
    
    public <T> List<T> parseArray(final Class<T> clazz) {
        final List<T> array = new ArrayList<T>();
        this.parseArray(clazz, array);
        return array;
    }
    
    public void parseArray(final Class<?> clazz, final Collection array) {
        this.parseArray((Type)clazz, array);
    }
    
    public void parseArray(final Type type, final Collection array) {
        this.parseArray(type, array, null);
    }
    
    public void parseArray(final Type type, final Collection array, final Object fieldName) {
        int token = this.lexer.token();
        if (token == 21 || token == 22) {
            this.lexer.nextToken();
            token = this.lexer.token();
        }
        if (token != 14) {
            throw new JSONException("expect '[', but " + JSONToken.name(token) + ", " + this.lexer.info());
        }
        ObjectDeserializer deserializer = null;
        if (Integer.TYPE == type) {
            deserializer = IntegerCodec.instance;
            this.lexer.nextToken(2);
        }
        else if (String.class == type) {
            deserializer = StringCodec.instance;
            this.lexer.nextToken(4);
        }
        else {
            deserializer = this.config.getDeserializer(type);
            this.lexer.nextToken(deserializer.getFastMatchToken());
        }
        final ParseContext context = this.context;
        this.setContext(array, fieldName);
        try {
            int i = 0;
            while (true) {
                if (this.lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                    while (this.lexer.token() == 16) {
                        this.lexer.nextToken();
                    }
                }
                if (this.lexer.token() == 15) {
                    break;
                }
                if (Integer.TYPE == type) {
                    final Object val = IntegerCodec.instance.deserialze(this, null, null);
                    array.add(val);
                }
                else if (String.class == type) {
                    String value;
                    if (this.lexer.token() == 4) {
                        value = this.lexer.stringVal();
                        this.lexer.nextToken(16);
                    }
                    else {
                        final Object obj = this.parse();
                        if (obj == null) {
                            value = null;
                        }
                        else {
                            value = obj.toString();
                        }
                    }
                    array.add(value);
                }
                else {
                    Object val;
                    if (this.lexer.token() == 8) {
                        this.lexer.nextToken();
                        val = null;
                    }
                    else {
                        val = deserializer.deserialze(this, type, i);
                    }
                    array.add(val);
                    this.checkListResolve(array);
                }
                if (this.lexer.token() == 16) {
                    this.lexer.nextToken(deserializer.getFastMatchToken());
                }
                ++i;
            }
        }
        finally {
            this.setContext(context);
        }
        this.lexer.nextToken(16);
    }
    
    public Object[] parseArray(final Type[] types) {
        if (this.lexer.token() == 8) {
            this.lexer.nextToken(16);
            return null;
        }
        if (this.lexer.token() != 14) {
            throw new JSONException("syntax error : " + this.lexer.tokenName());
        }
        final Object[] list = new Object[types.length];
        if (types.length == 0) {
            this.lexer.nextToken(15);
            if (this.lexer.token() != 15) {
                throw new JSONException("syntax error");
            }
            this.lexer.nextToken(16);
            return new Object[0];
        }
        else {
            this.lexer.nextToken(2);
            for (int i = 0; i < types.length; ++i) {
                Object value;
                if (this.lexer.token() == 8) {
                    value = null;
                    this.lexer.nextToken(16);
                }
                else {
                    final Type type = types[i];
                    if (type == Integer.TYPE || type == Integer.class) {
                        if (this.lexer.token() == 2) {
                            value = this.lexer.intValue();
                            this.lexer.nextToken(16);
                        }
                        else {
                            value = this.parse();
                            value = TypeUtils.cast(value, type, this.config);
                        }
                    }
                    else if (type == String.class) {
                        if (this.lexer.token() == 4) {
                            value = this.lexer.stringVal();
                            this.lexer.nextToken(16);
                        }
                        else {
                            value = this.parse();
                            value = TypeUtils.cast(value, type, this.config);
                        }
                    }
                    else {
                        boolean isArray = false;
                        Class<?> componentType = null;
                        if (i == types.length - 1 && type instanceof Class) {
                            final Class<?> clazz = (Class<?>)type;
                            if ((clazz != byte[].class && clazz != char[].class) || this.lexer.token() != 4) {
                                isArray = clazz.isArray();
                                componentType = clazz.getComponentType();
                            }
                        }
                        if (isArray && this.lexer.token() != 14) {
                            final List<Object> varList = new ArrayList<Object>();
                            final ObjectDeserializer deserializer = this.config.getDeserializer(componentType);
                            final int fastMatch = deserializer.getFastMatchToken();
                            if (this.lexer.token() != 15) {
                                while (true) {
                                    final Object item = deserializer.deserialze(this, type, null);
                                    varList.add(item);
                                    if (this.lexer.token() != 16) {
                                        break;
                                    }
                                    this.lexer.nextToken(fastMatch);
                                }
                                if (this.lexer.token() != 15) {
                                    throw new JSONException("syntax error :" + JSONToken.name(this.lexer.token()));
                                }
                            }
                            value = TypeUtils.cast(varList, type, this.config);
                        }
                        else {
                            final ObjectDeserializer deserializer2 = this.config.getDeserializer(type);
                            value = deserializer2.deserialze(this, type, i);
                        }
                    }
                }
                list[i] = value;
                if (this.lexer.token() == 15) {
                    break;
                }
                if (this.lexer.token() != 16) {
                    throw new JSONException("syntax error :" + JSONToken.name(this.lexer.token()));
                }
                if (i == types.length - 1) {
                    this.lexer.nextToken(15);
                }
                else {
                    this.lexer.nextToken(2);
                }
            }
            if (this.lexer.token() != 15) {
                throw new JSONException("syntax error");
            }
            this.lexer.nextToken(16);
            return list;
        }
    }
    
    public void parseObject(final Object object) {
        final Class<?> clazz = object.getClass();
        JavaBeanDeserializer beanDeser = null;
        final ObjectDeserializer deserializer = this.config.getDeserializer(clazz);
        if (deserializer instanceof JavaBeanDeserializer) {
            beanDeser = (JavaBeanDeserializer)deserializer;
        }
        if (this.lexer.token() != 12 && this.lexer.token() != 16) {
            throw new JSONException("syntax error, expect {, actual " + this.lexer.tokenName());
        }
        while (true) {
            final String key = this.lexer.scanSymbol(this.symbolTable);
            if (key == null) {
                if (this.lexer.token() == 13) {
                    this.lexer.nextToken(16);
                    return;
                }
                if (this.lexer.token() == 16 && this.lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                    continue;
                }
            }
            FieldDeserializer fieldDeser = null;
            if (beanDeser != null) {
                fieldDeser = beanDeser.getFieldDeserializer(key);
            }
            if (fieldDeser == null) {
                if (!this.lexer.isEnabled(Feature.IgnoreNotMatch)) {
                    throw new JSONException("setter not found, class " + clazz.getName() + ", property " + key);
                }
                this.lexer.nextTokenWithColon();
                this.parse();
                if (this.lexer.token() == 13) {
                    this.lexer.nextToken();
                    return;
                }
                continue;
            }
            else {
                final Class<?> fieldClass = fieldDeser.fieldInfo.fieldClass;
                final Type fieldType = fieldDeser.fieldInfo.fieldType;
                Object fieldValue;
                if (fieldClass == Integer.TYPE) {
                    this.lexer.nextTokenWithColon(2);
                    fieldValue = IntegerCodec.instance.deserialze(this, fieldType, null);
                }
                else if (fieldClass == String.class) {
                    this.lexer.nextTokenWithColon(4);
                    fieldValue = StringCodec.deserialze(this);
                }
                else if (fieldClass == Long.TYPE) {
                    this.lexer.nextTokenWithColon(2);
                    fieldValue = LongCodec.instance.deserialze(this, fieldType, null);
                }
                else {
                    final ObjectDeserializer fieldValueDeserializer = this.config.getDeserializer(fieldClass, fieldType);
                    this.lexer.nextTokenWithColon(fieldValueDeserializer.getFastMatchToken());
                    fieldValue = fieldValueDeserializer.deserialze(this, fieldType, null);
                }
                fieldDeser.setValue(object, fieldValue);
                if (this.lexer.token() == 16) {
                    continue;
                }
                if (this.lexer.token() == 13) {
                    this.lexer.nextToken(16);
                    return;
                }
                continue;
            }
        }
    }
    
    public Object parseArrayWithType(final Type collectionType) {
        if (this.lexer.token() == 8) {
            this.lexer.nextToken();
            return null;
        }
        final Type[] actualTypes = ((ParameterizedType)collectionType).getActualTypeArguments();
        if (actualTypes.length != 1) {
            throw new JSONException("not support type " + collectionType);
        }
        final Type actualTypeArgument = actualTypes[0];
        if (actualTypeArgument instanceof Class) {
            final List<Object> array = new ArrayList<Object>();
            this.parseArray((Class<?>)actualTypeArgument, array);
            return array;
        }
        if (actualTypeArgument instanceof WildcardType) {
            final WildcardType wildcardType = (WildcardType)actualTypeArgument;
            final Type upperBoundType = wildcardType.getUpperBounds()[0];
            if (!Object.class.equals(upperBoundType)) {
                final List<Object> array2 = new ArrayList<Object>();
                this.parseArray((Class<?>)upperBoundType, array2);
                return array2;
            }
            if (wildcardType.getLowerBounds().length == 0) {
                return this.parse();
            }
            throw new JSONException("not support type : " + collectionType);
        }
        else {
            if (actualTypeArgument instanceof TypeVariable) {
                final TypeVariable<?> typeVariable = (TypeVariable<?>)actualTypeArgument;
                final Type[] bounds = typeVariable.getBounds();
                if (bounds.length != 1) {
                    throw new JSONException("not support : " + typeVariable);
                }
                final Type boundType = bounds[0];
                if (boundType instanceof Class) {
                    final List<Object> array3 = new ArrayList<Object>();
                    this.parseArray((Class<?>)boundType, array3);
                    return array3;
                }
            }
            if (actualTypeArgument instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = (ParameterizedType)actualTypeArgument;
                final List<Object> array4 = new ArrayList<Object>();
                this.parseArray(parameterizedType, array4);
                return array4;
            }
            throw new JSONException("TODO : " + collectionType);
        }
    }
    
    public void acceptType(final String typeName) {
        final JSONLexer lexer = this.lexer;
        lexer.nextTokenWithColon();
        if (lexer.token() != 4) {
            throw new JSONException("type not match error");
        }
        if (typeName.equals(lexer.stringVal())) {
            lexer.nextToken();
            if (lexer.token() == 16) {
                lexer.nextToken();
            }
            return;
        }
        throw new JSONException("type not match error");
    }
    
    public int getResolveStatus() {
        return this.resolveStatus;
    }
    
    public void setResolveStatus(final int resolveStatus) {
        this.resolveStatus = resolveStatus;
    }
    
    public Object getObject(final String path) {
        for (int i = 0; i < this.contextArrayIndex; ++i) {
            if (path.equals(this.contextArray[i].toString())) {
                return this.contextArray[i].object;
            }
        }
        return null;
    }
    
    public void checkListResolve(final Collection array) {
        if (this.resolveStatus == 1) {
            if (array instanceof List) {
                final int index = array.size() - 1;
                final List list = (List)array;
                final ResolveTask task = this.getLastResolveTask();
                task.fieldDeserializer = new ResolveFieldDeserializer(this, list, index);
                task.ownerContext = this.context;
                this.setResolveStatus(0);
            }
            else {
                final ResolveTask task2 = this.getLastResolveTask();
                task2.fieldDeserializer = new ResolveFieldDeserializer(array);
                task2.ownerContext = this.context;
                this.setResolveStatus(0);
            }
        }
    }
    
    public void checkMapResolve(final Map object, final Object fieldName) {
        if (this.resolveStatus == 1) {
            final ResolveFieldDeserializer fieldResolver = new ResolveFieldDeserializer(object, fieldName);
            final ResolveTask task = this.getLastResolveTask();
            task.fieldDeserializer = fieldResolver;
            task.ownerContext = this.context;
            this.setResolveStatus(0);
        }
    }
    
    public Object parseObject(final Map object) {
        return this.parseObject(object, null);
    }
    
    public JSONObject parseObject() {
        final JSONObject object = new JSONObject(this.lexer.isEnabled(Feature.OrderedField));
        final Object parsedObject = this.parseObject(object);
        if (parsedObject instanceof JSONObject) {
            return (JSONObject)parsedObject;
        }
        if (parsedObject == null) {
            return null;
        }
        return new JSONObject((Map<String, Object>)parsedObject);
    }
    
    public final void parseArray(final Collection array) {
        this.parseArray(array, null);
    }
    
    public final void parseArray(final Collection array, final Object fieldName) {
        final JSONLexer lexer = this.lexer;
        if (lexer.token() == 21 || lexer.token() == 22) {
            lexer.nextToken();
        }
        if (lexer.token() != 14) {
            throw new JSONException("syntax error, expect [, actual " + JSONToken.name(lexer.token()) + ", pos " + lexer.pos() + ", fieldName " + fieldName);
        }
        lexer.nextToken(4);
        if (this.context != null && this.context.level > 512) {
            throw new JSONException("array level > 512");
        }
        final ParseContext context = this.context;
        this.setContext(array, fieldName);
        try {
            int i = 0;
            while (true) {
                if (lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                    while (lexer.token() == 16) {
                        lexer.nextToken();
                    }
                }
                Object value = null;
                switch (lexer.token()) {
                    case 2: {
                        value = lexer.integerValue();
                        lexer.nextToken(16);
                        break;
                    }
                    case 3: {
                        if (lexer.isEnabled(Feature.UseBigDecimal)) {
                            value = lexer.decimalValue(true);
                        }
                        else {
                            value = lexer.decimalValue(false);
                        }
                        lexer.nextToken(16);
                        break;
                    }
                    case 4: {
                        final String stringLiteral = lexer.stringVal();
                        lexer.nextToken(16);
                        if (lexer.isEnabled(Feature.AllowISO8601DateFormat)) {
                            final JSONScanner iso8601Lexer = new JSONScanner(stringLiteral);
                            if (iso8601Lexer.scanISO8601DateIfMatch()) {
                                value = iso8601Lexer.getCalendar().getTime();
                            }
                            else {
                                value = stringLiteral;
                            }
                            iso8601Lexer.close();
                            break;
                        }
                        value = stringLiteral;
                        break;
                    }
                    case 6: {
                        value = Boolean.TRUE;
                        lexer.nextToken(16);
                        break;
                    }
                    case 7: {
                        value = Boolean.FALSE;
                        lexer.nextToken(16);
                        break;
                    }
                    case 12: {
                        final JSONObject object = new JSONObject(lexer.isEnabled(Feature.OrderedField));
                        value = this.parseObject(object, i);
                        break;
                    }
                    case 14: {
                        final Collection items = new JSONArray();
                        this.parseArray(items, i);
                        if (lexer.isEnabled(Feature.UseObjectArray)) {
                            value = items.toArray();
                            break;
                        }
                        value = items;
                        break;
                    }
                    case 8: {
                        value = null;
                        lexer.nextToken(4);
                        break;
                    }
                    case 23: {
                        value = null;
                        lexer.nextToken(4);
                        break;
                    }
                    case 15: {
                        lexer.nextToken(16);
                        return;
                    }
                    case 20: {
                        throw new JSONException("unclosed jsonArray");
                    }
                    default: {
                        value = this.parse();
                        break;
                    }
                }
                array.add(value);
                this.checkListResolve(array);
                if (lexer.token() == 16) {
                    lexer.nextToken(4);
                }
                ++i;
            }
        }
        finally {
            this.setContext(context);
        }
    }
    
    public ParseContext getContext() {
        return this.context;
    }
    
    public List<ResolveTask> getResolveTaskList() {
        if (this.resolveTaskList == null) {
            this.resolveTaskList = new ArrayList<ResolveTask>(2);
        }
        return this.resolveTaskList;
    }
    
    public void addResolveTask(final ResolveTask task) {
        if (this.resolveTaskList == null) {
            this.resolveTaskList = new ArrayList<ResolveTask>(2);
        }
        this.resolveTaskList.add(task);
    }
    
    public ResolveTask getLastResolveTask() {
        return this.resolveTaskList.get(this.resolveTaskList.size() - 1);
    }
    
    public List<ExtraProcessor> getExtraProcessors() {
        if (this.extraProcessors == null) {
            this.extraProcessors = new ArrayList<ExtraProcessor>(2);
        }
        return this.extraProcessors;
    }
    
    public List<ExtraTypeProvider> getExtraTypeProviders() {
        if (this.extraTypeProviders == null) {
            this.extraTypeProviders = new ArrayList<ExtraTypeProvider>(2);
        }
        return this.extraTypeProviders;
    }
    
    public FieldTypeResolver getFieldTypeResolver() {
        return this.fieldTypeResolver;
    }
    
    public void setFieldTypeResolver(final FieldTypeResolver fieldTypeResolver) {
        this.fieldTypeResolver = fieldTypeResolver;
    }
    
    public void setContext(final ParseContext context) {
        if (this.lexer.isEnabled(Feature.DisableCircularReferenceDetect)) {
            return;
        }
        this.context = context;
    }
    
    public void popContext() {
        if (this.lexer.isEnabled(Feature.DisableCircularReferenceDetect)) {
            return;
        }
        this.context = this.context.parent;
        if (this.contextArrayIndex <= 0) {
            return;
        }
        --this.contextArrayIndex;
        this.contextArray[this.contextArrayIndex] = null;
    }
    
    public ParseContext setContext(final Object object, final Object fieldName) {
        if (this.lexer.isEnabled(Feature.DisableCircularReferenceDetect)) {
            return null;
        }
        return this.setContext(this.context, object, fieldName);
    }
    
    public ParseContext setContext(final ParseContext parent, final Object object, final Object fieldName) {
        if (this.lexer.isEnabled(Feature.DisableCircularReferenceDetect)) {
            return null;
        }
        this.addContext(this.context = new ParseContext(parent, object, fieldName));
        return this.context;
    }
    
    private void addContext(final ParseContext context) {
        final int i = this.contextArrayIndex++;
        if (this.contextArray == null) {
            this.contextArray = new ParseContext[8];
        }
        else if (i >= this.contextArray.length) {
            final int newLen = this.contextArray.length * 3 / 2;
            final ParseContext[] newArray = new ParseContext[newLen];
            System.arraycopy(this.contextArray, 0, newArray, 0, this.contextArray.length);
            this.contextArray = newArray;
        }
        this.contextArray[i] = context;
    }
    
    public Object parse() {
        return this.parse(null);
    }
    
    public Object parseKey() {
        if (this.lexer.token() == 18) {
            final String value = this.lexer.stringVal();
            this.lexer.nextToken(16);
            return value;
        }
        return this.parse(null);
    }
    
    public Object parse(final Object fieldName) {
        final JSONLexer lexer = this.lexer;
        switch (lexer.token()) {
            case 21: {
                lexer.nextToken();
                final HashSet<Object> set = new HashSet<Object>();
                this.parseArray(set, fieldName);
                return set;
            }
            case 22: {
                lexer.nextToken();
                final TreeSet<Object> treeSet = new TreeSet<Object>();
                this.parseArray(treeSet, fieldName);
                return treeSet;
            }
            case 14: {
                final JSONArray array = new JSONArray();
                this.parseArray(array, fieldName);
                if (lexer.isEnabled(Feature.UseObjectArray)) {
                    return array.toArray();
                }
                return array;
            }
            case 12: {
                final JSONObject object = new JSONObject(lexer.isEnabled(Feature.OrderedField));
                return this.parseObject(object, fieldName);
            }
            case 2: {
                final Number intValue = lexer.integerValue();
                lexer.nextToken();
                return intValue;
            }
            case 3: {
                final Object value = lexer.decimalValue(lexer.isEnabled(Feature.UseBigDecimal));
                lexer.nextToken();
                return value;
            }
            case 4: {
                final String stringLiteral = lexer.stringVal();
                lexer.nextToken(16);
                if (lexer.isEnabled(Feature.AllowISO8601DateFormat)) {
                    final JSONScanner iso8601Lexer = new JSONScanner(stringLiteral);
                    try {
                        if (iso8601Lexer.scanISO8601DateIfMatch()) {
                            return iso8601Lexer.getCalendar().getTime();
                        }
                    }
                    finally {
                        iso8601Lexer.close();
                    }
                }
                return stringLiteral;
            }
            case 8: {
                lexer.nextToken();
                return null;
            }
            case 23: {
                lexer.nextToken();
                return null;
            }
            case 6: {
                lexer.nextToken();
                return Boolean.TRUE;
            }
            case 7: {
                lexer.nextToken();
                return Boolean.FALSE;
            }
            case 9: {
                lexer.nextToken(18);
                if (lexer.token() != 18) {
                    throw new JSONException("syntax error");
                }
                lexer.nextToken(10);
                this.accept(10);
                final long time = lexer.integerValue().longValue();
                this.accept(2);
                this.accept(11);
                return new Date(time);
            }
            case 20: {
                if (lexer.isBlankInput()) {
                    return null;
                }
                throw new JSONException("unterminated json string, " + lexer.info());
            }
            case 26: {
                final byte[] bytes = lexer.bytesValue();
                lexer.nextToken();
                return bytes;
            }
            case 18: {
                final String identifier = lexer.stringVal();
                if ("NaN".equals(identifier)) {
                    lexer.nextToken();
                    return null;
                }
                throw new JSONException("syntax error, " + lexer.info());
            }
            default: {
                throw new JSONException("syntax error, " + lexer.info());
            }
        }
    }
    
    public void config(final Feature feature, final boolean state) {
        this.lexer.config(feature, state);
    }
    
    public boolean isEnabled(final Feature feature) {
        return this.lexer.isEnabled(feature);
    }
    
    public JSONLexer getLexer() {
        return this.lexer;
    }
    
    public final void accept(final int token) {
        final JSONLexer lexer = this.lexer;
        if (lexer.token() == token) {
            lexer.nextToken();
            return;
        }
        throw new JSONException("syntax error, expect " + JSONToken.name(token) + ", actual " + JSONToken.name(lexer.token()));
    }
    
    public final void accept(final int token, final int nextExpectToken) {
        final JSONLexer lexer = this.lexer;
        if (lexer.token() == token) {
            lexer.nextToken(nextExpectToken);
        }
        else {
            this.throwException(token);
        }
    }
    
    public void throwException(final int token) {
        throw new JSONException("syntax error, expect " + JSONToken.name(token) + ", actual " + JSONToken.name(this.lexer.token()));
    }
    
    @Override
    public void close() {
        final JSONLexer lexer = this.lexer;
        try {
            if (lexer.isEnabled(Feature.AutoCloseSource) && lexer.token() != 20) {
                throw new JSONException("not close json text, token : " + JSONToken.name(lexer.token()));
            }
        }
        finally {
            lexer.close();
        }
    }
    
    public Object resolveReference(final String ref) {
        if (this.contextArray == null) {
            return null;
        }
        for (int i = 0; i < this.contextArray.length && i < this.contextArrayIndex; ++i) {
            final ParseContext context = this.contextArray[i];
            if (context.toString().equals(ref)) {
                return context.object;
            }
        }
        return null;
    }
    
    public void handleResovleTask(final Object value) {
        if (this.resolveTaskList == null) {
            return;
        }
        for (int i = 0, size = this.resolveTaskList.size(); i < size; ++i) {
            final ResolveTask task = this.resolveTaskList.get(i);
            final String ref = task.referenceValue;
            Object object = null;
            if (task.ownerContext != null) {
                object = task.ownerContext.object;
            }
            Object refValue;
            if (ref.startsWith("$")) {
                refValue = this.getObject(ref);
                if (refValue == null) {
                    try {
                        final JSONPath jsonpath = JSONPath.compile(ref);
                        if (jsonpath.isRef()) {
                            refValue = jsonpath.eval(value);
                        }
                    }
                    catch (JSONPathException ex) {}
                }
            }
            else {
                refValue = task.context.object;
            }
            final FieldDeserializer fieldDeser = task.fieldDeserializer;
            if (fieldDeser != null) {
                if (refValue != null && refValue.getClass() == JSONObject.class && fieldDeser.fieldInfo != null && !Map.class.isAssignableFrom(fieldDeser.fieldInfo.fieldClass)) {
                    final Object root = this.contextArray[0].object;
                    final JSONPath jsonpath2 = JSONPath.compile(ref);
                    if (jsonpath2.isRef()) {
                        refValue = jsonpath2.eval(root);
                    }
                }
                fieldDeser.setValue(object, refValue);
            }
        }
    }
    
    public void parseExtra(final Object object, final String key) {
        final JSONLexer lexer = this.lexer;
        lexer.nextTokenWithColon();
        Type type = null;
        if (this.extraTypeProviders != null) {
            for (final ExtraTypeProvider extraProvider : this.extraTypeProviders) {
                type = extraProvider.getExtraType(object, key);
            }
        }
        final Object value = (type == null) ? this.parse() : this.parseObject(type);
        if (object instanceof ExtraProcessable) {
            final ExtraProcessable extraProcessable = (ExtraProcessable)object;
            extraProcessable.processExtra(key, value);
            return;
        }
        if (this.extraProcessors != null) {
            for (final ExtraProcessor process : this.extraProcessors) {
                process.processExtra(object, key, value);
            }
        }
        if (this.resolveStatus == 1) {
            this.resolveStatus = 0;
        }
    }
    
    public Object parse(final PropertyProcessable object, final Object fieldName) {
        if (this.lexer.token() != 12) {
            String msg = "syntax error, expect {, actual " + this.lexer.tokenName();
            if (fieldName instanceof String) {
                msg += ", fieldName ";
                msg += fieldName;
            }
            msg += ", ";
            msg += this.lexer.info();
            final JSONArray array = new JSONArray();
            this.parseArray(array, fieldName);
            if (array.size() == 1) {
                final Object first = array.get(0);
                if (first instanceof JSONObject) {
                    return first;
                }
            }
            throw new JSONException(msg);
        }
        final ParseContext context = this.context;
        try {
            int i = 0;
            while (true) {
                this.lexer.skipWhitespace();
                char ch = this.lexer.getCurrent();
                if (this.lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                    while (ch == ',') {
                        this.lexer.next();
                        this.lexer.skipWhitespace();
                        ch = this.lexer.getCurrent();
                    }
                }
                String key;
                if (ch == '\"') {
                    key = this.lexer.scanSymbol(this.symbolTable, '\"');
                    this.lexer.skipWhitespace();
                    ch = this.lexer.getCurrent();
                    if (ch != ':') {
                        throw new JSONException("expect ':' at " + this.lexer.pos());
                    }
                }
                else {
                    if (ch == '}') {
                        this.lexer.next();
                        this.lexer.resetStringPosition();
                        this.lexer.nextToken(16);
                        return object;
                    }
                    if (ch == '\'') {
                        if (!this.lexer.isEnabled(Feature.AllowSingleQuotes)) {
                            throw new JSONException("syntax error");
                        }
                        key = this.lexer.scanSymbol(this.symbolTable, '\'');
                        this.lexer.skipWhitespace();
                        ch = this.lexer.getCurrent();
                        if (ch != ':') {
                            throw new JSONException("expect ':' at " + this.lexer.pos());
                        }
                    }
                    else {
                        if (!this.lexer.isEnabled(Feature.AllowUnQuotedFieldNames)) {
                            throw new JSONException("syntax error");
                        }
                        key = this.lexer.scanSymbolUnQuoted(this.symbolTable);
                        this.lexer.skipWhitespace();
                        ch = this.lexer.getCurrent();
                        if (ch != ':') {
                            throw new JSONException("expect ':' at " + this.lexer.pos() + ", actual " + ch);
                        }
                    }
                }
                this.lexer.next();
                this.lexer.skipWhitespace();
                ch = this.lexer.getCurrent();
                this.lexer.resetStringPosition();
                if (key == JSON.DEFAULT_TYPE_KEY && !this.lexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                    final String typeName = this.lexer.scanSymbol(this.symbolTable, '\"');
                    final Class<?> clazz = this.config.checkAutoType(typeName, null, this.lexer.getFeatures());
                    if (!Map.class.isAssignableFrom(clazz)) {
                        final ObjectDeserializer deserializer = this.config.getDeserializer(clazz);
                        this.lexer.nextToken(16);
                        this.setResolveStatus(2);
                        if (context != null && !(fieldName instanceof Integer)) {
                            this.popContext();
                        }
                        return deserializer.deserialze(this, clazz, fieldName);
                    }
                    this.lexer.nextToken(16);
                    if (this.lexer.token() == 13) {
                        this.lexer.nextToken(16);
                        return object;
                    }
                }
                else {
                    this.lexer.nextToken();
                    if (i != 0) {
                        this.setContext(context);
                    }
                    final Type valueType = object.getType(key);
                    Object value;
                    if (this.lexer.token() == 8) {
                        value = null;
                        this.lexer.nextToken();
                    }
                    else {
                        value = this.parseObject(valueType, key);
                    }
                    object.apply(key, value);
                    this.setContext(context, value, key);
                    this.setContext(context);
                    final int tok = this.lexer.token();
                    if (tok == 20 || tok == 15) {
                        return object;
                    }
                    if (tok == 13) {
                        this.lexer.nextToken();
                        return object;
                    }
                }
                ++i;
            }
        }
        finally {
            this.setContext(context);
        }
    }
    
    static {
        primitiveClasses = new HashSet<Class<?>>();
        final Class[] array;
        final Class<?>[] classes = (Class<?>[])(array = new Class[] { Boolean.TYPE, Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE, Boolean.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, BigInteger.class, BigDecimal.class, String.class });
        for (final Class<?> clazz : array) {
            DefaultJSONParser.primitiveClasses.add(clazz);
        }
    }
    
    public static class ResolveTask
    {
        public final ParseContext context;
        public final String referenceValue;
        public FieldDeserializer fieldDeserializer;
        public ParseContext ownerContext;
        
        public ResolveTask(final ParseContext context, final String referenceValue) {
            this.context = context;
            this.referenceValue = referenceValue;
        }
    }
}
