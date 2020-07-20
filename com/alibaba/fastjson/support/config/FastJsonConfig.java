package com.alibaba.fastjson.support.config;

import java.util.Iterator;
import com.alibaba.fastjson.util.IOUtils;
import java.util.Map;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.parser.deserializer.ParseProcess;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import java.nio.charset.Charset;

public class FastJsonConfig
{
    private Charset charset;
    private SerializeConfig serializeConfig;
    private ParserConfig parserConfig;
    private ParseProcess parseProcess;
    private SerializerFeature[] serializerFeatures;
    private SerializeFilter[] serializeFilters;
    private Feature[] features;
    private Map<Class<?>, SerializeFilter> classSerializeFilters;
    private String dateFormat;
    private boolean writeContentLength;
    
    public FastJsonConfig() {
        this.charset = IOUtils.UTF8;
        this.serializeConfig = SerializeConfig.getGlobalInstance();
        this.parserConfig = ParserConfig.getGlobalInstance();
        this.serializerFeatures = new SerializerFeature[] { SerializerFeature.BrowserSecure };
        this.serializeFilters = new SerializeFilter[0];
        this.features = new Feature[0];
        this.writeContentLength = true;
    }
    
    public SerializeConfig getSerializeConfig() {
        return this.serializeConfig;
    }
    
    public void setSerializeConfig(final SerializeConfig serializeConfig) {
        this.serializeConfig = serializeConfig;
    }
    
    public ParserConfig getParserConfig() {
        return this.parserConfig;
    }
    
    public void setParserConfig(final ParserConfig parserConfig) {
        this.parserConfig = parserConfig;
    }
    
    public SerializerFeature[] getSerializerFeatures() {
        return this.serializerFeatures;
    }
    
    public void setSerializerFeatures(final SerializerFeature... serializerFeatures) {
        this.serializerFeatures = serializerFeatures;
    }
    
    public SerializeFilter[] getSerializeFilters() {
        return this.serializeFilters;
    }
    
    public void setSerializeFilters(final SerializeFilter... serializeFilters) {
        this.serializeFilters = serializeFilters;
    }
    
    public Feature[] getFeatures() {
        return this.features;
    }
    
    public void setFeatures(final Feature... features) {
        this.features = features;
    }
    
    public Map<Class<?>, SerializeFilter> getClassSerializeFilters() {
        return this.classSerializeFilters;
    }
    
    public void setClassSerializeFilters(final Map<Class<?>, SerializeFilter> classSerializeFilters) {
        if (classSerializeFilters == null) {
            return;
        }
        for (final Map.Entry<Class<?>, SerializeFilter> entry : classSerializeFilters.entrySet()) {
            this.serializeConfig.addFilter(entry.getKey(), entry.getValue());
        }
        this.classSerializeFilters = classSerializeFilters;
    }
    
    public String getDateFormat() {
        return this.dateFormat;
    }
    
    public void setDateFormat(final String dateFormat) {
        this.dateFormat = dateFormat;
    }
    
    public Charset getCharset() {
        return this.charset;
    }
    
    public void setCharset(final Charset charset) {
        this.charset = charset;
    }
    
    public boolean isWriteContentLength() {
        return this.writeContentLength;
    }
    
    public void setWriteContentLength(final boolean writeContentLength) {
        this.writeContentLength = writeContentLength;
    }
    
    public ParseProcess getParseProcess() {
        return this.parseProcess;
    }
    
    public void setParseProcess(final ParseProcess parseProcess) {
        this.parseProcess = parseProcess;
    }
}
