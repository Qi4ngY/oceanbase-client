package com.alibaba.fastjson.parser;

import java.util.concurrent.CopyOnWriteArrayList;
import java.io.InputStream;
import javax.sql.RowSet;
import javax.sql.DataSource;
import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.asm.TypeCollector;
import com.alibaba.fastjson.asm.ClassReader;
import java.util.EventListener;
import java.lang.reflect.Field;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.deserializer.DefaultFieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.ArrayListTypeFieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.FieldDeserializer;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.util.FieldInfo;
import java.lang.reflect.Constructor;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import java.lang.reflect.Modifier;
import com.alibaba.fastjson.util.JavaBeanInfo;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.net.InetAddress;
import com.alibaba.fastjson.parser.deserializer.PropertyProcessableDeserializer;
import com.alibaba.fastjson.parser.deserializer.PropertyProcessable;
import com.alibaba.fastjson.parser.deserializer.ThrowableDeserializer;
import java.util.HashSet;
import java.util.Set;
import com.alibaba.fastjson.serializer.ObjectArrayCodec;
import com.alibaba.fastjson.parser.deserializer.EnumDeserializer;
import com.alibaba.fastjson.util.ServiceLoader;
import com.alibaba.fastjson.parser.deserializer.AutowiredObjectDeserializer;
import com.alibaba.fastjson.support.moneta.MonetaCodec;
import com.alibaba.fastjson.serializer.ByteBufferCodec;
import com.alibaba.fastjson.serializer.GuavaCodec;
import com.alibaba.fastjson.serializer.JodaCodec;
import com.alibaba.fastjson.parser.deserializer.OptionalCodec;
import com.alibaba.fastjson.parser.deserializer.Jdk8DateCodec;
import com.alibaba.fastjson.serializer.AwtCodec;
import java.lang.reflect.TypeVariable;
import com.alibaba.fastjson.annotation.JSONType;
import java.lang.reflect.WildcardType;
import java.lang.reflect.ParameterizedType;
import java.util.Properties;
import com.alibaba.fastjson.parser.deserializer.JSONPDeserializer;
import com.alibaba.fastjson.JSONPObject;
import java.io.Closeable;
import java.io.Serializable;
import com.alibaba.fastjson.parser.deserializer.StackTraceElementDeserializer;
import java.util.concurrent.atomic.AtomicLongArray;
import com.alibaba.fastjson.serializer.AtomicCodec;
import java.util.concurrent.atomic.AtomicIntegerArray;
import com.alibaba.fastjson.JSONPath;
import java.nio.charset.Charset;
import java.util.regex.Pattern;
import java.net.URL;
import java.net.URI;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Inet6Address;
import java.net.Inet4Address;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import com.alibaba.fastjson.serializer.ReferenceCodec;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import com.alibaba.fastjson.serializer.CharArrayCodec;
import com.alibaba.fastjson.serializer.BooleanCodec;
import com.alibaba.fastjson.serializer.FloatCodec;
import com.alibaba.fastjson.serializer.BigDecimalCodec;
import java.math.BigDecimal;
import com.alibaba.fastjson.serializer.BigIntegerCodec;
import java.math.BigInteger;
import com.alibaba.fastjson.serializer.LongCodec;
import com.alibaba.fastjson.serializer.IntegerCodec;
import com.alibaba.fastjson.parser.deserializer.NumberDeserializer;
import com.alibaba.fastjson.serializer.CharacterCodec;
import com.alibaba.fastjson.serializer.StringCodec;
import com.alibaba.fastjson.parser.deserializer.JavaObjectDeserializer;
import java.util.Collection;
import java.util.TreeMap;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import com.alibaba.fastjson.serializer.CollectionCodec;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.parser.deserializer.MapDeserializer;
import com.alibaba.fastjson.JSONObject;
import javax.xml.datatype.XMLGregorianCalendar;
import com.alibaba.fastjson.serializer.CalendarCodec;
import java.util.Calendar;
import com.alibaba.fastjson.serializer.DateCodec;
import com.alibaba.fastjson.parser.deserializer.TimeDeserializer;
import java.sql.Time;
import java.sql.Date;
import com.alibaba.fastjson.parser.deserializer.SqlDateDeserializer;
import java.sql.Timestamp;
import com.alibaba.fastjson.serializer.MiscCodec;
import java.text.SimpleDateFormat;
import java.security.AccessControlException;
import com.alibaba.fastjson.util.ASMClassLoader;
import java.util.Arrays;
import java.util.ArrayList;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.util.ASMUtils;
import java.util.concurrent.ConcurrentHashMap;
import com.alibaba.fastjson.spi.Module;
import java.util.List;
import com.alibaba.fastjson.parser.deserializer.ASMDeserializerFactory;
import com.alibaba.fastjson.PropertyNamingStrategy;
import java.util.concurrent.ConcurrentMap;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import java.lang.reflect.Type;
import com.alibaba.fastjson.util.IdentityHashMap;

public class ParserConfig
{
    public static final String DENY_PROPERTY_INTERNAL = "fastjson.parser.deny.internal";
    public static final String DENY_PROPERTY = "fastjson.parser.deny";
    public static final String AUTOTYPE_ACCEPT = "fastjson.parser.autoTypeAccept";
    public static final String AUTOTYPE_SUPPORT_PROPERTY = "fastjson.parser.autoTypeSupport";
    public static final String SAFE_MODE_PROPERTY = "fastjson.parser.safeMode";
    public static final String[] DENYS_INTERNAL;
    public static final String[] DENYS;
    private static final String[] AUTO_TYPE_ACCEPT_LIST;
    public static final boolean AUTO_SUPPORT;
    public static final boolean SAFE_MODE;
    private static final long[] INTERNAL_WHITELIST_HASHCODES;
    public static ParserConfig global;
    private final IdentityHashMap<Type, ObjectDeserializer> deserializers;
    private final IdentityHashMap<Type, IdentityHashMap<Type, ObjectDeserializer>> mixInDeserializers;
    private final ConcurrentMap<String, Class<?>> typeMapping;
    private boolean asmEnable;
    public final SymbolTable symbolTable;
    public PropertyNamingStrategy propertyNamingStrategy;
    protected ClassLoader defaultClassLoader;
    protected ASMDeserializerFactory asmFactory;
    private static boolean awtError;
    private static boolean jdk8Error;
    private static boolean jodaError;
    private static boolean guavaError;
    private boolean autoTypeSupport;
    private long[] internalDenyHashCodes;
    private long[] denyHashCodes;
    private long[] acceptHashCodes;
    public final boolean fieldBased;
    private boolean jacksonCompatible;
    public boolean compatibleWithJavaBean;
    private List<Module> modules;
    private volatile List<AutoTypeCheckHandler> autoTypeCheckHandlers;
    private boolean safeMode;
    
