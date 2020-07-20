package com.alibaba.fastjson.support.spring;

import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.util.IOUtils;
import org.springframework.data.redis.serializer.SerializationException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.parser.ParserConfig;
import org.springframework.data.redis.serializer.RedisSerializer;

public class GenericFastJsonRedisSerializer implements RedisSerializer<Object>
{
    private static final ParserConfig defaultRedisConfig;
    
    public byte[] serialize(final Object object) throws SerializationException {
        if (object == null) {
            return new byte[0];
        }
        try {
            return JSON.toJSONBytes(object, SerializerFeature.WriteClassName);
        }
        catch (Exception ex) {
            throw new SerializationException("Could not serialize: " + ex.getMessage(), (Throwable)ex);
        }
    }
    
    public Object deserialize(final byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return JSON.parseObject(new String(bytes, IOUtils.UTF8), (Type)Object.class, GenericFastJsonRedisSerializer.defaultRedisConfig, new Feature[0]);
        }
        catch (Exception ex) {
            throw new SerializationException("Could not deserialize: " + ex.getMessage(), (Throwable)ex);
        }
    }
    
    static {
        (defaultRedisConfig = new ParserConfig()).setAutoTypeSupport(true);
    }
}
