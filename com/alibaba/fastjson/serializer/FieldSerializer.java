package com.alibaba.fastjson.serializer;

import java.util.Collection;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSON;
import java.util.Date;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.util.FieldInfo;

public class FieldSerializer implements Comparable<FieldSerializer>
{
    public final FieldInfo fieldInfo;
    protected final boolean writeNull;
    protected int features;
    private final String double_quoted_fieldPrefix;
    private String single_quoted_fieldPrefix;
    private String un_quoted_fieldPrefix;
    protected BeanContext fieldContext;
    private String format;
    protected boolean writeEnumUsingToString;
    protected boolean writeEnumUsingName;
    protected boolean disableCircularReferenceDetect;
    protected boolean serializeUsing;
    protected boolean persistenceXToMany;
    protected boolean browserCompatible;
    private RuntimeSerializerInfo runtimeInfo;
    
    public FieldSerializer(final Class<?> beanType, final FieldInfo fieldInfo) {
        this.writeEnumUsingToString = false;
        this.writeEnumUsingName = false;
        this.disableCircularReferenceDetect = false;
        this.serializeUsing = false;
        this.persistenceXToMany = false;
        this.fieldInfo = fieldInfo;
        this.fieldContext = new BeanContext(beanType, fieldInfo);
        if (beanType != null) {
            final JSONType jsonType = TypeUtils.getAnnotation(beanType, JSONType.class);
            if (jsonType != null) {
                for (final SerializerFeature feature : jsonType.serialzeFeatures()) {
                    if (feature == SerializerFeature.WriteEnumUsingToString) {
                        this.writeEnumUsingToString = true;
                    }
                    else if (feature == SerializerFeature.WriteEnumUsingName) {
                        this.writeEnumUsingName = true;
                    }
                    else if (feature == SerializerFeature.DisableCircularReferenceDetect) {
                        this.disableCircularReferenceDetect = true;
                    }
                    else if (feature == SerializerFeature.BrowserCompatible) {
                        this.features |= SerializerFeature.BrowserCompatible.mask;
                        this.browserCompatible = true;
                    }
                    else if (feature == SerializerFeature.WriteMapNullValue) {
                        this.features |= SerializerFeature.WriteMapNullValue.mask;
                    }
                }
            }
        }
        fieldInfo.setAccessible();
        this.double_quoted_fieldPrefix = '\"' + fieldInfo.name + "\":";
        boolean writeNull = false;
        final JSONField annotation = fieldInfo.getAnnotation();
        if (annotation != null) {
            for (final SerializerFeature feature2 : annotation.serialzeFeatures()) {
                if ((feature2.getMask() & SerializerFeature.WRITE_MAP_NULL_FEATURES) != 0x0) {
                    writeNull = true;
                    break;
                }
            }
            this.format = annotation.format();
            if (this.format.trim().length() == 0) {
                this.format = null;
            }
            for (final SerializerFeature feature2 : annotation.serialzeFeatures()) {
                if (feature2 == SerializerFeature.WriteEnumUsingToString) {
                    this.writeEnumUsingToString = true;
                }
                else if (feature2 == SerializerFeature.WriteEnumUsingName) {
                    this.writeEnumUsingName = true;
                }
                else if (feature2 == SerializerFeature.DisableCircularReferenceDetect) {
                    this.disableCircularReferenceDetect = true;
                }
                else if (feature2 == SerializerFeature.BrowserCompatible) {
                    this.browserCompatible = true;
                }
            }
            this.features |= SerializerFeature.of(annotation.serialzeFeatures());
        }
        this.writeNull = writeNull;
        this.persistenceXToMany = (TypeUtils.isAnnotationPresentOneToMany(fieldInfo.method) || TypeUtils.isAnnotationPresentManyToMany(fieldInfo.method));
    }
    
    public void writePrefix(final JSONSerializer serializer) throws IOException {
        final SerializeWriter out = serializer.out;
        if (out.quoteFieldNames) {
            final boolean useSingleQuotes = SerializerFeature.isEnabled(out.features, this.fieldInfo.serialzeFeatures, SerializerFeature.UseSingleQuotes);
            if (useSingleQuotes) {
                if (this.single_quoted_fieldPrefix == null) {
                    this.single_quoted_fieldPrefix = '\'' + this.fieldInfo.name + "':";
                }
                out.write(this.single_quoted_fieldPrefix);
            }
            else {
                out.write(this.double_quoted_fieldPrefix);
            }
        }
        else {
            if (this.un_quoted_fieldPrefix == null) {
                this.un_quoted_fieldPrefix = this.fieldInfo.name + ":";
            }
            out.write(this.un_quoted_fieldPrefix);
        }
    }
    
