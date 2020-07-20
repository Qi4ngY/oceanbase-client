package com.alibaba.fastjson.support.retrofit;

import java.io.IOException;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import okhttp3.MediaType;
import retrofit2.Converter;

public class Retrofit2ConverterFactory extends Converter.Factory
{
    private static final MediaType MEDIA_TYPE;
    private FastJsonConfig fastJsonConfig;
    @Deprecated
    private static final Feature[] EMPTY_SERIALIZER_FEATURES;
    @Deprecated
    private ParserConfig parserConfig;
    @Deprecated
    private int featureValues;
    @Deprecated
    private Feature[] features;
    @Deprecated
    private SerializeConfig serializeConfig;
    @Deprecated
    private SerializerFeature[] serializerFeatures;
    
    public Retrofit2ConverterFactory() {
        this.parserConfig = ParserConfig.getGlobalInstance();
        this.featureValues = JSON.DEFAULT_PARSER_FEATURE;
        this.fastJsonConfig = new FastJsonConfig();
    }
    
    public Retrofit2ConverterFactory(final FastJsonConfig fastJsonConfig) {
        this.parserConfig = ParserConfig.getGlobalInstance();
        this.featureValues = JSON.DEFAULT_PARSER_FEATURE;
        this.fastJsonConfig = fastJsonConfig;
    }
    
    public static Retrofit2ConverterFactory create() {
        return create(new FastJsonConfig());
    }
    
    public static Retrofit2ConverterFactory create(final FastJsonConfig fastJsonConfig) {
        if (fastJsonConfig == null) {
            throw new NullPointerException("fastJsonConfig == null");
        }
        return new Retrofit2ConverterFactory(fastJsonConfig);
    }
    
    public Converter<ResponseBody, Object> responseBodyConverter(final Type type, final Annotation[] annotations, final Retrofit retrofit) {
        return (Converter<ResponseBody, Object>)new ResponseBodyConverter(type);
    }
    
    public Converter<Object, RequestBody> requestBodyConverter(final Type type, final Annotation[] parameterAnnotations, final Annotation[] methodAnnotations, final Retrofit retrofit) {
        return (Converter<Object, RequestBody>)new RequestBodyConverter();
    }
    
    public FastJsonConfig getFastJsonConfig() {
        return this.fastJsonConfig;
    }
    
    public Retrofit2ConverterFactory setFastJsonConfig(final FastJsonConfig fastJsonConfig) {
        this.fastJsonConfig = fastJsonConfig;
        return this;
    }
    
    @Deprecated
    public ParserConfig getParserConfig() {
        return this.fastJsonConfig.getParserConfig();
    }
    
    @Deprecated
    public Retrofit2ConverterFactory setParserConfig(final ParserConfig config) {
        this.fastJsonConfig.setParserConfig(config);
        return this;
    }
    
    @Deprecated
    public int getParserFeatureValues() {
        return JSON.DEFAULT_PARSER_FEATURE;
    }
    
    @Deprecated
    public Retrofit2ConverterFactory setParserFeatureValues(final int featureValues) {
        return this;
    }
    
    @Deprecated
    public Feature[] getParserFeatures() {
        return this.fastJsonConfig.getFeatures();
    }
    
    @Deprecated
    public Retrofit2ConverterFactory setParserFeatures(final Feature[] features) {
        this.fastJsonConfig.setFeatures(features);
        return this;
    }
    
    @Deprecated
    public SerializeConfig getSerializeConfig() {
        return this.fastJsonConfig.getSerializeConfig();
    }
    
    @Deprecated
    public Retrofit2ConverterFactory setSerializeConfig(final SerializeConfig serializeConfig) {
        this.fastJsonConfig.setSerializeConfig(serializeConfig);
        return this;
    }
    
    @Deprecated
    public SerializerFeature[] getSerializerFeatures() {
        return this.fastJsonConfig.getSerializerFeatures();
    }
    
    @Deprecated
    public Retrofit2ConverterFactory setSerializerFeatures(final SerializerFeature[] features) {
        this.fastJsonConfig.setSerializerFeatures(features);
        return this;
    }
    
    static {
        MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
        EMPTY_SERIALIZER_FEATURES = new Feature[0];
    }
    
    final class ResponseBodyConverter<T> implements Converter<ResponseBody, T>
    {
        private Type type;
        
        ResponseBodyConverter(final Type type) {
            this.type = type;
        }
        
        public T convert(final ResponseBody value) throws IOException {
            try {
                return JSON.parseObject(value.bytes(), Retrofit2ConverterFactory.this.fastJsonConfig.getCharset(), this.type, Retrofit2ConverterFactory.this.fastJsonConfig.getParserConfig(), Retrofit2ConverterFactory.this.fastJsonConfig.getParseProcess(), JSON.DEFAULT_PARSER_FEATURE, Retrofit2ConverterFactory.this.fastJsonConfig.getFeatures());
            }
            catch (Exception e) {
                throw new IOException("JSON parse error: " + e.getMessage(), e);
            }
            finally {
                value.close();
            }
        }
    }
    
    final class RequestBodyConverter<T> implements Converter<T, RequestBody>
    {
        public RequestBody convert(final T value) throws IOException {
            try {
                final byte[] content = JSON.toJSONBytes(Retrofit2ConverterFactory.this.fastJsonConfig.getCharset(), value, Retrofit2ConverterFactory.this.fastJsonConfig.getSerializeConfig(), Retrofit2ConverterFactory.this.fastJsonConfig.getSerializeFilters(), Retrofit2ConverterFactory.this.fastJsonConfig.getDateFormat(), JSON.DEFAULT_GENERATE_FEATURE, Retrofit2ConverterFactory.this.fastJsonConfig.getSerializerFeatures());
                return RequestBody.create(Retrofit2ConverterFactory.MEDIA_TYPE, content);
            }
            catch (Exception e) {
                throw new IOException("Could not write JSON: " + e.getMessage(), e);
            }
        }
    }
}
