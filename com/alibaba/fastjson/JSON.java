package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.JSONScanner;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import java.util.Iterator;
import com.alibaba.fastjson.serializer.JavaBeanSerializer;
import java.lang.reflect.Array;
import com.alibaba.fastjson.serializer.JSONSerializable;
import com.alibaba.fastjson.util.TypeUtils;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.OutputStream;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import com.alibaba.fastjson.parser.JSONLexer;
import java.util.Collection;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import com.alibaba.fastjson.parser.deserializer.FieldTypeResolver;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessor;
import com.alibaba.fastjson.parser.deserializer.ExtraTypeProvider;
import com.alibaba.fastjson.parser.deserializer.ParseProcess;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.util.Properties;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import com.alibaba.fastjson.serializer.SerializeFilter;
import java.util.Locale;
import java.util.TimeZone;

public abstract class JSON implements JSONStreamAware, JSONAware
{
    public static TimeZone defaultTimeZone;
    public static Locale defaultLocale;
    public static String DEFAULT_TYPE_KEY;
    static final SerializeFilter[] emptyFilters;
    public static String DEFFAULT_DATE_FORMAT;
    public static int DEFAULT_PARSER_FEATURE;
    public static int DEFAULT_GENERATE_FEATURE;
    private static final ConcurrentHashMap<Type, Type> mixInsMapper;
    private static final ThreadLocal<byte[]> bytesLocal;
    private static final ThreadLocal<char[]> charsLocal;
    public static final String VERSION = "1.2.68";
    
    private static void config(final Properties properties) {
        final String featuresProperty = properties.getProperty("fastjson.serializerFeatures.MapSortField");
        final int mask = SerializerFeature.MapSortField.getMask();
        if ("true".equals(featuresProperty)) {
            JSON.DEFAULT_GENERATE_FEATURE |= mask;
        }
        else if ("false".equals(featuresProperty)) {
            JSON.DEFAULT_GENERATE_FEATURE &= ~mask;
        }
        if ("true".equals(properties.getProperty("parser.features.NonStringKeyAsString"))) {
            JSON.DEFAULT_PARSER_FEATURE |= Feature.NonStringKeyAsString.getMask();
        }
        if ("true".equals(properties.getProperty("parser.features.ErrorOnEnumNotMatch")) || "true".equals(properties.getProperty("fastjson.parser.features.ErrorOnEnumNotMatch"))) {
            JSON.DEFAULT_PARSER_FEATURE |= Feature.ErrorOnEnumNotMatch.getMask();
        }
        if ("false".equals(properties.getProperty("fastjson.asmEnable"))) {
            ParserConfig.getGlobalInstance().setAsmEnable(false);
            SerializeConfig.getGlobalInstance().setAsmEnable(false);
        }
    }
    
    public static void setDefaultTypeKey(final String typeKey) {
        JSON.DEFAULT_TYPE_KEY = typeKey;
        ParserConfig.global.symbolTable.addSymbol(typeKey, 0, typeKey.length(), typeKey.hashCode(), true);
    }
    
    public static Object parse(final String text) {
        return parse(text, JSON.DEFAULT_PARSER_FEATURE);
    }
    
    public static Object parse(final String text, final ParserConfig config) {
        return parse(text, config, JSON.DEFAULT_PARSER_FEATURE);
    }
    
    public static Object parse(final String text, final ParserConfig config, final Feature... features) {
        int featureValues = JSON.DEFAULT_PARSER_FEATURE;
        for (final Feature feature : features) {
            featureValues = Feature.config(featureValues, feature, true);
        }
        return parse(text, config, featureValues);
    }
    
    public static Object parse(final String text, final ParserConfig config, final int features) {
        if (text == null) {
            return null;
        }
        final DefaultJSONParser parser = new DefaultJSONParser(text, config, features);
        final Object value = parser.parse();
        parser.handleResovleTask(value);
        parser.close();
        return value;
    }
    
    public static Object parse(final String text, final int features) {
        return parse(text, ParserConfig.getGlobalInstance(), features);
    }
    