    public Object getPropertyValueDirect(final Object object) throws InvocationTargetException, IllegalAccessException {
        final Object fieldValue = this.fieldInfo.get(object);
        if (this.persistenceXToMany && !TypeUtils.isHibernateInitialized(fieldValue)) {
            return null;
        }
        return fieldValue;
    }
    
    public Object getPropertyValue(final Object object) throws InvocationTargetException, IllegalAccessException {
        final Object propertyValue = this.fieldInfo.get(object);
        if (this.format != null && propertyValue != null && (this.fieldInfo.fieldClass == Date.class || this.fieldInfo.fieldClass == java.sql.Date.class)) {
            final SimpleDateFormat dateFormat = new SimpleDateFormat(this.format, JSON.defaultLocale);
            dateFormat.setTimeZone(JSON.defaultTimeZone);
            return dateFormat.format(propertyValue);
        }
        return propertyValue;
    }
    
    @Override
    public int compareTo(final FieldSerializer o) {
        return this.fieldInfo.compareTo(o.fieldInfo);
    }
    
    public void writeValue(final JSONSerializer serializer, final Object propertyValue) throws Exception {
        if (this.runtimeInfo == null) {
            Class<?> runtimeFieldClass;
            if (propertyValue == null) {
                runtimeFieldClass = this.fieldInfo.fieldClass;
                if (runtimeFieldClass == Byte.TYPE) {
                    runtimeFieldClass = Byte.class;
                }
                else if (runtimeFieldClass == Short.TYPE) {
                    runtimeFieldClass = Short.class;
                }
                else if (runtimeFieldClass == Integer.TYPE) {
                    runtimeFieldClass = Integer.class;
                }
                else if (runtimeFieldClass == Long.TYPE) {
                    runtimeFieldClass = Long.class;
                }
                else if (runtimeFieldClass == Float.TYPE) {
                    runtimeFieldClass = Float.class;
                }
                else if (runtimeFieldClass == Double.TYPE) {
                    runtimeFieldClass = Double.class;
                }
                else if (runtimeFieldClass == Boolean.TYPE) {
                    runtimeFieldClass = Boolean.class;
                }
            }
            else {
                runtimeFieldClass = propertyValue.getClass();
            }
            ObjectSerializer fieldSerializer = null;
            final JSONField fieldAnnotation = this.fieldInfo.getAnnotation();
            if (fieldAnnotation != null && fieldAnnotation.serializeUsing() != Void.class) {
                fieldSerializer = (ObjectSerializer)fieldAnnotation.serializeUsing().newInstance();
                this.serializeUsing = true;
            }
            else {
                if (this.format != null) {
                    if (runtimeFieldClass == Double.TYPE || runtimeFieldClass == Double.class) {
                        fieldSerializer = new DoubleSerializer(this.format);
                    }
                    else if (runtimeFieldClass == Float.TYPE || runtimeFieldClass == Float.class) {
                        fieldSerializer = new FloatCodec(this.format);
                    }
                }
                if (fieldSerializer == null) {
                    fieldSerializer = serializer.getObjectWriter(runtimeFieldClass);
                }
            }
            this.runtimeInfo = new RuntimeSerializerInfo(fieldSerializer, runtimeFieldClass);
        }
        final RuntimeSerializerInfo runtimeInfo = this.runtimeInfo;
        final int fieldFeatures = (this.disableCircularReferenceDetect ? (this.fieldInfo.serialzeFeatures | SerializerFeature.DisableCircularReferenceDetect.mask) : this.fieldInfo.serialzeFeatures) | this.features;
        if (propertyValue == null) {
            final SerializeWriter out = serializer.out;
            if (this.fieldInfo.fieldClass == Object.class && out.isEnabled(SerializerFeature.WRITE_MAP_NULL_FEATURES)) {
                out.writeNull();
                return;
            }
            final Class<?> runtimeFieldClass2 = runtimeInfo.runtimeFieldClass;
            if (Number.class.isAssignableFrom(runtimeFieldClass2)) {
                out.writeNull(this.features, SerializerFeature.WriteNullNumberAsZero.mask);
                return;
            }
            if (String.class == runtimeFieldClass2) {
                out.writeNull(this.features, SerializerFeature.WriteNullStringAsEmpty.mask);
                return;
            }
            if (Boolean.class == runtimeFieldClass2) {
                out.writeNull(this.features, SerializerFeature.WriteNullBooleanAsFalse.mask);
                return;
            }
            if (Collection.class.isAssignableFrom(runtimeFieldClass2) || runtimeFieldClass2.isArray()) {
                out.writeNull(this.features, SerializerFeature.WriteNullListAsEmpty.mask);
                return;
            }
            final ObjectSerializer fieldSerializer2 = runtimeInfo.fieldSerializer;
            if (out.isEnabled(SerializerFeature.WRITE_MAP_NULL_FEATURES) && fieldSerializer2 instanceof JavaBeanSerializer) {
                out.writeNull();
                return;
            }
            fieldSerializer2.write(serializer, null, this.fieldInfo.name, this.fieldInfo.fieldType, fieldFeatures);
        }
        else {
            if (this.fieldInfo.isEnum) {
                if (this.writeEnumUsingName) {
                    serializer.out.writeString(((Enum)propertyValue).name());
                    return;
                }
                if (this.writeEnumUsingToString) {
                    serializer.out.writeString(((Enum)propertyValue).toString());
                    return;
                }
            }
            final Class<?> valueClass = propertyValue.getClass();
            ObjectSerializer valueSerializer;
            if (valueClass == runtimeInfo.runtimeFieldClass || this.serializeUsing) {
                valueSerializer = runtimeInfo.fieldSerializer;
            }
            else {
                valueSerializer = serializer.getObjectWriter(valueClass);
            }
            if (this.format != null && !(valueSerializer instanceof DoubleSerializer) && !(valueSerializer instanceof FloatCodec)) {
                if (valueSerializer instanceof ContextObjectSerializer) {
                    ((ContextObjectSerializer)valueSerializer).write(serializer, propertyValue, this.fieldContext);
                }
                else {
                    serializer.writeWithFormat(propertyValue, this.format);
                }
                return;
            }
            if (this.fieldInfo.unwrapped) {
                if (valueSerializer instanceof JavaBeanSerializer) {
                    final JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer)valueSerializer;
                    javaBeanSerializer.write(serializer, propertyValue, this.fieldInfo.name, this.fieldInfo.fieldType, fieldFeatures, true);
                    return;
                }
                if (valueSerializer instanceof MapSerializer) {
                    final MapSerializer mapSerializer = (MapSerializer)valueSerializer;
                    mapSerializer.write(serializer, propertyValue, this.fieldInfo.name, this.fieldInfo.fieldType, fieldFeatures, true);
                    return;
                }
            }
            if ((this.features & SerializerFeature.WriteClassName.mask) != 0x0 && valueClass != this.fieldInfo.fieldClass && valueSerializer instanceof JavaBeanSerializer) {
                ((JavaBeanSerializer)valueSerializer).write(serializer, propertyValue, this.fieldInfo.name, this.fieldInfo.fieldType, fieldFeatures, false);
                return;
            }
            if (this.browserCompatible && (this.fieldInfo.fieldClass == Long.TYPE || this.fieldInfo.fieldClass == Long.class)) {
                final long value = (long)propertyValue;
                if (value > 9007199254740991L || value < -9007199254740991L) {
                    serializer.getWriter().writeString(Long.toString(value));
                    return;
                }
            }
            valueSerializer.write(serializer, propertyValue, this.fieldInfo.name, this.fieldInfo.fieldType, fieldFeatures);
        }
    }
    
    static class RuntimeSerializerInfo
    {
        final ObjectSerializer fieldSerializer;
        final Class<?> runtimeFieldClass;
        
        public RuntimeSerializerInfo(final ObjectSerializer fieldSerializer, final Class<?> runtimeFieldClass) {
            this.fieldSerializer = fieldSerializer;
            this.runtimeFieldClass = runtimeFieldClass;
        }
    }
}
