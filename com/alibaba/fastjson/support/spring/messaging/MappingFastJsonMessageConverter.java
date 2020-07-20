package com.alibaba.fastjson.support.spring.messaging;

import java.lang.reflect.Type;
import com.alibaba.fastjson.JSON;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.Message;
import org.springframework.util.MimeType;
import java.nio.charset.Charset;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import org.springframework.messaging.converter.AbstractMessageConverter;

public class MappingFastJsonMessageConverter extends AbstractMessageConverter
{
    private FastJsonConfig fastJsonConfig;
    
    public FastJsonConfig getFastJsonConfig() {
        return this.fastJsonConfig;
    }
    
    public void setFastJsonConfig(final FastJsonConfig fastJsonConfig) {
        this.fastJsonConfig = fastJsonConfig;
    }
    
    public MappingFastJsonMessageConverter() {
        super(new MimeType("application", "json", Charset.forName("UTF-8")));
        this.fastJsonConfig = new FastJsonConfig();
    }
    
    protected boolean supports(final Class<?> clazz) {
        return true;
    }
    
    protected boolean canConvertFrom(final Message<?> message, final Class<?> targetClass) {
        return this.supports(targetClass);
    }
    
    protected boolean canConvertTo(final Object payload, final MessageHeaders headers) {
        return this.supports(payload.getClass());
    }
    
    protected Object convertFromInternal(final Message<?> message, final Class<?> targetClass, final Object conversionHint) {
        final Object payload = message.getPayload();
        Object obj = null;
        if (payload instanceof byte[]) {
            obj = JSON.parseObject((byte[])payload, this.fastJsonConfig.getCharset(), targetClass, this.fastJsonConfig.getParserConfig(), this.fastJsonConfig.getParseProcess(), JSON.DEFAULT_PARSER_FEATURE, this.fastJsonConfig.getFeatures());
        }
        else if (payload instanceof String) {
            obj = JSON.parseObject((String)payload, (Type)targetClass, this.fastJsonConfig.getParserConfig(), this.fastJsonConfig.getParseProcess(), JSON.DEFAULT_PARSER_FEATURE, this.fastJsonConfig.getFeatures());
        }
        return obj;
    }
    
    protected Object convertToInternal(final Object payload, final MessageHeaders headers, final Object conversionHint) {
        Object obj;
        if (byte[].class == this.getSerializedPayloadClass()) {
            if (payload instanceof String && JSON.isValid((String)payload)) {
                obj = ((String)payload).getBytes(this.fastJsonConfig.getCharset());
            }
            else {
                obj = JSON.toJSONBytes(this.fastJsonConfig.getCharset(), payload, this.fastJsonConfig.getSerializeConfig(), this.fastJsonConfig.getSerializeFilters(), this.fastJsonConfig.getDateFormat(), JSON.DEFAULT_GENERATE_FEATURE, this.fastJsonConfig.getSerializerFeatures());
            }
        }
        else if (payload instanceof String && JSON.isValid((String)payload)) {
            obj = payload;
        }
        else {
            obj = JSON.toJSONString(payload, this.fastJsonConfig.getSerializeConfig(), this.fastJsonConfig.getSerializeFilters(), this.fastJsonConfig.getDateFormat(), JSON.DEFAULT_GENERATE_FEATURE, this.fastJsonConfig.getSerializerFeatures());
        }
        return obj;
    }
}