    public static ParserConfig getGlobalInstance() {
        return ParserConfig.global;
    }
    
    public ParserConfig() {
        this(false);
    }
    
    public ParserConfig(final boolean fieldBase) {
        this(null, null, fieldBase);
    }
    
    public ParserConfig(final ClassLoader parentClassLoader) {
        this(null, parentClassLoader, false);
    }
    
    public ParserConfig(final ASMDeserializerFactory asmFactory) {
        this(asmFactory, null, false);
    }
    
    private ParserConfig(ASMDeserializerFactory asmFactory, final ClassLoader parentClassLoader, final boolean fieldBased) {
        this.deserializers = new IdentityHashMap<Type, ObjectDeserializer>();
        this.mixInDeserializers = new IdentityHashMap<Type, IdentityHashMap<Type, ObjectDeserializer>>(16);
        this.typeMapping = new ConcurrentHashMap<String, Class<?>>(16, 0.75f, 1);
        this.asmEnable = !ASMUtils.IS_ANDROID;
        this.symbolTable = new SymbolTable(4096);
        this.autoTypeSupport = ParserConfig.AUTO_SUPPORT;
        this.jacksonCompatible = false;
        this.compatibleWithJavaBean = TypeUtils.compatibleWithJavaBean;
        this.modules = new ArrayList<Module>();
        this.safeMode = ParserConfig.SAFE_MODE;
        this.denyHashCodes = new long[] { -9164606388214699518L, -8720046426850100497L, -8649961213709896794L, -8165637398350707645L, -8109300701639721088L, -7966123100503199569L, -7921218830998286408L, -7775351613326101303L, -7768608037458185275L, -7766605818834748097L, -6835437086156813536L, -6316154655839304624L, -6179589609550493385L, -6025144546313590215L, -5939269048541779808L, -5885964883385605994L, -5764804792063216819L, -5472097725414717105L, -5194641081268104286L, -4837536971810737970L, -4608341446948126581L, -4438775680185074100L, -4082057040235125754L, -3975378478825053783L, -3935185854875733362L, -3319207949486691020L, -3077205613010077203L, -2825378362173150292L, -2439930098895578154L, -2378990704010641148L, -2364987994247679115L, -2262244760619952081L, -2192804397019347313L, -2095516571388852610L, -1872417015366588117L, -1650485814983027158L, -1589194880214235129L, -905177026366752536L, -831789045734283466L, -582813228520337988L, -254670111376247151L, -190281065685395680L, -26639035867733124L, -9822483067882491L, 4750336058574309L, 33238344207745342L, 218512992947536312L, 313864100207897507L, 386461436234701831L, 823641066473609950L, 1073634739308289776L, 1153291637701043748L, 1203232727967308606L, 1459860845934817624L, 1502845958873959152L, 1534439610567445754L, 1698504441317515818L, 1818089308493370394L, 2078113382421334967L, 2164696723069287854L, 2653453629929770569L, 2660670623866180977L, 2731823439467737506L, 2836431254737891113L, 3089451460101527857L, 3114862868117605599L, 3256258368248066264L, 3547627781654598988L, 3637939656440441093L, 3688179072722109200L, 3718352661124136681L, 3730752432285826863L, 3794316665763266033L, 4046190361520671643L, 4147696707147271408L, 4254584350247334433L, 4814658433570175913L, 4841947709850912914L, 4904007817188630457L, 5100336081510080343L, 5274044858141538265L, 5347909877633654828L, 5450448828334921485L, 5474268165959054640L, 5596129856135573697L, 5688200883751798389L, 5751393439502795295L, 5944107969236155580L, 6007332606592876737L, 6280357960959217660L, 6456855723474196908L, 6511035576063254270L, 6534946468240507089L, 6734240326434096246L, 6742705432718011780L, 6854854816081053523L, 7123326897294507060L, 7179336928365889465L, 7375862386996623731L, 7442624256860549330L, 7658177784286215602L, 8055461369741094911L, 8389032537095247355L, 8409640769019589119L, 8488266005336625107L, 8537233257283452655L, 8838294710098435315L, 9140390920032557669L, 9140416208800006522L };
        final long[] hashCodes = new long[ParserConfig.AUTO_TYPE_ACCEPT_LIST.length];
        for (int i = 0; i < ParserConfig.AUTO_TYPE_ACCEPT_LIST.length; ++i) {
            hashCodes[i] = TypeUtils.fnv1a_64(ParserConfig.AUTO_TYPE_ACCEPT_LIST[i]);
        }
        Arrays.sort(hashCodes);
        this.acceptHashCodes = hashCodes;
        this.fieldBased = fieldBased;
        if (asmFactory == null && !ASMUtils.IS_ANDROID) {
            try {
                if (parentClassLoader == null) {
                    asmFactory = new ASMDeserializerFactory(new ASMClassLoader());
                }
                else {
                    asmFactory = new ASMDeserializerFactory(parentClassLoader);
                }
            }
            catch (ExceptionInInitializerError exceptionInInitializerError) {}
            catch (AccessControlException ex) {}
            catch (NoClassDefFoundError noClassDefFoundError) {}
        }
        if ((this.asmFactory = asmFactory) == null) {
            this.asmEnable = false;
        }
        this.initDeserializers();
        this.addItemsToDeny(ParserConfig.DENYS);
        this.addItemsToDeny0(ParserConfig.DENYS_INTERNAL);
        this.addItemsToAccept(ParserConfig.AUTO_TYPE_ACCEPT_LIST);
    }
    
