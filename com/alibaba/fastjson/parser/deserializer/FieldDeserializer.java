package com.alibaba.fastjson.parser.deserializer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.alibaba.fastjson.JSONException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.serializer.BeanContext;
import com.alibaba.fastjson.util.FieldInfo;

public abstract class FieldDeserializer
{
    public final FieldInfo fieldInfo;
    protected final Class<?> clazz;
    protected BeanContext beanContext;
    
    public FieldDeserializer(final Class<?> clazz, final FieldInfo fieldInfo) {
        this.clazz = clazz;
        this.fieldInfo = fieldInfo;
    }
    
    public abstract void parseField(final DefaultJSONParser p0, final Object p1, final Type p2, final Map<String, Object> p3);
    
    public int getFastMatchToken() {
        return 0;
    }
    
    public void setValue(final Object object, final boolean value) {
        this.setValue(object, (Object)value);
    }
    
    public void setValue(final Object object, final int value) {
        this.setValue(object, (Object)value);
    }
    
    public void setValue(final Object object, final long value) {
        this.setValue(object, (Object)value);
    }
    
    public void setValue(final Object object, final String value) {
        this.setValue(object, (Object)value);
    }
    
    public void setValue(final Object object, Object value) {
        if (value == null && this.fieldInfo.fieldClass.isPrimitive()) {
            return;
        }
        if (this.fieldInfo.fieldClass == String.class && this.fieldInfo.format != null && this.fieldInfo.format.equals("trim")) {
            value = ((String)value).trim();
        }
        try {
            final Method method = this.fieldInfo.method;
            if (method != null) {
                if (this.fieldInfo.getOnly) {
                    if (this.fieldInfo.fieldClass == AtomicInteger.class) {
                        final AtomicInteger atomic = (AtomicInteger)method.invoke(object, new Object[0]);
                        if (atomic != null) {
                            atomic.set(((AtomicInteger)value).get());
                        }
                    }
                    else if (this.fieldInfo.fieldClass == AtomicLong.class) {
                        final AtomicLong atomic2 = (AtomicLong)method.invoke(object, new Object[0]);
                        if (atomic2 != null) {
                            atomic2.set(((AtomicLong)value).get());
                        }
                    }
                    else if (this.fieldInfo.fieldClass == AtomicBoolean.class) {
                        final AtomicBoolean atomic3 = (AtomicBoolean)method.invoke(object, new Object[0]);
                        if (atomic3 != null) {
                            atomic3.set(((AtomicBoolean)value).get());
                        }
                    }
                    else if (Map.class.isAssignableFrom(method.getReturnType())) {
                        final Map map = (Map)method.invoke(object, new Object[0]);
                        if (map != null) {
                            if (map == Collections.emptyMap() || map.getClass().getName().startsWith("java.util.Collections$Unmodifiable")) {
                                return;
                            }
                            map.putAll((Map)value);
                        }
                    }
                    else {
                        final Collection collection = (Collection)method.invoke(object, new Object[0]);
                        if (collection != null && value != null) {
                            if (collection == Collections.emptySet() || collection == Collections.emptyList() || collection.getClass().getName().startsWith("java.util.Collections$Unmodifiable")) {
                                return;
                            }
                            collection.clear();
                            collection.addAll((Collection)value);
                        }
                    }
                }
                else {
                    method.invoke(object, value);
                }
            }
            else {
                final Field field = this.fieldInfo.field;
                if (this.fieldInfo.getOnly) {
                    if (this.fieldInfo.fieldClass == AtomicInteger.class) {
                        final AtomicInteger atomic4 = (AtomicInteger)field.get(object);
                        if (atomic4 != null) {
                            atomic4.set(((AtomicInteger)value).get());
                        }
                    }
                    else if (this.fieldInfo.fieldClass == AtomicLong.class) {
                        final AtomicLong atomic5 = (AtomicLong)field.get(object);
                        if (atomic5 != null) {
                            atomic5.set(((AtomicLong)value).get());
                        }
                    }
                    else if (this.fieldInfo.fieldClass == AtomicBoolean.class) {
                        final AtomicBoolean atomic6 = (AtomicBoolean)field.get(object);
                        if (atomic6 != null) {
                            atomic6.set(((AtomicBoolean)value).get());
                        }
                    }
                    else if (Map.class.isAssignableFrom(this.fieldInfo.fieldClass)) {
                        final Map map2 = (Map)field.get(object);
                        if (map2 != null) {
                            if (map2 == Collections.emptyMap() || map2.getClass().getName().startsWith("java.util.Collections$Unmodifiable")) {
                                return;
                            }
                            map2.putAll((Map)value);
                        }
                    }
                    else {
                        final Collection collection2 = (Collection)field.get(object);
                        if (collection2 != null && value != null) {
                            if (collection2 == Collections.emptySet() || collection2 == Collections.emptyList() || collection2.getClass().getName().startsWith("java.util.Collections$Unmodifiable")) {
                                return;
                            }
                            collection2.clear();
                            collection2.addAll((Collection)value);
                        }
                    }
                }
                else if (field != null) {
                    field.set(object, value);
                }
            }
        }
        catch (Exception e) {
            throw new JSONException("set property error, " + this.clazz.getName() + "#" + this.fieldInfo.name, e);
        }
    }
    
    public void setWrappedValue(final String key, final Object value) {
        throw new JSONException("TODO");
    }
}
