package com.alibaba.fastjson.parser.deserializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Method;
import java.util.Iterator;
import com.alibaba.fastjson.annotation.JSONField;
import java.util.List;
import com.alibaba.fastjson.parser.JSONToken;
import java.math.BigInteger;
import com.alibaba.fastjson.parser.JSONLexerBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.JSONLexer;
import java.math.BigDecimal;
import java.util.Date;
import com.alibaba.fastjson.parser.ParseContext;
import java.lang.reflect.Constructor;
import com.alibaba.fastjson.parser.Feature;
import java.util.Collection;
import com.alibaba.fastjson.JSONException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.util.Arrays;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.util.FieldInfo;
import java.util.HashMap;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.ParserConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import com.alibaba.fastjson.util.JavaBeanInfo;

public class JavaBeanDeserializer implements ObjectDeserializer
{
    private final FieldDeserializer[] fieldDeserializers;
    protected final FieldDeserializer[] sortedFieldDeserializers;
    protected final Class<?> clazz;
    public final JavaBeanInfo beanInfo;
    private ConcurrentMap<String, Object> extraFieldDeserializers;
    private final Map<String, FieldDeserializer> alterNameFieldDeserializers;
    private Map<String, FieldDeserializer> fieldDeserializerMap;
    private transient long[] smartMatchHashArray;
    private transient short[] smartMatchHashArrayMapping;
    private transient long[] hashArray;
    private transient short[] hashArrayMapping;
    
    public JavaBeanDeserializer(final ParserConfig config, final Class<?> clazz) {
        this(config, clazz, clazz);
    }
    
    public JavaBeanDeserializer(final ParserConfig config, final Class<?> clazz, final Type type) {
        this(config, JavaBeanInfo.build(clazz, type, config.propertyNamingStrategy, config.fieldBased, config.compatibleWithJavaBean, config.isJacksonCompatible()));
    }
    
    public JavaBeanDeserializer(final ParserConfig config, final JavaBeanInfo beanInfo) {
        this.clazz = beanInfo.clazz;
        this.beanInfo = beanInfo;
        Map<String, FieldDeserializer> alterNameFieldDeserializers = null;
        this.sortedFieldDeserializers = new FieldDeserializer[beanInfo.sortedFields.length];
        for (int i = 0, size = beanInfo.sortedFields.length; i < size; ++i) {
            final FieldInfo fieldInfo = beanInfo.sortedFields[i];
            final FieldDeserializer fieldDeserializer = config.createFieldDeserializer(config, beanInfo, fieldInfo);
            this.sortedFieldDeserializers[i] = fieldDeserializer;
            if (size > 128) {
                if (this.fieldDeserializerMap == null) {
                    this.fieldDeserializerMap = new HashMap<String, FieldDeserializer>();
                }
                this.fieldDeserializerMap.put(fieldInfo.name, fieldDeserializer);
            }
            for (final String name : fieldInfo.alternateNames) {
                if (alterNameFieldDeserializers == null) {
                    alterNameFieldDeserializers = new HashMap<String, FieldDeserializer>();
                }
                alterNameFieldDeserializers.put(name, fieldDeserializer);
            }
        }
        this.alterNameFieldDeserializers = alterNameFieldDeserializers;
        this.fieldDeserializers = new FieldDeserializer[beanInfo.fields.length];
        for (int i = 0, size = beanInfo.fields.length; i < size; ++i) {
            final FieldInfo fieldInfo = beanInfo.fields[i];
            final FieldDeserializer fieldDeserializer = this.getFieldDeserializer(fieldInfo.name);
            this.fieldDeserializers[i] = fieldDeserializer;
        }
    }
    
    public FieldDeserializer getFieldDeserializer(final String key) {
        return this.getFieldDeserializer(key, null);
    }
    
    public FieldDeserializer getFieldDeserializer(final String key, final int[] setFlags) {
        if (key == null) {
            return null;
        }
        if (this.fieldDeserializerMap != null) {
            final FieldDeserializer fieldDeserializer = this.fieldDeserializerMap.get(key);
            if (fieldDeserializer != null) {
                return fieldDeserializer;
            }
        }
        int low = 0;
        int high = this.sortedFieldDeserializers.length - 1;
        while (low <= high) {
            final int mid = low + high >>> 1;
            final String fieldName = this.sortedFieldDeserializers[mid].fieldInfo.name;
            final int cmp = fieldName.compareTo(key);
            if (cmp < 0) {
                low = mid + 1;
            }
            else if (cmp > 0) {
                high = mid - 1;
            }
            else {
                if (isSetFlag(mid, setFlags)) {
                    return null;
                }
                return this.sortedFieldDeserializers[mid];
            }
        }
        if (this.alterNameFieldDeserializers != null) {
            return this.alterNameFieldDeserializers.get(key);
        }
        return null;
    }
    
    public FieldDeserializer getFieldDeserializer(final long hash) {
        if (this.hashArray == null) {
            final long[] hashArray = new long[this.sortedFieldDeserializers.length];
            for (int i = 0; i < this.sortedFieldDeserializers.length; ++i) {
                hashArray[i] = TypeUtils.fnv1a_64(this.sortedFieldDeserializers[i].fieldInfo.name);
            }
            Arrays.sort(hashArray);
            this.hashArray = hashArray;
        }
        final int pos = Arrays.binarySearch(this.hashArray, hash);
        if (pos < 0) {
            return null;
        }
        if (this.hashArrayMapping == null) {
            final short[] mapping = new short[this.hashArray.length];
            Arrays.fill(mapping, (short)(-1));
            for (int j = 0; j < this.sortedFieldDeserializers.length; ++j) {
                final int p = Arrays.binarySearch(this.hashArray, TypeUtils.fnv1a_64(this.sortedFieldDeserializers[j].fieldInfo.name));
                if (p >= 0) {
                    mapping[p] = (short)j;
                }
            }
            this.hashArrayMapping = mapping;
        }
        final int setterIndex = this.hashArrayMapping[pos];
        if (setterIndex != -1) {
            return this.sortedFieldDeserializers[setterIndex];
        }
        return null;
    }
    
    static boolean isSetFlag(final int i, final int[] setFlags) {
        if (setFlags == null) {
            return false;
        }
        final int flagIndex = i / 32;
        final int bitIndex = i % 32;
        return flagIndex < setFlags.length && (setFlags[flagIndex] & 1 << bitIndex) != 0x0;
    }
    
