package com.alibaba.fastjson.serializer;

import java.lang.reflect.Proxy;
import com.alibaba.fastjson.support.moneta.MonetaCodec;
import com.alibaba.fastjson.support.springfox.SwaggerJsonSerializer;
import com.alibaba.fastjson.parser.deserializer.OptionalCodec;
import com.alibaba.fastjson.parser.deserializer.Jdk8DateCodec;
import org.w3c.dom.Node;
import java.util.Iterator;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.Enumeration;
import com.alibaba.fastjson.JSONStreamAware;
import com.alibaba.fastjson.JSONAware;
import java.util.Date;
import java.util.Collection;
import com.alibaba.fastjson.util.ServiceLoader;
import java.util.LinkedList;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.UUID;
import java.net.URL;
import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;
import java.nio.charset.Charset;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Inet6Address;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.TimeZone;
import java.util.Currency;
import java.text.SimpleDateFormat;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.ArrayList;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.util.ASMUtils;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Map;
import com.alibaba.fastjson.JSONException;
import java.util.Arrays;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.spi.Module;
import java.util.List;
import java.lang.reflect.Type;
import com.alibaba.fastjson.util.IdentityHashMap;
import com.alibaba.fastjson.PropertyNamingStrategy;

public class SerializeConfig
{
    public static final SerializeConfig globalInstance;
    private static boolean awtError;
    private static boolean jdk8Error;
    private static boolean oracleJdbcError;
    private static boolean springfoxError;
    private static boolean guavaError;
    private static boolean jsonnullError;
    private static boolean jsonobjectError;
    private static boolean jodaError;
    private boolean asm;
    private ASMSerializerFactory asmFactory;
    protected String typeKey;
    public PropertyNamingStrategy propertyNamingStrategy;
    private final IdentityHashMap<Type, ObjectSerializer> serializers;
    private final IdentityHashMap<Type, IdentityHashMap<Type, ObjectSerializer>> mixInSerializers;
    private final boolean fieldBased;
    private long[] denyClasses;
    private List<Module> modules;
    
    public String getTypeKey() {
        return this.typeKey;
    }
    
    public void setTypeKey(final String typeKey) {
        this.typeKey = typeKey;
    }
    
    private final JavaBeanSerializer createASMSerializer(final SerializeBeanInfo beanInfo) throws Exception {
        final JavaBeanSerializer serializer = this.asmFactory.createJavaBeanSerializer(beanInfo);
        for (int i = 0; i < serializer.sortedGetters.length; ++i) {
            final FieldSerializer fieldDeser = serializer.sortedGetters[i];
            final Class<?> fieldClass = fieldDeser.fieldInfo.fieldClass;
            if (fieldClass.isEnum()) {
                final ObjectSerializer fieldSer = this.getObjectWriter(fieldClass);
                if (!(fieldSer instanceof EnumSerializer)) {
                    serializer.writeDirect = false;
                }
            }
        }
        return serializer;
    }
    
    public final ObjectSerializer createJavaBeanSerializer(final Class<?> clazz) {
        final String className = clazz.getName();
        final long hashCode64 = TypeUtils.fnv1a_64(className);
        if (Arrays.binarySearch(this.denyClasses, hashCode64) >= 0) {
            throw new JSONException("not support class : " + className);
        }
        final SerializeBeanInfo beanInfo = TypeUtils.buildBeanInfo(clazz, null, this.propertyNamingStrategy, this.fieldBased);
        if (beanInfo.fields.length == 0 && Iterable.class.isAssignableFrom(clazz)) {
            return MiscCodec.instance;
        }
        return this.createJavaBeanSerializer(beanInfo);
    }
    