    public static Object parse(final byte[] input, final Feature... features) {
        final char[] chars = allocateChars(input.length);
        final int len = IOUtils.decodeUTF8(input, 0, input.length, chars);
        if (len < 0) {
            return null;
        }
        return parse(new String(chars, 0, len), features);
    }
    
    public static Object parse(final byte[] input, final int off, final int len, final CharsetDecoder charsetDecoder, final Feature... features) {
        if (input == null || input.length == 0) {
            return null;
        }
        int featureValues = JSON.DEFAULT_PARSER_FEATURE;
        for (final Feature feature : features) {
            featureValues = Feature.config(featureValues, feature, true);
        }
        return parse(input, off, len, charsetDecoder, featureValues);
    }
    
    public static Object parse(final byte[] input, final int off, final int len, final CharsetDecoder charsetDecoder, final int features) {
        charsetDecoder.reset();
        final int scaleLength = (int)(len * (double)charsetDecoder.maxCharsPerByte());
        final char[] chars = allocateChars(scaleLength);
        final ByteBuffer byteBuf = ByteBuffer.wrap(input, off, len);
        final CharBuffer charBuf = CharBuffer.wrap(chars);
        IOUtils.decode(charsetDecoder, byteBuf, charBuf);
        final int position = charBuf.position();
        final DefaultJSONParser parser = new DefaultJSONParser(chars, position, ParserConfig.getGlobalInstance(), features);
        final Object value = parser.parse();
        parser.handleResovleTask(value);
        parser.close();
        return value;
    }
    
    public static Object parse(final String text, final Feature... features) {
        int featureValues = JSON.DEFAULT_PARSER_FEATURE;
        for (final Feature feature : features) {
            featureValues = Feature.config(featureValues, feature, true);
        }
        return parse(text, featureValues);
    }
    
    public static JSONObject parseObject(final String text, final Feature... features) {
        return (JSONObject)parse(text, features);
    }
    
    public static JSONObject parseObject(final String text) {
        final Object obj = parse(text);
        if (obj instanceof JSONObject) {
            return (JSONObject)obj;
        }
        try {
            return (JSONObject)toJSON(obj);
        }
        catch (RuntimeException e) {
            throw new JSONException("can not cast to JSONObject.", e);
        }
    }
    
    public static <T> T parseObject(final String text, final TypeReference<T> type, final Feature... features) {
        return parseObject(text, type.type, ParserConfig.global, JSON.DEFAULT_PARSER_FEATURE, features);
    }
    
    public static <T> T parseObject(final String json, final Class<T> clazz, final Feature... features) {
        return parseObject(json, (Type)clazz, ParserConfig.global, null, JSON.DEFAULT_PARSER_FEATURE, features);
    }
    
    public static <T> T parseObject(final String text, final Class<T> clazz, final ParseProcess processor, final Feature... features) {
        return parseObject(text, (Type)clazz, ParserConfig.global, processor, JSON.DEFAULT_PARSER_FEATURE, features);
    }
    
    public static <T> T parseObject(final String json, final Type type, final Feature... features) {
        return parseObject(json, type, ParserConfig.global, JSON.DEFAULT_PARSER_FEATURE, features);
    }
    
    public static <T> T parseObject(final String input, final Type clazz, final ParseProcess processor, final Feature... features) {
        return parseObject(input, clazz, ParserConfig.global, processor, JSON.DEFAULT_PARSER_FEATURE, features);
    }
    
    public static <T> T parseObject(final String input, final Type clazz, int featureValues, final Feature... features) {
        if (input == null) {
            return null;
        }
        for (final Feature feature : features) {
            featureValues = Feature.config(featureValues, feature, true);
        }
        final DefaultJSONParser parser = new DefaultJSONParser(input, ParserConfig.getGlobalInstance(), featureValues);
        final T value = parser.parseObject(clazz);
        parser.handleResovleTask(value);
        parser.close();
        return value;
    }
    
    public static <T> T parseObject(final String input, final Type clazz, final ParserConfig config, final Feature... features) {
        return parseObject(input, clazz, config, null, JSON.DEFAULT_PARSER_FEATURE, features);
    }
    
    public static <T> T parseObject(final String input, final Type clazz, final ParserConfig config, final int featureValues, final Feature... features) {
        return parseObject(input, clazz, config, null, featureValues, features);
    }
    