    private void initDeserializers() {
        this.deserializers.put(SimpleDateFormat.class, MiscCodec.instance);
        this.deserializers.put(Timestamp.class, SqlDateDeserializer.instance_timestamp);
        this.deserializers.put(Date.class, SqlDateDeserializer.instance);
        this.deserializers.put(Time.class, TimeDeserializer.instance);
        this.deserializers.put(java.util.Date.class, DateCodec.instance);
        this.deserializers.put(Calendar.class, CalendarCodec.instance);
        this.deserializers.put(XMLGregorianCalendar.class, CalendarCodec.instance);
        this.deserializers.put(JSONObject.class, MapDeserializer.instance);
        this.deserializers.put(JSONArray.class, CollectionCodec.instance);
        this.deserializers.put(Map.class, MapDeserializer.instance);
        this.deserializers.put(HashMap.class, MapDeserializer.instance);
        this.deserializers.put(LinkedHashMap.class, MapDeserializer.instance);
        this.deserializers.put(TreeMap.class, MapDeserializer.instance);
        this.deserializers.put(ConcurrentMap.class, MapDeserializer.instance);
        this.deserializers.put(ConcurrentHashMap.class, MapDeserializer.instance);
        this.deserializers.put(Collection.class, CollectionCodec.instance);
        this.deserializers.put(List.class, CollectionCodec.instance);
        this.deserializers.put(ArrayList.class, CollectionCodec.instance);
        this.deserializers.put(Object.class, JavaObjectDeserializer.instance);
        this.deserializers.put(String.class, StringCodec.instance);
        this.deserializers.put(StringBuffer.class, StringCodec.instance);
        this.deserializers.put(StringBuilder.class, StringCodec.instance);
        this.deserializers.put(Character.TYPE, CharacterCodec.instance);
        this.deserializers.put(Character.class, CharacterCodec.instance);
        this.deserializers.put(Byte.TYPE, NumberDeserializer.instance);
        this.deserializers.put(Byte.class, NumberDeserializer.instance);
        this.deserializers.put(Short.TYPE, NumberDeserializer.instance);
        this.deserializers.put(Short.class, NumberDeserializer.instance);
        this.deserializers.put(Integer.TYPE, IntegerCodec.instance);
        this.deserializers.put(Integer.class, IntegerCodec.instance);
        this.deserializers.put(Long.TYPE, LongCodec.instance);
        this.deserializers.put(Long.class, LongCodec.instance);
        this.deserializers.put(BigInteger.class, BigIntegerCodec.instance);
        this.deserializers.put(BigDecimal.class, BigDecimalCodec.instance);
        this.deserializers.put(Float.TYPE, FloatCodec.instance);
        this.deserializers.put(Float.class, FloatCodec.instance);
        this.deserializers.put(Double.TYPE, NumberDeserializer.instance);
        this.deserializers.put(Double.class, NumberDeserializer.instance);
        this.deserializers.put(Boolean.TYPE, BooleanCodec.instance);
        this.deserializers.put(Boolean.class, BooleanCodec.instance);
        this.deserializers.put(Class.class, MiscCodec.instance);
        this.deserializers.put(char[].class, new CharArrayCodec());
        this.deserializers.put(AtomicBoolean.class, BooleanCodec.instance);
        this.deserializers.put(AtomicInteger.class, IntegerCodec.instance);
        this.deserializers.put(AtomicLong.class, LongCodec.instance);
        this.deserializers.put(AtomicReference.class, ReferenceCodec.instance);
        this.deserializers.put(WeakReference.class, ReferenceCodec.instance);
        this.deserializers.put(SoftReference.class, ReferenceCodec.instance);
        this.deserializers.put(UUID.class, MiscCodec.instance);
        this.deserializers.put(TimeZone.class, MiscCodec.instance);
        this.deserializers.put(Locale.class, MiscCodec.instance);
        this.deserializers.put(Currency.class, MiscCodec.instance);
        this.deserializers.put(Inet4Address.class, MiscCodec.instance);
        this.deserializers.put(Inet6Address.class, MiscCodec.instance);
        this.deserializers.put(InetSocketAddress.class, MiscCodec.instance);
        this.deserializers.put(File.class, MiscCodec.instance);
        this.deserializers.put(URI.class, MiscCodec.instance);
        this.deserializers.put(URL.class, MiscCodec.instance);
        this.deserializers.put(Pattern.class, MiscCodec.instance);
        this.deserializers.put(Charset.class, MiscCodec.instance);
        this.deserializers.put(JSONPath.class, MiscCodec.instance);
        this.deserializers.put(Number.class, NumberDeserializer.instance);
        this.deserializers.put(AtomicIntegerArray.class, AtomicCodec.instance);
        this.deserializers.put(AtomicLongArray.class, AtomicCodec.instance);
        this.deserializers.put(StackTraceElement.class, StackTraceElementDeserializer.instance);
        this.deserializers.put(Serializable.class, JavaObjectDeserializer.instance);
        this.deserializers.put(Cloneable.class, JavaObjectDeserializer.instance);
        this.deserializers.put(Comparable.class, JavaObjectDeserializer.instance);
        this.deserializers.put(Closeable.class, JavaObjectDeserializer.instance);
        this.deserializers.put(JSONPObject.class, new JSONPDeserializer());
    }
    
    private static String[] splitItemsFormProperty(final String property) {
        if (property != null && property.length() > 0) {
            return property.split(",");
        }
        return null;
    }
    
    public void configFromPropety(final Properties properties) {
        String property = properties.getProperty("fastjson.parser.deny");
        String[] items = splitItemsFormProperty(property);
        this.addItemsToDeny(items);
        property = properties.getProperty("fastjson.parser.autoTypeAccept");
        items = splitItemsFormProperty(property);
        this.addItemsToAccept(items);
        property = properties.getProperty("fastjson.parser.autoTypeSupport");
        if ("true".equals(property)) {
            this.autoTypeSupport = true;
        }
        else if ("false".equals(property)) {
            this.autoTypeSupport = false;
        }
    }
    
    private void addItemsToDeny0(final String[] items) {
        if (items == null) {
            return;
        }
        for (int i = 0; i < items.length; ++i) {
            final String item = items[i];
            this.addDenyInternal(item);
        }
    }
    
    private void addItemsToDeny(final String[] items) {
        if (items == null) {
            return;
        }
        for (int i = 0; i < items.length; ++i) {
            final String item = items[i];
            this.addDeny(item);
        }
    }
    
    private void addItemsToAccept(final String[] items) {
        if (items == null) {
            return;
        }
        for (int i = 0; i < items.length; ++i) {
            final String item = items[i];
            this.addAccept(item);
        }
    }
    
    public boolean isSafeMode() {
        return this.safeMode;
    }
    