    public ObjectSerializer createJavaBeanSerializer(final SerializeBeanInfo beanInfo) {
        final JSONType jsonType = beanInfo.jsonType;
        boolean asm = this.asm && !this.fieldBased;
        if (jsonType != null) {
            final Class<?> serializerClass = jsonType.serializer();
            if (serializerClass != Void.class) {
                try {
                    final Object seralizer = serializerClass.newInstance();
                    if (seralizer instanceof ObjectSerializer) {
                        return (ObjectSerializer)seralizer;
                    }
                }
                catch (Throwable t) {}
            }
            if (!jsonType.asm()) {
                asm = false;
            }
            if (asm) {
                for (final SerializerFeature feature : jsonType.serialzeFeatures()) {
                    if (SerializerFeature.WriteNonStringValueAsString == feature || SerializerFeature.WriteEnumUsingToString == feature || SerializerFeature.NotWriteDefaultValue == feature || SerializerFeature.BrowserCompatible == feature) {
                        asm = false;
                        break;
                    }
                }
            }
            if (asm) {
                final Class<? extends SerializeFilter>[] filterClasses = jsonType.serialzeFilters();
                if (filterClasses.length != 0) {
                    asm = false;
                }
            }
        }
        final Class<?> clazz = beanInfo.beanType;
        if (!Modifier.isPublic(beanInfo.beanType.getModifiers())) {
            return new JavaBeanSerializer(beanInfo);
        }
        if ((asm && this.asmFactory.classLoader.isExternalClass(clazz)) || clazz == Serializable.class || clazz == Object.class) {
            asm = false;
        }
        if (asm && !ASMUtils.checkName(clazz.getSimpleName())) {
            asm = false;
        }
        if (asm && beanInfo.beanType.isInterface()) {
            asm = false;
        }
        if (asm) {
            for (final FieldInfo fieldInfo : beanInfo.fields) {
                final Field field = fieldInfo.field;
                if (field != null && !field.getType().equals(fieldInfo.fieldClass)) {
                    asm = false;
                    break;
                }
                final Method method = fieldInfo.method;
                if (method != null && !method.getReturnType().equals(fieldInfo.fieldClass)) {
                    asm = false;
                    break;
                }
                final JSONField annotation = fieldInfo.getAnnotation();
                if (annotation != null) {
                    final String format = annotation.format();
                    if (format.length() != 0 && (fieldInfo.fieldClass != String.class || !"trim".equals(format))) {
                        asm = false;
                        break;
                    }
                    if (!ASMUtils.checkName(annotation.name()) || annotation.jsonDirect() || annotation.serializeUsing() != Void.class || annotation.unwrapped()) {
                        asm = false;
                        break;
                    }
                    for (final SerializerFeature feature2 : annotation.serialzeFeatures()) {
                        if (SerializerFeature.WriteNonStringValueAsString == feature2 || SerializerFeature.WriteEnumUsingToString == feature2 || SerializerFeature.NotWriteDefaultValue == feature2 || SerializerFeature.BrowserCompatible == feature2 || SerializerFeature.WriteClassName == feature2) {
                            asm = false;
                            break;
                        }
                    }
                    if (TypeUtils.isAnnotationPresentOneToMany(method) || TypeUtils.isAnnotationPresentManyToMany(method)) {
                        asm = false;
                        break;
                    }
                    if (annotation.defaultValue() != null && !"".equals(annotation.defaultValue())) {
                        asm = false;
                        break;
                    }
                }
            }
        }
        if (asm) {
            try {
                final ObjectSerializer asmSerializer = this.createASMSerializer(beanInfo);
                if (asmSerializer != null) {
                    return asmSerializer;
                }
            }
            catch (ClassNotFoundException ex) {}
            catch (ClassFormatError classFormatError) {}
            catch (ClassCastException ex2) {}
            catch (OutOfMemoryError e) {
                if (e.getMessage().indexOf("Metaspace") != -1) {
                    throw e;
                }
            }
            catch (Throwable e2) {
                throw new JSONException("create asm serializer error, verson 1.2.68, class " + clazz, e2);
            }
        }
        return new JavaBeanSerializer(beanInfo);
    }
    
    public boolean isAsmEnable() {
        return this.asm;
    }
    
    public void setAsmEnable(final boolean asmEnable) {
        if (ASMUtils.IS_ANDROID) {
            return;
        }
        this.asm = asmEnable;
    }
    
    public static SerializeConfig getGlobalInstance() {
        return SerializeConfig.globalInstance;
    }
    
    public SerializeConfig() {
        this(8192);
    }
    
    public SerializeConfig(final boolean fieldBase) {
        this(8192, fieldBase);
    }
    
    public SerializeConfig(final int tableSize) {
        this(tableSize, false);
    }
    
