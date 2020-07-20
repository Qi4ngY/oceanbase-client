package com.alibaba.fastjson.parser.deserializer;

import java.lang.reflect.Type;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Array;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.util.FieldInfo;
import java.util.Collection;
import java.util.Map;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.util.List;

public final class ResolveFieldDeserializer extends FieldDeserializer
{
    private final int index;
    private final List list;
    private final DefaultJSONParser parser;
    private final Object key;
    private final Map map;
    private final Collection collection;
    
    public ResolveFieldDeserializer(final DefaultJSONParser parser, final List list, final int index) {
        super(null, null);
        this.parser = parser;
        this.index = index;
        this.list = list;
        this.key = null;
        this.map = null;
        this.collection = null;
    }
    
    public ResolveFieldDeserializer(final Map map, final Object index) {
        super(null, null);
        this.parser = null;
        this.index = -1;
        this.list = null;
        this.key = index;
        this.map = map;
        this.collection = null;
    }
    
    public ResolveFieldDeserializer(final Collection collection) {
        super(null, null);
        this.parser = null;
        this.index = -1;
        this.list = null;
        this.key = null;
        this.map = null;
        this.collection = collection;
    }
    
    @Override
    public void setValue(final Object object, final Object value) {
        if (this.map != null) {
            this.map.put(this.key, value);
            return;
        }
        if (this.collection != null) {
            this.collection.add(value);
            return;
        }
        this.list.set(this.index, value);
        if (this.list instanceof JSONArray) {
            final JSONArray jsonArray = (JSONArray)this.list;
            final Object array = jsonArray.getRelatedArray();
            if (array != null) {
                final int arrayLength = Array.getLength(array);
                if (arrayLength > this.index) {
                    Object item;
                    if (jsonArray.getComponentType() != null) {
                        item = TypeUtils.cast(value, jsonArray.getComponentType(), this.parser.getConfig());
                    }
                    else {
                        item = value;
                    }
                    Array.set(array, this.index, item);
                }
            }
        }
    }
    
    @Override
    public void parseField(final DefaultJSONParser parser, final Object object, final Type objectType, final Map<String, Object> fieldValues) {
    }
}
