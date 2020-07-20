package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.util.FieldInfo;
import java.lang.reflect.Field;
import com.alibaba.fastjson.JSONException;
import java.util.Collections;
import java.util.Collection;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.WildcardType;
import java.io.IOException;
import java.lang.reflect.Type;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.util.TypeUtils;
import java.util.HashMap;
import java.util.Map;

public class JavaBeanSerializer extends SerializeFilterable implements ObjectSerializer
{
    protected final FieldSerializer[] getters;
    protected final FieldSerializer[] sortedGetters;
    protected SerializeBeanInfo beanInfo;
    private transient volatile long[] hashArray;
    private transient volatile short[] hashArrayMapping;
    
    public JavaBeanSerializer(final Class<?> beanType) {
        this(beanType, (Map<String, String>)null);
    }
    
    public JavaBeanSerializer(final Class<?> beanType, final String... aliasList) {
        this(beanType, createAliasMap(aliasList));
    }
    
    static Map<String, String> createAliasMap(final String... aliasList) {
        final Map<String, String> aliasMap = new HashMap<String, String>();
        for (final String alias : aliasList) {
            aliasMap.put(alias, alias);
        }
        return aliasMap;
    }
    
    public Class<?> getType() {
        return this.beanInfo.beanType;
    }
    
    public JavaBeanSerializer(final Class<?> beanType, final Map<String, String> aliasMap) {
        this(TypeUtils.buildBeanInfo(beanType, aliasMap, null));
    }
    
    public JavaBeanSerializer(final SerializeBeanInfo beanInfo) {
        this.beanInfo = beanInfo;
        this.sortedGetters = new FieldSerializer[beanInfo.sortedFields.length];
        for (int i = 0; i < this.sortedGetters.length; ++i) {
            this.sortedGetters[i] = new FieldSerializer(beanInfo.beanType, beanInfo.sortedFields[i]);
        }
        if (beanInfo.fields == beanInfo.sortedFields) {
            this.getters = this.sortedGetters;
        }
        else {
            this.getters = new FieldSerializer[beanInfo.fields.length];
            boolean hashNotMatch = false;
            for (int j = 0; j < this.getters.length; ++j) {
                final FieldSerializer fieldSerializer = this.getFieldSerializer(beanInfo.fields[j].name);
                if (fieldSerializer == null) {
                    hashNotMatch = true;
                    break;
                }
                this.getters[j] = fieldSerializer;
            }
            if (hashNotMatch) {
                System.arraycopy(this.sortedGetters, 0, this.getters, 0, this.sortedGetters.length);
            }
        }
        if (beanInfo.jsonType != null) {
            for (final Class<? extends SerializeFilter> filterClass : beanInfo.jsonType.serialzeFilters()) {
                try {
                    final SerializeFilter filter = (SerializeFilter)filterClass.getConstructor((Class<?>[])new Class[0]).newInstance(new Object[0]);
                    this.addFilter(filter);
                }
                catch (Exception ex) {}
            }
        }
    }
    
    public void writeDirectNonContext(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        this.write(serializer, object, fieldName, fieldType, features);
    }
    
    public void writeAsArray(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        this.write(serializer, object, fieldName, fieldType, features);
    }
    
