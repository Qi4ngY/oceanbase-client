package com.alibaba.fastjson.support.jaxrs;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.Writer;
import java.io.Reader;
import javax.ws.rs.ext.ContextResolver;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import com.alibaba.fastjson.JSONException;
import javax.ws.rs.WebApplicationException;
import com.alibaba.fastjson.JSON;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.OutputStream;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Type;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Providers;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.nio.charset.Charset;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.MessageBodyReader;

@Provider
@Consumes({ "*/*" })
@Produces({ "*/*" })
public class FastJsonProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object>
{
    public static final Class<?>[] DEFAULT_UNREADABLES;
    public static final Class<?>[] DEFAULT_UNWRITABLES;
    @Deprecated
    protected Charset charset;
    @Deprecated
    protected SerializerFeature[] features;
    @Deprecated
    protected SerializeFilter[] filters;
    @Deprecated
    protected String dateFormat;
    @Context
    protected Providers providers;
    private FastJsonConfig fastJsonConfig;
    private Class<?>[] clazzes;
    private boolean pretty;
    
    public FastJsonConfig getFastJsonConfig() {
        return this.fastJsonConfig;
    }
    
    public void setFastJsonConfig(final FastJsonConfig fastJsonConfig) {
        this.fastJsonConfig = fastJsonConfig;
    }
    
    public FastJsonProvider() {
        this.charset = Charset.forName("UTF-8");
        this.features = new SerializerFeature[0];
        this.filters = new SerializeFilter[0];
        this.fastJsonConfig = new FastJsonConfig();
        this.clazzes = null;
    }
    
    public FastJsonProvider(final Class<?>[] clazzes) {
        this.charset = Charset.forName("UTF-8");
        this.features = new SerializerFeature[0];
        this.filters = new SerializeFilter[0];
        this.fastJsonConfig = new FastJsonConfig();
        this.clazzes = null;
        this.clazzes = clazzes;
    }
    
    public FastJsonProvider setPretty(final boolean p) {
        this.pretty = p;
        return this;
    }
    
    @Deprecated
    public FastJsonProvider(final String charset) {
        this.charset = Charset.forName("UTF-8");
        this.features = new SerializerFeature[0];
        this.filters = new SerializeFilter[0];
        this.fastJsonConfig = new FastJsonConfig();
        this.clazzes = null;
        this.fastJsonConfig.setCharset(Charset.forName(charset));
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
    
    protected boolean isAssignableFrom(final Class<?> type, final Class<?>[] classes) {
        if (type == null) {
            return false;
        }
        for (final Class<?> cls : classes) {
            if (cls.isAssignableFrom(type)) {
                return false;
            }
        }
        return true;
    }
    
    protected boolean isValidType(final Class<?> type, final Annotation[] classAnnotations) {
        if (type == null) {
            return false;
        }
        if (this.clazzes != null) {
            for (final Class<?> cls : this.clazzes) {
                if (cls == type) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    
    protected boolean hasMatchingMediaType(final MediaType mediaType) {
        if (mediaType != null) {
            final String subtype = mediaType.getSubtype();
            return "json".equalsIgnoreCase(subtype) || subtype.endsWith("+json") || "javascript".equals(subtype) || "x-javascript".equals(subtype) || "x-json".equals(subtype) || "x-www-form-urlencoded".equalsIgnoreCase(subtype) || subtype.endsWith("x-www-form-urlencoded");
        }
        return true;
    }
    
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return this.hasMatchingMediaType(mediaType) && this.isAssignableFrom(type, FastJsonProvider.DEFAULT_UNWRITABLES) && this.isValidType(type, annotations);
    }
    
    public long getSize(final Object t, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return -1L;
    }
    
    public void writeTo(final Object obj, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream) throws IOException, WebApplicationException {
        final FastJsonConfig fastJsonConfig = this.locateConfigProvider(type, mediaType);
        SerializerFeature[] serializerFeatures = fastJsonConfig.getSerializerFeatures();
        if (this.pretty) {
            if (serializerFeatures == null) {
                serializerFeatures = new SerializerFeature[] { SerializerFeature.PrettyFormat };
            }
            else {
                final List<SerializerFeature> featureList = new ArrayList<SerializerFeature>(Arrays.asList(serializerFeatures));
                featureList.add(SerializerFeature.PrettyFormat);
                serializerFeatures = featureList.toArray(serializerFeatures);
            }
            fastJsonConfig.setSerializerFeatures(serializerFeatures);
        }
        try {
            JSON.writeJSONString(entityStream, fastJsonConfig.getCharset(), obj, fastJsonConfig.getSerializeConfig(), fastJsonConfig.getSerializeFilters(), fastJsonConfig.getDateFormat(), JSON.DEFAULT_GENERATE_FEATURE, fastJsonConfig.getSerializerFeatures());
            entityStream.flush();
        }
        catch (JSONException ex) {
            throw new WebApplicationException((Throwable)ex);
        }
    }
    
    public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return this.hasMatchingMediaType(mediaType) && this.isAssignableFrom(type, FastJsonProvider.DEFAULT_UNREADABLES) && this.isValidType(type, annotations);
    }
    
    public Object readFrom(final Class<Object> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders, final InputStream entityStream) throws IOException, WebApplicationException {
        try {
            final FastJsonConfig fastJsonConfig = this.locateConfigProvider(type, mediaType);
            return JSON.parseObject(entityStream, fastJsonConfig.getCharset(), genericType, fastJsonConfig.getParserConfig(), fastJsonConfig.getParseProcess(), JSON.DEFAULT_PARSER_FEATURE, fastJsonConfig.getFeatures());
        }
        catch (JSONException ex) {
            throw new WebApplicationException((Throwable)ex);
        }
    }
    
    protected FastJsonConfig locateConfigProvider(final Class<?> type, final MediaType mediaType) {
        if (this.providers != null) {
            ContextResolver<FastJsonConfig> resolver = (ContextResolver<FastJsonConfig>)this.providers.getContextResolver((Class)FastJsonConfig.class, mediaType);
            if (resolver == null) {
                resolver = (ContextResolver<FastJsonConfig>)this.providers.getContextResolver((Class)FastJsonConfig.class, (MediaType)null);
            }
            if (resolver != null) {
                return (FastJsonConfig)resolver.getContext((Class)type);
            }
        }
        return this.fastJsonConfig;
    }
    
    static {
        DEFAULT_UNREADABLES = new Class[] { InputStream.class, Reader.class };
        DEFAULT_UNWRITABLES = new Class[] { InputStream.class, OutputStream.class, Writer.class, StreamingOutput.class, Response.class };
    }
}
