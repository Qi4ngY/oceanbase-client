package com.alibaba.fastjson;

import java.io.StreamCorruptedException;
import com.alibaba.fastjson.parser.Feature;
import java.io.ObjectStreamClass;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.io.IOException;
import java.util.Iterator;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import com.alibaba.fastjson.annotation.JSONField;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.sql.Timestamp;
import java.util.Date;
import java.math.BigInteger;
import java.math.BigDecimal;
import com.alibaba.fastjson.parser.ParserConfig;
import java.lang.reflect.Type;
import com.alibaba.fastjson.util.TypeUtils;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.lang.reflect.InvocationHandler;
import java.io.Serializable;
import java.util.Map;

public class JSONObject extends JSON implements Map<String, Object>, Cloneable, Serializable, InvocationHandler
{
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private final Map<String, Object> map;
    
    public JSONObject() {
        this(16, false);
    }
    
    public JSONObject(final Map<String, Object> map) {
        if (map == null) {
            throw new IllegalArgumentException("map is null.");
        }
        this.map = map;
    }
    
    public JSONObject(final boolean ordered) {
        this(16, ordered);
    }
    
    public JSONObject(final int initialCapacity) {
        this(initialCapacity, false);
    }
    
    public JSONObject(final int initialCapacity, final boolean ordered) {
        if (ordered) {
            this.map = new LinkedHashMap<String, Object>(initialCapacity);
        }
        else {
            this.map = new HashMap<String, Object>(initialCapacity);
        }
    }
    