    public SerializeConfig(final int tableSize, final boolean fieldBase) {
        this.asm = !ASMUtils.IS_ANDROID;
        this.typeKey = JSON.DEFAULT_TYPE_KEY;
        this.denyClasses = new long[] { 4165360493669296979L, 4446674157046724083L };
        this.modules = new ArrayList<Module>();
        this.fieldBased = fieldBase;
        this.serializers = new IdentityHashMap<Type, ObjectSerializer>(tableSize);
        this.mixInSerializers = new IdentityHashMap<Type, IdentityHashMap<Type, ObjectSerializer>>(16);
        try {
            if (this.asm) {
                this.asmFactory = new ASMSerializerFactory();
            }
        }
        catch (Throwable eror) {
            this.asm = false;
        }
        this.initSerializers();
    }
    
    private void initSerializers() {
        this.put(Boolean.class, BooleanCodec.instance);
        this.put(Character.class, CharacterCodec.instance);
        this.put(Byte.class, IntegerCodec.instance);
        this.put(Short.class, IntegerCodec.instance);
        this.put(Integer.class, IntegerCodec.instance);
        this.put(Long.class, LongCodec.instance);
        this.put(Float.class, FloatCodec.instance);
        this.put(Double.class, DoubleSerializer.instance);
        this.put(BigDecimal.class, BigDecimalCodec.instance);
        this.put(BigInteger.class, BigIntegerCodec.instance);
        this.put(String.class, StringCodec.instance);
        this.put(byte[].class, PrimitiveArraySerializer.instance);
        this.put(short[].class, PrimitiveArraySerializer.instance);
        this.put(int[].class, PrimitiveArraySerializer.instance);
        this.put(long[].class, PrimitiveArraySerializer.instance);
        this.put(float[].class, PrimitiveArraySerializer.instance);
        this.put(double[].class, PrimitiveArraySerializer.instance);
        this.put(boolean[].class, PrimitiveArraySerializer.instance);
        this.put(char[].class, PrimitiveArraySerializer.instance);
        this.put(Object[].class, ObjectArrayCodec.instance);
        this.put(Class.class, MiscCodec.instance);
        this.put(SimpleDateFormat.class, MiscCodec.instance);
        this.put(Currency.class, new MiscCodec());
        this.put(TimeZone.class, MiscCodec.instance);
        this.put(InetAddress.class, MiscCodec.instance);
        this.put(Inet4Address.class, MiscCodec.instance);
        this.put(Inet6Address.class, MiscCodec.instance);
        this.put(InetSocketAddress.class, MiscCodec.instance);
        this.put(File.class, MiscCodec.instance);
        this.put(Appendable.class, AppendableSerializer.instance);
        this.put(StringBuffer.class, AppendableSerializer.instance);
        this.put(StringBuilder.class, AppendableSerializer.instance);
        this.put(Charset.class, ToStringSerializer.instance);
        this.put(Pattern.class, ToStringSerializer.instance);
        this.put(Locale.class, ToStringSerializer.instance);
        this.put(URI.class, ToStringSerializer.instance);
        this.put(URL.class, ToStringSerializer.instance);
        this.put(UUID.class, ToStringSerializer.instance);
        this.put(AtomicBoolean.class, AtomicCodec.instance);
        this.put(AtomicInteger.class, AtomicCodec.instance);
        this.put(AtomicLong.class, AtomicCodec.instance);
        this.put(AtomicReference.class, ReferenceCodec.instance);
        this.put(AtomicIntegerArray.class, AtomicCodec.instance);
        this.put(AtomicLongArray.class, AtomicCodec.instance);
        this.put(WeakReference.class, ReferenceCodec.instance);
        this.put(SoftReference.class, ReferenceCodec.instance);
        this.put(LinkedList.class, CollectionCodec.instance);
    }
    
    public void addFilter(final Class<?> clazz, final SerializeFilter filter) {
        final ObjectSerializer serializer = this.getObjectWriter(clazz);
        if (serializer instanceof SerializeFilterable) {
            final SerializeFilterable filterable = (SerializeFilterable)serializer;
            if (this != SerializeConfig.globalInstance && filterable == MapSerializer.instance) {
                final MapSerializer newMapSer = new MapSerializer();
                this.put(clazz, newMapSer);
                newMapSer.addFilter(filter);
                return;
            }
            filterable.addFilter(filter);
        }
    }
    
