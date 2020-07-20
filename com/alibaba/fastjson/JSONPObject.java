package com.alibaba.fastjson;

import java.io.IOException;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.lang.reflect.Type;
import com.alibaba.fastjson.serializer.JSONSerializer;
import java.util.ArrayList;
import java.util.List;
import com.alibaba.fastjson.serializer.JSONSerializable;

public class JSONPObject implements JSONSerializable
{
    public static String SECURITY_PREFIX;
    private String function;
    private final List<Object> parameters;
    
    public JSONPObject() {
        this.parameters = new ArrayList<Object>();
    }
    
    public JSONPObject(final String function) {
        this.parameters = new ArrayList<Object>();
        this.function = function;
    }
    
    public String getFunction() {
        return this.function;
    }
    
    public void setFunction(final String function) {
        this.function = function;
    }
    
    public List<Object> getParameters() {
        return this.parameters;
    }
    
    public void addParameter(final Object parameter) {
        this.parameters.add(parameter);
    }
    
    public String toJSONString() {
        return this.toString();
    }
    
    @Override
    public void write(final JSONSerializer serializer, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter writer = serializer.out;
        if ((features & SerializerFeature.BrowserSecure.mask) != 0x0 || writer.isEnabled(SerializerFeature.BrowserSecure.mask)) {
            writer.write(JSONPObject.SECURITY_PREFIX);
        }
        writer.write(this.function);
        writer.write(40);
        for (int i = 0; i < this.parameters.size(); ++i) {
            if (i != 0) {
                writer.write(44);
            }
            serializer.write(this.parameters.get(i));
        }
        writer.write(41);
    }
    
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
    
    static {
        JSONPObject.SECURITY_PREFIX = "/**/";
    }
}
