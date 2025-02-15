package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSONException;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;

public class PropertyProcessableDeserializer implements ObjectDeserializer
{
    public final Class<PropertyProcessable> type;
    
    public PropertyProcessableDeserializer(final Class<PropertyProcessable> type) {
        this.type = type;
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        PropertyProcessable processable;
        try {
            processable = this.type.newInstance();
        }
        catch (Exception e) {
            throw new JSONException("craete instance error");
        }
        final Object object = parser.parse(processable, fieldName);
        return (T)object;
    }
    
    @Override
    public int getFastMatchToken() {
        return 12;
    }
}