    @Override
    public int size() {
        return this.map.size();
    }
    
    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }
    
    @Override
    public boolean containsKey(final Object key) {
        boolean result = this.map.containsKey(key);
        if (!result && key instanceof Number) {
            result = this.map.containsKey(key.toString());
        }
        return result;
    }
    
    @Override
    public boolean containsValue(final Object value) {
        return this.map.containsValue(value);
    }
    
    @Override
    public Object get(final Object key) {
        Object val = this.map.get(key);
        if (val == null && key instanceof Number) {
            val = this.map.get(key.toString());
        }
        return val;
    }
    
    public JSONObject getJSONObject(final String key) {
        final Object value = this.map.get(key);
        if (value instanceof JSONObject) {
            return (JSONObject)value;
        }
        if (value instanceof Map) {
            return new JSONObject((Map<String, Object>)value);
        }
        if (value instanceof String) {
            return JSON.parseObject((String)value);
        }
        return (JSONObject)JSON.toJSON(value);
    }
    
    public JSONArray getJSONArray(final String key) {
        final Object value = this.map.get(key);
        if (value instanceof JSONArray) {
            return (JSONArray)value;
        }
        if (value instanceof List) {
            return new JSONArray((List<Object>)value);
        }
        if (value instanceof String) {
            return (JSONArray)JSON.parse((String)value);
        }
        return (JSONArray)JSON.toJSON(value);
    }
    
    public <T> T getObject(final String key, final Class<T> clazz) {
        final Object obj = this.map.get(key);
        return TypeUtils.castToJavaBean(obj, clazz);
    }
    
    public <T> T getObject(final String key, final Type type) {
        final Object obj = this.map.get(key);
        return TypeUtils.cast(obj, type, ParserConfig.getGlobalInstance());
    }
    
    public <T> T getObject(final String key, final TypeReference typeReference) {
        final Object obj = this.map.get(key);
        if (typeReference == null) {
            return (T)obj;
        }
        return TypeUtils.cast(obj, typeReference.getType(), ParserConfig.getGlobalInstance());
    }
    
    public Boolean getBoolean(final String key) {
        final Object value = this.get(key);
        if (value == null) {
            return null;
        }
        return TypeUtils.castToBoolean(value);
    }
    
    public byte[] getBytes(final String key) {
        final Object value = this.get(key);
        if (value == null) {
            return null;
        }
        return TypeUtils.castToBytes(value);
    }
    
    public boolean getBooleanValue(final String key) {
        final Object value = this.get(key);
        final Boolean booleanVal = TypeUtils.castToBoolean(value);
        return booleanVal != null && booleanVal;
    }
    
    public Byte getByte(final String key) {
        final Object value = this.get(key);
        return TypeUtils.castToByte(value);
    }
    
    public byte getByteValue(final String key) {
        final Object value = this.get(key);
        final Byte byteVal = TypeUtils.castToByte(value);
        if (byteVal == null) {
            return 0;
        }
        return byteVal;
    }
    
    public Short getShort(final String key) {
        final Object value = this.get(key);
        return TypeUtils.castToShort(value);
    }
    
    public short getShortValue(final String key) {
        final Object value = this.get(key);
        final Short shortVal = TypeUtils.castToShort(value);
        if (shortVal == null) {
            return 0;
        }
        return shortVal;
    }
    
    public Integer getInteger(final String key) {
        final Object value = this.get(key);
        return TypeUtils.castToInt(value);
    }
    
    public int getIntValue(final String key) {
        final Object value = this.get(key);
        final Integer intVal = TypeUtils.castToInt(value);
        if (intVal == null) {
            return 0;
        }
        return intVal;
    }
    
    public Long getLong(final String key) {
        final Object value = this.get(key);
        return TypeUtils.castToLong(value);
    }
    
    public long getLongValue(final String key) {
        final Object value = this.get(key);
        final Long longVal = TypeUtils.castToLong(value);
        if (longVal == null) {
            return 0L;
        }
        return longVal;
    }
    
    public Float getFloat(final String key) {
        final Object value = this.get(key);
        return TypeUtils.castToFloat(value);
    }
    
    public float getFloatValue(final String key) {
        final Object value = this.get(key);
        final Float floatValue = TypeUtils.castToFloat(value);
        if (floatValue == null) {
            return 0.0f;
        }
        return floatValue;
    }
    
    public Double getDouble(final String key) {
        final Object value = this.get(key);
        return TypeUtils.castToDouble(value);
    }
    
    public double getDoubleValue(final String key) {
        final Object value = this.get(key);
        final Double doubleValue = TypeUtils.castToDouble(value);
        if (doubleValue == null) {
            return 0.0;
        }
        return doubleValue;
    }
    
    public BigDecimal getBigDecimal(final String key) {
        final Object value = this.get(key);
        return TypeUtils.castToBigDecimal(value);
    }
    
    public BigInteger getBigInteger(final String key) {
        final Object value = this.get(key);
        return TypeUtils.castToBigInteger(value);
    }
    
    public String getString(final String key) {
        final Object value = this.get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }
    
    public Date getDate(final String key) {
        final Object value = this.get(key);
        return TypeUtils.castToDate(value);
    }
    
    public java.sql.Date getSqlDate(final String key) {
        final Object value = this.get(key);
        return TypeUtils.castToSqlDate(value);
    }
    
    public Timestamp getTimestamp(final String key) {
        final Object value = this.get(key);
        return TypeUtils.castToTimestamp(value);
    }
    
    @Override
    public Object put(final String key, final Object value) {
        return this.map.put(key, value);
    }
    
    public JSONObject fluentPut(final String key, final Object value) {
        this.map.put(key, value);
        return this;
    }
    
    @Override
    public void putAll(final Map<? extends String, ?> m) {
        this.map.putAll(m);
    }
    
    public JSONObject fluentPutAll(final Map<? extends String, ?> m) {
        this.map.putAll(m);
        return this;
    }
    
    @Override
    public void clear() {
        this.map.clear();
    }
    
    public JSONObject fluentClear() {
        this.map.clear();
        return this;
    }
    
    @Override
    public Object remove(final Object key) {
        return this.map.remove(key);
    }
    
    public JSONObject fluentRemove(final Object key) {
        this.map.remove(key);
        return this;
    }
    
    @Override
    public Set<String> keySet() {
        return this.map.keySet();
    }
    
    @Override
    public Collection<Object> values() {
        return this.map.values();
    }
    
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.map.entrySet();
    }
    
    public Object clone() {
        return new JSONObject((this.map instanceof LinkedHashMap) ? new LinkedHashMap<String, Object>(this.map) : new HashMap<String, Object>(this.map));
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this.map.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return this.map.hashCode();
    }
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 1) {
            if (method.getName().equals("equals")) {
                return this.equals(args[0]);
            }
            final Class<?> returnType = method.getReturnType();
            if (returnType != Void.TYPE) {
                throw new JSONException("illegal setter");
            }
            String name = null;
            final JSONField annotation = TypeUtils.getAnnotation(method, JSONField.class);
            if (annotation != null && annotation.name().length() != 0) {
                name = annotation.name();
            }
            if (name == null) {
                name = method.getName();
                if (!name.startsWith("set")) {
                    throw new JSONException("illegal setter");
                }
                name = name.substring(3);
                if (name.length() == 0) {
                    throw new JSONException("illegal setter");
                }
                name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            }
            this.map.put(name, args[0]);
            return null;
        }
        else {
            if (parameterTypes.length != 0) {
                throw new UnsupportedOperationException(method.toGenericString());
            }
            final Class<?> returnType = method.getReturnType();
            if (returnType == Void.TYPE) {
                throw new JSONException("illegal getter");
            }
            String name = null;
            final JSONField annotation = TypeUtils.getAnnotation(method, JSONField.class);
            if (annotation != null && annotation.name().length() != 0) {
                name = annotation.name();
            }
            if (name == null) {
                name = method.getName();
                if (name.startsWith("get")) {
                    name = name.substring(3);
                    if (name.length() == 0) {
                        throw new JSONException("illegal getter");
                    }
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                }
                else if (name.startsWith("is")) {
                    name = name.substring(2);
                    if (name.length() == 0) {
                        throw new JSONException("illegal getter");
                    }
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                }
                else {
                    if (name.startsWith("hashCode")) {
                        return this.hashCode();
                    }
                    if (name.startsWith("toString")) {
                        return this.toString();
                    }
                    throw new JSONException("illegal getter");
                }
            }
            final Object value = this.map.get(name);
            return TypeUtils.cast(value, method.getGenericReturnType(), ParserConfig.getGlobalInstance());
        }
    }
    
    public Map<String, Object> getInnerMap() {
        return this.map;
    }
    
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        SecureObjectInputStream.ensureFields();
        if (SecureObjectInputStream.fields != null && !SecureObjectInputStream.fields_error) {
            final ObjectInputStream secIn = new SecureObjectInputStream(in);
            try {
                secIn.defaultReadObject();
                return;
            }
            catch (NotActiveException ex) {}
        }
        in.defaultReadObject();
        for (final Entry entry : this.map.entrySet()) {
            final Object key = entry.getKey();
            if (key != null) {
                ParserConfig.global.checkAutoType(key.getClass());
            }
            final Object value = entry.getValue();
            if (value != null) {
                ParserConfig.global.checkAutoType(value.getClass());
            }
        }
    }
    
    @Override
    public <T> T toJavaObject(final Class<T> clazz) {
        if (clazz == Map.class || clazz == JSONObject.class || clazz == JSON.class) {
            return (T)this;
        }
        if (clazz == Object.class && !this.containsKey(JSON.DEFAULT_TYPE_KEY)) {
            return (T)this;
        }
        return TypeUtils.castToJavaBean(this, clazz, ParserConfig.getGlobalInstance());
    }
    
    public <T> T toJavaObject(final Class<T> clazz, final ParserConfig config, final int features) {
        if (clazz == Map.class) {
            return (T)this;
        }
        if (clazz == Object.class && !this.containsKey(JSON.DEFAULT_TYPE_KEY)) {
            return (T)this;
        }
        return TypeUtils.castToJavaBean(this, clazz, config);
    }
    
    static class SecureObjectInputStream extends ObjectInputStream
    {
        static Field[] fields;
        static volatile boolean fields_error;
        
        static void ensureFields() {
            if (SecureObjectInputStream.fields == null && !SecureObjectInputStream.fields_error) {
                try {
                    final Field[] declaredFields = ObjectInputStream.class.getDeclaredFields();
                    final String[] fieldnames = { "bin", "passHandle", "handles", "curContext" };
                    final Field[] array = new Field[fieldnames.length];
                    for (int i = 0; i < fieldnames.length; ++i) {
                        final Field field = TypeUtils.getField(ObjectInputStream.class, fieldnames[i], declaredFields);
                        field.setAccessible(true);
                        array[i] = field;
                    }
                    SecureObjectInputStream.fields = array;
                }
                catch (Throwable error) {
                    SecureObjectInputStream.fields_error = true;
                }
            }
        }
        
        public SecureObjectInputStream(final ObjectInputStream in) throws IOException {
            super(in);
            try {
                for (int i = 0; i < SecureObjectInputStream.fields.length; ++i) {
                    final Field field = SecureObjectInputStream.fields[i];
                    final Object value = field.get(in);
                    field.set(this, value);
                }
            }
            catch (IllegalAccessException e) {
                SecureObjectInputStream.fields_error = true;
            }
        }
        
        @Override
        protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            String name = desc.getName();
            if (name.length() > 2) {
                final int index = name.lastIndexOf(91);
                if (index != -1) {
                    name = name.substring(index + 1);
                }
                if (name.length() > 2 && name.charAt(0) == 'L' && name.charAt(name.length() - 1) == ';') {
                    name = name.substring(1, name.length() - 1);
                }
                ParserConfig.global.checkAutoType(name, null, Feature.SupportAutoType.mask);
            }
            return super.resolveClass(desc);
        }
        
        @Override
        protected Class<?> resolveProxyClass(final String[] interfaces) throws IOException, ClassNotFoundException {
            for (final String interfacename : interfaces) {
                ParserConfig.global.checkAutoType(interfacename, null);
            }
            return super.resolveProxyClass(interfaces);
        }
        
        @Override
        protected void readStreamHeader() throws IOException, StreamCorruptedException {
        }
    }
}