    public void config(final Class<?> clazz, final SerializerFeature feature, final boolean value) {
        ObjectSerializer serializer = this.getObjectWriter(clazz, false);
        if (serializer == null) {
            final SerializeBeanInfo beanInfo = TypeUtils.buildBeanInfo(clazz, null, this.propertyNamingStrategy);
            if (value) {
                final SerializeBeanInfo serializeBeanInfo = beanInfo;
                serializeBeanInfo.features |= feature.mask;
            }
            else {
                final SerializeBeanInfo serializeBeanInfo2 = beanInfo;
                serializeBeanInfo2.features &= ~feature.mask;
            }
            serializer = this.createJavaBeanSerializer(beanInfo);
            this.put(clazz, serializer);
            return;
        }
        if (serializer instanceof JavaBeanSerializer) {
            final JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer)serializer;
            final SerializeBeanInfo beanInfo2 = javaBeanSerializer.beanInfo;
            final int originalFeaturs = beanInfo2.features;
            if (value) {
                final SerializeBeanInfo serializeBeanInfo3 = beanInfo2;
                serializeBeanInfo3.features |= feature.mask;
            }
            else {
                final SerializeBeanInfo serializeBeanInfo4 = beanInfo2;
                serializeBeanInfo4.features &= ~feature.mask;
            }
            if (originalFeaturs == beanInfo2.features) {
                return;
            }
            final Class<?> serializerClass = serializer.getClass();
            if (serializerClass != JavaBeanSerializer.class) {
                final ObjectSerializer newSerializer = this.createJavaBeanSerializer(beanInfo2);
                this.put(clazz, newSerializer);
            }
        }
    }
    
    public ObjectSerializer getObjectWriter(final Class<?> clazz) {
        return this.getObjectWriter(clazz, true);
    }
    
    public ObjectSerializer getObjectWriter(final Class<?> clazz, final boolean create) {
        ObjectSerializer writer = this.get(clazz);
        if (writer == null) {
            try {
                final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                for (final Object o : ServiceLoader.load(AutowiredObjectSerializer.class, classLoader)) {
                    if (!(o instanceof AutowiredObjectSerializer)) {
                        continue;
                    }
                    final AutowiredObjectSerializer autowired = (AutowiredObjectSerializer)o;
                    for (final Type forType : autowired.getAutowiredFor()) {
                        this.put(forType, autowired);
                    }
                }
            }
            catch (ClassCastException ex) {}
            writer = this.get(clazz);
        }
        if (writer == null) {
            final ClassLoader classLoader = JSON.class.getClassLoader();
            if (classLoader != Thread.currentThread().getContextClassLoader()) {
                try {
                    for (final Object o : ServiceLoader.load(AutowiredObjectSerializer.class, classLoader)) {
                        if (!(o instanceof AutowiredObjectSerializer)) {
                            continue;
                        }
                        final AutowiredObjectSerializer autowired = (AutowiredObjectSerializer)o;
                        for (final Type forType : autowired.getAutowiredFor()) {
                            this.put(forType, autowired);
                        }
                    }
                }
                catch (ClassCastException ex2) {}
                writer = this.get(clazz);
            }
        }
        for (final Module module : this.modules) {
            writer = module.createSerializer(this, clazz);
            if (writer != null) {
                this.put(clazz, writer);
                return writer;
            }
        }
        if (writer == null) {
            final String className = clazz.getName();
            if (Map.class.isAssignableFrom(clazz)) {
                this.put(clazz, writer = MapSerializer.instance);
            }
            else if (List.class.isAssignableFrom(clazz)) {
                this.put(clazz, writer = ListSerializer.instance);
            }
            else if (Collection.class.isAssignableFrom(clazz)) {
                this.put(clazz, writer = CollectionCodec.instance);
            }
            else if (Date.class.isAssignableFrom(clazz)) {
                this.put(clazz, writer = DateCodec.instance);
            }
            else if (JSONAware.class.isAssignableFrom(clazz)) {
                this.put(clazz, writer = JSONAwareSerializer.instance);
            }
            else if (JSONSerializable.class.isAssignableFrom(clazz)) {
                this.put(clazz, writer = JSONSerializableSerializer.instance);
            }
            else if (JSONStreamAware.class.isAssignableFrom(clazz)) {
                this.put(clazz, writer = MiscCodec.instance);
            }
            else if (clazz.isEnum()) {
                final JSONType jsonType = TypeUtils.getAnnotation(clazz, JSONType.class);
                if (jsonType != null && jsonType.serializeEnumAsJavaBean()) {
                    this.put(clazz, writer = this.createJavaBeanSerializer(clazz));
                }
                else {
                    this.put(clazz, writer = EnumSerializer.instance);
                }
            }
            else {
                final Class<?> superClass;
                if ((superClass = clazz.getSuperclass()) != null && superClass.isEnum()) {
                    final JSONType jsonType = TypeUtils.getAnnotation(superClass, JSONType.class);
                    if (jsonType != null && jsonType.serializeEnumAsJavaBean()) {
                        this.put(clazz, writer = this.createJavaBeanSerializer(clazz));
                    }
                    else {
                        this.put(clazz, writer = EnumSerializer.instance);
                    }
                }
                else if (clazz.isArray()) {
                    final Class<?> componentType = clazz.getComponentType();
                    final ObjectSerializer compObjectSerializer = this.getObjectWriter(componentType);
                    this.put(clazz, writer = new ArraySerializer(componentType, compObjectSerializer));
                }
                else if (Throwable.class.isAssignableFrom(clazz)) {
                    final SerializeBeanInfo buildBeanInfo;
                    final SerializeBeanInfo beanInfo = buildBeanInfo = TypeUtils.buildBeanInfo(clazz, null, this.propertyNamingStrategy);
                    buildBeanInfo.features |= SerializerFeature.WriteClassName.mask;
                    this.put(clazz, writer = new JavaBeanSerializer(beanInfo));
                }
                else if (TimeZone.class.isAssignableFrom(clazz) || Map.Entry.class.isAssignableFrom(clazz)) {
                    this.put(clazz, writer = MiscCodec.instance);
                }
                else if (Appendable.class.isAssignableFrom(clazz)) {
                    this.put(clazz, writer = AppendableSerializer.instance);
                }
                else if (Charset.class.isAssignableFrom(clazz)) {
                    this.put(clazz, writer = ToStringSerializer.instance);
                }
                else if (Enumeration.class.isAssignableFrom(clazz)) {
                    this.put(clazz, writer = EnumerationSerializer.instance);
                }
                else if (Calendar.class.isAssignableFrom(clazz) || XMLGregorianCalendar.class.isAssignableFrom(clazz)) {
                    this.put(clazz, writer = CalendarCodec.instance);
                }
                else if (TypeUtils.isClob(clazz)) {
                    this.put(clazz, writer = ClobSeriliazer.instance);
                }
                else if (TypeUtils.isPath(clazz)) {
                    this.put(clazz, writer = ToStringSerializer.instance);
                }
                else if (Iterator.class.isAssignableFrom(clazz)) {
                    this.put(clazz, writer = MiscCodec.instance);
                }
                else if (Node.class.isAssignableFrom(clazz)) {
                    this.put(clazz, writer = MiscCodec.instance);
                }
                else {
                    if (className.startsWith("java.awt.") && AwtCodec.support(clazz) && !SerializeConfig.awtError) {
                        try {
                            final String[] array;
                            final String[] names = array = new String[] { "java.awt.Color", "java.awt.Font", "java.awt.Point", "java.awt.Rectangle" };
                            for (final String name : array) {
                                if (name.equals(className)) {
                                    this.put(Class.forName(name), writer = AwtCodec.instance);
                                    return writer;
                                }
                            }
                        }
                        catch (Throwable e) {
                            SerializeConfig.awtError = true;
                        }
                    }
                    Label_1416: {
                        if (!SerializeConfig.jdk8Error) {
                            if (!className.startsWith("java.time.") && !className.startsWith("java.util.Optional") && !className.equals("java.util.concurrent.atomic.LongAdder")) {
                                if (!className.equals("java.util.concurrent.atomic.DoubleAdder")) {
                                    break Label_1416;
                                }
                            }
                            try {
                                final String[] array2;
                                String[] names = array2 = new String[] { "java.time.LocalDateTime", "java.time.LocalDate", "java.time.LocalTime", "java.time.ZonedDateTime", "java.time.OffsetDateTime", "java.time.OffsetTime", "java.time.ZoneOffset", "java.time.ZoneRegion", "java.time.Period", "java.time.Duration", "java.time.Instant" };
                                for (final String name : array2) {
                                    if (name.equals(className)) {
                                        this.put(Class.forName(name), writer = Jdk8DateCodec.instance);
                                        return writer;
                                    }
                                }
                                final String[] array3;
                                names = (array3 = new String[] { "java.util.Optional", "java.util.OptionalDouble", "java.util.OptionalInt", "java.util.OptionalLong" });
                                for (final String name : array3) {
                                    if (name.equals(className)) {
                                        this.put(Class.forName(name), writer = OptionalCodec.instance);
                                        return writer;
                                    }
                                }
                                final String[] array4;
                                names = (array4 = new String[] { "java.util.concurrent.atomic.LongAdder", "java.util.concurrent.atomic.DoubleAdder" });
                                for (final String name : array4) {
                                    if (name.equals(className)) {
                                        this.put(Class.forName(name), writer = AdderSerializer.instance);
                                        return writer;
                                    }
                                }
                            }
                            catch (Throwable e) {
                                SerializeConfig.jdk8Error = true;
                            }
                        }
                    }
                    if (!SerializeConfig.oracleJdbcError && className.startsWith("oracle.sql.")) {
                        try {
                            final String[] array5;
                            final String[] names = array5 = new String[] { "oracle.sql.DATE", "oracle.sql.TIMESTAMP" };
                            for (final String name : array5) {
                                if (name.equals(className)) {
                                    this.put(Class.forName(name), writer = DateCodec.instance);
                                    return writer;
                                }
                            }
                        }
                        catch (Throwable e) {
                            SerializeConfig.oracleJdbcError = true;
                        }
                    }
                    if (!SerializeConfig.springfoxError && className.equals("springfox.documentation.spring.web.json.Json")) {
                        try {
                            this.put(Class.forName("springfox.documentation.spring.web.json.Json"), writer = SwaggerJsonSerializer.instance);
                            return writer;
                        }
                        catch (ClassNotFoundException e2) {
                            SerializeConfig.springfoxError = true;
                        }
                    }
                    if (!SerializeConfig.guavaError && className.startsWith("com.google.common.collect.")) {
                        try {
                            final String[] array6;
                            final String[] names = array6 = new String[] { "com.google.common.collect.HashMultimap", "com.google.common.collect.LinkedListMultimap", "com.google.common.collect.LinkedHashMultimap", "com.google.common.collect.ArrayListMultimap", "com.google.common.collect.TreeMultimap" };
                            for (final String name : array6) {
                                if (name.equals(className)) {
                                    this.put(Class.forName(name), writer = GuavaCodec.instance);
                                    return writer;
                                }
                            }
                        }
                        catch (ClassNotFoundException e2) {
                            SerializeConfig.guavaError = true;
                        }
                    }
                    if (!SerializeConfig.jsonnullError && className.equals("net.sf.json.JSONNull")) {
                        try {
                            this.put(Class.forName("net.sf.json.JSONNull"), writer = MiscCodec.instance);
                            return writer;
                        }
                        catch (ClassNotFoundException e2) {
                            SerializeConfig.jsonnullError = true;
                        }
                    }
                    if (!SerializeConfig.jsonobjectError && className.equals("org.json.JSONObject")) {
                        try {
                            this.put(Class.forName("org.json.JSONObject"), writer = JSONObjectCodec.instance);
                            return writer;
                        }
                        catch (ClassNotFoundException e2) {
                            SerializeConfig.jsonobjectError = true;
                        }
                    }
                    if (!SerializeConfig.jodaError && className.startsWith("org.joda.")) {
                        try {
                            final String[] array7;
                            final String[] names = array7 = new String[] { "org.joda.time.LocalDate", "org.joda.time.LocalDateTime", "org.joda.time.LocalTime", "org.joda.time.Instant", "org.joda.time.DateTime", "org.joda.time.Period", "org.joda.time.Duration", "org.joda.time.DateTimeZone", "org.joda.time.UTCDateTimeZone", "org.joda.time.tz.CachedDateTimeZone", "org.joda.time.tz.FixedDateTimeZone" };
                            for (final String name : array7) {
                                if (name.equals(className)) {
                                    this.put(Class.forName(name), writer = JodaCodec.instance);
                                    return writer;
                                }
                            }
                        }
                        catch (ClassNotFoundException e2) {
                            SerializeConfig.jodaError = true;
                        }
                    }
                    if ("java.nio.HeapByteBuffer".equals(className)) {
                        this.put(clazz, writer = ByteBufferCodec.instance);
                        return writer;
                    }
                    if ("org.javamoney.moneta.Money".equals(className)) {
                        this.put(clazz, writer = MonetaCodec.instance);
                        return writer;
                    }
                    final Class[] interfaces = clazz.getInterfaces();
                    if (interfaces.length == 1 && interfaces[0].isAnnotation()) {
                        this.put(clazz, AnnotationSerializer.instance);
                        return AnnotationSerializer.instance;
                    }
                    if (TypeUtils.isProxy(clazz)) {
                        final Class<?> superClazz = clazz.getSuperclass();
                        final ObjectSerializer superWriter = this.getObjectWriter(superClazz);
                        this.put(clazz, superWriter);
                        return superWriter;
                    }
                    if (Proxy.isProxyClass(clazz)) {
                        Class handlerClass = null;
                        if (interfaces.length == 2) {
                            handlerClass = interfaces[1];
                        }
                        else {
                            for (final Class proxiedInterface : interfaces) {
                                if (!proxiedInterface.getName().startsWith("org.springframework.aop.")) {
                                    if (handlerClass != null) {
                                        handlerClass = null;
                                        break;
                                    }
                                    handlerClass = proxiedInterface;
                                }
                            }
                        }
                        if (handlerClass != null) {
                            final ObjectSerializer superWriter = this.getObjectWriter(handlerClass);
                            this.put(clazz, superWriter);
                            return superWriter;
                        }
                    }
                    if (create) {
                        writer = this.createJavaBeanSerializer(clazz);
                        this.put(clazz, writer);
                    }
                }
            }
            if (writer == null) {
                writer = this.get(clazz);
            }
        }
        return writer;
    }
    
    public final ObjectSerializer get(final Type type) {
        final Type mixin = JSON.getMixInAnnotations(type);
        if (null == mixin) {
            return this.serializers.get(type);
        }
        final IdentityHashMap<Type, ObjectSerializer> mixInClasses = this.mixInSerializers.get(type);
        if (mixInClasses == null) {
            return null;
        }
        return mixInClasses.get(mixin);
    }
    
    public boolean put(final Object type, final Object value) {
        return this.put((Type)type, (ObjectSerializer)value);
    }
    
    public boolean put(final Type type, final ObjectSerializer value) {
        final Type mixin = JSON.getMixInAnnotations(type);
        if (mixin != null) {
            IdentityHashMap<Type, ObjectSerializer> mixInClasses = this.mixInSerializers.get(type);
            if (mixInClasses == null) {
                mixInClasses = new IdentityHashMap<Type, ObjectSerializer>(4);
                this.mixInSerializers.put(type, mixInClasses);
            }
            return mixInClasses.put(mixin, value);
        }
        return this.serializers.put(type, value);
    }
    
    public void configEnumAsJavaBean(final Class<? extends Enum>... enumClasses) {
        for (final Class<? extends Enum> enumClass : enumClasses) {
            this.put(enumClass, this.createJavaBeanSerializer(enumClass));
        }
    }
    
    public void setPropertyNamingStrategy(final PropertyNamingStrategy propertyNamingStrategy) {
        this.propertyNamingStrategy = propertyNamingStrategy;
    }
    
    public void clearSerializers() {
        this.serializers.clear();
        this.initSerializers();
    }
    
    public void register(final Module module) {
        this.modules.add(module);
    }
    
    static {
        globalInstance = new SerializeConfig();
        SerializeConfig.awtError = false;
        SerializeConfig.jdk8Error = false;
        SerializeConfig.oracleJdbcError = false;
        SerializeConfig.springfoxError = false;
        SerializeConfig.guavaError = false;
        SerializeConfig.jsonnullError = false;
        SerializeConfig.jsonobjectError = false;
        SerializeConfig.jodaError = false;
    }
}