    public void setSafeMode(final boolean safeMode) {
        this.safeMode = safeMode;
    }
    
    public boolean isAutoTypeSupport() {
        return this.autoTypeSupport;
    }
    
    public void setAutoTypeSupport(final boolean autoTypeSupport) {
        this.autoTypeSupport = autoTypeSupport;
    }
    
    public boolean isAsmEnable() {
        return this.asmEnable;
    }
    
    public void setAsmEnable(final boolean asmEnable) {
        this.asmEnable = asmEnable;
    }
    
    @Deprecated
    public IdentityHashMap<Type, ObjectDeserializer> getDerializers() {
        return this.deserializers;
    }
    
    public IdentityHashMap<Type, ObjectDeserializer> getDeserializers() {
        return this.deserializers;
    }
    
    public ObjectDeserializer getDeserializer(final Type type) {
        final ObjectDeserializer deserializer = this.get(type);
        if (deserializer != null) {
            return deserializer;
        }
        if (type instanceof Class) {
            return this.getDeserializer((Class<?>)type, type);
        }
        if (!(type instanceof ParameterizedType)) {
            if (type instanceof WildcardType) {
                final WildcardType wildcardType = (WildcardType)type;
                final Type[] upperBounds = wildcardType.getUpperBounds();
                if (upperBounds.length == 1) {
                    final Type upperBoundType = upperBounds[0];
                    return this.getDeserializer(upperBoundType);
                }
            }
            return JavaObjectDeserializer.instance;
        }
        final Type rawType = ((ParameterizedType)type).getRawType();
        if (rawType instanceof Class) {
            return this.getDeserializer((Class<?>)rawType, type);
        }
        return this.getDeserializer(rawType);
    }
    
