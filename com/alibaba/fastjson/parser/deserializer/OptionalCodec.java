package com.alibaba.fastjson.parser.deserializer;

import java.io.IOException;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.serializer.JSONSerializer;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import com.alibaba.fastjson.util.TypeUtils;
import java.util.OptionalInt;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.serializer.ObjectSerializer;

public class OptionalCodec implements ObjectSerializer, ObjectDeserializer
{
    public static OptionalCodec instance;
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, Type type, final Object fieldName) {
        if (type == OptionalInt.class) {
            final Object obj = parser.parseObject(Integer.class);
            final Integer value = TypeUtils.castToInt(obj);
            if (value == null) {
                return (T)OptionalInt.empty();
            }
            return (T)OptionalInt.of(value);
        }
        else if (type == OptionalLong.class) {
            final Object obj = parser.parseObject(Long.class);
            final Long value2 = TypeUtils.castToLong(obj);
            if (value2 == null) {
                return (T)OptionalLong.empty();
            }
            return (T)OptionalLong.of(value2);
        }
        else if (type == OptionalDouble.class) {
            final Object obj = parser.parseObject(Double.class);
            final Double value3 = TypeUtils.castToDouble(obj);
            if (value3 == null) {
                return (T)OptionalDouble.empty();
            }
            return (T)OptionalDouble.of(value3);
        }
        else {
            type = TypeUtils.unwrapOptional(type);
            final Object value4 = parser.parseObject(type);
            if (value4 == null) {
                return (T)Optional.empty();
            }
            return (T)Optional.of(value4);
        }
    }
    
    @Override
    public int getFastMatchToken() {
        return 12;
    }
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        if (object == null) {
            serializer.writeNull();
            return;
        }
        if (object instanceof Optional) {
            final Optional<?> optional = (Optional<?>)object;
            final Object value = optional.isPresent() ? optional.get() : null;
            serializer.write(value);
            return;
        }
        if (object instanceof OptionalDouble) {
            final OptionalDouble optional2 = (OptionalDouble)object;
            if (optional2.isPresent()) {
                final double value2 = optional2.getAsDouble();
                serializer.write(value2);
            }
            else {
                serializer.writeNull();
            }
            return;
        }
        if (object instanceof OptionalInt) {
            final OptionalInt optional3 = (OptionalInt)object;
            if (optional3.isPresent()) {
                final int value3 = optional3.getAsInt();
                serializer.out.writeInt(value3);
            }
            else {
                serializer.writeNull();
            }
            return;
        }
        if (object instanceof OptionalLong) {
            final OptionalLong optional4 = (OptionalLong)object;
            if (optional4.isPresent()) {
                final long value4 = optional4.getAsLong();
                serializer.out.writeLong(value4);
            }
            else {
                serializer.writeNull();
            }
            return;
        }
        throw new JSONException("not support optional : " + object.getClass());
    }
    
    static {
        OptionalCodec.instance = new OptionalCodec();
    }
}