    public static <T> T parseObject(final String input, final Type clazz, final ParserConfig config, final ParseProcess processor, int featureValues, final Feature... features) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        if (features != null) {
            for (final Feature feature : features) {
                featureValues |= feature.mask;
            }
        }
        final DefaultJSONParser parser = new DefaultJSONParser(input, config, featureValues);
        if (processor != null) {
            if (processor instanceof ExtraTypeProvider) {
                parser.getExtraTypeProviders().add((ExtraTypeProvider)processor);
            }
            if (processor instanceof ExtraProcessor) {
                parser.getExtraProcessors().add((ExtraProcessor)processor);
            }
            if (processor instanceof FieldTypeResolver) {
                parser.setFieldTypeResolver((FieldTypeResolver)processor);
            }
        }
        final T value = parser.parseObject(clazz, null);
        parser.handleResovleTask(value);
        parser.close();
        return value;
    }
    
    public static <T> T parseObject(final byte[] bytes, final Type clazz, final Feature... features) {
        return parseObject(bytes, 0, bytes.length, IOUtils.UTF8, clazz, features);
    }
    
    public static <T> T parseObject(final byte[] bytes, final int offset, final int len, final Charset charset, final Type clazz, final Feature... features) {
        return parseObject(bytes, offset, len, charset, clazz, ParserConfig.global, null, JSON.DEFAULT_PARSER_FEATURE, features);
    }
    
    public static <T> T parseObject(final byte[] bytes, final Charset charset, final Type clazz, final ParserConfig config, final ParseProcess processor, final int featureValues, final Feature... features) {
        return parseObject(bytes, 0, bytes.length, charset, clazz, config, processor, featureValues, features);
    }
    
    public static <T> T parseObject(final byte[] bytes, final int offset, final int len, Charset charset, final Type clazz, final ParserConfig config, final ParseProcess processor, final int featureValues, final Feature... features) {
        if (charset == null) {
            charset = IOUtils.UTF8;
        }
        String strVal;
        if (charset == IOUtils.UTF8) {
            final char[] chars = allocateChars(bytes.length);
            final int chars_len = IOUtils.decodeUTF8(bytes, offset, len, chars);
            if (chars_len < 0) {
                return null;
            }
            strVal = new String(chars, 0, chars_len);
        }
        else {
            if (len < 0) {
                return null;
            }
            strVal = new String(bytes, offset, len, charset);
        }
        return parseObject(strVal, clazz, config, processor, featureValues, features);
    }
    
    public static <T> T parseObject(final byte[] input, final int off, final int len, final CharsetDecoder charsetDecoder, final Type clazz, final Feature... features) {
        charsetDecoder.reset();
        final int scaleLength = (int)(len * (double)charsetDecoder.maxCharsPerByte());
        final char[] chars = allocateChars(scaleLength);
        final ByteBuffer byteBuf = ByteBuffer.wrap(input, off, len);
        final CharBuffer charByte = CharBuffer.wrap(chars);
        IOUtils.decode(charsetDecoder, byteBuf, charByte);
        final int position = charByte.position();
        return parseObject(chars, position, clazz, features);
    }
    
    public static <T> T parseObject(final char[] input, final int length, final Type clazz, final Feature... features) {
        if (input == null || input.length == 0) {
            return null;
        }
        int featureValues = JSON.DEFAULT_PARSER_FEATURE;
        for (final Feature feature : features) {
            featureValues = Feature.config(featureValues, feature, true);
        }
        final DefaultJSONParser parser = new DefaultJSONParser(input, length, ParserConfig.getGlobalInstance(), featureValues);
        final T value = parser.parseObject(clazz);
        parser.handleResovleTask(value);
        parser.close();
        return value;
    }
    
    public static <T> T parseObject(final InputStream is, final Type type, final Feature... features) throws IOException {
        return parseObject(is, IOUtils.UTF8, type, features);
    }
    
    public static <T> T parseObject(final InputStream is, final Charset charset, final Type type, final Feature... features) throws IOException {
        return parseObject(is, charset, type, ParserConfig.global, features);
    }
    
    public static <T> T parseObject(final InputStream is, final Charset charset, final Type type, final ParserConfig config, final Feature... features) throws IOException {
        return parseObject(is, charset, type, config, null, JSON.DEFAULT_PARSER_FEATURE, features);
    }
    
    public static <T> T parseObject(final InputStream is, Charset charset, final Type type, final ParserConfig config, final ParseProcess processor, final int featureValues, final Feature... features) throws IOException {
        if (charset == null) {
            charset = IOUtils.UTF8;
        }
        byte[] bytes = allocateBytes(65536);
        int offset = 0;
        while (true) {
            final int readCount = is.read(bytes, offset, bytes.length - offset);
            if (readCount == -1) {
                break;
            }
            offset += readCount;
            if (offset != bytes.length) {
                continue;
            }
            final byte[] newBytes = new byte[bytes.length * 3 / 2];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            bytes = newBytes;
        }
        return parseObject(bytes, 0, offset, charset, type, config, processor, featureValues, features);
    }
    
    public static <T> T parseObject(final String text, final Class<T> clazz) {
        return parseObject(text, clazz, new Feature[0]);
    }
    
    public static JSONArray parseArray(final String text) {
        if (text == null) {
            return null;
        }
        final DefaultJSONParser parser = new DefaultJSONParser(text, ParserConfig.getGlobalInstance());
        final JSONLexer lexer = parser.lexer;
        JSONArray array;
        if (lexer.token() == 8) {
            lexer.nextToken();
            array = null;
        }
        else if (lexer.token() == 20) {
            array = null;
        }
        else {
            array = new JSONArray();
            parser.parseArray(array);
            parser.handleResovleTask(array);
        }
        parser.close();
        return array;
    }
    
    public static <T> List<T> parseArray(final String text, final Class<T> clazz) {
        if (text == null) {
            return null;
        }
        final DefaultJSONParser parser = new DefaultJSONParser(text, ParserConfig.getGlobalInstance());
        final JSONLexer lexer = parser.lexer;
        final int token = lexer.token();
        List<T> list;
        if (token == 8) {
            lexer.nextToken();
            list = null;
        }
        else if (token == 20 && lexer.isBlankInput()) {
            list = null;
        }
        else {
            list = new ArrayList<T>();
            parser.parseArray(clazz, list);
            parser.handleResovleTask(list);
        }
        parser.close();
        return list;
    }
    
    public static List<Object> parseArray(final String text, final Type[] types) {
        if (text == null) {
            return null;
        }
        final DefaultJSONParser parser = new DefaultJSONParser(text, ParserConfig.getGlobalInstance());
        final Object[] objectArray = parser.parseArray(types);
        List<Object> list;
        if (objectArray == null) {
            list = null;
        }
        else {
            list = Arrays.asList(objectArray);
        }
        parser.handleResovleTask(list);
        parser.close();
        return list;
    }
    
    public static String toJSONString(final Object object) {
        return toJSONString(object, JSON.emptyFilters, new SerializerFeature[0]);
    }
    
    public static String toJSONString(final Object object, final SerializerFeature... features) {
        return toJSONString(object, JSON.DEFAULT_GENERATE_FEATURE, features);
    }
    
    public static String toJSONString(final Object object, final int defaultFeatures, final SerializerFeature... features) {
        final SerializeWriter out = new SerializeWriter(null, defaultFeatures, features);
        try {
            final JSONSerializer serializer = new JSONSerializer(out);
            serializer.write(object);
            return out.toString();
        }
        finally {
            out.close();
        }
    }
    
    public static String toJSONStringWithDateFormat(final Object object, final String dateFormat, final SerializerFeature... features) {
        return toJSONString(object, SerializeConfig.globalInstance, null, dateFormat, JSON.DEFAULT_GENERATE_FEATURE, features);
    }
    
    public static String toJSONString(final Object object, final SerializeFilter filter, final SerializerFeature... features) {
        return toJSONString(object, SerializeConfig.globalInstance, new SerializeFilter[] { filter }, null, JSON.DEFAULT_GENERATE_FEATURE, features);
    }
    
    public static String toJSONString(final Object object, final SerializeFilter[] filters, final SerializerFeature... features) {
        return toJSONString(object, SerializeConfig.globalInstance, filters, null, JSON.DEFAULT_GENERATE_FEATURE, features);
    }
    
    public static byte[] toJSONBytes(final Object object, final SerializerFeature... features) {
        return toJSONBytes(object, JSON.DEFAULT_GENERATE_FEATURE, features);
    }
    
    public static byte[] toJSONBytes(final Object object, final SerializeFilter filter, final SerializerFeature... features) {
        return toJSONBytes(object, SerializeConfig.globalInstance, new SerializeFilter[] { filter }, JSON.DEFAULT_GENERATE_FEATURE, features);
    }
    
    public static byte[] toJSONBytes(final Object object, final int defaultFeatures, final SerializerFeature... features) {
        return toJSONBytes(object, SerializeConfig.globalInstance, defaultFeatures, features);
    }
    
    public static String toJSONString(final Object object, final SerializeConfig config, final SerializerFeature... features) {
        return toJSONString(object, config, (SerializeFilter)null, features);
    }
    
    public static String toJSONString(final Object object, final SerializeConfig config, final SerializeFilter filter, final SerializerFeature... features) {
        return toJSONString(object, config, new SerializeFilter[] { filter }, null, JSON.DEFAULT_GENERATE_FEATURE, features);
    }
    
    public static String toJSONString(final Object object, final SerializeConfig config, final SerializeFilter[] filters, final SerializerFeature... features) {
        return toJSONString(object, config, filters, null, JSON.DEFAULT_GENERATE_FEATURE, features);
    }
    
    public static String toJSONString(final Object object, final SerializeConfig config, final SerializeFilter[] filters, final String dateFormat, final int defaultFeatures, final SerializerFeature... features) {
        final SerializeWriter out = new SerializeWriter(null, defaultFeatures, features);
        try {
            final JSONSerializer serializer = new JSONSerializer(out, config);
            if (dateFormat != null && dateFormat.length() != 0) {
                serializer.setDateFormat(dateFormat);
                serializer.config(SerializerFeature.WriteDateUseDateFormat, true);
            }
            if (filters != null) {
                for (final SerializeFilter filter : filters) {
                    serializer.addFilter(filter);
                }
            }
            serializer.write(object);
            return out.toString();
        }
        finally {
            out.close();
        }
    }
    
    @Deprecated
    public static String toJSONStringZ(final Object object, final SerializeConfig mapping, final SerializerFeature... features) {
        return toJSONString(object, mapping, JSON.emptyFilters, null, 0, features);
    }
    
    public static byte[] toJSONBytes(final Object object, final SerializeConfig config, final SerializerFeature... features) {
        return toJSONBytes(object, config, JSON.emptyFilters, JSON.DEFAULT_GENERATE_FEATURE, features);
    }
    
    public static byte[] toJSONBytes(final Object object, final SerializeConfig config, final int defaultFeatures, final SerializerFeature... features) {
        return toJSONBytes(object, config, JSON.emptyFilters, defaultFeatures, features);
    }
    
    public static byte[] toJSONBytes(final Object object, final SerializeFilter[] filters, final SerializerFeature... features) {
        return toJSONBytes(object, SerializeConfig.globalInstance, filters, JSON.DEFAULT_GENERATE_FEATURE, features);
    }
    
    public static byte[] toJSONBytes(final Object object, final SerializeConfig config, final SerializeFilter filter, final SerializerFeature... features) {
        return toJSONBytes(object, config, new SerializeFilter[] { filter }, JSON.DEFAULT_GENERATE_FEATURE, features);
    }
    
    public static byte[] toJSONBytes(final Object object, final SerializeConfig config, final SerializeFilter[] filters, final int defaultFeatures, final SerializerFeature... features) {
        return toJSONBytes(object, config, filters, null, defaultFeatures, features);
    }
    
    public static byte[] toJSONBytes(final Object object, final SerializeConfig config, final SerializeFilter[] filters, final String dateFormat, final int defaultFeatures, final SerializerFeature... features) {
        return toJSONBytes(IOUtils.UTF8, object, config, filters, dateFormat, defaultFeatures, features);
    }
    
    public static byte[] toJSONBytes(final Charset charset, final Object object, final SerializeConfig config, final SerializeFilter[] filters, final String dateFormat, final int defaultFeatures, final SerializerFeature... features) {
        final SerializeWriter out = new SerializeWriter(null, defaultFeatures, features);
        try {
            final JSONSerializer serializer = new JSONSerializer(out, config);
            if (dateFormat != null && dateFormat.length() != 0) {
                serializer.setDateFormat(dateFormat);
                serializer.config(SerializerFeature.WriteDateUseDateFormat, true);
            }
            if (filters != null) {
                for (final SerializeFilter filter : filters) {
                    serializer.addFilter(filter);
                }
            }
            serializer.write(object);
            return out.toBytes(charset);
        }
        finally {
            out.close();
        }
    }
    
    public static String toJSONString(final Object object, final boolean prettyFormat) {
        if (!prettyFormat) {
            return toJSONString(object);
        }
        return toJSONString(object, SerializerFeature.PrettyFormat);
    }
    
    @Deprecated
    public static void writeJSONStringTo(final Object object, final Writer writer, final SerializerFeature... features) {
        writeJSONString(writer, object, features);
    }
    
    public static void writeJSONString(final Writer writer, final Object object, final SerializerFeature... features) {
        writeJSONString(writer, object, JSON.DEFAULT_GENERATE_FEATURE, features);
    }
    
    public static void writeJSONString(final Writer writer, final Object object, final int defaultFeatures, final SerializerFeature... features) {
        final SerializeWriter out = new SerializeWriter(writer, defaultFeatures, features);
        try {
            final JSONSerializer serializer = new JSONSerializer(out);
            serializer.write(object);
        }
        finally {
            out.close();
        }
    }
    
    public static final int writeJSONString(final OutputStream os, final Object object, final SerializerFeature... features) throws IOException {
        return writeJSONString(os, object, JSON.DEFAULT_GENERATE_FEATURE, features);
    }
    
    public static final int writeJSONString(final OutputStream os, final Object object, final int defaultFeatures, final SerializerFeature... features) throws IOException {
        return writeJSONString(os, IOUtils.UTF8, object, SerializeConfig.globalInstance, null, null, defaultFeatures, features);
    }
    
    public static final int writeJSONString(final OutputStream os, final Charset charset, final Object object, final SerializerFeature... features) throws IOException {
        return writeJSONString(os, charset, object, SerializeConfig.globalInstance, null, null, JSON.DEFAULT_GENERATE_FEATURE, features);
    }
    
    public static final int writeJSONString(final OutputStream os, final Charset charset, final Object object, final SerializeConfig config, final SerializeFilter[] filters, final String dateFormat, final int defaultFeatures, final SerializerFeature... features) throws IOException {
        final SerializeWriter writer = new SerializeWriter(null, defaultFeatures, features);
        try {
            final JSONSerializer serializer = new JSONSerializer(writer, config);
            if (dateFormat != null && dateFormat.length() != 0) {
                serializer.setDateFormat(dateFormat);
                serializer.config(SerializerFeature.WriteDateUseDateFormat, true);
            }
            if (filters != null) {
                for (final SerializeFilter filter : filters) {
                    serializer.addFilter(filter);
                }
            }
            serializer.write(object);
            final int len = writer.writeToEx(os, charset);
            return len;
        }
        finally {
            writer.close();
        }
    }
    
    @Override
    public String toString() {
        return this.toJSONString();
    }
    
    @Override
    public String toJSONString() {
        final SerializeWriter out = new SerializeWriter();
        try {
            new JSONSerializer(out).write(this);
            return out.toString();
        }
        finally {
            out.close();
        }
    }
    
    public String toString(final SerializerFeature... features) {
        final SerializeWriter out = new SerializeWriter(null, JSON.DEFAULT_GENERATE_FEATURE, features);
        try {
            new JSONSerializer(out).write(this);
            return out.toString();
        }
        finally {
            out.close();
        }
    }
    
    @Override
    public void writeJSONString(final Appendable appendable) {
        final SerializeWriter out = new SerializeWriter();
        try {
            new JSONSerializer(out).write(this);
            appendable.append(out.toString());
        }
        catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
        finally {
            out.close();
        }
    }
    
    public static Object toJSON(final Object javaObject) {
        return toJSON(javaObject, SerializeConfig.globalInstance);
    }
    
    @Deprecated
    public static Object toJSON(final Object javaObject, final ParserConfig parserConfig) {
        return toJSON(javaObject, SerializeConfig.globalInstance);
    }
    
    public static Object toJSON(final Object javaObject, final SerializeConfig config) {
        if (javaObject == null) {
            return null;
        }
        if (javaObject instanceof JSON) {
            return javaObject;
        }
        if (javaObject instanceof Map) {
            final Map<Object, Object> map = (Map<Object, Object>)javaObject;
            final int size = map.size();
            Map innerMap;
            if (map instanceof LinkedHashMap) {
                innerMap = new LinkedHashMap(size);
            }
            else if (map instanceof TreeMap) {
                innerMap = new TreeMap();
            }
            else {
                innerMap = new HashMap(size);
            }
            final JSONObject json = new JSONObject(innerMap);
            for (final Map.Entry<Object, Object> entry : map.entrySet()) {
                final Object key = entry.getKey();
                final String jsonKey = TypeUtils.castToString(key);
                final Object jsonValue = toJSON(entry.getValue(), config);
                json.put(jsonKey, jsonValue);
            }
            return json;
        }
        if (javaObject instanceof Collection) {
            final Collection<Object> collection = (Collection<Object>)javaObject;
            final JSONArray array = new JSONArray(collection.size());
            for (final Object item : collection) {
                final Object jsonValue2 = toJSON(item, config);
                array.add(jsonValue2);
            }
            return array;
        }
        if (javaObject instanceof JSONSerializable) {
            final String json2 = toJSONString(javaObject);
            return parse(json2);
        }
        final Class<?> clazz = javaObject.getClass();
        if (clazz.isEnum()) {
            return ((Enum)javaObject).name();
        }
        if (clazz.isArray()) {
            final int len = Array.getLength(javaObject);
            final JSONArray array2 = new JSONArray(len);
            for (int i = 0; i < len; ++i) {
                final Object item2 = Array.get(javaObject, i);
                final Object jsonValue3 = toJSON(item2);
                array2.add(jsonValue3);
            }
            return array2;
        }
        if (ParserConfig.isPrimitive2(clazz)) {
            return javaObject;
        }
        final ObjectSerializer serializer = config.getObjectWriter(clazz);
        if (serializer instanceof JavaBeanSerializer) {
            final JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer)serializer;
            final JSONObject json = new JSONObject();
            try {
                final Map<String, Object> values = javaBeanSerializer.getFieldValuesMap(javaObject);
                for (final Map.Entry<String, Object> entry2 : values.entrySet()) {
                    json.put(entry2.getKey(), toJSON(entry2.getValue(), config));
                }
            }
            catch (Exception e) {
                throw new JSONException("toJSON error", e);
            }
            return json;
        }
        final String text = toJSONString(javaObject);
        return parse(text);
    }
    
    public static <T> T toJavaObject(final JSON json, final Class<T> clazz) {
        return TypeUtils.cast(json, clazz, ParserConfig.getGlobalInstance());
    }
    
    public <T> T toJavaObject(final Class<T> clazz) {
        if (clazz == JSONArray.class || clazz == JSON.class || clazz == Collection.class || clazz == List.class) {
            return (T)this;
        }
        return TypeUtils.cast(this, clazz, ParserConfig.getGlobalInstance());
    }
    
    public <T> T toJavaObject(final Type type) {
        return TypeUtils.cast(this, type, ParserConfig.getGlobalInstance());
    }
    
    public <T> T toJavaObject(final TypeReference typeReference) {
        final Type type = (typeReference != null) ? typeReference.getType() : null;
        return TypeUtils.cast(this, type, ParserConfig.getGlobalInstance());
    }
    
    private static byte[] allocateBytes(final int length) {
        byte[] chars = JSON.bytesLocal.get();
        if (chars == null) {
            if (length <= 65536) {
                chars = new byte[65536];
                JSON.bytesLocal.set(chars);
            }
            else {
                chars = new byte[length];
            }
        }
        else if (chars.length < length) {
            chars = new byte[length];
        }
        return chars;
    }
    
    private static char[] allocateChars(final int length) {
        char[] chars = JSON.charsLocal.get();
        if (chars == null) {
            if (length <= 65536) {
                chars = new char[65536];
                JSON.charsLocal.set(chars);
            }
            else {
                chars = new char[length];
            }
        }
        else if (chars.length < length) {
            chars = new char[length];
        }
        return chars;
    }
    
    @Deprecated
    public static boolean isValid(final String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        final JSONScanner lexer = new JSONScanner(str);
        try {
            lexer.nextToken();
            final int token = lexer.token();
            switch (token) {
                case 12: {
                    if (lexer.getCurrent() == '\u001a') {
                        return false;
                    }
                    lexer.skipObject(true);
                    break;
                }
                case 14: {
                    lexer.skipArray(true);
                    break;
                }
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8: {
                    lexer.nextToken();
                    break;
                }
                default: {
                    return false;
                }
            }
            return lexer.token() == 20;
        }
        catch (Exception ex) {
            return false;
        }
        finally {
            lexer.close();
        }
    }
    
    @Deprecated
    public static boolean isValidObject(final String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        final JSONScanner lexer = new JSONScanner(str);
        try {
            lexer.nextToken();
            final int token = lexer.token();
            if (token != 12) {
                return false;
            }
            if (lexer.getCurrent() == '\u001a') {
                return false;
            }
            lexer.skipObject(true);
            return lexer.token() == 20;
        }
        catch (Exception ex) {
            return false;
        }
        finally {
            lexer.close();
        }
    }
    
    @Deprecated
    public static boolean isValidArray(final String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        final JSONScanner lexer = new JSONScanner(str);
        try {
            lexer.nextToken();
            final int token = lexer.token();
            if (token == 14) {
                lexer.skipArray(true);
                return lexer.token() == 20;
            }
            return false;
        }
        catch (Exception ex) {
            return false;
        }
        finally {
            lexer.close();
        }
    }
    
    public static <T> void handleResovleTask(final DefaultJSONParser parser, final T value) {
        parser.handleResovleTask(value);
    }
    
    public static void addMixInAnnotations(final Type target, final Type mixinSource) {
        if (target != null && mixinSource != null) {
            JSON.mixInsMapper.put(target, mixinSource);
        }
    }
    
    public static void removeMixInAnnotations(final Type target) {
        if (target != null) {
            JSON.mixInsMapper.remove(target);
        }
    }
    
    public static void clearMixInAnnotations() {
        JSON.mixInsMapper.clear();
    }
    
    public static Type getMixInAnnotations(final Type target) {
        if (target != null) {
            return JSON.mixInsMapper.get(target);
        }
        return null;
    }
    
    static {
        JSON.defaultTimeZone = TimeZone.getDefault();
        JSON.defaultLocale = Locale.getDefault();
        JSON.DEFAULT_TYPE_KEY = "@type";
        emptyFilters = new SerializeFilter[0];
        JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        mixInsMapper = new ConcurrentHashMap<Type, Type>(16);
        int features = 0;
        features |= Feature.AutoCloseSource.getMask();
        features |= Feature.InternFieldNames.getMask();
        features |= Feature.UseBigDecimal.getMask();
        features |= Feature.AllowUnQuotedFieldNames.getMask();
        features |= Feature.AllowSingleQuotes.getMask();
        features |= Feature.AllowArbitraryCommas.getMask();
        features |= Feature.SortFeidFastMatch.getMask();
        features = (JSON.DEFAULT_PARSER_FEATURE = (features | Feature.IgnoreNotMatch.getMask()));
        features = 0;
        features |= SerializerFeature.QuoteFieldNames.getMask();
        features |= SerializerFeature.SkipTransientField.getMask();
        features |= SerializerFeature.WriteEnumUsingName.getMask();
        features = (JSON.DEFAULT_GENERATE_FEATURE = (features | SerializerFeature.SortField.getMask()));
        config(IOUtils.DEFAULT_PROPERTIES);
        bytesLocal = new ThreadLocal<byte[]>();
        charsLocal = new ThreadLocal<char[]>();
    }
}
