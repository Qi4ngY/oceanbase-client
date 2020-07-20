package com.alibaba.fastjson.serializer;

import java.util.Iterator;
import java.util.Collection;
import java.io.Closeable;
import com.alibaba.fastjson.util.IOUtils;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.io.IOException;
import com.alibaba.fastjson.JSONException;
import java.io.Writer;
import java.util.Collections;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSON;
import java.util.Locale;
import java.util.TimeZone;
import java.util.IdentityHashMap;
import java.text.DateFormat;

public class JSONSerializer extends SerializeFilterable
{
    protected final SerializeConfig config;
    public final SerializeWriter out;
    private int indentCount;
    private String indent;
    private String dateFormatPattern;
    private DateFormat dateFormat;
    protected IdentityHashMap<Object, SerialContext> references;
    protected SerialContext context;
    protected TimeZone timeZone;
    protected Locale locale;
    
    public JSONSerializer() {
        this(new SerializeWriter(), SerializeConfig.getGlobalInstance());
    }
    
    public JSONSerializer(final SerializeWriter out) {
        this(out, SerializeConfig.getGlobalInstance());
    }
    
    public JSONSerializer(final SerializeConfig config) {
        this(new SerializeWriter(), config);
    }
    
    public JSONSerializer(final SerializeWriter out, final SerializeConfig config) {
        this.indentCount = 0;
        this.indent = "\t";
        this.references = null;
        this.timeZone = JSON.defaultTimeZone;
        this.locale = JSON.defaultLocale;
        this.out = out;
        this.config = config;
    }
    
    public String getDateFormatPattern() {
        if (this.dateFormat instanceof SimpleDateFormat) {
            return ((SimpleDateFormat)this.dateFormat).toPattern();
        }
        return this.dateFormatPattern;
    }
    
    public DateFormat getDateFormat() {
        if (this.dateFormat == null && this.dateFormatPattern != null) {
            (this.dateFormat = new SimpleDateFormat(this.dateFormatPattern, this.locale)).setTimeZone(this.timeZone);
        }
        return this.dateFormat;
    }
    
    public void setDateFormat(final DateFormat dateFormat) {
        this.dateFormat = dateFormat;
        if (this.dateFormatPattern != null) {
            this.dateFormatPattern = null;
        }
    }
    
    public void setDateFormat(final String dateFormat) {
        this.dateFormatPattern = dateFormat;
        if (this.dateFormat != null) {
            this.dateFormat = null;
        }
    }
    
    public SerialContext getContext() {
        return this.context;
    }
    
    public void setContext(final SerialContext context) {
        this.context = context;
    }
    
    public void setContext(final SerialContext parent, final Object object, final Object fieldName, final int features) {
        this.setContext(parent, object, fieldName, features, 0);
    }
    
    public void setContext(final SerialContext parent, final Object object, final Object fieldName, final int features, final int fieldFeatures) {
        if (this.out.disableCircularReferenceDetect) {
            return;
        }
        this.context = new SerialContext(parent, object, fieldName, features, fieldFeatures);
        if (this.references == null) {
            this.references = new IdentityHashMap<Object, SerialContext>();
        }
        this.references.put(object, this.context);
    }
    
    public void setContext(final Object object, final Object fieldName) {
        this.setContext(this.context, object, fieldName, 0);
    }
    
    public void popContext() {
        if (this.context != null) {
            this.context = this.context.parent;
        }
    }
    
    public final boolean isWriteClassName(final Type fieldType, final Object obj) {
        return this.out.isEnabled(SerializerFeature.WriteClassName) && (fieldType != null || !this.out.isEnabled(SerializerFeature.NotWriteRootClassName) || (this.context != null && this.context.parent != null));
    }
    
    public boolean containsReference(final Object value) {
        if (this.references == null) {
            return false;
        }
        final SerialContext refContext = this.references.get(value);
        if (refContext == null) {
            return false;
        }
        if (value == Collections.emptyMap()) {
            return false;
        }
        final Object fieldName = refContext.fieldName;
        return fieldName == null || fieldName instanceof Integer || fieldName instanceof String;
    }
    
    public void writeReference(final Object object) {
        final SerialContext context = this.context;
        final Object current = context.object;
        if (object == current) {
            this.out.write("{\"$ref\":\"@\"}");
            return;
        }
        final SerialContext parentContext = context.parent;
        if (parentContext != null && object == parentContext.object) {
            this.out.write("{\"$ref\":\"..\"}");
            return;
        }
        SerialContext rootContext;
        for (rootContext = context; rootContext.parent != null; rootContext = rootContext.parent) {}
        if (object == rootContext.object) {
            this.out.write("{\"$ref\":\"$\"}");
        }
        else {
            this.out.write("{\"$ref\":\"");
            final String path = this.references.get(object).toString();
            this.out.write(path);
            this.out.write("\"}");
        }
    }
    
    public boolean checkValue(final SerializeFilterable filterable) {
        return (this.valueFilters != null && this.valueFilters.size() > 0) || (this.contextValueFilters != null && this.contextValueFilters.size() > 0) || (filterable.valueFilters != null && filterable.valueFilters.size() > 0) || (filterable.contextValueFilters != null && filterable.contextValueFilters.size() > 0) || this.out.writeNonStringValueAsString;
    }
    
    public boolean hasNameFilters(final SerializeFilterable filterable) {
        return (this.nameFilters != null && this.nameFilters.size() > 0) || (filterable.nameFilters != null && filterable.nameFilters.size() > 0);
    }
    
