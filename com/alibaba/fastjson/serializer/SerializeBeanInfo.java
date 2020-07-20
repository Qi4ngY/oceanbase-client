package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.annotation.JSONType;

public class SerializeBeanInfo
{
    protected final Class<?> beanType;
    protected final String typeName;
    protected final String typeKey;
    protected final JSONType jsonType;
    protected final FieldInfo[] fields;
    protected final FieldInfo[] sortedFields;
    protected int features;
    
    public SerializeBeanInfo(final Class<?> beanType, final JSONType jsonType, final String typeName, final String typeKey, final int features, final FieldInfo[] fields, final FieldInfo[] sortedFields) {
        this.beanType = beanType;
        this.jsonType = jsonType;
        this.typeName = typeName;
        this.typeKey = typeKey;
        this.features = features;
        this.fields = fields;
        this.sortedFields = sortedFields;
    }
}
