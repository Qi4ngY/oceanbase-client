package com.alibaba.fastjson;

import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import com.alibaba.fastjson.parser.ParserConfig;
import java.sql.Timestamp;
import java.util.Date;
import java.math.BigInteger;
import java.math.BigDecimal;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.util.TypeUtils;
import java.util.Map;
import java.util.ListIterator;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.lang.reflect.Type;
import java.io.Serializable;
import java.util.RandomAccess;
import java.util.List;

public class JSONArray extends JSON implements List<Object>, Cloneable, RandomAccess, Serializable
{
    private static final long serialVersionUID = 1L;
    private final List<Object> list;
    protected transient Object relatedArray;
    protected transient Type componentType;
    
    public JSONArray() {
        this.list = new ArrayList<Object>();
    }
    
    public JSONArray(final List<Object> list) {
        if (list == null) {
            throw new IllegalArgumentException("list is null.");
        }
        this.list = list;
    }
    
    public JSONArray(final int initialCapacity) {
        this.list = new ArrayList<Object>(initialCapacity);
    }
    
    public Object getRelatedArray() {
        return this.relatedArray;
    }
    
    public void setRelatedArray(final Object relatedArray) {
        this.relatedArray = relatedArray;
    }
    
    public Type getComponentType() {
        return this.componentType;
    }
    
    public void setComponentType(final Type componentType) {
        this.componentType = componentType;
    }
    