    public void writeAsArrayNonContext(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        this.write(serializer, object, fieldName, fieldType, features);
    }
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        this.write(serializer, object, fieldName, fieldType, features, false);
    }
    
    public void writeNoneASM(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        this.write(serializer, object, fieldName, fieldType, features, false);
    }
    
    protected void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features, final boolean unwrapped) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
            return;
        }
        if (this.writeReference(serializer, object, features)) {
            return;
        }
        FieldSerializer[] getters;
        if (out.sortField) {
            getters = this.sortedGetters;
        }
        else {
            getters = this.getters;
        }
        final SerialContext parent = serializer.context;
        if (!this.beanInfo.beanType.isEnum()) {
            serializer.setContext(parent, object, fieldName, this.beanInfo.features, features);
        }
        final boolean writeAsArray = this.isWriteAsArray(serializer, features);
        FieldSerializer errorFieldSerializer = null;
        try {
            final char startSeperator = writeAsArray ? '[' : '{';
            final char endSeperator = writeAsArray ? ']' : '}';
            if (!unwrapped) {
                out.append(startSeperator);
            }
            if (getters.length > 0 && out.isEnabled(SerializerFeature.PrettyFormat)) {
                serializer.incrementIndent();
                serializer.println();
            }
            boolean commaFlag = false;
            if ((this.beanInfo.features & SerializerFeature.WriteClassName.mask) != 0x0 || (features & SerializerFeature.WriteClassName.mask) != 0x0 || serializer.isWriteClassName(fieldType, object)) {
                final Class<?> objClass = object.getClass();
                Type type;
                if (objClass != fieldType && fieldType instanceof WildcardType) {
                    type = TypeUtils.getClass(fieldType);
                }
                else {
                    type = fieldType;
                }
                if (objClass != type) {
                    this.writeClassName(serializer, this.beanInfo.typeKey, object);
                    commaFlag = true;
                }
            }
            final char seperator = commaFlag ? ',' : '\0';
            final boolean writeClassName = out.isEnabled(SerializerFeature.WriteClassName);
            final char newSeperator = this.writeBefore(serializer, object, seperator);
            commaFlag = (newSeperator == ',');
            final boolean skipTransient = out.isEnabled(SerializerFeature.SkipTransientField);
            final boolean ignoreNonFieldGetter = out.isEnabled(SerializerFeature.IgnoreNonFieldGetter);
            for (int i = 0; i < getters.length; ++i) {
                final FieldSerializer fieldSerializer = getters[i];
                final Field field = fieldSerializer.fieldInfo.field;
                final FieldInfo fieldInfo = fieldSerializer.fieldInfo;
                final String fieldInfoName = fieldInfo.name;
                final Class<?> fieldClass = fieldInfo.fieldClass;
                final boolean fieldUseSingleQuotes = SerializerFeature.isEnabled(out.features, fieldInfo.serialzeFeatures, SerializerFeature.UseSingleQuotes);
                final boolean directWritePrefix = out.quoteFieldNames && !fieldUseSingleQuotes;
                if (!skipTransient || fieldInfo == null || !fieldInfo.fieldTransient) {
                    if (!ignoreNonFieldGetter || field != null) {
                        boolean notApply = false;
                        if (!this.applyName(serializer, object, fieldInfoName) || !this.applyLabel(serializer, fieldInfo.label)) {
                            if (!writeAsArray) {
                                continue;
                            }
                            notApply = true;
                        }
                        if (this.beanInfo.typeKey == null || !fieldInfoName.equals(this.beanInfo.typeKey) || !serializer.isWriteClassName(fieldType, object)) {
                            Object propertyValue;
                            if (notApply) {
                                propertyValue = null;
                            }
                            else {
                                try {
                                    propertyValue = fieldSerializer.getPropertyValueDirect(object);
                                }
                                catch (InvocationTargetException ex) {
                                    errorFieldSerializer = fieldSerializer;
                                    if (!out.isEnabled(SerializerFeature.IgnoreErrorGetter)) {
                                        throw ex;
                                    }
                                    propertyValue = null;
                                }
                            }
                            if (this.apply(serializer, object, fieldInfoName, propertyValue)) {
                                if (fieldClass == String.class && "trim".equals(fieldInfo.format) && propertyValue != null) {
                                    propertyValue = ((String)propertyValue).trim();
                                }
                                String key = fieldInfoName;
                                key = this.processKey(serializer, object, key, propertyValue);
                                final Object originalValue = propertyValue;
                                propertyValue = this.processValue(serializer, fieldSerializer.fieldContext, object, fieldInfoName, propertyValue, features);
                                if (propertyValue == null) {
                                    int serialzeFeatures = fieldInfo.serialzeFeatures;
                                    final JSONField jsonField = fieldInfo.getAnnotation();
                                    if (this.beanInfo.jsonType != null) {
                                        serialzeFeatures |= SerializerFeature.of(this.beanInfo.jsonType.serialzeFeatures());
                                    }
                                    if (jsonField != null && !"".equals(jsonField.defaultValue())) {
                                        propertyValue = jsonField.defaultValue();
                                    }
                                    else if (fieldClass == Boolean.class) {
                                        final int defaultMask = SerializerFeature.WriteNullBooleanAsFalse.mask;
                                        final int mask = defaultMask | SerializerFeature.WriteMapNullValue.mask;
                                        if (!writeAsArray && (serialzeFeatures & mask) == 0x0 && (out.features & mask) == 0x0) {
                                            continue;
                                        }
                                        if ((serialzeFeatures & defaultMask) != 0x0) {
                                            propertyValue = false;
                                        }
                                        else if ((out.features & defaultMask) != 0x0 && (serialzeFeatures & SerializerFeature.WriteMapNullValue.mask) == 0x0) {
                                            propertyValue = false;
                                        }
                                    }
                                    else if (fieldClass == String.class) {
                                        final int defaultMask = SerializerFeature.WriteNullStringAsEmpty.mask;
                                        final int mask = defaultMask | SerializerFeature.WriteMapNullValue.mask;
                                        if (!writeAsArray && (serialzeFeatures & mask) == 0x0 && (out.features & mask) == 0x0) {
                                            continue;
                                        }
                                        if ((serialzeFeatures & defaultMask) != 0x0) {
                                            propertyValue = "";
                                        }
                                        else if ((out.features & defaultMask) != 0x0 && (serialzeFeatures & SerializerFeature.WriteMapNullValue.mask) == 0x0) {
                                            propertyValue = "";
                                        }
                                    }
                                    else if (Number.class.isAssignableFrom(fieldClass)) {
                                        final int defaultMask = SerializerFeature.WriteNullNumberAsZero.mask;
                                        final int mask = defaultMask | SerializerFeature.WriteMapNullValue.mask;
                                        if (!writeAsArray && (serialzeFeatures & mask) == 0x0 && (out.features & mask) == 0x0) {
                                            continue;
                                        }
                                        if ((serialzeFeatures & defaultMask) != 0x0) {
                                            propertyValue = 0;
                                        }
                                        else if ((out.features & defaultMask) != 0x0 && (serialzeFeatures & SerializerFeature.WriteMapNullValue.mask) == 0x0) {
                                            propertyValue = 0;
                                        }
                                    }
                                    else if (Collection.class.isAssignableFrom(fieldClass)) {
                                        final int defaultMask = SerializerFeature.WriteNullListAsEmpty.mask;
                                        final int mask = defaultMask | SerializerFeature.WriteMapNullValue.mask;
                                        if (!writeAsArray && (serialzeFeatures & mask) == 0x0 && (out.features & mask) == 0x0) {
                                            continue;
                                        }
                                        if ((serialzeFeatures & defaultMask) != 0x0) {
                                            propertyValue = Collections.emptyList();
                                        }
                                        else if ((out.features & defaultMask) != 0x0 && (serialzeFeatures & SerializerFeature.WriteMapNullValue.mask) == 0x0) {
                                            propertyValue = Collections.emptyList();
                                        }
                                    }
                                    else if (!writeAsArray && !fieldSerializer.writeNull && !out.isEnabled(SerializerFeature.WriteMapNullValue.mask) && (serialzeFeatures & SerializerFeature.WriteMapNullValue.mask) == 0x0) {
                                        continue;
                                    }
                                }
                                if (propertyValue != null && (out.notWriteDefaultValue || (fieldInfo.serialzeFeatures & SerializerFeature.NotWriteDefaultValue.mask) != 0x0 || (this.beanInfo.features & SerializerFeature.NotWriteDefaultValue.mask) != 0x0)) {
                                    final Class<?> fieldCLass = fieldInfo.fieldClass;
                                    if (fieldCLass == Byte.TYPE && propertyValue instanceof Byte && (byte)propertyValue == 0) {
                                        continue;
                                    }
                                    if (fieldCLass == Short.TYPE && propertyValue instanceof Short && (short)propertyValue == 0) {
                                        continue;
                                    }
                                    if (fieldCLass == Integer.TYPE && propertyValue instanceof Integer && (int)propertyValue == 0) {
                                        continue;
                                    }
                                    if (fieldCLass == Long.TYPE && propertyValue instanceof Long && (long)propertyValue == 0L) {
                                        continue;
                                    }
                                    if (fieldCLass == Float.TYPE && propertyValue instanceof Float && (float)propertyValue == 0.0f) {
                                        continue;
                                    }
                                    if (fieldCLass == Double.TYPE && propertyValue instanceof Double && (double)propertyValue == 0.0) {
                                        continue;
                                    }
                                    if (fieldCLass == Boolean.TYPE && propertyValue instanceof Boolean && !(boolean)propertyValue) {
                                        continue;
                                    }
                                }
                                if (commaFlag) {
                                    if (fieldInfo.unwrapped && propertyValue instanceof Map && ((Map)propertyValue).size() == 0) {
                                        continue;
                                    }
                                    out.write(44);
                                    if (out.isEnabled(SerializerFeature.PrettyFormat)) {
                                        serializer.println();
                                    }
                                }
                                if (key != fieldInfoName) {
                                    if (!writeAsArray) {
                                        out.writeFieldName(key, true);
                                    }
                                    serializer.write(propertyValue);
                                }
                                else if (originalValue != propertyValue) {
                                    if (!writeAsArray) {
                                        fieldSerializer.writePrefix(serializer);
                                    }
                                    serializer.write(propertyValue);
                                }
                                else {
                                    if (!writeAsArray) {
                                        final boolean isMap = Map.class.isAssignableFrom(fieldClass);
                                        final boolean isJavaBean = (!fieldClass.isPrimitive() && !fieldClass.getName().startsWith("java.")) || fieldClass == Object.class;
                                        if (writeClassName || !fieldInfo.unwrapped || (!isMap && !isJavaBean)) {
                                            if (directWritePrefix) {
                                                out.write(fieldInfo.name_chars, 0, fieldInfo.name_chars.length);
                                            }
                                            else {
                                                fieldSerializer.writePrefix(serializer);
                                            }
                                        }
                                    }
                                    if (!writeAsArray) {
                                        final JSONField fieldAnnotation = fieldInfo.getAnnotation();
                                        if (fieldClass == String.class && (fieldAnnotation == null || fieldAnnotation.serializeUsing() == Void.class)) {
                                            if (propertyValue == null) {
                                                int serialzeFeatures2 = fieldSerializer.features;
                                                if (this.beanInfo.jsonType != null) {
                                                    serialzeFeatures2 |= SerializerFeature.of(this.beanInfo.jsonType.serialzeFeatures());
                                                }
                                                if ((out.features & SerializerFeature.WriteNullStringAsEmpty.mask) != 0x0 && (serialzeFeatures2 & SerializerFeature.WriteMapNullValue.mask) == 0x0) {
                                                    out.writeString("");
                                                }
                                                else if ((serialzeFeatures2 & SerializerFeature.WriteNullStringAsEmpty.mask) != 0x0) {
                                                    out.writeString("");
                                                }
                                                else {
                                                    out.writeNull();
                                                }
                                            }
                                            else {
                                                final String propertyValueString = (String)propertyValue;
                                                if (fieldUseSingleQuotes) {
                                                    out.writeStringWithSingleQuote(propertyValueString);
                                                }
                                                else {
                                                    out.writeStringWithDoubleQuote(propertyValueString, '\0');
                                                }
                                            }
                                        }
                                        else {
                                            if (fieldInfo.unwrapped && propertyValue instanceof Map && ((Map)propertyValue).size() == 0) {
                                                commaFlag = false;
                                                continue;
                                            }
                                            fieldSerializer.writeValue(serializer, propertyValue);
                                        }
                                    }
                                    else {
                                        fieldSerializer.writeValue(serializer, propertyValue);
                                    }
                                }
                                boolean fieldUnwrappedNull = false;
                                if (fieldInfo.unwrapped && propertyValue instanceof Map) {
                                    final Map map = (Map)propertyValue;
                                    if (map.size() == 0) {
                                        fieldUnwrappedNull = true;
                                    }
                                    else if (!serializer.isEnabled(SerializerFeature.WriteMapNullValue)) {
                                        boolean hasNotNull = false;
                                        for (final Object value : map.values()) {
                                            if (value != null) {
                                                hasNotNull = true;
                                                break;
                                            }
                                        }
                                        if (!hasNotNull) {
                                            fieldUnwrappedNull = true;
                                        }
                                    }
                                }
                                if (!fieldUnwrappedNull) {
                                    commaFlag = true;
                                }
                            }
                        }
                    }
                }
            }
            this.writeAfter(serializer, object, commaFlag ? ',' : '\0');
            if (getters.length > 0 && out.isEnabled(SerializerFeature.PrettyFormat)) {
                serializer.decrementIdent();
                serializer.println();
            }
            if (!unwrapped) {
                out.append(endSeperator);
            }
        }
        catch (Exception e) {
            String errorMessage = "write javaBean error, fastjson version 1.2.68";
            if (object != null) {
                errorMessage = errorMessage + ", class " + object.getClass().getName();
            }
            if (fieldName != null) {
                errorMessage = errorMessage + ", fieldName : " + fieldName;
            }
            else if (errorFieldSerializer != null && errorFieldSerializer.fieldInfo != null) {
                final FieldInfo fieldInfo2 = errorFieldSerializer.fieldInfo;
                if (fieldInfo2.method != null) {
                    errorMessage = errorMessage + ", method : " + fieldInfo2.method.getName();
                }
                else {
                    errorMessage = errorMessage + ", fieldName : " + errorFieldSerializer.fieldInfo.name;
                }
            }
            if (e.getMessage() != null) {
                errorMessage = errorMessage + ", " + e.getMessage();
            }
            Throwable cause = null;
            if (e instanceof InvocationTargetException) {
                cause = e.getCause();
            }
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(errorMessage, cause);
        }
        finally {
            serializer.context = parent;
        }
    }
    
    protected void writeClassName(final JSONSerializer serializer, String typeKey, final Object object) {
        if (typeKey == null) {
            typeKey = serializer.config.typeKey;
        }
        serializer.out.writeFieldName(typeKey, false);
        String typeName = this.beanInfo.typeName;
        if (typeName == null) {
            Class<?> clazz = object.getClass();
            if (TypeUtils.isProxy(clazz)) {
                clazz = clazz.getSuperclass();
            }
            typeName = clazz.getName();
        }
        serializer.write(typeName);
    }
    
    public boolean writeReference(final JSONSerializer serializer, final Object object, final int fieldFeatures) {
        final SerialContext context = serializer.context;
        final int mask = SerializerFeature.DisableCircularReferenceDetect.mask;
        if (context == null || (context.features & mask) != 0x0 || (fieldFeatures & mask) != 0x0) {
            return false;
        }
        if (serializer.references != null && serializer.references.containsKey(object)) {
            serializer.writeReference(object);
            return true;
        }
        return false;
    }
    
    protected boolean isWriteAsArray(final JSONSerializer serializer) {
        return this.isWriteAsArray(serializer, 0);
    }
    
    protected boolean isWriteAsArray(final JSONSerializer serializer, final int fieldFeatrues) {
        final int mask = SerializerFeature.BeanToArray.mask;
        return (this.beanInfo.features & mask) != 0x0 || serializer.out.beanToArray || (fieldFeatrues & mask) != 0x0;
    }
    
    public Object getFieldValue(final Object object, final String key) {
        final FieldSerializer fieldDeser = this.getFieldSerializer(key);
        if (fieldDeser == null) {
            throw new JSONException("field not found. " + key);
        }
        try {
            return fieldDeser.getPropertyValue(object);
        }
        catch (InvocationTargetException ex) {
            throw new JSONException("getFieldValue error." + key, ex);
        }
        catch (IllegalAccessException ex2) {
            throw new JSONException("getFieldValue error." + key, ex2);
        }
    }
    
    public Object getFieldValue(final Object object, final String key, final long keyHash, final boolean throwFieldNotFoundException) {
        final FieldSerializer fieldDeser = this.getFieldSerializer(keyHash);
        if (fieldDeser == null) {
            if (throwFieldNotFoundException) {
                throw new JSONException("field not found. " + key);
            }
            return null;
        }
        else {
            try {
                return fieldDeser.getPropertyValue(object);
            }
            catch (InvocationTargetException ex) {
                throw new JSONException("getFieldValue error." + key, ex);
            }
            catch (IllegalAccessException ex2) {
                throw new JSONException("getFieldValue error." + key, ex2);
            }
        }
    }
    
    public FieldSerializer getFieldSerializer(final String key) {
        if (key == null) {
            return null;
        }
        int low = 0;
        int high = this.sortedGetters.length - 1;
        while (low <= high) {
            final int mid = low + high >>> 1;
            final String fieldName = this.sortedGetters[mid].fieldInfo.name;
            final int cmp = fieldName.compareTo(key);
            if (cmp < 0) {
                low = mid + 1;
            }
            else {
                if (cmp <= 0) {
                    return this.sortedGetters[mid];
                }
                high = mid - 1;
            }
        }
        return null;
    }
    
    public FieldSerializer getFieldSerializer(final long hash) {
        PropertyNamingStrategy[] namingStrategies = null;
        if (this.hashArray == null) {
            namingStrategies = PropertyNamingStrategy.values();
            final long[] hashArray = new long[this.sortedGetters.length * namingStrategies.length];
            int index = 0;
            for (int i = 0; i < this.sortedGetters.length; ++i) {
                final String name = this.sortedGetters[i].fieldInfo.name;
                hashArray[index++] = TypeUtils.fnv1a_64(name);
                for (int j = 0; j < namingStrategies.length; ++j) {
                    final String name_t = namingStrategies[j].translate(name);
                    if (!name.equals(name_t)) {
                        hashArray[index++] = TypeUtils.fnv1a_64(name_t);
                    }
                }
            }
            Arrays.sort(hashArray, 0, index);
            System.arraycopy(hashArray, 0, this.hashArray = new long[index], 0, index);
        }
        final int pos = Arrays.binarySearch(this.hashArray, hash);
        if (pos < 0) {
            return null;
        }
        if (this.hashArrayMapping == null) {
            if (namingStrategies == null) {
                namingStrategies = PropertyNamingStrategy.values();
            }
            final short[] mapping = new short[this.hashArray.length];
            Arrays.fill(mapping, (short)(-1));
            for (int i = 0; i < this.sortedGetters.length; ++i) {
                final String name = this.sortedGetters[i].fieldInfo.name;
                final int p = Arrays.binarySearch(this.hashArray, TypeUtils.fnv1a_64(name));
                if (p >= 0) {
                    mapping[p] = (short)i;
                }
                for (int k = 0; k < namingStrategies.length; ++k) {
                    final String name_t2 = namingStrategies[k].translate(name);
                    if (!name.equals(name_t2)) {
                        final int p_t = Arrays.binarySearch(this.hashArray, TypeUtils.fnv1a_64(name_t2));
                        if (p_t >= 0) {
                            mapping[p_t] = (short)i;
                        }
                    }
                }
            }
            this.hashArrayMapping = mapping;
        }
        final int getterIndex = this.hashArrayMapping[pos];
        if (getterIndex != -1) {
            return this.sortedGetters[getterIndex];
        }
        return null;
    }
    
    public List<Object> getFieldValues(final Object object) throws Exception {
        final List<Object> fieldValues = new ArrayList<Object>(this.sortedGetters.length);
        for (final FieldSerializer getter : this.sortedGetters) {
            fieldValues.add(getter.getPropertyValue(object));
        }
        return fieldValues;
    }
    
    public List<Object> getObjectFieldValues(final Object object) throws Exception {
        final List<Object> fieldValues = new ArrayList<Object>(this.sortedGetters.length);
        for (final FieldSerializer getter : this.sortedGetters) {
            final Class fieldClass = getter.fieldInfo.fieldClass;
            if (!fieldClass.isPrimitive()) {
                if (!fieldClass.getName().startsWith("java.lang.")) {
                    fieldValues.add(getter.getPropertyValue(object));
                }
            }
        }
        return fieldValues;
    }
    
    public int getSize(final Object object) throws Exception {
        int size = 0;
        for (final FieldSerializer getter : this.sortedGetters) {
            final Object value = getter.getPropertyValueDirect(object);
            if (value != null) {
                ++size;
            }
        }
        return size;
    }
    
    public Set<String> getFieldNames(final Object object) throws Exception {
        final Set<String> fieldNames = new HashSet<String>();
        for (final FieldSerializer getter : this.sortedGetters) {
            final Object value = getter.getPropertyValueDirect(object);
            if (value != null) {
                fieldNames.add(getter.fieldInfo.name);
            }
        }
        return fieldNames;
    }
    
    public Map<String, Object> getFieldValuesMap(final Object object) throws Exception {
        final Map<String, Object> map = new LinkedHashMap<String, Object>(this.sortedGetters.length);
        boolean skipTransient = true;
        FieldInfo fieldInfo = null;
        for (final FieldSerializer getter : this.sortedGetters) {
            skipTransient = SerializerFeature.isEnabled(getter.features, SerializerFeature.SkipTransientField);
            fieldInfo = getter.fieldInfo;
            if (!skipTransient || fieldInfo == null || !fieldInfo.fieldTransient) {
                if (getter.fieldInfo.unwrapped) {
                    final Object unwrappedValue = getter.getPropertyValue(object);
                    final Object map2 = JSON.toJSON(unwrappedValue);
                    if (map2 instanceof Map) {
                        map.putAll((Map<? extends String, ?>)map2);
                    }
                    else {
                        map.put(getter.fieldInfo.name, getter.getPropertyValue(object));
                    }
                }
                else {
                    map.put(getter.fieldInfo.name, getter.getPropertyValue(object));
                }
            }
        }
        return map;
    }
    
    protected BeanContext getBeanContext(final int orinal) {
        return this.sortedGetters[orinal].fieldContext;
    }
    
    protected Type getFieldType(final int ordinal) {
        return this.sortedGetters[ordinal].fieldInfo.fieldType;
    }
    
    protected char writeBefore(final JSONSerializer jsonBeanDeser, final Object object, char seperator) {
        if (jsonBeanDeser.beforeFilters != null) {
            for (final BeforeFilter beforeFilter : jsonBeanDeser.beforeFilters) {
                seperator = beforeFilter.writeBefore(jsonBeanDeser, object, seperator);
            }
        }
        if (this.beforeFilters != null) {
            for (final BeforeFilter beforeFilter : this.beforeFilters) {
                seperator = beforeFilter.writeBefore(jsonBeanDeser, object, seperator);
            }
        }
        return seperator;
    }
    
    protected char writeAfter(final JSONSerializer jsonBeanDeser, final Object object, char seperator) {
        if (jsonBeanDeser.afterFilters != null) {
            for (final AfterFilter afterFilter : jsonBeanDeser.afterFilters) {
                seperator = afterFilter.writeAfter(jsonBeanDeser, object, seperator);
            }
        }
        if (this.afterFilters != null) {
            for (final AfterFilter afterFilter : this.afterFilters) {
                seperator = afterFilter.writeAfter(jsonBeanDeser, object, seperator);
            }
        }
        return seperator;
    }
    
    protected boolean applyLabel(final JSONSerializer jsonBeanDeser, final String label) {
        if (jsonBeanDeser.labelFilters != null) {
            for (final LabelFilter propertyFilter : jsonBeanDeser.labelFilters) {
                if (!propertyFilter.apply(label)) {
                    return false;
                }
            }
        }
        if (this.labelFilters != null) {
            for (final LabelFilter propertyFilter : this.labelFilters) {
                if (!propertyFilter.apply(label)) {
                    return false;
                }
            }
        }
        return true;
    }
}
