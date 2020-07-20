package com.alibaba.fastjson.support.spring;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import org.springframework.core.ResolvableType;
import java.util.List;
import org.springframework.http.HttpHeaders;
import java.io.OutputStream;
import com.alibaba.fastjson.JSONPObject;
import org.springframework.util.StringUtils;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSON;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.io.IOException;
import org.springframework.http.HttpInputMessage;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.AbstractHttpMessageConverter;

public class FastJsonHttpMessageConverter extends AbstractHttpMessageConverter<Object> implements GenericHttpMessageConverter<Object>
{
    public static final MediaType APPLICATION_JAVASCRIPT;
    @Deprecated
    protected SerializerFeature[] features;
    @Deprecated
    protected SerializeFilter[] filters;
    @Deprecated
    protected String dateFormat;
    private FastJsonConfig fastJsonConfig;
    
    public FastJsonConfig getFastJsonConfig() {
        return this.fastJsonConfig;
    }
    
    public void setFastJsonConfig(final FastJsonConfig fastJsonConfig) {
        this.fastJsonConfig = fastJsonConfig;
    }
    
    public FastJsonHttpMessageConverter() {
        super(MediaType.ALL);
        this.features = new SerializerFeature[0];
        this.filters = new SerializeFilter[0];
        this.fastJsonConfig = new FastJsonConfig();
    }
    
    @Deprecated
    public Charset getCharset() {
        return this.fastJsonConfig.getCharset();
    }
    
    @Deprecated
    public void setCharset(final Charset charset) {
        this.fastJsonConfig.setCharset(charset);
    }
    
    @Deprecated
    public String getDateFormat() {
        return this.fastJsonConfig.getDateFormat();
    }
    
    @Deprecated
    public void setDateFormat(final String dateFormat) {
        this.fastJsonConfig.setDateFormat(dateFormat);
    }
    
    @Deprecated
    public SerializerFeature[] getFeatures() {
        return this.fastJsonConfig.getSerializerFeatures();
    }
    
    @Deprecated
    public void setFeatures(final SerializerFeature... features) {
        this.fastJsonConfig.setSerializerFeatures(features);
    }
    
    @Deprecated
    public SerializeFilter[] getFilters() {
        return this.fastJsonConfig.getSerializeFilters();
    }
    
    @Deprecated
    public void setFilters(final SerializeFilter... filters) {
        this.fastJsonConfig.setSerializeFilters(filters);
    }
    
    @Deprecated
    public void addSerializeFilter(final SerializeFilter filter) {
        if (filter == null) {
            return;
        }
        final int length = this.fastJsonConfig.getSerializeFilters().length;
        final SerializeFilter[] filters = new SerializeFilter[length + 1];
        System.arraycopy(this.fastJsonConfig.getSerializeFilters(), 0, filters, 0, length);
        filters[filters.length - 1] = filter;
        this.fastJsonConfig.setSerializeFilters(filters);
    }
    
    protected boolean supports(final Class<?> clazz) {
        return true;
    }
    
    public boolean canRead(final Type type, final Class<?> contextClass, final MediaType mediaType) {
        return super.canRead((Class)contextClass, mediaType);
    }
    
    public boolean canWrite(final Type type, final Class<?> clazz, final MediaType mediaType) {
        return super.canWrite((Class)clazz, mediaType);
    }
    
    public Object read(final Type type, final Class<?> contextClass, final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return this.readType(this.getType(type, contextClass), inputMessage);
    }
    
    public void write(final Object o, final Type type, final MediaType contentType, final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        super.write(o, contentType, outputMessage);
    }
    
    protected Object readInternal(final Class<?> clazz, final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return this.readType(this.getType(clazz, null), inputMessage);
    }
    
    private Object readType(final Type type, final HttpInputMessage inputMessage) {
        try {
            final InputStream in = inputMessage.getBody();
            return JSON.parseObject(in, this.fastJsonConfig.getCharset(), type, this.fastJsonConfig.getParserConfig(), this.fastJsonConfig.getParseProcess(), JSON.DEFAULT_PARSER_FEATURE, this.fastJsonConfig.getFeatures());
        }
        catch (JSONException ex) {
            throw new HttpMessageNotReadableException("JSON parse error: " + ex.getMessage(), (Throwable)ex);
        }
        catch (IOException ex2) {
            throw new HttpMessageNotReadableException("I/O error while reading input message", (Throwable)ex2);
        }
    }
    