    public boolean hasPropertyFilters(final SerializeFilterable filterable) {
        return (this.propertyFilters != null && this.propertyFilters.size() > 0) || (filterable.propertyFilters != null && filterable.propertyFilters.size() > 0);
    }
    
    public int getIndentCount() {
        return this.indentCount;
    }
    
    public void incrementIndent() {
        ++this.indentCount;
    }
    
    public void decrementIdent() {
        --this.indentCount;
    }
    
    public void println() {
        this.out.write(10);
        for (int i = 0; i < this.indentCount; ++i) {
            this.out.write(this.indent);
        }
    }
    
    public SerializeWriter getWriter() {
        return this.out;
    }
    
    @Override
    public String toString() {
        return this.out.toString();
    }
    
    public void config(final SerializerFeature feature, final boolean state) {
        this.out.config(feature, state);
    }
    
    public boolean isEnabled(final SerializerFeature feature) {
        return this.out.isEnabled(feature);
    }
    
    public void writeNull() {
        this.out.writeNull();
    }
    
    public SerializeConfig getMapping() {
        return this.config;
    }
    
    public static void write(final Writer out, final Object object) {
        final SerializeWriter writer = new SerializeWriter();
        try {
            final JSONSerializer serializer = new JSONSerializer(writer);
            serializer.write(object);
            writer.writeTo(out);
        }
        catch (IOException ex) {
            throw new JSONException(ex.getMessage(), ex);
        }
        finally {
            writer.close();
        }
    }
    
    public static void write(final SerializeWriter out, final Object object) {
        final JSONSerializer serializer = new JSONSerializer(out);
        serializer.write(object);
    }
    
    public final void write(final Object object) {
        if (object == null) {
            this.out.writeNull();
            return;
        }
        final Class<?> clazz = object.getClass();
        final ObjectSerializer writer = this.getObjectWriter(clazz);
        try {
            writer.write(this, object, null, null, 0);
        }
        catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }
    
    public final void writeAs(final Object object, final Class type) {
        if (object == null) {
            this.out.writeNull();
            return;
        }
        final ObjectSerializer writer = this.getObjectWriter(type);
        try {
            writer.write(this, object, null, null, 0);
        }
        catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }
    
    public final void writeWithFieldName(final Object object, final Object fieldName) {
        this.writeWithFieldName(object, fieldName, null, 0);
    }
    
    protected final void writeKeyValue(final char seperator, final String key, final Object value) {
        if (seperator != '\0') {
            this.out.write(seperator);
        }
        this.out.writeFieldName(key);
        this.write(value);
    }
    
    public final void writeWithFieldName(final Object object, final Object fieldName, final Type fieldType, final int fieldFeatures) {
        try {
            if (object == null) {
                this.out.writeNull();
                return;
            }
            final Class<?> clazz = object.getClass();
            final ObjectSerializer writer = this.getObjectWriter(clazz);
            writer.write(this, object, fieldName, fieldType, fieldFeatures);
        }
        catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }
    
    public final void writeWithFormat(final Object object, final String format) {
        if (object instanceof Date) {
            if ("unixtime".equals(format)) {
                final long seconds = ((Date)object).getTime() / 1000L;
                this.out.writeInt((int)seconds);
                return;
            }
            if ("millis".equals(format)) {
                this.out.writeLong(((Date)object).getTime());
                return;
            }
            DateFormat dateFormat = this.getDateFormat();
            if (dateFormat == null) {
                try {
                    dateFormat = new SimpleDateFormat(format, this.locale);
                }
                catch (IllegalArgumentException e) {
                    final String format2 = format.replaceAll("T", "'T'");
                    dateFormat = new SimpleDateFormat(format2, this.locale);
                }
                dateFormat.setTimeZone(this.timeZone);
            }
            final String text = dateFormat.format((Date)object);
            this.out.writeString(text);
        }
        else {
            if (object instanceof byte[]) {
                final byte[] bytes = (byte[])object;
                if ("gzip".equals(format) || "gzip,base64".equals(format)) {
                    GZIPOutputStream gzipOut = null;
                    try {
                        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                        if (bytes.length < 512) {
                            gzipOut = new GZIPOutputStream(byteOut, bytes.length);
                        }
                        else {
                            gzipOut = new GZIPOutputStream(byteOut);
                        }
                        gzipOut.write(bytes);
                        gzipOut.finish();
                        this.out.writeByteArray(byteOut.toByteArray());
                    }
                    catch (IOException ex) {
                        throw new JSONException("write gzipBytes error", ex);
                    }
                    finally {
                        IOUtils.close(gzipOut);
                    }
                }
                else if ("hex".equals(format)) {
                    this.out.writeHex(bytes);
                }
                else {
                    this.out.writeByteArray(bytes);
                }
                return;
            }
            if (object instanceof Collection) {
                final Collection collection = (Collection)object;
                final Iterator iterator = collection.iterator();
                this.out.write(91);
                for (int i = 0; i < collection.size(); ++i) {
                    final Object item = iterator.next();
                    if (i != 0) {
                        this.out.write(44);
                    }
                    this.writeWithFormat(item, format);
                }
                this.out.write(93);
                return;
            }
            this.write(object);
        }
    }
    
    public final void write(final String text) {
        StringCodec.instance.write(this, text);
    }
    
    public ObjectSerializer getObjectWriter(final Class<?> clazz) {
        return this.config.getObjectWriter(clazz);
    }
    
    public void close() {
        this.out.close();
    }
}