    @Override
    public int size() {
        return this.list.size();
    }
    
    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }
    
    @Override
    public boolean contains(final Object o) {
        return this.list.contains(o);
    }
    
    @Override
    public Iterator<Object> iterator() {
        return this.list.iterator();
    }
    
    @Override
    public Object[] toArray() {
        return this.list.toArray();
    }
    
    @Override
    public <T> T[] toArray(final T[] a) {
        return this.list.toArray(a);
    }
    
    @Override
    public boolean add(final Object e) {
        return this.list.add(e);
    }
    
    public JSONArray fluentAdd(final Object e) {
        this.list.add(e);
        return this;
    }
    
    @Override
    public boolean remove(final Object o) {
        return this.list.remove(o);
    }
    
    public JSONArray fluentRemove(final Object o) {
        this.list.remove(o);
        return this;
    }
    
    @Override
    public boolean containsAll(final Collection<?> c) {
        return this.list.containsAll(c);
    }
    
    @Override
    public boolean addAll(final Collection<?> c) {
        return this.list.addAll(c);
    }
    
    public JSONArray fluentAddAll(final Collection<?> c) {
        this.list.addAll(c);
        return this;
    }
    
    @Override
    public boolean addAll(final int index, final Collection<?> c) {
        return this.list.addAll(index, c);
    }
    
    public JSONArray fluentAddAll(final int index, final Collection<?> c) {
        this.list.addAll(index, c);
        return this;
    }
    
    @Override
    public boolean removeAll(final Collection<?> c) {
        return this.list.removeAll(c);
    }
    
    public JSONArray fluentRemoveAll(final Collection<?> c) {
        this.list.removeAll(c);
        return this;
    }
    
    @Override
    public boolean retainAll(final Collection<?> c) {
        return this.list.retainAll(c);
    }
    
    public JSONArray fluentRetainAll(final Collection<?> c) {
        this.list.retainAll(c);
        return this;
    }
    
    @Override
    public void clear() {
        this.list.clear();
    }
    
    public JSONArray fluentClear() {
        this.list.clear();
        return this;
    }
    
    @Override
    public Object set(final int index, final Object element) {
        if (index == -1) {
            this.list.add(element);
            return null;
        }
        if (this.list.size() <= index) {
            for (int i = this.list.size(); i < index; ++i) {
                this.list.add(null);
            }
            this.list.add(element);
            return null;
        }
        return this.list.set(index, element);
    }
    
    public JSONArray fluentSet(final int index, final Object element) {
        this.set(index, element);
        return this;
    }
    
    @Override
    public void add(final int index, final Object element) {
        this.list.add(index, element);
    }
    
    public JSONArray fluentAdd(final int index, final Object element) {
        this.list.add(index, element);
        return this;
    }
    
    @Override
    public Object remove(final int index) {
        return this.list.remove(index);
    }
    
    public JSONArray fluentRemove(final int index) {
        this.list.remove(index);
        return this;
    }
    
    @Override
    public int indexOf(final Object o) {
        return this.list.indexOf(o);
    }
    
    @Override
    public int lastIndexOf(final Object o) {
        return this.list.lastIndexOf(o);
    }
    
    @Override
    public ListIterator<Object> listIterator() {
        return this.list.listIterator();
    }
    
    @Override
    public ListIterator<Object> listIterator(final int index) {
        return this.list.listIterator(index);
    }
    
    @Override
    public List<Object> subList(final int fromIndex, final int toIndex) {
        return this.list.subList(fromIndex, toIndex);
    }
    
    @Override
    public Object get(final int index) {
        return this.list.get(index);
    }
    
    public JSONObject getJSONObject(final int index) {
        final Object value = this.list.get(index);
        if (value instanceof JSONObject) {
            return (JSONObject)value;
        }
        if (value instanceof Map) {
            return new JSONObject((Map<String, Object>)value);
        }
        return (JSONObject)JSON.toJSON(value);
    }
    
    public JSONArray getJSONArray(final int index) {
        final Object value = this.list.get(index);
        if (value instanceof JSONArray) {
            return (JSONArray)value;
        }
        if (value instanceof List) {
            return new JSONArray((List<Object>)value);
        }
        return (JSONArray)JSON.toJSON(value);
    }
    
    public <T> T getObject(final int index, final Class<T> clazz) {
        final Object obj = this.list.get(index);
        return TypeUtils.castToJavaBean(obj, clazz);
    }
    
    public <T> T getObject(final int index, final Type type) {
        final Object obj = this.list.get(index);
        if (type instanceof Class) {
            return TypeUtils.castToJavaBean(obj, (Class<T>)type);
        }
        final String json = JSON.toJSONString(obj);
        return JSON.parseObject(json, type, new Feature[0]);
    }
    
    public Boolean getBoolean(final int index) {
        final Object value = this.get(index);
        if (value == null) {
            return null;
        }
        return TypeUtils.castToBoolean(value);
    }
    
    public boolean getBooleanValue(final int index) {
        final Object value = this.get(index);
        return value != null && TypeUtils.castToBoolean(value);
    }
    
    public Byte getByte(final int index) {
        final Object value = this.get(index);
        return TypeUtils.castToByte(value);
    }
    
    public byte getByteValue(final int index) {
        final Object value = this.get(index);
        final Byte byteVal = TypeUtils.castToByte(value);
        if (byteVal == null) {
            return 0;
        }
        return byteVal;
    }
    
    public Short getShort(final int index) {
        final Object value = this.get(index);
        return TypeUtils.castToShort(value);
    }
    
    public short getShortValue(final int index) {
        final Object value = this.get(index);
        final Short shortVal = TypeUtils.castToShort(value);
        if (shortVal == null) {
            return 0;
        }
        return shortVal;
    }
    
    public Integer getInteger(final int index) {
        final Object value = this.get(index);
        return TypeUtils.castToInt(value);
    }
    
    public int getIntValue(final int index) {
        final Object value = this.get(index);
        final Integer intVal = TypeUtils.castToInt(value);
        if (intVal == null) {
            return 0;
        }
        return intVal;
    }
    
    public Long getLong(final int index) {
        final Object value = this.get(index);
        return TypeUtils.castToLong(value);
    }
    
    public long getLongValue(final int index) {
        final Object value = this.get(index);
        final Long longVal = TypeUtils.castToLong(value);
        if (longVal == null) {
            return 0L;
        }
        return longVal;
    }
    
    public Float getFloat(final int index) {
        final Object value = this.get(index);
        return TypeUtils.castToFloat(value);
    }
    
    public float getFloatValue(final int index) {
        final Object value = this.get(index);
        final Float floatValue = TypeUtils.castToFloat(value);
        if (floatValue == null) {
            return 0.0f;
        }
        return floatValue;
    }
    
    public Double getDouble(final int index) {
        final Object value = this.get(index);
        return TypeUtils.castToDouble(value);
    }
    
    public double getDoubleValue(final int index) {
        final Object value = this.get(index);
        final Double doubleValue = TypeUtils.castToDouble(value);
        if (doubleValue == null) {
            return 0.0;
        }
        return doubleValue;
    }
    
    public BigDecimal getBigDecimal(final int index) {
        final Object value = this.get(index);
        return TypeUtils.castToBigDecimal(value);
    }
    
    public BigInteger getBigInteger(final int index) {
        final Object value = this.get(index);
        return TypeUtils.castToBigInteger(value);
    }
    
    public String getString(final int index) {
        final Object value = this.get(index);
        return TypeUtils.castToString(value);
    }
    
    public Date getDate(final int index) {
        final Object value = this.get(index);
        return TypeUtils.castToDate(value);
    }
    
    public java.sql.Date getSqlDate(final int index) {
        final Object value = this.get(index);
        return TypeUtils.castToSqlDate(value);
    }
    
    public Timestamp getTimestamp(final int index) {
        final Object value = this.get(index);
        return TypeUtils.castToTimestamp(value);
    }
    
    public <T> List<T> toJavaList(final Class<T> clazz) {
        final List<T> list = new ArrayList<T>(this.size());
        final ParserConfig config = ParserConfig.getGlobalInstance();
        for (final Object item : this) {
            final T classItem = TypeUtils.cast(item, clazz, config);
            list.add(classItem);
        }
        return list;
    }
    
    public Object clone() {
        return new JSONArray(new ArrayList<Object>(this.list));
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this.list.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return this.list.hashCode();
    }
    
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        JSONObject.SecureObjectInputStream.ensureFields();
        if (JSONObject.SecureObjectInputStream.fields != null && !JSONObject.SecureObjectInputStream.fields_error) {
            final ObjectInputStream secIn = new JSONObject.SecureObjectInputStream(in);
            try {
                secIn.defaultReadObject();
                return;
            }
            catch (NotActiveException ex) {}
        }
        in.defaultReadObject();
        for (final Object item : this.list) {
            if (item != null) {
                ParserConfig.global.checkAutoType(item.getClass().getName(), null);
            }
        }
    }
}