    public ObjectDeserializer getDeserializer(final Class<?> clazz, Type type) {
        ObjectDeserializer deserializer = this.get(type);
        if (deserializer != null) {
            return deserializer;
        }
        if (type == null) {
            type = clazz;
        }
        deserializer = this.get(type);
        if (deserializer != null) {
            return deserializer;
        }
        final JSONType annotation = TypeUtils.getAnnotation(clazz, JSONType.class);
        if (annotation != null) {
            final Class<?> mappingTo = annotation.mappingTo();
            if (mappingTo != Void.class) {
                return this.getDeserializer(mappingTo, mappingTo);
            }
        }
        if (type instanceof WildcardType || type instanceof TypeVariable || type instanceof ParameterizedType) {
            deserializer = this.get(clazz);
        }
        if (deserializer != null) {
            return deserializer;
        }
        for (final Module module : this.modules) {
            deserializer = module.createDeserializer(this, clazz);
            if (deserializer != null) {
                this.putDeserializer(type, deserializer);
                return deserializer;
            }
        }
        String className = clazz.getName();
        className = className.replace('$', '.');
        if (className.startsWith("java.awt.") && AwtCodec.support(clazz) && !ParserConfig.awtError) {
            final String[] names = { "java.awt.Point", "java.awt.Font", "java.awt.Rectangle", "java.awt.Color" };
            try {
                for (final String name : names) {
                    if (name.equals(className)) {
                        this.putDeserializer(Class.forName(name), deserializer = AwtCodec.instance);
                        return deserializer;
                    }
                }
            }
            catch (Throwable e) {
                ParserConfig.awtError = true;
            }
            deserializer = AwtCodec.instance;
        }
        if (!ParserConfig.jdk8Error) {
            try {
                if (className.startsWith("java.time.")) {
                    final String[] array2;
                    final String[] names = array2 = new String[] { "java.time.LocalDateTime", "java.time.LocalDate", "java.time.LocalTime", "java.time.ZonedDateTime", "java.time.OffsetDateTime", "java.time.OffsetTime", "java.time.ZoneOffset", "java.time.ZoneRegion", "java.time.ZoneId", "java.time.Period", "java.time.Duration", "java.time.Instant" };
                    for (final String name : array2) {
                        if (name.equals(className)) {
                            this.putDeserializer(Class.forName(name), deserializer = Jdk8DateCodec.instance);
                            return deserializer;
                        }
                    }
                }
                else if (className.startsWith("java.util.Optional")) {
                    final String[] array3;
                    final String[] names = array3 = new String[] { "java.util.Optional", "java.util.OptionalDouble", "java.util.OptionalInt", "java.util.OptionalLong" };
                    for (final String name : array3) {
                        if (name.equals(className)) {
                            this.putDeserializer(Class.forName(name), deserializer = OptionalCodec.instance);
                            return deserializer;
                        }
                    }
                }
            }
            catch (Throwable e2) {
                ParserConfig.jdk8Error = true;
            }
        }
        if (!ParserConfig.jodaError) {
            try {
                if (className.startsWith("org.joda.time.")) {
                    final String[] array4;
                    final String[] names = array4 = new String[] { "org.joda.time.DateTime", "org.joda.time.LocalDate", "org.joda.time.LocalDateTime", "org.joda.time.LocalTime", "org.joda.time.Instant", "org.joda.time.Period", "org.joda.time.Duration", "org.joda.time.DateTimeZone", "org.joda.time.format.DateTimeFormatter" };
                    for (final String name : array4) {
                        if (name.equals(className)) {
                            this.putDeserializer(Class.forName(name), deserializer = JodaCodec.instance);
                            return deserializer;
                        }
                    }
                }
            }
            catch (Throwable e2) {
                ParserConfig.jodaError = true;
            }
        }
        if (!ParserConfig.guavaError && className.startsWith("com.google.common.collect.")) {
            try {
                final String[] array5;
                final String[] names = array5 = new String[] { "com.google.common.collect.HashMultimap", "com.google.common.collect.LinkedListMultimap", "com.google.common.collect.LinkedHashMultimap", "com.google.common.collect.ArrayListMultimap", "com.google.common.collect.TreeMultimap" };
                for (final String name : array5) {
                    if (name.equals(className)) {
                        this.putDeserializer(Class.forName(name), deserializer = GuavaCodec.instance);
                        return deserializer;
                    }
                }
            }
            catch (ClassNotFoundException e3) {
                ParserConfig.guavaError = true;
            }
        }
        if (className.equals("java.nio.ByteBuffer")) {
            this.putDeserializer(clazz, deserializer = ByteBufferCodec.instance);
        }
        if (className.equals("java.nio.file.Path")) {
            this.putDeserializer(clazz, deserializer = MiscCodec.instance);
        }
        if (clazz == Map.Entry.class) {
            this.putDeserializer(clazz, deserializer = MiscCodec.instance);
        }
        if (className.equals("org.javamoney.moneta.Money")) {
            this.putDeserializer(clazz, deserializer = MonetaCodec.instance);
        }
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            for (final AutowiredObjectDeserializer autowired : ServiceLoader.load(AutowiredObjectDeserializer.class, classLoader)) {
                for (final Type forType : autowired.getAutowiredFor()) {
                    this.putDeserializer(forType, autowired);
                }
            }
        }
        catch (Exception ex) {}
        if (deserializer == null) {
            deserializer = this.get(type);
        }
        if (deserializer != null) {
            return deserializer;
        }
        if (clazz.isEnum()) {
            if (this.jacksonCompatible) {
                final Method[] methods2;
                final Method[] methods = methods2 = clazz.getMethods();
                for (final Method method : methods2) {
                    if (TypeUtils.isJacksonCreator(method)) {
                        deserializer = this.createJavaBeanDeserializer(clazz, type);
                        this.putDeserializer(type, deserializer);
                        return deserializer;
                    }
                }
            }
            Class<?> deserClass = null;
            final JSONType jsonType = TypeUtils.getAnnotation(clazz, JSONType.class);
            if (jsonType != null) {
                deserClass = jsonType.deserializer();
                try {
                    deserializer = (ObjectDeserializer)deserClass.newInstance();
                    this.putDeserializer(clazz, deserializer);
                    return deserializer;
                }
                catch (Throwable t) {}
            }
            deserializer = new EnumDeserializer(clazz);
        }
        else if (clazz.isArray()) {
            deserializer = ObjectArrayCodec.instance;
        }
        else if (clazz == Set.class || clazz == HashSet.class || clazz == Collection.class || clazz == List.class || clazz == ArrayList.class) {
            deserializer = CollectionCodec.instance;
        }
        else if (Collection.class.isAssignableFrom(clazz)) {
            deserializer = CollectionCodec.instance;
        }
        else if (Map.class.isAssignableFrom(clazz)) {
            deserializer = MapDeserializer.instance;
        }
        else if (Throwable.class.isAssignableFrom(clazz)) {
            deserializer = new ThrowableDeserializer(this, clazz);
        }
        else if (PropertyProcessable.class.isAssignableFrom(clazz)) {
            deserializer = new PropertyProcessableDeserializer((Class<PropertyProcessable>)clazz);
        }
        else if (clazz == InetAddress.class) {
            deserializer = MiscCodec.instance;
        }
        else {
            deserializer = this.createJavaBeanDeserializer(clazz, type);
        }
        this.putDeserializer(type, deserializer);
        return deserializer;
    }
    
    public void initJavaBeanDeserializers(final Class<?>... classes) {
        if (classes == null) {
            return;
        }
        for (final Class<?> type : classes) {
            if (type != null) {
                final ObjectDeserializer deserializer = this.createJavaBeanDeserializer(type, type);
                this.putDeserializer(type, deserializer);
            }
        }
    }
    
    public ObjectDeserializer createJavaBeanDeserializer(final Class<?> clazz, final Type type) {
        boolean asmEnable = this.asmEnable & !this.fieldBased;
        Label_0149: {
            if (asmEnable) {
                final JSONType jsonType = TypeUtils.getAnnotation(clazz, JSONType.class);
                if (jsonType != null) {
                    final Class<?> deserializerClass = jsonType.deserializer();
                    if (deserializerClass != Void.class) {
                        try {
                            final Object deseralizer = deserializerClass.newInstance();
                            if (deseralizer instanceof ObjectDeserializer) {
                                return (ObjectDeserializer)deseralizer;
                            }
                        }
                        catch (Throwable t) {}
                    }
                    asmEnable = jsonType.asm();
                }
                if (asmEnable) {
                    Class<?> superClass = JavaBeanInfo.getBuilderClass(clazz, jsonType);
                    if (superClass == null) {
                        superClass = clazz;
                    }
                    while (Modifier.isPublic(superClass.getModifiers())) {
                        superClass = superClass.getSuperclass();
                        if (superClass == Object.class || superClass == null) {
                            break Label_0149;
                        }
                    }
                    asmEnable = false;
                }
            }
        }
        if (clazz.getTypeParameters().length != 0) {
            asmEnable = false;
        }
        if (asmEnable && this.asmFactory != null && this.asmFactory.classLoader.isExternalClass(clazz)) {
            asmEnable = false;
        }
        if (asmEnable) {
            asmEnable = ASMUtils.checkName(clazz.getSimpleName());
        }
        if (asmEnable) {
            if (clazz.isInterface()) {
                asmEnable = false;
            }
            final JavaBeanInfo beanInfo = JavaBeanInfo.build(clazz, type, this.propertyNamingStrategy, false, TypeUtils.compatibleWithJavaBean, this.jacksonCompatible);
            if (asmEnable && beanInfo.fields.length > 200) {
                asmEnable = false;
            }
            final Constructor<?> defaultConstructor = beanInfo.defaultConstructor;
            if (asmEnable && defaultConstructor == null && !clazz.isInterface()) {
                asmEnable = false;
            }
            for (final FieldInfo fieldInfo : beanInfo.fields) {
                if (fieldInfo.getOnly) {
                    asmEnable = false;
                    break;
                }
                final Class<?> fieldClass = fieldInfo.fieldClass;
                if (!Modifier.isPublic(fieldClass.getModifiers())) {
                    asmEnable = false;
                    break;
                }
                if (fieldClass.isMemberClass() && !Modifier.isStatic(fieldClass.getModifiers())) {
                    asmEnable = false;
                    break;
                }
                if (fieldInfo.getMember() != null && !ASMUtils.checkName(fieldInfo.getMember().getName())) {
                    asmEnable = false;
                    break;
                }
                final JSONField annotation = fieldInfo.getAnnotation();
                if ((annotation != null && (!ASMUtils.checkName(annotation.name()) || annotation.format().length() != 0 || annotation.deserializeUsing() != Void.class || annotation.parseFeatures().length != 0 || annotation.unwrapped())) || (fieldInfo.method != null && fieldInfo.method.getParameterTypes().length > 1)) {
                    asmEnable = false;
                    break;
                }
                if (fieldClass.isEnum()) {
                    final ObjectDeserializer fieldDeser = this.getDeserializer(fieldClass);
                    if (!(fieldDeser instanceof EnumDeserializer)) {
                        asmEnable = false;
                        break;
                    }
                }
            }
        }
        if (asmEnable && clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
            asmEnable = false;
        }
        if (asmEnable && TypeUtils.isXmlField(clazz)) {
            asmEnable = false;
        }
        if (!asmEnable) {
            return new JavaBeanDeserializer(this, clazz, type);
        }
        final JavaBeanInfo beanInfo = JavaBeanInfo.build(clazz, type, this.propertyNamingStrategy);
        try {
            return this.asmFactory.createJavaBeanDeserializer(this, beanInfo);
        }
        catch (NoSuchMethodException ex) {
            return new JavaBeanDeserializer(this, clazz, type);
        }
        catch (JSONException asmError) {
            return new JavaBeanDeserializer(this, beanInfo);
        }
        catch (Exception e) {
            throw new JSONException("create asm deserializer error, " + clazz.getName(), e);
        }
    }
    
    public FieldDeserializer createFieldDeserializer(final ParserConfig mapping, final JavaBeanInfo beanInfo, final FieldInfo fieldInfo) {
        final Class<?> clazz = beanInfo.clazz;
        final Class<?> fieldClass = fieldInfo.fieldClass;
        Class<?> deserializeUsing = null;
        final JSONField annotation = fieldInfo.getAnnotation();
        if (annotation != null) {
            deserializeUsing = annotation.deserializeUsing();
            if (deserializeUsing == Void.class) {
                deserializeUsing = null;
            }
        }
        if (deserializeUsing == null && (fieldClass == List.class || fieldClass == ArrayList.class)) {
            return new ArrayListTypeFieldDeserializer(mapping, clazz, fieldInfo);
        }
        return new DefaultFieldDeserializer(mapping, clazz, fieldInfo);
    }
    
    public void putDeserializer(final Type type, final ObjectDeserializer deserializer) {
        final Type mixin = JSON.getMixInAnnotations(type);
        if (mixin != null) {
            IdentityHashMap<Type, ObjectDeserializer> mixInClasses = this.mixInDeserializers.get(type);
            if (mixInClasses == null) {
                mixInClasses = new IdentityHashMap<Type, ObjectDeserializer>(4);
                this.mixInDeserializers.put(type, mixInClasses);
            }
            mixInClasses.put(mixin, deserializer);
        }
        else {
            this.deserializers.put(type, deserializer);
        }
    }
    
    public ObjectDeserializer get(final Type type) {
        final Type mixin = JSON.getMixInAnnotations(type);
        if (null == mixin) {
            return this.deserializers.get(type);
        }
        final IdentityHashMap<Type, ObjectDeserializer> mixInClasses = this.mixInDeserializers.get(type);
        if (mixInClasses == null) {
            return null;
        }
        return mixInClasses.get(mixin);
    }
    
    public ObjectDeserializer getDeserializer(final FieldInfo fieldInfo) {
        return this.getDeserializer(fieldInfo.fieldClass, fieldInfo.fieldType);
    }
    
    @Deprecated
    public boolean isPrimitive(final Class<?> clazz) {
        return isPrimitive2(clazz);
    }
    
    @Deprecated
    public static boolean isPrimitive2(final Class<?> clazz) {
        return clazz.isPrimitive() || clazz == Boolean.class || clazz == Character.class || clazz == Byte.class || clazz == Short.class || clazz == Integer.class || clazz == Long.class || clazz == Float.class || clazz == Double.class || clazz == BigInteger.class || clazz == BigDecimal.class || clazz == String.class || clazz == java.util.Date.class || clazz == Date.class || clazz == Time.class || clazz == Timestamp.class || clazz.isEnum();
    }
    
    public static void parserAllFieldToCache(final Class<?> clazz, final Map<String, Field> fieldCacheMap) {
        final Field[] declaredFields;
        final Field[] fields = declaredFields = clazz.getDeclaredFields();
        for (final Field field : declaredFields) {
            final String fieldName = field.getName();
            if (!fieldCacheMap.containsKey(fieldName)) {
                fieldCacheMap.put(fieldName, field);
            }
        }
        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            parserAllFieldToCache(clazz.getSuperclass(), fieldCacheMap);
        }
    }
    
    public static Field getFieldFromCache(final String fieldName, final Map<String, Field> fieldCacheMap) {
        Field field = fieldCacheMap.get(fieldName);
        if (field == null) {
            field = fieldCacheMap.get("_" + fieldName);
        }
        if (field == null) {
            field = fieldCacheMap.get("m_" + fieldName);
        }
        if (field == null) {
            final char c0 = fieldName.charAt(0);
            if (c0 >= 'a' && c0 <= 'z') {
                final char[] charArray;
                final char[] chars = charArray = fieldName.toCharArray();
                final int n = 0;
                charArray[n] -= ' ';
                final String fieldNameX = new String(chars);
                field = fieldCacheMap.get(fieldNameX);
            }
            if (fieldName.length() > 2) {
                final char c2 = fieldName.charAt(1);
                if (fieldName.length() > 2 && c0 >= 'a' && c0 <= 'z' && c2 >= 'A' && c2 <= 'Z') {
                    for (final Map.Entry<String, Field> entry : fieldCacheMap.entrySet()) {
                        if (fieldName.equalsIgnoreCase(entry.getKey())) {
                            field = entry.getValue();
                            break;
                        }
                    }
                }
            }
        }
        return field;
    }
    
    public ClassLoader getDefaultClassLoader() {
        return this.defaultClassLoader;
    }
    
    public void setDefaultClassLoader(final ClassLoader defaultClassLoader) {
        this.defaultClassLoader = defaultClassLoader;
    }
    
    public void addDenyInternal(final String name) {
        if (name == null || name.length() == 0) {
            return;
        }
        final long hash = TypeUtils.fnv1a_64(name);
        if (this.internalDenyHashCodes == null) {
            this.internalDenyHashCodes = new long[] { hash };
            return;
        }
        if (Arrays.binarySearch(this.internalDenyHashCodes, hash) >= 0) {
            return;
        }
        final long[] hashCodes = new long[this.internalDenyHashCodes.length + 1];
        hashCodes[hashCodes.length - 1] = hash;
        System.arraycopy(this.internalDenyHashCodes, 0, hashCodes, 0, this.internalDenyHashCodes.length);
        Arrays.sort(hashCodes);
        this.internalDenyHashCodes = hashCodes;
    }
    
    public void addDeny(final String name) {
        if (name == null || name.length() == 0) {
            return;
        }
        final long hash = TypeUtils.fnv1a_64(name);
        if (Arrays.binarySearch(this.denyHashCodes, hash) >= 0) {
            return;
        }
        final long[] hashCodes = new long[this.denyHashCodes.length + 1];
        hashCodes[hashCodes.length - 1] = hash;
        System.arraycopy(this.denyHashCodes, 0, hashCodes, 0, this.denyHashCodes.length);
        Arrays.sort(hashCodes);
        this.denyHashCodes = hashCodes;
    }
    
    public void addAccept(final String name) {
        if (name == null || name.length() == 0) {
            return;
        }
        final long hash = TypeUtils.fnv1a_64(name);
        if (Arrays.binarySearch(this.acceptHashCodes, hash) >= 0) {
            return;
        }
        final long[] hashCodes = new long[this.acceptHashCodes.length + 1];
        hashCodes[hashCodes.length - 1] = hash;
        System.arraycopy(this.acceptHashCodes, 0, hashCodes, 0, this.acceptHashCodes.length);
        Arrays.sort(hashCodes);
        this.acceptHashCodes = hashCodes;
    }
    
    public Class<?> checkAutoType(final Class type) {
        if (this.get(type) != null) {
            return (Class<?>)type;
        }
        return this.checkAutoType(type.getName(), null, JSON.DEFAULT_PARSER_FEATURE);
    }
    
    public Class<?> checkAutoType(final String typeName, final Class<?> expectClass) {
        return this.checkAutoType(typeName, expectClass, JSON.DEFAULT_PARSER_FEATURE);
    }
    
    public Class<?> checkAutoType(final String typeName, final Class<?> expectClass, final int features) {
        if (typeName == null) {
            return null;
        }
        if (this.autoTypeCheckHandlers != null) {
            for (final AutoTypeCheckHandler h : this.autoTypeCheckHandlers) {
                final Class<?> type = h.handler(typeName, expectClass, features);
                if (type != null) {
                    return type;
                }
            }
        }
        final int safeModeMask = Feature.SafeMode.mask;
        final boolean safeMode = this.safeMode || (features & safeModeMask) != 0x0 || (JSON.DEFAULT_PARSER_FEATURE & safeModeMask) != 0x0;
        if (safeMode) {
            throw new JSONException("safeMode not support autoType : " + typeName);
        }
        if (typeName.length() >= 192 || typeName.length() < 3) {
            throw new JSONException("autoType is not support. " + typeName);
        }
        final boolean expectClassFlag = expectClass != null && expectClass != Object.class && expectClass != Serializable.class && expectClass != Cloneable.class && expectClass != Closeable.class && expectClass != EventListener.class && expectClass != Iterable.class && expectClass != Collection.class;
        final String className = typeName.replace('$', '.');
        final long BASIC = -3750763034362895579L;
        final long PRIME = 1099511628211L;
        final long h2 = (0xCBF29CE484222325L ^ (long)className.charAt(0)) * 1099511628211L;
        if (h2 == -5808493101479473382L) {
            throw new JSONException("autoType is not support. " + typeName);
        }
        if ((h2 ^ (long)className.charAt(className.length() - 1)) * 1099511628211L == 655701488918567152L) {
            throw new JSONException("autoType is not support. " + typeName);
        }
        final long h3 = (((0xCBF29CE484222325L ^ (long)className.charAt(0)) * 1099511628211L ^ (long)className.charAt(1)) * 1099511628211L ^ (long)className.charAt(2)) * 1099511628211L;
        final long fullHash = TypeUtils.fnv1a_64(className);
        final boolean internalWhite = Arrays.binarySearch(ParserConfig.INTERNAL_WHITELIST_HASHCODES, fullHash) >= 0;
        if (this.internalDenyHashCodes != null) {
            long hash = h3;
            for (int i = 3; i < className.length(); ++i) {
                hash ^= className.charAt(i);
                hash *= 1099511628211L;
                if (Arrays.binarySearch(this.internalDenyHashCodes, hash) >= 0) {
                    throw new JSONException("autoType is not support. " + typeName);
                }
            }
        }
        if (!internalWhite && (this.autoTypeSupport || expectClassFlag)) {
            long hash = h3;
            for (int i = 3; i < className.length(); ++i) {
                hash ^= className.charAt(i);
                hash *= 1099511628211L;
                if (Arrays.binarySearch(this.acceptHashCodes, hash) >= 0) {
                    final Class<?> clazz = TypeUtils.loadClass(typeName, this.defaultClassLoader, true);
                    if (clazz != null) {
                        return clazz;
                    }
                }
                if (Arrays.binarySearch(this.denyHashCodes, hash) >= 0 && TypeUtils.getClassFromMapping(typeName) == null && Arrays.binarySearch(this.acceptHashCodes, fullHash) < 0) {
                    throw new JSONException("autoType is not support. " + typeName);
                }
            }
        }
        Class<?> clazz = TypeUtils.getClassFromMapping(typeName);
        if (clazz == null) {
            clazz = (Class<?>)this.deserializers.findClass(typeName);
        }
        if (clazz == null) {
            clazz = this.typeMapping.get(typeName);
        }
        if (internalWhite) {
            clazz = TypeUtils.loadClass(typeName, this.defaultClassLoader, true);
        }
        if (clazz != null) {
            if (expectClass != null && clazz != HashMap.class && !expectClass.isAssignableFrom(clazz)) {
                throw new JSONException("type not match. " + typeName + " -> " + expectClass.getName());
            }
            return clazz;
        }
        else {
            if (!this.autoTypeSupport) {
                long hash = h3;
                int i = 3;
                while (i < className.length()) {
                    final char c = className.charAt(i);
                    hash ^= c;
                    hash *= 1099511628211L;
                    if (Arrays.binarySearch(this.denyHashCodes, hash) >= 0) {
                        throw new JSONException("autoType is not support. " + typeName);
                    }
                    if (Arrays.binarySearch(this.acceptHashCodes, hash) >= 0) {
                        clazz = TypeUtils.loadClass(typeName, this.defaultClassLoader, true);
                        if (expectClass != null && expectClass.isAssignableFrom(clazz)) {
                            throw new JSONException("type not match. " + typeName + " -> " + expectClass.getName());
                        }
                        return clazz;
                    }
                    else {
                        ++i;
                    }
                }
            }
            boolean jsonType = false;
            InputStream is = null;
            try {
                final String resource = typeName.replace('.', '/') + ".class";
                if (this.defaultClassLoader != null) {
                    is = this.defaultClassLoader.getResourceAsStream(resource);
                }
                else {
                    is = ParserConfig.class.getClassLoader().getResourceAsStream(resource);
                }
                if (is != null) {
                    final ClassReader classReader = new ClassReader(is, true);
                    final TypeCollector visitor = new TypeCollector("<clinit>", new Class[0]);
                    classReader.accept(visitor);
                    jsonType = visitor.hasJsonType();
                }
            }
            catch (Exception ex) {}
            finally {
                IOUtils.close(is);
            }
            final int mask = Feature.SupportAutoType.mask;
            final boolean autoTypeSupport = this.autoTypeSupport || (features & mask) != 0x0 || (JSON.DEFAULT_PARSER_FEATURE & mask) != 0x0;
            if (autoTypeSupport || jsonType || expectClassFlag) {
                final boolean cacheClass = autoTypeSupport || jsonType;
                clazz = TypeUtils.loadClass(typeName, this.defaultClassLoader, cacheClass);
            }
            if (clazz != null) {
                if (jsonType) {
                    TypeUtils.addMapping(typeName, clazz);
                    return clazz;
                }
                if (ClassLoader.class.isAssignableFrom(clazz) || DataSource.class.isAssignableFrom(clazz) || RowSet.class.isAssignableFrom(clazz)) {
                    throw new JSONException("autoType is not support. " + typeName);
                }
                if (expectClass != null) {
                    if (expectClass.isAssignableFrom(clazz)) {
                        TypeUtils.addMapping(typeName, clazz);
                        return clazz;
                    }
                    throw new JSONException("type not match. " + typeName + " -> " + expectClass.getName());
                }
                else {
                    final JavaBeanInfo beanInfo = JavaBeanInfo.build(clazz, clazz, this.propertyNamingStrategy);
                    if (beanInfo.creatorConstructor != null && autoTypeSupport) {
                        throw new JSONException("autoType is not support. " + typeName);
                    }
                }
            }
            if (!autoTypeSupport) {
                throw new JSONException("autoType is not support. " + typeName);
            }
            if (clazz != null) {
                TypeUtils.addMapping(typeName, clazz);
            }
            return clazz;
        }
    }
    
    public void clearDeserializers() {
        this.deserializers.clear();
        this.initDeserializers();
    }
    
    public boolean isJacksonCompatible() {
        return this.jacksonCompatible;
    }
    
    public void setJacksonCompatible(final boolean jacksonCompatible) {
        this.jacksonCompatible = jacksonCompatible;
    }
    
    public void register(final String typeName, final Class type) {
        this.typeMapping.putIfAbsent(typeName, type);
    }
    
    public void register(final Module module) {
        this.modules.add(module);
    }
    
    public void addAutoTypeCheckHandler(final AutoTypeCheckHandler h) {
        List<AutoTypeCheckHandler> autoTypeCheckHandlers = this.autoTypeCheckHandlers;
        if (autoTypeCheckHandlers == null) {
            autoTypeCheckHandlers = (this.autoTypeCheckHandlers = new CopyOnWriteArrayList<AutoTypeCheckHandler>());
        }
        autoTypeCheckHandlers.add(h);
    }
    
    static {
        String property = IOUtils.getStringProperty("fastjson.parser.deny.internal");
        DENYS_INTERNAL = splitItemsFormProperty(property);
        property = IOUtils.getStringProperty("fastjson.parser.deny");
        DENYS = splitItemsFormProperty(property);
        property = IOUtils.getStringProperty("fastjson.parser.autoTypeSupport");
        AUTO_SUPPORT = "true".equals(property);
        property = IOUtils.getStringProperty("fastjson.parser.safeMode");
        SAFE_MODE = "true".equals(property);
        property = IOUtils.getStringProperty("fastjson.parser.autoTypeAccept");
        String[] items = splitItemsFormProperty(property);
        if (items == null) {
            items = new String[0];
        }
        AUTO_TYPE_ACCEPT_LIST = items;
        INTERNAL_WHITELIST_HASHCODES = new long[] { -9013707057526259810L, -8773806119481270567L, -8421588593326113468L, -8070393259084821111L, -7858127399773263546L, -7043543676283957292L, -6976602508726000783L, -6293031534589903644L, -6081111809668363619L, -5779433778261875721L, -5399450433995651784L, -4540135604787511831L, -4207865850564917696L, -3950343444501679205L, -3714900953609113456L, -3393714734093696063L, -3378497329992063044L, -2631228350337215662L, -2551988546877734201L, -2473987886800209058L, -2265617974881722705L, -1759511109484434299L, -1477946458560579955L, -816725787720647462L, -520183782617964618L, 59775428743665658L, 484499585846206473L, 532945107123976213L, 711449177569584898L, 829148494126372070L, 956883420092542580L, 1233162291719202522L, 1696465274354442213L, 1863557081881630420L, 2238472697200138595L, 2380202963256720577L, 2643099543618286743L, 2793877891138577121L, 3804572268889088203L, 4567982875926242015L, 4784070066737926537L, 4960004821520561233L, 5348524593377618456L, 5454920836284873808L, 5695987590363189151L, 6073645722991901167L, 6114875255374330593L, 6137737446243999215L, 6160752908990493848L, 6939315124833099497L, 7048426940343117278L, 7267793227937552092L, 8331868837379820532L, 8357451534615459155L, 8890227807433646566L, 9166532985682478006L, 9215131087512669423L };
        ParserConfig.global = new ParserConfig();
        ParserConfig.awtError = false;
        ParserConfig.jdk8Error = false;
        ParserConfig.jodaError = false;
        ParserConfig.guavaError = false;
    }
    
    public interface AutoTypeCheckHandler
    {
        Class<?> handler(final String p0, final Class<?> p1, final int p2);
    }
}
