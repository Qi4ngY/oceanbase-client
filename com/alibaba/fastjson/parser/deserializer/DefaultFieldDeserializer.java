package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.parser.ParseContext;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.io.ByteArrayInputStream;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.parser.ParserConfig;

public class DefaultFieldDeserializer extends FieldDeserializer
{
    protected ObjectDeserializer fieldValueDeserilizer;
    protected boolean customDeserilizer;
    
    public DefaultFieldDeserializer(final ParserConfig config, final Class<?> clazz, final FieldInfo fieldInfo) {
        super(clazz, fieldInfo);
        this.customDeserilizer = false;
        final JSONField annotation = fieldInfo.getAnnotation();
        if (annotation != null) {
            final Class<?> deserializeUsing = annotation.deserializeUsing();
            this.customDeserilizer = (deserializeUsing != null && deserializeUsing != Void.class);
        }
    }
    
    public ObjectDeserializer getFieldValueDeserilizer(final ParserConfig config) {
        if (this.fieldValueDeserilizer == null) {
            final JSONField annotation = this.fieldInfo.getAnnotation();
            if (annotation != null && annotation.deserializeUsing() != Void.class) {
                final Class<?> deserializeUsing = annotation.deserializeUsing();
                try {
                    this.fieldValueDeserilizer = (ObjectDeserializer)deserializeUsing.newInstance();
                }
                catch (Exception ex) {
                    throw new JSONException("create deserializeUsing ObjectDeserializer error", ex);
                }
            }
            else {
                this.fieldValueDeserilizer = config.getDeserializer(this.fieldInfo.fieldClass, this.fieldInfo.fieldType);
            }
        }
        return this.fieldValueDeserilizer;
    }
    
    @Override
    public void parseField(final DefaultJSONParser parser, final Object object, final Type objectType, final Map<String, Object> fieldValues) {
        if (this.fieldValueDeserilizer == null) {
            this.getFieldValueDeserilizer(parser.getConfig());
        }
        ObjectDeserializer fieldValueDeserilizer = this.fieldValueDeserilizer;
        Type fieldType = this.fieldInfo.fieldType;
        if (objectType instanceof ParameterizedType) {
            final ParseContext objContext = parser.getContext();
            if (objContext != null) {
                objContext.type = objectType;
            }
            if (fieldType != objectType) {
                fieldType = FieldInfo.getFieldType(this.clazz, objectType, fieldType);
                fieldValueDeserilizer = parser.getConfig().getDeserializer(fieldType);
            }
        }
        Object value;
        if (fieldValueDeserilizer instanceof JavaBeanDeserializer && this.fieldInfo.parserFeatures != 0) {
            final JavaBeanDeserializer javaBeanDeser = (JavaBeanDeserializer)fieldValueDeserilizer;
            value = javaBeanDeser.deserialze(parser, fieldType, this.fieldInfo.name, this.fieldInfo.parserFeatures);
        }
        else if (this.fieldInfo.format != null && fieldValueDeserilizer instanceof ContextObjectDeserializer) {
            value = ((ContextObjectDeserializer)fieldValueDeserilizer).deserialze(parser, fieldType, this.fieldInfo.name, this.fieldInfo.format, this.fieldInfo.parserFeatures);
        }
        else {
            value = fieldValueDeserilizer.deserialze(parser, fieldType, this.fieldInfo.name);
        }
        if (value instanceof byte[] && ("gzip".equals(this.fieldInfo.format) || "gzip,base64".equals(this.fieldInfo.format))) {
            final byte[] bytes = (byte[])value;
            GZIPInputStream gzipIn = null;
            try {
                gzipIn = new GZIPInputStream(new ByteArrayInputStream(bytes));
                final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                while (true) {
                    final byte[] buf = new byte[1024];
                    final int len = gzipIn.read(buf);
                    if (len == -1) {
                        break;
                    }
                    if (len <= 0) {
                        continue;
                    }
                    byteOut.write(buf, 0, len);
                }
                value = byteOut.toByteArray();
            }
            catch (IOException ex) {
                throw new JSONException("unzip bytes error.", ex);
            }
        }
        if (parser.getResolveStatus() == 1) {
            final DefaultJSONParser.ResolveTask task = parser.getLastResolveTask();
            task.fieldDeserializer = this;
            task.ownerContext = parser.getContext();
            parser.setResolveStatus(0);
        }
        else if (object == null) {
            fieldValues.put(this.fieldInfo.name, value);
        }
        else {
            this.setValue(object, value);
        }
    }
    
    @Override
    public int getFastMatchToken() {
        if (this.fieldValueDeserilizer != null) {
            return this.fieldValueDeserilizer.getFastMatchToken();
        }
        return 2;
    }
    
    public void parseFieldUnwrapped(final DefaultJSONParser parser, final Object object, final Type objectType, final Map<String, Object> fieldValues) {
        throw new JSONException("TODO");
    }
}
