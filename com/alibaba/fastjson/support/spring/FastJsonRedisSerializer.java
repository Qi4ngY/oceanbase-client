package com.alibaba.fastjson.support.spring;

import java.lang.reflect.Type;
import org.springframework.data.redis.serializer.SerializationException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import org.springframework.data.redis.serializer.RedisSerializer;

public class FastJsonRedisSerializer<T> implements RedisSerializer<T>
{
    private FastJsonConfig fastJsonConfig;
    private Class<T> type;
    
    public FastJsonRedisSerializer(final Class<T> type) {
        this.fastJsonConfig = new FastJsonConfig();
        this.type = type;
    }
    
    public FastJsonConfig getFastJsonConfig() {
        return this.fastJsonConfig;
    }
    
    public void setFastJsonConfig(final FastJsonConfig fastJsonConfig) {
        this.fastJsonConfig = fastJsonConfig;
    }
    
    public byte[] serialize(final T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        try {
            return JSON.toJSONBytes(this.fastJsonConfig.getCharset(), t, this.fastJsonConfig.getSerializeConfig(), this.fastJsonConfig.getSerializeFilters(), this.fastJsonConfig.getDateFormat(), JSON.DEFAULT_GENERATE_FEATURE, this.fastJsonConfig.getSerializerFeatures());
        }
        catch (Exception ex) {
            throw new SerializationException("Could not serialize: " + ex.getMessage(), (Throwable)ex);
        }
    }
    
    public T deserialize(final byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return JSON.parseObject(bytes, this.fastJsonConfig.getCharset(), this.type, this.fastJsonConfig.getParserConfig(), this.fastJsonConfig.getParseProcess(), JSON.DEFAULT_PARSER_FEATURE, this.fastJsonConfig.getFeatures());
        }
        catch (Exception ex) {
            throw new SerializationException("Could not deserialize: " + ex.getMessage(), (Throwable)ex);
        }
    }
}