    protected void writeInternal(final Object object, final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        final ByteArrayOutputStream outnew = new ByteArrayOutputStream();
        try {
            final HttpHeaders headers = outputMessage.getHeaders();
            final SerializeFilter[] globalFilters = this.fastJsonConfig.getSerializeFilters();
            final List<SerializeFilter> allFilters = new ArrayList<SerializeFilter>(Arrays.asList(globalFilters));
            boolean isJsonp = false;
            Object value = this.strangeCodeForJackson(object);
            if (value instanceof FastJsonContainer) {
                final FastJsonContainer fastJsonContainer = (FastJsonContainer)value;
                final PropertyPreFilters filters = fastJsonContainer.getFilters();
                allFilters.addAll(filters.getFilters());
                value = fastJsonContainer.getValue();
            }
            if (value instanceof MappingFastJsonValue) {
                if (!StringUtils.isEmpty((Object)((MappingFastJsonValue)value).getJsonpFunction())) {
                    isJsonp = true;
                }
            }
            else if (value instanceof JSONPObject) {
                isJsonp = true;
            }
            final int len = JSON.writeJSONString(outnew, this.fastJsonConfig.getCharset(), value, this.fastJsonConfig.getSerializeConfig(), allFilters.toArray(new SerializeFilter[allFilters.size()]), this.fastJsonConfig.getDateFormat(), JSON.DEFAULT_GENERATE_FEATURE, this.fastJsonConfig.getSerializerFeatures());
            if (isJsonp) {
                headers.setContentType(FastJsonHttpMessageConverter.APPLICATION_JAVASCRIPT);
            }
            if (this.fastJsonConfig.isWriteContentLength()) {
                headers.setContentLength((long)len);
            }
            outnew.writeTo(outputMessage.getBody());
        }
        catch (JSONException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), (Throwable)ex);
        }
        finally {
            outnew.close();
        }
    }
    
    private Object strangeCodeForJackson(final Object obj) {
        if (obj != null) {
            final String className = obj.getClass().getName();
            if ("com.fasterxml.jackson.databind.node.ObjectNode".equals(className)) {
                return obj.toString();
            }
        }
        return obj;
    }
    
    protected Type getType(final Type type, final Class<?> contextClass) {
        if (isSupport()) {
            return getType(type, contextClass);
        }
        return type;
    }
    
    static {
        APPLICATION_JAVASCRIPT = new MediaType("application", "javascript");
    }
    
    private static class Spring4TypeResolvableHelper
    {
        private static boolean hasClazzResolvableType;
        
        private static boolean isSupport() {
            return Spring4TypeResolvableHelper.hasClazzResolvableType;
        }
        
        private static Type getType(final Type type, final Class<?> contextClass) {
            if (contextClass != null) {
                final ResolvableType resolvedType = ResolvableType.forType(type);
                if (type instanceof TypeVariable) {
                    final ResolvableType resolvedTypeVariable = resolveVariable((TypeVariable<?>)type, ResolvableType.forClass((Class)contextClass));
                    if (resolvedTypeVariable != ResolvableType.NONE) {
                        return resolvedTypeVariable.resolve();
                    }
                }
                else if (type instanceof ParameterizedType && resolvedType.hasUnresolvableGenerics()) {
                    final ParameterizedType parameterizedType = (ParameterizedType)type;
                    final Class<?>[] generics = (Class<?>[])new Class[parameterizedType.getActualTypeArguments().length];
                    final Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    for (int i = 0; i < typeArguments.length; ++i) {
                        final Type typeArgument = typeArguments[i];
                        if (typeArgument instanceof TypeVariable) {
                            final ResolvableType resolvedTypeArgument = resolveVariable((TypeVariable<?>)typeArgument, ResolvableType.forClass((Class)contextClass));
                            if (resolvedTypeArgument != ResolvableType.NONE) {
                                generics[i] = (Class<?>)resolvedTypeArgument.resolve();
                            }
                            else {
                                generics[i] = (Class<?>)ResolvableType.forType(typeArgument).resolve();
                            }
                        }
                        else {
                            generics[i] = (Class<?>)ResolvableType.forType(typeArgument).resolve();
                        }
                    }
                    return ResolvableType.forClassWithGenerics(resolvedType.getRawClass(), (Class[])generics).getType();
                }
            }
            return type;
        }
        
        private static ResolvableType resolveVariable(final TypeVariable<?> typeVariable, final ResolvableType contextType) {
            if (contextType.hasGenerics()) {
                final ResolvableType resolvedType = ResolvableType.forType((Type)typeVariable, contextType);
                if (resolvedType.resolve() != null) {
                    return resolvedType;
                }
            }
            final ResolvableType superType = contextType.getSuperType();
            if (superType != ResolvableType.NONE) {
                final ResolvableType resolvedType = resolveVariable(typeVariable, superType);
                if (resolvedType.resolve() != null) {
                    return resolvedType;
                }
            }
            for (final ResolvableType ifc : contextType.getInterfaces()) {
                final ResolvableType resolvedType = resolveVariable(typeVariable, ifc);
                if (resolvedType.resolve() != null) {
                    return resolvedType;
                }
            }
            return ResolvableType.NONE;
        }
        
        static {
            try {
                Class.forName("org.springframework.core.ResolvableType");
                Spring4TypeResolvableHelper.hasClazzResolvableType = true;
            }
            catch (ClassNotFoundException e) {
                Spring4TypeResolvableHelper.hasClazzResolvableType = false;
            }
        }
    }
}