    public Object createInstance(final DefaultJSONParser parser, final Type type) {
        if (type instanceof Class && this.clazz.isInterface()) {
            final Class<?> clazz = (Class<?>)type;
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            final JSONObject obj = new JSONObject();
            final Object proxy = Proxy.newProxyInstance(loader, new Class[] { clazz }, obj);
            return proxy;
        }
        if (this.beanInfo.defaultConstructor == null && this.beanInfo.factoryMethod == null) {
            return null;
        }
        if (this.beanInfo.factoryMethod != null && this.beanInfo.defaultConstructorParameterSize > 0) {
            return null;
        }
        Object object;
        try {
            final Constructor<?> constructor = this.beanInfo.defaultConstructor;
            if (this.beanInfo.defaultConstructorParameterSize == 0) {
                if (constructor != null) {
                    object = constructor.newInstance(new Object[0]);
                }
                else {
                    object = this.beanInfo.factoryMethod.invoke(null, new Object[0]);
                }
            }
            else {
                final ParseContext context = parser.getContext();
                if (context == null || context.object == null) {
                    throw new JSONException("can't create non-static inner class instance.");
                }
                if (!(type instanceof Class)) {
                    throw new JSONException("can't create non-static inner class instance.");
                }
                final String typeName = ((Class)type).getName();
                final int lastIndex = typeName.lastIndexOf(36);
                final String parentClassName = typeName.substring(0, lastIndex);
                final Object ctxObj = context.object;
                String parentName = ctxObj.getClass().getName();
                Object param = null;
                if (!parentName.equals(parentClassName)) {
                    final ParseContext parentContext = context.parent;
                    if (parentContext != null && parentContext.object != null && ("java.util.ArrayList".equals(parentName) || "java.util.List".equals(parentName) || "java.util.Collection".equals(parentName) || "java.util.Map".equals(parentName) || "java.util.HashMap".equals(parentName))) {
                        parentName = parentContext.object.getClass().getName();
                        if (parentName.equals(parentClassName)) {
                            param = parentContext.object;
                        }
                    }
                    else {
                        param = ctxObj;
                    }
                }
                else {
                    param = ctxObj;
                }
                if (param == null || (param instanceof Collection && ((Collection)param).isEmpty())) {
                    throw new JSONException("can't create non-static inner class instance.");
                }
                object = constructor.newInstance(param);
            }
        }
        catch (JSONException e) {
            throw e;
        }
        catch (Exception e2) {
            throw new JSONException("create instance error, class " + this.clazz.getName(), e2);
        }
        if (parser != null && parser.lexer.isEnabled(Feature.InitStringFieldAsEmpty)) {
            for (final FieldInfo fieldInfo : this.beanInfo.fields) {
                if (fieldInfo.fieldClass == String.class) {
                    try {
                        fieldInfo.set(object, "");
                    }
                    catch (Exception e3) {
                        throw new JSONException("create instance error, class " + this.clazz.getName(), e3);
                    }
                }
            }
        }
        return object;
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        return this.deserialze(parser, type, fieldName, 0);
    }
    
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName, final int features) {
        return this.deserialze(parser, type, fieldName, null, features, null);
    }
    
    public <T> T deserialzeArrayMapping(final DefaultJSONParser parser, final Type type, final Object fieldName, Object object) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() != 14) {
            throw new JSONException("error");
        }
        String typeName = null;
        if ((typeName = lexer.scanTypeName(parser.symbolTable)) != null) {
            ObjectDeserializer deserializer = getSeeAlso(parser.getConfig(), this.beanInfo, typeName);
            Class<?> userType = null;
            if (deserializer == null) {
                final Class<?> expectClass = TypeUtils.getClass(type);
                userType = parser.getConfig().checkAutoType(typeName, expectClass, lexer.getFeatures());
                deserializer = parser.getConfig().getDeserializer(userType);
            }
            if (deserializer instanceof JavaBeanDeserializer) {
                return (T)((JavaBeanDeserializer)deserializer).deserialzeArrayMapping(parser, type, fieldName, object);
            }
        }
        object = this.createInstance(parser, type);
        for (int i = 0, size = this.sortedFieldDeserializers.length; i < size; ++i) {
            final char seperator = (i == size - 1) ? ']' : ',';
            final FieldDeserializer fieldDeser = this.sortedFieldDeserializers[i];
            final Class<?> fieldClass = fieldDeser.fieldInfo.fieldClass;
            if (fieldClass == Integer.TYPE) {
                final int value = lexer.scanInt(seperator);
                fieldDeser.setValue(object, value);
            }
            else if (fieldClass == String.class) {
                final String value2 = lexer.scanString(seperator);
                fieldDeser.setValue(object, value2);
            }
            else if (fieldClass == Long.TYPE) {
                final long value3 = lexer.scanLong(seperator);
                fieldDeser.setValue(object, value3);
            }
            else if (fieldClass.isEnum()) {
                final char ch = lexer.getCurrent();
                Object value4;
                if (ch == '\"' || ch == 'n') {
                    value4 = lexer.scanEnum(fieldClass, parser.getSymbolTable(), seperator);
                }
                else if (ch >= '0' && ch <= '9') {
                    final int ordinal = lexer.scanInt(seperator);
                    final EnumDeserializer enumDeser = (EnumDeserializer)((DefaultFieldDeserializer)fieldDeser).getFieldValueDeserilizer(parser.getConfig());
                    value4 = enumDeser.valueOf(ordinal);
                }
                else {
                    value4 = this.scanEnum(lexer, seperator);
                }
                fieldDeser.setValue(object, value4);
            }
            else if (fieldClass == Boolean.TYPE) {
                final boolean value5 = lexer.scanBoolean(seperator);
                fieldDeser.setValue(object, value5);
            }
            else if (fieldClass == Float.TYPE) {
                final float value6 = lexer.scanFloat(seperator);
                fieldDeser.setValue(object, value6);
            }
            else if (fieldClass == Double.TYPE) {
                final double value7 = lexer.scanDouble(seperator);
                fieldDeser.setValue(object, value7);
            }
            else if (fieldClass == Date.class && lexer.getCurrent() == '1') {
                final long longValue = lexer.scanLong(seperator);
                fieldDeser.setValue(object, new Date(longValue));
            }
            else if (fieldClass == BigDecimal.class) {
                final BigDecimal value8 = lexer.scanDecimal(seperator);
                fieldDeser.setValue(object, value8);
            }
            else {
                lexer.nextToken(14);
                final Object value9 = parser.parseObject(fieldDeser.fieldInfo.fieldType, fieldDeser.fieldInfo.name);
                fieldDeser.setValue(object, value9);
                if (lexer.token() == 15) {
                    break;
                }
                this.check(lexer, (seperator == ']') ? 15 : 16);
            }
        }
        lexer.nextToken(16);
        return (T)object;
    }
    
    protected void check(final JSONLexer lexer, final int token) {
        if (lexer.token() != token) {
            throw new JSONException("syntax error");
        }
    }
    
    protected Enum<?> scanEnum(final JSONLexer lexer, final char seperator) {
        throw new JSONException("illegal enum. " + lexer.info());
    }
    
    protected <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName, Object object, final int features, int[] setFlags) {
        if (type == JSON.class || type == JSONObject.class) {
            return (T)parser.parse();
        }
        final JSONLexerBase lexer = (JSONLexerBase)parser.lexer;
        final ParserConfig config = parser.getConfig();
        int token = lexer.token();
        if (token == 8) {
            lexer.nextToken(16);
            return null;
        }
        ParseContext context = parser.getContext();
        if (object != null && context != null) {
            context = context.parent;
        }
        ParseContext childContext = null;
        try {
            Map<String, Object> fieldValues = null;
            if (token == 13) {
                lexer.nextToken(16);
                if (object == null) {
                    object = this.createInstance(parser, type);
                }
                return (T)object;
            }
            if (token == 14) {
                final int mask = Feature.SupportArrayToBean.mask;
                final boolean isSupportArrayToBean = (this.beanInfo.parserFeatures & mask) != 0x0 || lexer.isEnabled(Feature.SupportArrayToBean) || (features & mask) != 0x0;
                if (isSupportArrayToBean) {
                    return this.deserialzeArrayMapping(parser, type, fieldName, object);
                }
            }
            if (token != 12 && token != 16) {
                if (lexer.isBlankInput()) {
                    return null;
                }
                if (token == 4) {
                    final String strVal = lexer.stringVal();
                    if (strVal.length() == 0) {
                        lexer.nextToken();
                        return null;
                    }
                    if (this.beanInfo.jsonType != null) {
                        for (final Class<?> seeAlsoClass : this.beanInfo.jsonType.seeAlso()) {
                            if (Enum.class.isAssignableFrom(seeAlsoClass)) {
                                try {
                                    final Enum<?> e = Enum.valueOf(seeAlsoClass, strVal);
                                    return (T)e;
                                }
                                catch (IllegalArgumentException ex2) {}
                            }
                        }
                    }
                }
                if (token == 14 && lexer.getCurrent() == ']') {
                    lexer.next();
                    lexer.nextToken();
                    return null;
                }
                if (this.beanInfo.factoryMethod != null && this.beanInfo.fields.length == 1) {
                    try {
                        final FieldInfo field = this.beanInfo.fields[0];
                        if (field.fieldClass == Integer.class) {
                            if (token == 2) {
                                final int intValue = lexer.intValue();
                                lexer.nextToken();
                                return (T)this.createFactoryInstance(config, intValue);
                            }
                        }
                        else if (field.fieldClass == String.class && token == 4) {
                            final String stringVal = lexer.stringVal();
                            lexer.nextToken();
                            return (T)this.createFactoryInstance(config, stringVal);
                        }
                    }
                    catch (Exception ex) {
                        throw new JSONException(ex.getMessage(), ex);
                    }
                }
                final StringBuilder buf = new StringBuilder().append("syntax error, expect {, actual ").append(lexer.tokenName()).append(", pos ").append(lexer.pos());
                if (fieldName instanceof String) {
                    buf.append(", fieldName ").append(fieldName);
                }
                buf.append(", fastjson-version ").append("1.2.68");
                throw new JSONException(buf.toString());
            }
            else {
                if (parser.resolveStatus == 2) {
                    parser.resolveStatus = 0;
                }
                final String typeKey = this.beanInfo.typeKey;
                int fieldIndex = 0;
                int notMatchCount = 0;
                while (true) {
                    String key = null;
                    FieldDeserializer fieldDeser = null;
                    FieldInfo fieldInfo = null;
                    Class<?> fieldClass = null;
                    JSONField feildAnnotation = null;
                    boolean customDeserilizer = false;
                    if (fieldIndex < this.sortedFieldDeserializers.length && notMatchCount < 16) {
                        fieldDeser = this.sortedFieldDeserializers[fieldIndex];
                        fieldInfo = fieldDeser.fieldInfo;
                        fieldClass = fieldInfo.fieldClass;
                        feildAnnotation = fieldInfo.getAnnotation();
                        if (feildAnnotation != null && fieldDeser instanceof DefaultFieldDeserializer) {
                            customDeserilizer = ((DefaultFieldDeserializer)fieldDeser).customDeserilizer;
                        }
                    }
                    boolean matchField = false;
                    boolean valueParsed = false;
                    Object fieldValue = null;
                    Label_2898: {
                        if (fieldDeser != null) {
                            final char[] name_chars = fieldInfo.name_chars;
                            if (customDeserilizer && lexer.matchField(name_chars)) {
                                matchField = true;
                            }
                            else if (fieldClass == Integer.TYPE || fieldClass == Integer.class) {
                                final int intVal = lexer.scanFieldInt(name_chars);
                                if (intVal == 0 && lexer.matchStat == 5) {
                                    fieldValue = null;
                                }
                                else {
                                    fieldValue = intVal;
                                }
                                if (lexer.matchStat > 0) {
                                    matchField = true;
                                    valueParsed = true;
                                }
                                else if (lexer.matchStat == -2) {
                                    ++notMatchCount;
                                    break Label_2898;
                                }
                            }
                            else if (fieldClass == Long.TYPE || fieldClass == Long.class) {
                                final long longVal = lexer.scanFieldLong(name_chars);
                                if (longVal == 0L && lexer.matchStat == 5) {
                                    fieldValue = null;
                                }
                                else {
                                    fieldValue = longVal;
                                }
                                if (lexer.matchStat > 0) {
                                    matchField = true;
                                    valueParsed = true;
                                }
                                else if (lexer.matchStat == -2) {
                                    ++notMatchCount;
                                    break Label_2898;
                                }
                            }
                            else if (fieldClass == String.class) {
                                fieldValue = lexer.scanFieldString(name_chars);
                                if (lexer.matchStat > 0) {
                                    matchField = true;
                                    valueParsed = true;
                                }
                                else if (lexer.matchStat == -2) {
                                    ++notMatchCount;
                                    break Label_2898;
                                }
                            }
                            else if (fieldClass == Date.class && fieldInfo.format == null) {
                                fieldValue = lexer.scanFieldDate(name_chars);
                                if (lexer.matchStat > 0) {
                                    matchField = true;
                                    valueParsed = true;
                                }
                                else if (lexer.matchStat == -2) {
                                    ++notMatchCount;
                                    break Label_2898;
                                }
                            }
                            else if (fieldClass == BigDecimal.class) {
                                fieldValue = lexer.scanFieldDecimal(name_chars);
                                if (lexer.matchStat > 0) {
                                    matchField = true;
                                    valueParsed = true;
                                }
                                else if (lexer.matchStat == -2) {
                                    ++notMatchCount;
                                    break Label_2898;
                                }
                            }
                            else if (fieldClass == BigInteger.class) {
                                fieldValue = lexer.scanFieldBigInteger(name_chars);
                                if (lexer.matchStat > 0) {
                                    matchField = true;
                                    valueParsed = true;
                                }
                                else if (lexer.matchStat == -2) {
                                    ++notMatchCount;
                                    break Label_2898;
                                }
                            }
                            else if (fieldClass == Boolean.TYPE || fieldClass == Boolean.class) {
                                final boolean booleanVal = lexer.scanFieldBoolean(name_chars);
                                if (lexer.matchStat == 5) {
                                    fieldValue = null;
                                }
                                else {
                                    fieldValue = booleanVal;
                                }
                                if (lexer.matchStat > 0) {
                                    matchField = true;
                                    valueParsed = true;
                                }
                                else if (lexer.matchStat == -2) {
                                    ++notMatchCount;
                                    break Label_2898;
                                }
                            }
                            else if (fieldClass == Float.TYPE || fieldClass == Float.class) {
                                final float floatVal = lexer.scanFieldFloat(name_chars);
                                if (floatVal == 0.0f && lexer.matchStat == 5) {
                                    fieldValue = null;
                                }
                                else {
                                    fieldValue = floatVal;
                                }
                                if (lexer.matchStat > 0) {
                                    matchField = true;
                                    valueParsed = true;
                                }
                                else if (lexer.matchStat == -2) {
                                    ++notMatchCount;
                                    break Label_2898;
                                }
                            }
                            else if (fieldClass == Double.TYPE || fieldClass == Double.class) {
                                final double doubleVal = lexer.scanFieldDouble(name_chars);
                                if (doubleVal == 0.0 && lexer.matchStat == 5) {
                                    fieldValue = null;
                                }
                                else {
                                    fieldValue = doubleVal;
                                }
                                if (lexer.matchStat > 0) {
                                    matchField = true;
                                    valueParsed = true;
                                }
                                else if (lexer.matchStat == -2) {
                                    ++notMatchCount;
                                    break Label_2898;
                                }
                            }
                            else if (fieldClass.isEnum() && parser.getConfig().getDeserializer(fieldClass) instanceof EnumDeserializer && (feildAnnotation == null || feildAnnotation.deserializeUsing() == Void.class)) {
                                if (fieldDeser instanceof DefaultFieldDeserializer) {
                                    final ObjectDeserializer fieldValueDeserilizer = ((DefaultFieldDeserializer)fieldDeser).fieldValueDeserilizer;
                                    fieldValue = this.scanEnum(lexer, name_chars, fieldValueDeserilizer);
                                    if (lexer.matchStat > 0) {
                                        matchField = true;
                                        valueParsed = true;
                                    }
                                    else if (lexer.matchStat == -2) {
                                        ++notMatchCount;
                                        break Label_2898;
                                    }
                                }
                            }
                            else if (fieldClass == int[].class) {
                                fieldValue = lexer.scanFieldIntArray(name_chars);
                                if (lexer.matchStat > 0) {
                                    matchField = true;
                                    valueParsed = true;
                                }
                                else if (lexer.matchStat == -2) {
                                    ++notMatchCount;
                                    break Label_2898;
                                }
                            }
                            else if (fieldClass == float[].class) {
                                fieldValue = lexer.scanFieldFloatArray(name_chars);
                                if (lexer.matchStat > 0) {
                                    matchField = true;
                                    valueParsed = true;
                                }
                                else if (lexer.matchStat == -2) {
                                    ++notMatchCount;
                                    break Label_2898;
                                }
                            }
                            else if (fieldClass == float[][].class) {
                                fieldValue = lexer.scanFieldFloatArray2(name_chars);
                                if (lexer.matchStat > 0) {
                                    matchField = true;
                                    valueParsed = true;
                                }
                                else if (lexer.matchStat == -2) {
                                    ++notMatchCount;
                                    break Label_2898;
                                }
                            }
                            else {
                                if (!lexer.matchField(name_chars)) {
                                    break Label_2898;
                                }
                                matchField = true;
                            }
                        }
                        if (!matchField) {
                            key = lexer.scanSymbol(parser.symbolTable);
                            if (key == null) {
                                token = lexer.token();
                                if (token == 13) {
                                    lexer.nextToken(16);
                                    break;
                                }
                                if (token == 16 && lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                                    break Label_2898;
                                }
                            }
                            if ("$ref" == key && context != null) {
                                lexer.nextTokenWithColon(4);
                                token = lexer.token();
                                if (token != 4) {
                                    throw new JSONException("illegal ref, " + JSONToken.name(token));
                                }
                                String ref = lexer.stringVal();
                                if ("@".equals(ref)) {
                                    object = context.object;
                                }
                                else if ("..".equals(ref)) {
                                    final ParseContext parentContext = context.parent;
                                    if (parentContext.object != null) {
                                        object = parentContext.object;
                                    }
                                    else {
                                        parser.addResolveTask(new DefaultJSONParser.ResolveTask(parentContext, ref));
                                        parser.resolveStatus = 1;
                                    }
                                }
                                else if ("$".equals(ref)) {
                                    ParseContext rootContext;
                                    for (rootContext = context; rootContext.parent != null; rootContext = rootContext.parent) {}
                                    if (rootContext.object != null) {
                                        object = rootContext.object;
                                    }
                                    else {
                                        parser.addResolveTask(new DefaultJSONParser.ResolveTask(rootContext, ref));
                                        parser.resolveStatus = 1;
                                    }
                                }
                                else {
                                    if (ref.indexOf(92) > 0) {
                                        final StringBuilder buf2 = new StringBuilder();
                                        for (int i = 0; i < ref.length(); ++i) {
                                            char ch = ref.charAt(i);
                                            if (ch == '\\') {
                                                ch = ref.charAt(++i);
                                            }
                                            buf2.append(ch);
                                        }
                                        ref = buf2.toString();
                                    }
                                    final Object refObj = parser.resolveReference(ref);
                                    if (refObj != null) {
                                        object = refObj;
                                    }
                                    else {
                                        parser.addResolveTask(new DefaultJSONParser.ResolveTask(context, ref));
                                        parser.resolveStatus = 1;
                                    }
                                }
                                lexer.nextToken(13);
                                if (lexer.token() != 13) {
                                    throw new JSONException("illegal ref");
                                }
                                lexer.nextToken(16);
                                parser.setContext(context, object, fieldName);
                                return (T)object;
                            }
                            else if ((typeKey != null && typeKey.equals(key)) || JSON.DEFAULT_TYPE_KEY == key) {
                                lexer.nextTokenWithColon(4);
                                if (lexer.token() != 4) {
                                    throw new JSONException("syntax error");
                                }
                                final String typeName = lexer.stringVal();
                                lexer.nextToken(16);
                                if (!typeName.equals(this.beanInfo.typeName) && !parser.isEnabled(Feature.IgnoreAutoType)) {
                                    ObjectDeserializer deserializer = getSeeAlso(config, this.beanInfo, typeName);
                                    Class<?> userType = null;
                                    if (deserializer == null) {
                                        final Class<?> expectClass = TypeUtils.getClass(type);
                                        userType = config.checkAutoType(typeName, expectClass, lexer.getFeatures());
                                        deserializer = parser.getConfig().getDeserializer(userType);
                                    }
                                    final Object typedObject = deserializer.deserialze(parser, userType, fieldName);
                                    if (deserializer instanceof JavaBeanDeserializer) {
                                        final JavaBeanDeserializer javaBeanDeserializer = (JavaBeanDeserializer)deserializer;
                                        if (typeKey != null) {
                                            final FieldDeserializer typeKeyFieldDeser = javaBeanDeserializer.getFieldDeserializer(typeKey);
                                            if (typeKeyFieldDeser != null) {
                                                typeKeyFieldDeser.setValue(typedObject, typeName);
                                            }
                                        }
                                    }
                                    return (T)typedObject;
                                }
                                if (lexer.token() == 13) {
                                    lexer.nextToken();
                                    break;
                                }
                                break Label_2898;
                            }
                        }
                        if (object == null && fieldValues == null) {
                            object = this.createInstance(parser, type);
                            if (object == null) {
                                fieldValues = new HashMap<String, Object>(this.fieldDeserializers.length);
                            }
                            childContext = parser.setContext(context, object, fieldName);
                            if (setFlags == null) {
                                setFlags = new int[this.fieldDeserializers.length / 32 + 1];
                            }
                        }
                        if (matchField) {
                            if (!valueParsed) {
                                fieldDeser.parseField(parser, object, type, fieldValues);
                            }
                            else {
                                if (object == null) {
                                    fieldValues.put(fieldInfo.name, fieldValue);
                                }
                                else if (fieldValue == null) {
                                    if (fieldClass != Integer.TYPE && fieldClass != Long.TYPE && fieldClass != Float.TYPE && fieldClass != Double.TYPE && fieldClass != Boolean.TYPE) {
                                        fieldDeser.setValue(object, fieldValue);
                                    }
                                }
                                else {
                                    fieldDeser.setValue(object, fieldValue);
                                }
                                if (setFlags != null) {
                                    final int flagIndex = fieldIndex / 32;
                                    final int bitIndex = fieldIndex % 32;
                                    final int[] array = setFlags;
                                    final int n2 = flagIndex;
                                    array[n2] |= 1 << bitIndex;
                                }
                                if (lexer.matchStat == 4) {
                                    break;
                                }
                            }
                        }
                        else {
                            final boolean match = this.parseField(parser, key, object, type, (fieldValues == null) ? new HashMap<String, Object>(this.fieldDeserializers.length) : fieldValues, setFlags);
                            if (!match) {
                                if (lexer.token() == 13) {
                                    lexer.nextToken();
                                    break;
                                }
                                break Label_2898;
                            }
                            else if (lexer.token() == 17) {
                                throw new JSONException("syntax error, unexpect token ':'");
                            }
                        }
                        if (lexer.token() != 16) {
                            if (lexer.token() == 13) {
                                lexer.nextToken(16);
                                break;
                            }
                            if (lexer.token() == 18 || lexer.token() == 1) {
                                throw new JSONException("syntax error, unexpect token " + JSONToken.name(lexer.token()));
                            }
                        }
                    }
                    ++fieldIndex;
                }
                if (object == null) {
                    if (fieldValues == null) {
                        object = this.createInstance(parser, type);
                        if (childContext == null) {
                            childContext = parser.setContext(context, object, fieldName);
                        }
                        return (T)object;
                    }
                    final String[] paramNames = this.beanInfo.creatorConstructorParameters;
                    Object[] params;
                    if (paramNames != null) {
                        params = new Object[paramNames.length];
                        for (int j = 0; j < paramNames.length; ++j) {
                            final String paramName = paramNames[j];
                            Object param = fieldValues.remove(paramName);
                            if (param == null) {
                                final Type fieldType = this.beanInfo.creatorConstructorParameterTypes[j];
                                final FieldInfo fieldInfo2 = this.beanInfo.fields[j];
                                if (fieldType == Byte.TYPE) {
                                    param = 0;
                                }
                                else if (fieldType == Short.TYPE) {
                                    param = 0;
                                }
                                else if (fieldType == Integer.TYPE) {
                                    param = 0;
                                }
                                else if (fieldType == Long.TYPE) {
                                    param = 0L;
                                }
                                else if (fieldType == Float.TYPE) {
                                    param = 0.0f;
                                }
                                else if (fieldType == Double.TYPE) {
                                    param = 0.0;
                                }
                                else if (fieldType == Boolean.TYPE) {
                                    param = Boolean.FALSE;
                                }
                                else if (fieldType == String.class && (fieldInfo2.parserFeatures & Feature.InitStringFieldAsEmpty.mask) != 0x0) {
                                    param = "";
                                }
                            }
                            else if (this.beanInfo.creatorConstructorParameterTypes != null && j < this.beanInfo.creatorConstructorParameterTypes.length) {
                                final Type paramType = this.beanInfo.creatorConstructorParameterTypes[j];
                                if (paramType instanceof Class) {
                                    final Class paramClass = (Class)paramType;
                                    if (!paramClass.isInstance(param) && param instanceof List) {
                                        final List list = (List)param;
                                        if (list.size() == 1) {
                                            final Object first = list.get(0);
                                            if (paramClass.isInstance(first)) {
                                                param = list.get(0);
                                            }
                                        }
                                    }
                                }
                            }
                            params[j] = param;
                        }
                    }
                    else {
                        final FieldInfo[] fieldInfoList = this.beanInfo.fields;
                        final int size = fieldInfoList.length;
                        params = new Object[size];
                        for (int k = 0; k < size; ++k) {
                            final FieldInfo fieldInfo3 = fieldInfoList[k];
                            Object param2 = fieldValues.get(fieldInfo3.name);
                            if (param2 == null) {
                                final Type fieldType2 = fieldInfo3.fieldType;
                                if (fieldType2 == Byte.TYPE) {
                                    param2 = 0;
                                }
                                else if (fieldType2 == Short.TYPE) {
                                    param2 = 0;
                                }
                                else if (fieldType2 == Integer.TYPE) {
                                    param2 = 0;
                                }
                                else if (fieldType2 == Long.TYPE) {
                                    param2 = 0L;
                                }
                                else if (fieldType2 == Float.TYPE) {
                                    param2 = 0.0f;
                                }
                                else if (fieldType2 == Double.TYPE) {
                                    param2 = 0.0;
                                }
                                else if (fieldType2 == Boolean.TYPE) {
                                    param2 = Boolean.FALSE;
                                }
                                else if (fieldType2 == String.class && (fieldInfo3.parserFeatures & Feature.InitStringFieldAsEmpty.mask) != 0x0) {
                                    param2 = "";
                                }
                            }
                            params[k] = param2;
                        }
                    }
                    if (this.beanInfo.creatorConstructor != null) {
                        boolean hasNull = false;
                        if (this.beanInfo.kotlin) {
                            int l = 0;
                            while (l < params.length) {
                                if (params[l] == null && this.beanInfo.fields != null && l < this.beanInfo.fields.length) {
                                    final FieldInfo fieldInfo = this.beanInfo.fields[l];
                                    if (fieldInfo.fieldClass == String.class) {
                                        hasNull = true;
                                        break;
                                    }
                                    break;
                                }
                                else {
                                    ++l;
                                }
                            }
                        }
                        try {
                            if (hasNull && this.beanInfo.kotlinDefaultConstructor != null) {
                                object = this.beanInfo.kotlinDefaultConstructor.newInstance(new Object[0]);
                                for (int l = 0; l < params.length; ++l) {
                                    final Object param = params[l];
                                    if (param != null && this.beanInfo.fields != null && l < this.beanInfo.fields.length) {
                                        final FieldInfo fieldInfo3 = this.beanInfo.fields[l];
                                        fieldInfo3.set(object, param);
                                    }
                                }
                            }
                            else {
                                object = this.beanInfo.creatorConstructor.newInstance(params);
                            }
                        }
                        catch (Exception e2) {
                            throw new JSONException("create instance error, " + paramNames + ", " + this.beanInfo.creatorConstructor.toGenericString(), e2);
                        }
                        if (paramNames != null) {
                            for (final Map.Entry<String, Object> entry : fieldValues.entrySet()) {
                                final FieldDeserializer fieldDeserializer = this.getFieldDeserializer(entry.getKey());
                                if (fieldDeserializer != null) {
                                    fieldDeserializer.setValue(object, entry.getValue());
                                }
                            }
                        }
                    }
                    else if (this.beanInfo.factoryMethod != null) {
                        try {
                            object = this.beanInfo.factoryMethod.invoke(null, params);
                        }
                        catch (Exception e3) {
                            throw new JSONException("create factory method error, " + this.beanInfo.factoryMethod.toString(), e3);
                        }
                    }
                    if (childContext != null) {
                        childContext.object = object;
                    }
                }
                final Method buildMethod = this.beanInfo.buildMethod;
                if (buildMethod == null) {
                    return (T)object;
                }
                Object builtObj;
                try {
                    builtObj = buildMethod.invoke(object, new Object[0]);
                }
                catch (Exception e3) {
                    throw new JSONException("build object error", e3);
                }
                return (T)builtObj;
            }
        }
        finally {
            if (childContext != null) {
                childContext.object = object;
            }
            parser.setContext(context);
        }
    }
    
    protected Enum scanEnum(final JSONLexerBase lexer, final char[] name_chars, final ObjectDeserializer fieldValueDeserilizer) {
        EnumDeserializer enumDeserializer = null;
        if (fieldValueDeserilizer instanceof EnumDeserializer) {
            enumDeserializer = (EnumDeserializer)fieldValueDeserilizer;
        }
        if (enumDeserializer == null) {
            lexer.matchStat = -1;
            return null;
        }
        final long enumNameHashCode = lexer.scanEnumSymbol(name_chars);
        if (lexer.matchStat > 0) {
            final Enum e = enumDeserializer.getEnumByHashCode(enumNameHashCode);
            if (e == null) {
                if (enumNameHashCode == -3750763034362895579L) {
                    return null;
                }
                if (lexer.isEnabled(Feature.ErrorOnEnumNotMatch)) {
                    throw new JSONException("not match enum value, " + enumDeserializer.enumClass);
                }
            }
            return e;
        }
        return null;
    }
    
    public boolean parseField(final DefaultJSONParser parser, final String key, final Object object, final Type objectType, final Map<String, Object> fieldValues) {
        return this.parseField(parser, key, object, objectType, fieldValues, null);
    }
    
    public boolean parseField(final DefaultJSONParser parser, final String key, final Object object, final Type objectType, final Map<String, Object> fieldValues, final int[] setFlags) {
        final JSONLexer lexer = parser.lexer;
        final int disableFieldSmartMatchMask = Feature.DisableFieldSmartMatch.mask;
        FieldDeserializer fieldDeserializer;
        if (lexer.isEnabled(disableFieldSmartMatchMask) || (this.beanInfo.parserFeatures & disableFieldSmartMatchMask) != 0x0) {
            fieldDeserializer = this.getFieldDeserializer(key);
        }
        else {
            fieldDeserializer = this.smartMatch(key, setFlags);
        }
        final int mask = Feature.SupportNonPublicField.mask;
        if (fieldDeserializer == null && (lexer.isEnabled(mask) || (this.beanInfo.parserFeatures & mask) != 0x0)) {
            if (this.extraFieldDeserializers == null) {
                final ConcurrentHashMap extraFieldDeserializers = new ConcurrentHashMap(1, 0.75f, 1);
                for (Class c = this.clazz; c != null && c != Object.class; c = c.getSuperclass()) {
                    final Field[] declaredFields;
                    final Field[] fields = declaredFields = c.getDeclaredFields();
                    for (final Field field : declaredFields) {
                        String fieldName = field.getName();
                        if (this.getFieldDeserializer(fieldName) == null) {
                            final int fieldModifiers = field.getModifiers();
                            if ((fieldModifiers & 0x10) == 0x0) {
                                if ((fieldModifiers & 0x8) == 0x0) {
                                    final JSONField jsonField = TypeUtils.getAnnotation(field, JSONField.class);
                                    if (jsonField != null) {
                                        final String alteredFieldName = jsonField.name();
                                        if (!"".equals(alteredFieldName)) {
                                            fieldName = alteredFieldName;
                                        }
                                    }
                                    extraFieldDeserializers.put(fieldName, field);
                                }
                            }
                        }
                    }
                }
                this.extraFieldDeserializers = (ConcurrentMap<String, Object>)extraFieldDeserializers;
            }
            final Object deserOrField = this.extraFieldDeserializers.get(key);
            if (deserOrField != null) {
                if (deserOrField instanceof FieldDeserializer) {
                    fieldDeserializer = (FieldDeserializer)deserOrField;
                }
                else {
                    final Field field2 = (Field)deserOrField;
                    field2.setAccessible(true);
                    final FieldInfo fieldInfo = new FieldInfo(key, field2.getDeclaringClass(), field2.getType(), field2.getGenericType(), field2, 0, 0, 0);
                    fieldDeserializer = new DefaultFieldDeserializer(parser.getConfig(), this.clazz, fieldInfo);
                    this.extraFieldDeserializers.put(key, fieldDeserializer);
                }
            }
        }
        if (fieldDeserializer == null) {
            if (!lexer.isEnabled(Feature.IgnoreNotMatch)) {
                throw new JSONException("setter not found, class " + this.clazz.getName() + ", property " + key);
            }
            int fieldIndex = -1;
            for (int i = 0; i < this.sortedFieldDeserializers.length; ++i) {
                final FieldDeserializer fieldDeser = this.sortedFieldDeserializers[i];
                final FieldInfo fieldInfo2 = fieldDeser.fieldInfo;
                if (fieldInfo2.unwrapped && fieldDeser instanceof DefaultFieldDeserializer) {
                    if (fieldInfo2.field != null) {
                        final DefaultFieldDeserializer defaultFieldDeserializer = (DefaultFieldDeserializer)fieldDeser;
                        final ObjectDeserializer fieldValueDeser = defaultFieldDeserializer.getFieldValueDeserilizer(parser.getConfig());
                        if (fieldValueDeser instanceof JavaBeanDeserializer) {
                            final JavaBeanDeserializer javaBeanFieldValueDeserializer = (JavaBeanDeserializer)fieldValueDeser;
                            final FieldDeserializer unwrappedFieldDeser = javaBeanFieldValueDeserializer.getFieldDeserializer(key);
                            if (unwrappedFieldDeser != null) {
                                try {
                                    Object fieldObject = fieldInfo2.field.get(object);
                                    if (fieldObject == null) {
                                        fieldObject = ((JavaBeanDeserializer)fieldValueDeser).createInstance(parser, fieldInfo2.fieldType);
                                        fieldDeser.setValue(object, fieldObject);
                                    }
                                    lexer.nextTokenWithColon(defaultFieldDeserializer.getFastMatchToken());
                                    unwrappedFieldDeser.parseField(parser, fieldObject, objectType, fieldValues);
                                    fieldIndex = i;
                                }
                                catch (Exception e) {
                                    throw new JSONException("parse unwrapped field error.", e);
                                }
                            }
                        }
                        else if (fieldValueDeser instanceof MapDeserializer) {
                            final MapDeserializer javaBeanFieldValueDeserializer2 = (MapDeserializer)fieldValueDeser;
                            try {
                                Map fieldObject2 = (Map)fieldInfo2.field.get(object);
                                if (fieldObject2 == null) {
                                    fieldObject2 = javaBeanFieldValueDeserializer2.createMap(fieldInfo2.fieldType);
                                    fieldDeser.setValue(object, fieldObject2);
                                }
                                lexer.nextTokenWithColon();
                                final Object fieldValue = parser.parse(key);
                                fieldObject2.put(key, fieldValue);
                            }
                            catch (Exception e2) {
                                throw new JSONException("parse unwrapped field error.", e2);
                            }
                            fieldIndex = i;
                        }
                    }
                    else if (fieldInfo2.method.getParameterTypes().length == 2) {
                        lexer.nextTokenWithColon();
                        final Object fieldValue2 = parser.parse(key);
                        try {
                            fieldInfo2.method.invoke(object, key, fieldValue2);
                        }
                        catch (Exception e3) {
                            throw new JSONException("parse unwrapped field error.", e3);
                        }
                        fieldIndex = i;
                    }
                }
            }
            if (fieldIndex != -1) {
                if (setFlags != null) {
                    final int flagIndex = fieldIndex / 32;
                    final int bitIndex = fieldIndex % 32;
                    final int n = flagIndex;
                    setFlags[n] |= 1 << bitIndex;
                }
                return true;
            }
            parser.parseExtra(object, key);
            return false;
        }
        else {
            int fieldIndex = -1;
            for (int i = 0; i < this.sortedFieldDeserializers.length; ++i) {
                if (this.sortedFieldDeserializers[i] == fieldDeserializer) {
                    fieldIndex = i;
                    break;
                }
            }
            if (fieldIndex != -1 && setFlags != null && key.startsWith("_") && isSetFlag(fieldIndex, setFlags)) {
                parser.parseExtra(object, key);
                return false;
            }
            lexer.nextTokenWithColon(fieldDeserializer.getFastMatchToken());
            fieldDeserializer.parseField(parser, object, objectType, fieldValues);
            if (setFlags != null) {
                final int flagIndex = fieldIndex / 32;
                final int bitIndex = fieldIndex % 32;
                final int n2 = flagIndex;
                setFlags[n2] |= 1 << bitIndex;
            }
            return true;
        }
    }
    
    public FieldDeserializer smartMatch(final String key) {
        return this.smartMatch(key, null);
    }
    
    public FieldDeserializer smartMatch(final String key, final int[] setFlags) {
        if (key == null) {
            return null;
        }
        FieldDeserializer fieldDeserializer = this.getFieldDeserializer(key, setFlags);
        if (fieldDeserializer == null) {
            long smartKeyHash = TypeUtils.fnv1a_64_lower(key);
            if (this.smartMatchHashArray == null) {
                final long[] hashArray = new long[this.sortedFieldDeserializers.length];
                for (int i = 0; i < this.sortedFieldDeserializers.length; ++i) {
                    hashArray[i] = TypeUtils.fnv1a_64_lower(this.sortedFieldDeserializers[i].fieldInfo.name);
                }
                Arrays.sort(hashArray);
                this.smartMatchHashArray = hashArray;
            }
            int pos = Arrays.binarySearch(this.smartMatchHashArray, smartKeyHash);
            boolean is = false;
            if (pos < 0 && (is = key.startsWith("is"))) {
                smartKeyHash = TypeUtils.fnv1a_64_lower(key.substring(2));
                pos = Arrays.binarySearch(this.smartMatchHashArray, smartKeyHash);
            }
            if (pos >= 0) {
                if (this.smartMatchHashArrayMapping == null) {
                    final short[] mapping = new short[this.smartMatchHashArray.length];
                    Arrays.fill(mapping, (short)(-1));
                    for (int j = 0; j < this.sortedFieldDeserializers.length; ++j) {
                        final int p = Arrays.binarySearch(this.smartMatchHashArray, TypeUtils.fnv1a_64_lower(this.sortedFieldDeserializers[j].fieldInfo.name));
                        if (p >= 0) {
                            mapping[p] = (short)j;
                        }
                    }
                    this.smartMatchHashArrayMapping = mapping;
                }
                final int deserIndex = this.smartMatchHashArrayMapping[pos];
                if (deserIndex != -1 && !isSetFlag(deserIndex, setFlags)) {
                    fieldDeserializer = this.sortedFieldDeserializers[deserIndex];
                }
            }
            if (fieldDeserializer != null) {
                final FieldInfo fieldInfo = fieldDeserializer.fieldInfo;
                if ((fieldInfo.parserFeatures & Feature.DisableFieldSmartMatch.mask) != 0x0) {
                    return null;
                }
                final Class fieldClass = fieldInfo.fieldClass;
                if (is && fieldClass != Boolean.TYPE && fieldClass != Boolean.class) {
                    fieldDeserializer = null;
                }
            }
        }
        return fieldDeserializer;
    }
    
    @Override
    public int getFastMatchToken() {
        return 12;
    }
    
    private Object createFactoryInstance(final ParserConfig config, final Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return this.beanInfo.factoryMethod.invoke(null, value);
    }
    
    public Object createInstance(final Map<String, Object> map, final ParserConfig config) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Object object = null;
        if (this.beanInfo.creatorConstructor != null || this.beanInfo.factoryMethod != null) {
            final FieldInfo[] fieldInfoList = this.beanInfo.fields;
            final int size = fieldInfoList.length;
            final Object[] params = new Object[size];
            Map<String, Integer> missFields = null;
            for (int i = 0; i < size; ++i) {
                final FieldInfo fieldInfo = fieldInfoList[i];
                Object param = map.get(fieldInfo.name);
                if (param == null) {
                    final Class<?> fieldClass = fieldInfo.fieldClass;
                    if (fieldClass == Integer.TYPE) {
                        param = 0;
                    }
                    else if (fieldClass == Long.TYPE) {
                        param = 0L;
                    }
                    else if (fieldClass == Short.TYPE) {
                        param = 0;
                    }
                    else if (fieldClass == Byte.TYPE) {
                        param = 0;
                    }
                    else if (fieldClass == Float.TYPE) {
                        param = 0.0f;
                    }
                    else if (fieldClass == Double.TYPE) {
                        param = 0.0;
                    }
                    else if (fieldClass == Character.TYPE) {
                        param = '0';
                    }
                    else if (fieldClass == Boolean.TYPE) {
                        param = false;
                    }
                    if (missFields == null) {
                        missFields = new HashMap<String, Integer>();
                    }
                    missFields.put(fieldInfo.name, i);
                }
                params[i] = param;
            }
            if (missFields != null) {
                for (final Map.Entry<String, Object> entry : map.entrySet()) {
                    final String key = entry.getKey();
                    final Object value = entry.getValue();
                    final FieldDeserializer fieldDeser = this.smartMatch(key);
                    if (fieldDeser != null) {
                        final Integer index = missFields.get(fieldDeser.fieldInfo.name);
                        if (index == null) {
                            continue;
                        }
                        params[index] = value;
                    }
                }
            }
            if (this.beanInfo.creatorConstructor != null) {
                boolean hasNull = false;
                if (this.beanInfo.kotlin) {
                    int j = 0;
                    while (j < params.length) {
                        if (params[j] == null && this.beanInfo.fields != null && j < this.beanInfo.fields.length) {
                            final FieldInfo fieldInfo2 = this.beanInfo.fields[j];
                            if (fieldInfo2.fieldClass == String.class) {
                                hasNull = true;
                                break;
                            }
                            break;
                        }
                        else {
                            ++j;
                        }
                    }
                }
                if (hasNull && this.beanInfo.kotlinDefaultConstructor != null) {
                    try {
                        object = this.beanInfo.kotlinDefaultConstructor.newInstance(new Object[0]);
                        for (int j = 0; j < params.length; ++j) {
                            final Object param = params[j];
                            if (param != null && this.beanInfo.fields != null && j < this.beanInfo.fields.length) {
                                final FieldInfo fieldInfo3 = this.beanInfo.fields[j];
                                fieldInfo3.set(object, param);
                            }
                        }
                        return object;
                    }
                    catch (Exception e) {
                        throw new JSONException("create instance error, " + this.beanInfo.creatorConstructor.toGenericString(), e);
                    }
                }
                try {
                    object = this.beanInfo.creatorConstructor.newInstance(params);
                }
                catch (Exception e) {
                    throw new JSONException("create instance error, " + this.beanInfo.creatorConstructor.toGenericString(), e);
                }
            }
            else if (this.beanInfo.factoryMethod != null) {
                try {
                    object = this.beanInfo.factoryMethod.invoke(null, params);
                }
                catch (Exception e2) {
                    throw new JSONException("create factory method error, " + this.beanInfo.factoryMethod.toString(), e2);
                }
            }
            return object;
        }
        object = this.createInstance(null, this.clazz);
        for (final Map.Entry<String, Object> entry2 : map.entrySet()) {
            final String key2 = entry2.getKey();
            Object value2 = entry2.getValue();
            final FieldDeserializer fieldDeser2 = this.smartMatch(key2);
            if (fieldDeser2 == null) {
                continue;
            }
            final FieldInfo fieldInfo = fieldDeser2.fieldInfo;
            final Field field = fieldDeser2.fieldInfo.field;
            final Type paramType = fieldInfo.fieldType;
            if (fieldInfo.declaringClass != null && fieldInfo.getAnnotation() != null && fieldInfo.getAnnotation().deserializeUsing() != Void.class && fieldInfo.fieldClass.isInstance(value2)) {
                final DefaultJSONParser parser = new DefaultJSONParser(JSON.toJSONString(value2));
                fieldDeser2.parseField(parser, object, paramType, null);
            }
            else {
                if (field != null) {
                    final Class fieldType = field.getType();
                    if (fieldType == Boolean.TYPE) {
                        if (value2 == Boolean.FALSE) {
                            field.setBoolean(object, false);
                            continue;
                        }
                        if (value2 == Boolean.TRUE) {
                            field.setBoolean(object, true);
                            continue;
                        }
                    }
                    else if (fieldType == Integer.TYPE) {
                        if (value2 instanceof Number) {
                            field.setInt(object, ((Number)value2).intValue());
                            continue;
                        }
                    }
                    else if (fieldType == Long.TYPE) {
                        if (value2 instanceof Number) {
                            field.setLong(object, ((Number)value2).longValue());
                            continue;
                        }
                    }
                    else if (fieldType == Float.TYPE) {
                        if (value2 instanceof Number) {
                            field.setFloat(object, ((Number)value2).floatValue());
                            continue;
                        }
                        if (value2 instanceof String) {
                            final String strVal = (String)value2;
                            float floatValue;
                            if (strVal.length() <= 10) {
                                floatValue = TypeUtils.parseFloat(strVal);
                            }
                            else {
                                floatValue = Float.parseFloat(strVal);
                            }
                            field.setFloat(object, floatValue);
                            continue;
                        }
                    }
                    else if (fieldType == Double.TYPE) {
                        if (value2 instanceof Number) {
                            field.setDouble(object, ((Number)value2).doubleValue());
                            continue;
                        }
                        if (value2 instanceof String) {
                            final String strVal = (String)value2;
                            double doubleValue;
                            if (strVal.length() <= 10) {
                                doubleValue = TypeUtils.parseDouble(strVal);
                            }
                            else {
                                doubleValue = Double.parseDouble(strVal);
                            }
                            field.setDouble(object, doubleValue);
                            continue;
                        }
                    }
                    else if (value2 != null && paramType == value2.getClass()) {
                        field.set(object, value2);
                        continue;
                    }
                }
                final String format = fieldInfo.format;
                if (format != null && paramType == Date.class) {
                    value2 = TypeUtils.castToDate(value2, format);
                }
                else if (format != null && paramType instanceof Class && ((Class)paramType).getName().equals("java.time.LocalDateTime")) {
                    value2 = TypeUtils.castToLocalDateTime(value2, format);
                }
                else if (paramType instanceof ParameterizedType) {
                    value2 = TypeUtils.cast(value2, (ParameterizedType)paramType, config);
                }
                else {
                    value2 = TypeUtils.cast(value2, paramType, config);
                }
                fieldDeser2.setValue(object, value2);
            }
        }
        if (this.beanInfo.buildMethod != null) {
            Object builtObj;
            try {
                builtObj = this.beanInfo.buildMethod.invoke(object, new Object[0]);
            }
            catch (Exception e3) {
                throw new JSONException("build object error", e3);
            }
            return builtObj;
        }
        return object;
    }
    
    public Type getFieldType(final int ordinal) {
        return this.sortedFieldDeserializers[ordinal].fieldInfo.fieldType;
    }
    
    protected Object parseRest(final DefaultJSONParser parser, final Type type, final Object fieldName, final Object instance, final int features) {
        return this.parseRest(parser, type, fieldName, instance, features, new int[0]);
    }
    
    protected Object parseRest(final DefaultJSONParser parser, final Type type, final Object fieldName, final Object instance, final int features, final int[] setFlags) {
        final Object value = this.deserialze(parser, type, fieldName, instance, features, setFlags);
        return value;
    }
    
    protected static JavaBeanDeserializer getSeeAlso(final ParserConfig config, final JavaBeanInfo beanInfo, final String typeName) {
        if (beanInfo.jsonType == null) {
            return null;
        }
        for (final Class<?> seeAlsoClass : beanInfo.jsonType.seeAlso()) {
            final ObjectDeserializer seeAlsoDeser = config.getDeserializer(seeAlsoClass);
            if (seeAlsoDeser instanceof JavaBeanDeserializer) {
                final JavaBeanDeserializer seeAlsoJavaBeanDeser = (JavaBeanDeserializer)seeAlsoDeser;
                final JavaBeanInfo subBeanInfo = seeAlsoJavaBeanDeser.beanInfo;
                if (subBeanInfo.typeName.equals(typeName)) {
                    return seeAlsoJavaBeanDeser;
                }
                final JavaBeanDeserializer subSeeAlso = getSeeAlso(config, subBeanInfo, typeName);
                if (subSeeAlso != null) {
                    return subSeeAlso;
                }
            }
        }
        return null;
    }
    
    protected static void parseArray(final Collection collection, final ObjectDeserializer deser, final DefaultJSONParser parser, final Type type, final Object fieldName) {
        final JSONLexerBase lexer = (JSONLexerBase)parser.lexer;
        int token = lexer.token();
        if (token == 8) {
            lexer.nextToken(16);
            token = lexer.token();
            return;
        }
        if (token != 14) {
            parser.throwException(token);
        }
        char ch = lexer.getCurrent();
        if (ch == '[') {
            lexer.next();
            lexer.setToken(14);
        }
        else {
            lexer.nextToken(14);
        }
        if (lexer.token() == 15) {
            lexer.nextToken();
            return;
        }
        int index = 0;
        while (true) {
            final Object item = deser.deserialze(parser, type, index);
            collection.add(item);
            ++index;
            if (lexer.token() != 16) {
                break;
            }
            ch = lexer.getCurrent();
            if (ch == '[') {
                lexer.next();
                lexer.setToken(14);
            }
            else {
                lexer.nextToken(14);
            }
        }
        token = lexer.token();
        if (token != 15) {
            parser.throwException(token);
        }
        ch = lexer.getCurrent();
        if (ch == ',') {
            lexer.next();
            lexer.setToken(16);
        }
        else {
            lexer.nextToken(16);
        }
    }
}
