package com.alibaba.fastjson.support.spring;

import com.alibaba.fastjson.serializer.SerializerFeature;
import java.io.IOException;
import com.alibaba.fastjson.serializer.SerializeWriter;
import java.lang.reflect.Type;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.JSONSerializable;

@Deprecated
public class MappingFastJsonValue implements JSONSerializable
{
    private static final String SECURITY_PREFIX = "/**/";
    private static final int BrowserSecureMask;
    private Object value;
    private String jsonpFunction;
    
    public MappingFastJsonValue(final Object value) {
        this.value = value;
    }
    
    public void setValue(final Object value) {
        this.value = value;
    }
    
    public Object getValue() {
        return this.value;
    }
    
    public void setJsonpFunction(final String functionName) {
        this.jsonpFunction = functionName;
    }
    
    public String getJsonpFunction() {
        return this.jsonpFunction;
    }
    
    @Override
    public void write(final JSONSerializer serializer, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter writer = serializer.out;
        if (this.jsonpFunction == null) {
            serializer.write(this.value);
            return;
        }
        if ((features & MappingFastJsonValue.BrowserSecureMask) != 0x0 || writer.isEnabled(MappingFastJsonValue.BrowserSecureMask)) {
            writer.write("/**/");
        }
        writer.write(this.jsonpFunction);
        writer.write(40);
        serializer.write(this.value);
        writer.write(41);
    }
    
    static {
        BrowserSecureMask = SerializerFeature.BrowserSecure.mask;
    }
}
