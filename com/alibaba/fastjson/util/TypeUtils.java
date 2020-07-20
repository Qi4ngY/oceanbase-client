package com.alibaba.fastjson.util;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Deque;
import java.util.Queue;
import java.util.EnumSet;
import java.util.AbstractCollection;
import java.lang.reflect.GenericArrayType;
import java.security.AccessControlException;
import java.lang.reflect.AccessibleObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.annotation.JSONField;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializeBeanInfo;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONPObject;
import java.util.UUID;
import java.util.BitSet;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.LinkedHashSet;
import java.util.WeakHashMap;
import java.util.IdentityHashMap;
import java.util.TreeMap;
import java.util.Hashtable;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import java.util.LinkedHashMap;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Set;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.ParameterizedType;
import com.alibaba.fastjson.parser.deserializer.EnumDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Currency;
import com.alibaba.fastjson.serializer.CalendarCodec;
import java.lang.reflect.Array;
import java.util.Collection;
import com.alibaba.fastjson.parser.ParserConfig;
import java.util.regex.Matcher;
import java.util.Iterator;
import java.sql.Timestamp;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.JSONScanner;
import java.util.Calendar;
import java.util.Date;
import java.math.BigInteger;
import com.alibaba.fastjson.JSONException;
import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentMap;
import java.util.Map;
import java.lang.reflect.Constructor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class TypeUtils
{
    private static final Pattern NUMBER_WITH_TRAILING_ZEROS_PATTERN;
    public static boolean compatibleWithJavaBean;
    public static boolean compatibleWithFieldName;
    private static boolean setAccessibleEnable;
    private static boolean oracleTimestampMethodInited;
    private static Method oracleTimestampMethod;
    private static boolean oracleDateMethodInited;
    private static Method oracleDateMethod;
    private static boolean optionalClassInited;
    private static Class<?> optionalClass;
    private static boolean transientClassInited;
    private static Class<? extends Annotation> transientClass;
    private static Class<? extends Annotation> class_OneToMany;
    private static boolean class_OneToMany_error;
    private static Class<? extends Annotation> class_ManyToMany;
    private static boolean class_ManyToMany_error;
    private static Method method_HibernateIsInitialized;
    private static boolean method_HibernateIsInitialized_error;
    private static volatile Class kotlin_metadata;
    private static volatile boolean kotlin_metadata_error;
    private static volatile boolean kotlin_class_klass_error;
    private static volatile Constructor kotlin_kclass_constructor;
    private static volatile Method kotlin_kclass_getConstructors;
    private static volatile Method kotlin_kfunction_getParameters;
    private static volatile Method kotlin_kparameter_getName;
    private static volatile boolean kotlin_error;
    private static volatile Map<Class, String[]> kotlinIgnores;
    private static volatile boolean kotlinIgnores_error;
    private static ConcurrentMap<String, Class<?>> mappings;
    private static Class<?> pathClass;
    private static boolean pathClass_error;
    private static Class<? extends Annotation> class_JacksonCreator;
    private static boolean class_JacksonCreator_error;
    private static volatile Class class_Clob;
    private static volatile boolean class_Clob_error;
    private static volatile Class class_XmlAccessType;
    private static volatile Class class_XmlAccessorType;
    private static volatile boolean classXmlAccessorType_error;
    private static volatile Method method_XmlAccessorType_value;
    private static volatile Field field_XmlAccessType_FIELD;
    private static volatile Object field_XmlAccessType_FIELD_VALUE;
    
    public static boolean isXmlField(final Class clazz) {
        if (TypeUtils.class_XmlAccessorType == null && !TypeUtils.classXmlAccessorType_error) {
            try {
                TypeUtils.class_XmlAccessorType = Class.forName("javax.xml.bind.annotation.XmlAccessorType");
            }
            catch (Throwable ex) {
                TypeUtils.classXmlAccessorType_error = true;
            }
        }
        if (TypeUtils.class_XmlAccessorType == null) {
            return false;
        }
        final Annotation annotation = getAnnotation(clazz, (Class<Annotation>)TypeUtils.class_XmlAccessorType);
        if (annotation == null) {
            return false;
        }
        if (TypeUtils.method_XmlAccessorType_value == null && !TypeUtils.classXmlAccessorType_error) {
            try {
                TypeUtils.method_XmlAccessorType_value = TypeUtils.class_XmlAccessorType.getMethod("value", (Class[])new Class[0]);
            }
            catch (Throwable ex2) {
                TypeUtils.classXmlAccessorType_error = true;
            }
        }
        if (TypeUtils.method_XmlAccessorType_value == null) {
            return false;
        }
        Object value = null;
        if (!TypeUtils.classXmlAccessorType_error) {
            try {
                value = TypeUtils.method_XmlAccessorType_value.invoke(annotation, new Object[0]);
            }
            catch (Throwable ex3) {
                TypeUtils.classXmlAccessorType_error = true;
            }
        }
        if (value == null) {
            return false;
        }
        if (TypeUtils.class_XmlAccessType == null && !TypeUtils.classXmlAccessorType_error) {
            try {
                TypeUtils.class_XmlAccessType = Class.forName("javax.xml.bind.annotation.XmlAccessType");
                TypeUtils.field_XmlAccessType_FIELD = TypeUtils.class_XmlAccessType.getField("FIELD");
                TypeUtils.field_XmlAccessType_FIELD_VALUE = TypeUtils.field_XmlAccessType_FIELD.get(null);
            }
            catch (Throwable ex3) {
                TypeUtils.classXmlAccessorType_error = true;
            }
        }
        return value == TypeUtils.field_XmlAccessType_FIELD_VALUE;
    }
    
    public static Annotation getXmlAccessorType(final Class clazz) {
        if (TypeUtils.class_XmlAccessorType == null && !TypeUtils.classXmlAccessorType_error) {
            try {
                TypeUtils.class_XmlAccessorType = Class.forName("javax.xml.bind.annotation.XmlAccessorType");
            }
            catch (Throwable ex) {
                TypeUtils.classXmlAccessorType_error = true;
            }
        }
        if (TypeUtils.class_XmlAccessorType == null) {
            return null;
        }
        return getAnnotation(clazz, (Class<Annotation>)TypeUtils.class_XmlAccessorType);
    }
    
    public static boolean isClob(final Class clazz) {
        if (TypeUtils.class_Clob == null && !TypeUtils.class_Clob_error) {
            try {
                TypeUtils.class_Clob = Class.forName("java.sql.Clob");
            }
            catch (Throwable ex) {
                TypeUtils.class_Clob_error = true;
            }
        }
        return TypeUtils.class_Clob != null && TypeUtils.class_Clob.isAssignableFrom(clazz);
    }
    
    public static String castToString(final Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
    
    public static Byte castToByte(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return byteValue((BigDecimal)value);
        }
        if (value instanceof Number) {
            return ((Number)value).byteValue();
        }
        if (!(value instanceof String)) {
            throw new JSONException("can not cast to byte, value : " + value);
        }
        final String strVal = (String)value;
        if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
            return null;
        }
        return Byte.parseByte(strVal);
    }
    
    public static Character castToChar(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Character) {
            return (Character)value;
        }
        if (!(value instanceof String)) {
            throw new JSONException("can not cast to char, value : " + value);
        }
        final String strVal = (String)value;
        if (strVal.length() == 0) {
            return null;
        }
        if (strVal.length() != 1) {
            throw new JSONException("can not cast to char, value : " + value);
        }
        return strVal.charAt(0);
    }
    
    public static Short castToShort(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return shortValue((BigDecimal)value);
        }
        if (value instanceof Number) {
            return ((Number)value).shortValue();
        }
        if (!(value instanceof String)) {
            throw new JSONException("can not cast to short, value : " + value);
        }
        final String strVal = (String)value;
        if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
            return null;
        }
        return Short.parseShort(strVal);
    }
    
    public static BigDecimal castToBigDecimal(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal)value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger)value);
        }
        final String strVal = value.toString();
        if (strVal.length() == 0) {
            return null;
        }
        if (value instanceof Map && ((Map)value).size() == 0) {
            return null;
        }
        return new BigDecimal(strVal);
    }
    
    public static BigInteger castToBigInteger(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigInteger) {
            return (BigInteger)value;
        }
        if (value instanceof Float || value instanceof Double) {
            return BigInteger.valueOf(((Number)value).longValue());
        }
        if (value instanceof BigDecimal) {
            final BigDecimal decimal = (BigDecimal)value;
            final int scale = decimal.scale();
            if (scale > -1000 && scale < 1000) {
                return ((BigDecimal)value).toBigInteger();
            }
        }
        final String strVal = value.toString();
        if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
            return null;
        }
        return new BigInteger(strVal);
    }
    
    public static Float castToFloat(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number)value).floatValue();
        }
        if (!(value instanceof String)) {
            throw new JSONException("can not cast to float, value : " + value);
        }
        String strVal = value.toString();
        if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
            return null;
        }
        if (strVal.indexOf(44) != -1) {
            strVal = strVal.replaceAll(",", "");
        }
        return Float.parseFloat(strVal);
    }
    
    public static Double castToDouble(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number)value).doubleValue();
        }
        if (!(value instanceof String)) {
            throw new JSONException("can not cast to double, value : " + value);
        }
        String strVal = value.toString();
        if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
            return null;
        }
        if (strVal.indexOf(44) != -1) {
            strVal = strVal.replaceAll(",", "");
        }
        return Double.parseDouble(strVal);
    }
    
    public static Date castToDate(final Object value) {
        return castToDate(value, null);
    }
    
    public static Date castToDate(final Object value, String format) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date)value;
        }
        if (value instanceof Calendar) {
            return ((Calendar)value).getTime();
        }
        long longValue = -1L;
        if (value instanceof BigDecimal) {
            longValue = longValue((BigDecimal)value);
            return new Date(longValue);
        }
        if (value instanceof Number) {
            longValue = ((Number)value).longValue();
            if ("unixtime".equals(format)) {
                longValue *= 1000L;
            }
            return new Date(longValue);
        }
        if (value instanceof String) {
            String strVal = (String)value;
            final JSONScanner dateLexer = new JSONScanner(strVal);
            try {
                if (dateLexer.scanISO8601DateIfMatch(false)) {
                    final Calendar calendar = dateLexer.getCalendar();
                    return calendar.getTime();
                }
            }
            finally {
                dateLexer.close();
            }
            if (strVal.startsWith("/Date(") && strVal.endsWith(")/")) {
                strVal = strVal.substring(6, strVal.length() - 2);
            }
            if (strVal.indexOf(45) > 0 || strVal.indexOf(43) > 0) {
                if (format == null) {
                    if (strVal.length() == JSON.DEFFAULT_DATE_FORMAT.length() || (strVal.length() == 22 && JSON.DEFFAULT_DATE_FORMAT.equals("yyyyMMddHHmmssSSSZ"))) {
                        format = JSON.DEFFAULT_DATE_FORMAT;
                    }
                    else if (strVal.length() == 10) {
                        format = "yyyy-MM-dd";
                    }
                    else if (strVal.length() == "yyyy-MM-dd HH:mm:ss".length()) {
                        format = "yyyy-MM-dd HH:mm:ss";
                    }
                    else if (strVal.length() == 29 && strVal.charAt(26) == ':' && strVal.charAt(28) == '0') {
                        format = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
                    }
                    else if (strVal.length() == 23 && strVal.charAt(19) == ',') {
                        format = "yyyy-MM-dd HH:mm:ss,SSS";
                    }
                    else {
                        format = "yyyy-MM-dd HH:mm:ss.SSS";
                    }
                }
                final SimpleDateFormat dateFormat = new SimpleDateFormat(format, JSON.defaultLocale);
                dateFormat.setTimeZone(JSON.defaultTimeZone);
                try {
                    return dateFormat.parse(strVal);
                }
                catch (ParseException e2) {
                    throw new JSONException("can not cast to Date, value : " + strVal);
                }
            }
            if (strVal.length() == 0) {
                return null;
            }
            longValue = Long.parseLong(strVal);
        }
        if (longValue != -1L) {
            return new Date(longValue);
        }
        final Class<?> clazz = value.getClass();
        if ("oracle.sql.TIMESTAMP".equals(clazz.getName())) {
            if (TypeUtils.oracleTimestampMethod == null && !TypeUtils.oracleTimestampMethodInited) {
                try {
                    TypeUtils.oracleTimestampMethod = clazz.getMethod("toJdbc", (Class<?>[])new Class[0]);
                }
                catch (NoSuchMethodException ex) {}
                finally {
                    TypeUtils.oracleTimestampMethodInited = true;
                }
            }
            Object result;
            try {
                result = TypeUtils.oracleTimestampMethod.invoke(value, new Object[0]);
            }
            catch (Exception e) {
                throw new JSONException("can not cast oracle.sql.TIMESTAMP to Date", e);
            }
            return (Date)result;
        }
        if ("oracle.sql.DATE".equals(clazz.getName())) {
            if (TypeUtils.oracleDateMethod == null && !TypeUtils.oracleDateMethodInited) {
                try {
                    TypeUtils.oracleDateMethod = clazz.getMethod("toJdbc", (Class<?>[])new Class[0]);
                }
                catch (NoSuchMethodException ex2) {}
                finally {
                    TypeUtils.oracleDateMethodInited = true;
                }
            }
            Object result;
            try {
                result = TypeUtils.oracleDateMethod.invoke(value, new Object[0]);
            }
            catch (Exception e) {
                throw new JSONException("can not cast oracle.sql.DATE to Date", e);
            }
            return (Date)result;
        }
        throw new JSONException("can not cast to Date, value : " + value);
    }
    
    public static java.sql.Date castToSqlDate(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date) {
            return (java.sql.Date)value;
        }
        if (value instanceof Date) {
            return new java.sql.Date(((Date)value).getTime());
        }
        if (value instanceof Calendar) {
            return new java.sql.Date(((Calendar)value).getTimeInMillis());
        }
        long longValue = 0L;
        if (value instanceof BigDecimal) {
            longValue = longValue((BigDecimal)value);
        }
        else if (value instanceof Number) {
            longValue = ((Number)value).longValue();
        }
        if (value instanceof String) {
            final String strVal = (String)value;
            if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                return null;
            }
            if (isNumber(strVal)) {
                longValue = Long.parseLong(strVal);
            }
            else {
                final JSONScanner scanner = new JSONScanner(strVal);
                if (!scanner.scanISO8601DateIfMatch(false)) {
                    throw new JSONException("can not cast to Timestamp, value : " + strVal);
                }
                longValue = scanner.getCalendar().getTime().getTime();
            }
        }
        if (longValue <= 0L) {
            throw new JSONException("can not cast to Date, value : " + value);
        }
        return new java.sql.Date(longValue);
    }
    
    public static long longExtractValue(final Number number) {
        if (number instanceof BigDecimal) {
            return ((BigDecimal)number).longValueExact();
        }
        return number.longValue();
    }
    
    public static Time castToSqlTime(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Time) {
            return (Time)value;
        }
        if (value instanceof Date) {
            return new Time(((Date)value).getTime());
        }
        if (value instanceof Calendar) {
            return new Time(((Calendar)value).getTimeInMillis());
        }
        long longValue = 0L;
        if (value instanceof BigDecimal) {
            longValue = longValue((BigDecimal)value);
        }
        else if (value instanceof Number) {
            longValue = ((Number)value).longValue();
        }
        if (value instanceof String) {
            final String strVal = (String)value;
            if (strVal.length() == 0 || "null".equalsIgnoreCase(strVal)) {
                return null;
            }
            if (isNumber(strVal)) {
                longValue = Long.parseLong(strVal);
            }
            else {
                final JSONScanner scanner = new JSONScanner(strVal);
                if (!scanner.scanISO8601DateIfMatch(false)) {
                    throw new JSONException("can not cast to Timestamp, value : " + strVal);
                }
                longValue = scanner.getCalendar().getTime().getTime();
            }
        }
        if (longValue <= 0L) {
            throw new JSONException("can not cast to Date, value : " + value);
        }
        return new Time(longValue);
    }
    
    public static Timestamp castToTimestamp(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Calendar) {
            return new Timestamp(((Calendar)value).getTimeInMillis());
        }
        if (value instanceof Timestamp) {
            return (Timestamp)value;
        }
        if (value instanceof Date) {
            return new Timestamp(((Date)value).getTime());
        }
        long longValue = 0L;
        if (value instanceof BigDecimal) {
            longValue = longValue((BigDecimal)value);
        }
        else if (value instanceof Number) {
            longValue = ((Number)value).longValue();
        }
        if (value instanceof String) {
            String strVal = (String)value;
            if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.endsWith(".000000000")) {
                strVal = strVal.substring(0, strVal.length() - 10);
            }
            else if (strVal.endsWith(".000000")) {
                strVal = strVal.substring(0, strVal.length() - 7);
            }
            if (strVal.length() == 29 && strVal.charAt(4) == '-' && strVal.charAt(7) == '-' && strVal.charAt(10) == ' ' && strVal.charAt(13) == ':' && strVal.charAt(16) == ':' && strVal.charAt(19) == '.') {
                final int year = num(strVal.charAt(0), strVal.charAt(1), strVal.charAt(2), strVal.charAt(3));
                final int month = num(strVal.charAt(5), strVal.charAt(6));
                final int day = num(strVal.charAt(8), strVal.charAt(9));
                final int hour = num(strVal.charAt(11), strVal.charAt(12));
                final int minute = num(strVal.charAt(14), strVal.charAt(15));
                final int second = num(strVal.charAt(17), strVal.charAt(18));
                final int nanos = num(strVal.charAt(20), strVal.charAt(21), strVal.charAt(22), strVal.charAt(23), strVal.charAt(24), strVal.charAt(25), strVal.charAt(26), strVal.charAt(27), strVal.charAt(28));
                return new Timestamp(year - 1900, month - 1, day, hour, minute, second, nanos);
            }
            if (isNumber(strVal)) {
                longValue = Long.parseLong(strVal);
            }
            else {
                final JSONScanner scanner = new JSONScanner(strVal);
                if (!scanner.scanISO8601DateIfMatch(false)) {
                    throw new JSONException("can not cast to Timestamp, value : " + strVal);
                }
                longValue = scanner.getCalendar().getTime().getTime();
            }
        }
        if (longValue <= 0L) {
            throw new JSONException("can not cast to Timestamp, value : " + value);
        }
        return new Timestamp(longValue);
    }
    
    static int num(final char c0, final char c1) {
        if (c0 >= '0' && c0 <= '9' && c1 >= '0' && c1 <= '9') {
            return (c0 - '0') * 10 + (c1 - '0');
        }
        return -1;
    }
    
    static int num(final char c0, final char c1, final char c2, final char c3) {
        if (c0 >= '0' && c0 <= '9' && c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9' && c3 >= '0' && c3 <= '9') {
            return (c0 - '0') * 1000 + (c1 - '0') * 100 + (c2 - '0') * 10 + (c3 - '0');
        }
        return -1;
    }
    
    static int num(final char c0, final char c1, final char c2, final char c3, final char c4, final char c5, final char c6, final char c7, final char c8) {
        if (c0 >= '0' && c0 <= '9' && c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9' && c3 >= '0' && c3 <= '9' && c4 >= '0' && c4 <= '9' && c5 >= '0' && c5 <= '9' && c6 >= '0' && c6 <= '9' && c7 >= '0' && c7 <= '9' && c8 >= '0' && c8 <= '9') {
            return (c0 - '0') * 100000000 + (c1 - '0') * 10000000 + (c2 - '0') * 1000000 + (c3 - '0') * 100000 + (c4 - '0') * 10000 + (c5 - '0') * 1000 + (c6 - '0') * 100 + (c7 - '0') * 10 + (c8 - '0');
        }
        return -1;
    }
    
    public static boolean isNumber(final String str) {
        for (int i = 0; i < str.length(); ++i) {
            final char ch = str.charAt(i);
            if (ch == '+' || ch == '-') {
                if (i != 0) {
                    return false;
                }
            }
            else if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }
    
    public static Long castToLong(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return longValue((BigDecimal)value);
        }
        if (value instanceof Number) {
            return ((Number)value).longValue();
        }
        if (value instanceof String) {
            String strVal = (String)value;
            if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.indexOf(44) != -1) {
                strVal = strVal.replaceAll(",", "");
            }
            try {
                return Long.parseLong(strVal);
            }
            catch (NumberFormatException ex) {
                final JSONScanner dateParser = new JSONScanner(strVal);
                Calendar calendar = null;
                if (dateParser.scanISO8601DateIfMatch(false)) {
                    calendar = dateParser.getCalendar();
                }
                dateParser.close();
                if (calendar != null) {
                    return calendar.getTimeInMillis();
                }
            }
        }
        if (value instanceof Map) {
            final Map map = (Map)value;
            if (map.size() == 2 && map.containsKey("andIncrement") && map.containsKey("andDecrement")) {
                final Iterator iter = map.values().iterator();
                iter.next();
                final Object value2 = iter.next();
                return castToLong(value2);
            }
        }
        throw new JSONException("can not cast to long, value : " + value);
    }
    
    public static byte byteValue(final BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }
        final int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.byteValue();
        }
        return decimal.byteValueExact();
    }
    
    public static short shortValue(final BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }
        final int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.shortValue();
        }
        return decimal.shortValueExact();
    }
    
    public static int intValue(final BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }
        final int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.intValue();
        }
        return decimal.intValueExact();
    }
    
    public static long longValue(final BigDecimal decimal) {
        if (decimal == null) {
            return 0L;
        }
        final int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.longValue();
        }
        return decimal.longValueExact();
    }
    
    public static Integer castToInt(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer)value;
        }
        if (value instanceof BigDecimal) {
            return intValue((BigDecimal)value);
        }
        if (value instanceof Number) {
            return ((Number)value).intValue();
        }
        if (value instanceof String) {
            String strVal = (String)value;
            if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.indexOf(44) != -1) {
                strVal = strVal.replaceAll(",", "");
            }
            final Matcher matcher = TypeUtils.NUMBER_WITH_TRAILING_ZEROS_PATTERN.matcher(strVal);
            if (matcher.find()) {
                strVal = matcher.replaceAll("");
            }
            return Integer.parseInt(strVal);
        }
        else {
            if (value instanceof Boolean) {
                return ((boolean)value) ? 1 : 0;
            }
            if (value instanceof Map) {
                final Map map = (Map)value;
                if (map.size() == 2 && map.containsKey("andIncrement") && map.containsKey("andDecrement")) {
                    final Iterator iter = map.values().iterator();
                    iter.next();
                    final Object value2 = iter.next();
                    return castToInt(value2);
                }
            }
            throw new JSONException("can not cast to int, value : " + value);
        }
    }
    
    public static byte[] castToBytes(final Object value) {
        if (value instanceof byte[]) {
            return (byte[])value;
        }
        if (value instanceof String) {
            return IOUtils.decodeBase64((String)value);
        }
        throw new JSONException("can not cast to byte[], value : " + value);
    }
    
    public static Boolean castToBoolean(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean)value;
        }
        if (value instanceof BigDecimal) {
            return intValue((BigDecimal)value) == 1;
        }
        if (value instanceof Number) {
            return ((Number)value).intValue() == 1;
        }
        if (value instanceof String) {
            final String strVal = (String)value;
            if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                return null;
            }
            if ("true".equalsIgnoreCase(strVal) || "1".equals(strVal)) {
                return Boolean.TRUE;
            }
            if ("false".equalsIgnoreCase(strVal) || "0".equals(strVal)) {
                return Boolean.FALSE;
            }
            if ("Y".equalsIgnoreCase(strVal) || "T".equals(strVal)) {
                return Boolean.TRUE;
            }
            if ("F".equalsIgnoreCase(strVal) || "N".equals(strVal)) {
                return Boolean.FALSE;
            }
        }
        throw new JSONException("can not cast to boolean, value : " + value);
    }
    
    public static <T> T castToJavaBean(final Object obj, final Class<T> clazz) {
        return cast(obj, clazz, ParserConfig.getGlobalInstance());
    }
    
    public static <T> T cast(final Object obj, final Class<T> clazz, final ParserConfig config) {
        if (obj == null) {
            if (clazz == Integer.TYPE) {
                return (T)Integer.valueOf(0);
            }
            if (clazz == Long.TYPE) {
                return (T)Long.valueOf(0L);
            }
            if (clazz == Short.TYPE) {
                return (T)0;
            }
            if (clazz == Byte.TYPE) {
                return (T)0;
            }
            if (clazz == Float.TYPE) {
                return (T)Float.valueOf(0.0f);
            }
            if (clazz == Double.TYPE) {
                return (T)Double.valueOf(0.0);
            }
            if (clazz == Boolean.TYPE) {
                return (T)Boolean.FALSE;
            }
            return null;
        }
        else {
            if (clazz == null) {
                throw new IllegalArgumentException("clazz is null");
            }
            if (clazz == obj.getClass()) {
                return (T)obj;
            }
            if (obj instanceof Map) {
                if (clazz == Map.class) {
                    return (T)obj;
                }
                final Map map = (Map)obj;
                if (clazz == Object.class && !map.containsKey(JSON.DEFAULT_TYPE_KEY)) {
                    return (T)obj;
                }
                return castToJavaBean((Map<String, Object>)obj, clazz, config);
            }
            else {
                if (clazz.isArray()) {
                    if (obj instanceof Collection) {
                        final Collection collection = (Collection)obj;
                        int index = 0;
                        final Object array = Array.newInstance(clazz.getComponentType(), collection.size());
                        for (final Object item : collection) {
                            final Object value = cast(item, clazz.getComponentType(), config);
                            Array.set(array, index, value);
                            ++index;
                        }
                        return (T)array;
                    }
                    if (clazz == byte[].class) {
                        return (T)(Object)castToBytes(obj);
                    }
                }
                if (clazz.isAssignableFrom(obj.getClass())) {
                    return (T)obj;
                }
                if (clazz == Boolean.TYPE || clazz == Boolean.class) {
                    return (T)castToBoolean(obj);
                }
                if (clazz == Byte.TYPE || clazz == Byte.class) {
                    return (T)castToByte(obj);
                }
                if (clazz == Character.TYPE || clazz == Character.class) {
                    return (T)castToChar(obj);
                }
                if (clazz == Short.TYPE || clazz == Short.class) {
                    return (T)castToShort(obj);
                }
                if (clazz == Integer.TYPE || clazz == Integer.class) {
                    return (T)castToInt(obj);
                }
                if (clazz == Long.TYPE || clazz == Long.class) {
                    return (T)castToLong(obj);
                }
                if (clazz == Float.TYPE || clazz == Float.class) {
                    return (T)castToFloat(obj);
                }
                if (clazz == Double.TYPE || clazz == Double.class) {
                    return (T)castToDouble(obj);
                }
                if (clazz == String.class) {
                    return (T)castToString(obj);
                }
                if (clazz == BigDecimal.class) {
                    return (T)castToBigDecimal(obj);
                }
                if (clazz == BigInteger.class) {
                    return (T)castToBigInteger(obj);
                }
                if (clazz == Date.class) {
                    return (T)castToDate(obj);
                }
                if (clazz == java.sql.Date.class) {
                    return (T)castToSqlDate(obj);
                }
                if (clazz == Time.class) {
                    return (T)castToSqlTime(obj);
                }
                if (clazz == Timestamp.class) {
                    return (T)castToTimestamp(obj);
                }
                if (clazz.isEnum()) {
                    return (T)castToEnum(obj, (Class<Object>)clazz, config);
                }
                if (Calendar.class.isAssignableFrom(clazz)) {
                    final Date date = castToDate(obj);
                    Calendar calendar;
                    if (clazz == Calendar.class) {
                        calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
                    }
                    else {
                        try {
                            calendar = (Calendar)clazz.newInstance();
                        }
                        catch (Exception e) {
                            throw new JSONException("can not cast to : " + clazz.getName(), e);
                        }
                    }
                    calendar.setTime(date);
                    return (T)calendar;
                }
                final String className = clazz.getName();
                if (className.equals("javax.xml.datatype.XMLGregorianCalendar")) {
                    final Date date2 = castToDate(obj);
                    final Calendar calendar2 = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
                    calendar2.setTime(date2);
                    return (T)CalendarCodec.instance.createXMLGregorianCalendar(calendar2);
                }
                if (obj instanceof String) {
                    final String strVal = (String)obj;
                    if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                        return null;
                    }
                    if (clazz == Currency.class) {
                        return (T)Currency.getInstance(strVal);
                    }
                    if (clazz == Locale.class) {
                        return (T)toLocale(strVal);
                    }
                    if (className.startsWith("java.time.")) {
                        final String json = JSON.toJSONString(strVal);
                        return JSON.parseObject(json, clazz);
                    }
                }
                final ObjectDeserializer objectDeserializer = config.get(clazz);
                if (objectDeserializer != null) {
                    final String str = JSON.toJSONString(obj);
                    return JSON.parseObject(str, clazz);
                }
                throw new JSONException("can not cast to : " + clazz.getName());
            }
        }
    }
    
    public static Locale toLocale(final String strVal) {
        final String[] items = strVal.split("_");
        if (items.length == 1) {
            return new Locale(items[0]);
        }
        if (items.length == 2) {
            return new Locale(items[0], items[1]);
        }
        return new Locale(items[0], items[1], items[2]);
    }
    
    public static <T> T castToEnum(final Object obj, final Class<T> clazz, ParserConfig mapping) {
        try {
            if (obj instanceof String) {
                final String name = (String)obj;
                if (name.length() == 0) {
                    return null;
                }
                if (mapping == null) {
                    mapping = ParserConfig.getGlobalInstance();
                }
                final ObjectDeserializer deserializer = mapping.getDeserializer(clazz);
                if (deserializer instanceof EnumDeserializer) {
                    final EnumDeserializer enumDeserializer = (EnumDeserializer)deserializer;
                    return (T)enumDeserializer.getEnumByHashCode(fnv1a_64(name));
                }
                return Enum.valueOf(clazz, name);
            }
            else {
                if (obj instanceof BigDecimal) {
                    final int ordinal = intValue((BigDecimal)obj);
                    final Object[] values = clazz.getEnumConstants();
                    if (ordinal < values.length) {
                        return (T)values[ordinal];
                    }
                }
                if (obj instanceof Number) {
                    final int ordinal = ((Number)obj).intValue();
                    final Object[] values = clazz.getEnumConstants();
                    if (ordinal < values.length) {
                        return (T)values[ordinal];
                    }
                }
            }
        }
        catch (Exception ex) {
            throw new JSONException("can not cast to : " + clazz.getName(), ex);
        }
        throw new JSONException("can not cast to : " + clazz.getName());
    }
    
    public static <T> T cast(final Object obj, final Type type, final ParserConfig mapping) {
        if (obj == null) {
            return null;
        }
        if (type instanceof Class) {
            return cast(obj, (Class<T>)type, mapping);
        }
        if (type instanceof ParameterizedType) {
            return cast(obj, (ParameterizedType)type, mapping);
        }
        if (obj instanceof String) {
            final String strVal = (String)obj;
            if (strVal.length() == 0 || "null".equals(strVal) || "NULL".equals(strVal)) {
                return null;
            }
        }
        if (type instanceof TypeVariable) {
            return (T)obj;
        }
        throw new JSONException("can not cast to : " + type);
    }
    
    public static <T> T cast(final Object obj, final ParameterizedType type, ParserConfig mapping) {
        final Type rawTye = type.getRawType();
        if (rawTye == List.class || rawTye == ArrayList.class) {
            final Type itemType = type.getActualTypeArguments()[0];
            if (obj instanceof List) {
                final List listObj = (List)obj;
                final List arrayList = new ArrayList(listObj.size());
                for (int i = 0; i < listObj.size(); ++i) {
                    final Object item = listObj.get(i);
                    Object itemValue;
                    if (itemType instanceof Class) {
                        if (item != null && item.getClass() == JSONObject.class) {
                            itemValue = ((JSONObject)item).toJavaObject((Class<Object>)itemType, mapping, 0);
                        }
                        else {
                            itemValue = cast(item, (Class<Object>)itemType, mapping);
                        }
                    }
                    else {
                        itemValue = cast(item, itemType, mapping);
                    }
                    arrayList.add(itemValue);
                }
                return (T)arrayList;
            }
        }
        if (rawTye == Set.class || rawTye == HashSet.class || rawTye == TreeSet.class || rawTye == Collection.class || rawTye == List.class || rawTye == ArrayList.class) {
            final Type itemType = type.getActualTypeArguments()[0];
            if (obj instanceof Iterable) {
                Collection collection;
                if (rawTye == Set.class || rawTye == HashSet.class) {
                    collection = new HashSet();
                }
                else if (rawTye == TreeSet.class) {
                    collection = new TreeSet();
                }
                else {
                    collection = new ArrayList();
                }
                for (final Object item2 : (Iterable)obj) {
                    Object itemValue2;
                    if (itemType instanceof Class) {
                        if (item2 != null && item2.getClass() == JSONObject.class) {
                            itemValue2 = ((JSONObject)item2).toJavaObject((Class<Object>)itemType, mapping, 0);
                        }
                        else {
                            itemValue2 = cast(item2, (Class<Object>)itemType, mapping);
                        }
                    }
                    else {
                        itemValue2 = cast(item2, itemType, mapping);
                    }
                    collection.add(itemValue2);
                }
                return (T)collection;
            }
        }
        if (rawTye == Map.class || rawTye == HashMap.class) {
            final Type keyType = type.getActualTypeArguments()[0];
            final Type valueType = type.getActualTypeArguments()[1];
            if (obj instanceof Map) {
                final Map map = new HashMap();
                for (final Map.Entry entry : ((Map)obj).entrySet()) {
                    final Object key = cast(entry.getKey(), keyType, mapping);
                    final Object value = cast(entry.getValue(), valueType, mapping);
                    map.put(key, value);
                }
                return (T)map;
            }
        }
        if (obj instanceof String) {
            final String strVal = (String)obj;
            if (strVal.length() == 0) {
                return null;
            }
        }
        final Type[] actualTypeArguments = type.getActualTypeArguments();
        if (actualTypeArguments.length == 1) {
            final Type argType = type.getActualTypeArguments()[0];
            if (argType instanceof WildcardType) {
                return cast(obj, rawTye, mapping);
            }
        }
        if (rawTye == Map.Entry.class && obj instanceof Map && ((Map)obj).size() == 1) {
            final Map.Entry entry2 = (Map.Entry)((Map)obj).entrySet().iterator().next();
            final Object entryValue = entry2.getValue();
            if (actualTypeArguments.length == 2 && entryValue instanceof Map) {
                final Type valueType2 = actualTypeArguments[1];
                entry2.setValue(cast(entryValue, valueType2, mapping));
            }
            return (T)entry2;
        }
        if (rawTye instanceof Class) {
            if (mapping == null) {
                mapping = ParserConfig.global;
            }
            final ObjectDeserializer deserializer = mapping.getDeserializer(rawTye);
            if (deserializer != null) {
                final String str = JSON.toJSONString(obj);
                final DefaultJSONParser parser = new DefaultJSONParser(str, mapping);
                return deserializer.deserialze(parser, type, null);
            }
        }
        throw new JSONException("can not cast to : " + type);
    }
    
    public static <T> T castToJavaBean(final Map<String, Object> map, final Class<T> clazz, ParserConfig config) {
        try {
            if (clazz == StackTraceElement.class) {
                final String declaringClass = map.get("className");
                final String methodName = map.get("methodName");
                final String fileName = map.get("fileName");
                final Number value = map.get("lineNumber");
                int lineNumber;
                if (value == null) {
                    lineNumber = 0;
                }
                else if (value instanceof BigDecimal) {
                    lineNumber = ((BigDecimal)value).intValueExact();
                }
                else {
                    lineNumber = value.intValue();
                }
                return (T)new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
            }
            final Object iClassObject = map.get(JSON.DEFAULT_TYPE_KEY);
            if (iClassObject instanceof String) {
                final String className = (String)iClassObject;
                if (config == null) {
                    config = ParserConfig.global;
                }
                final Class<?> loadClazz = config.checkAutoType(className, null);
                if (loadClazz == null) {
                    throw new ClassNotFoundException(className + " not found");
                }
                if (!loadClazz.equals(clazz)) {
                    return (T)castToJavaBean(map, loadClazz, config);
                }
            }
            if (clazz.isInterface()) {
                JSONObject object;
                if (map instanceof JSONObject) {
                    object = (JSONObject)map;
                }
                else {
                    object = new JSONObject(map);
                }
                if (config == null) {
                    config = ParserConfig.getGlobalInstance();
                }
                final ObjectDeserializer deserializer = config.get(clazz);
                if (deserializer != null) {
                    final String json = JSON.toJSONString(object);
                    return JSON.parseObject(json, clazz);
                }
                return (T)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { clazz }, object);
            }
            else {
                if (clazz == Locale.class) {
                    final Object arg0 = map.get("language");
                    final Object arg2 = map.get("country");
                    if (arg0 instanceof String) {
                        final String language = (String)arg0;
                        if (arg2 instanceof String) {
                            final String country = (String)arg2;
                            return (T)new Locale(language, country);
                        }
                        if (arg2 == null) {
                            return (T)new Locale(language);
                        }
                    }
                }
                if (clazz == String.class && map instanceof JSONObject) {
                    return (T)map.toString();
                }
                if (clazz == JSON.class && map instanceof JSONObject) {
                    return (T)map;
                }
                if (clazz == LinkedHashMap.class && map instanceof JSONObject) {
                    final JSONObject jsonObject = (JSONObject)map;
                    final Map innerMap = jsonObject.getInnerMap();
                    if (innerMap instanceof LinkedHashMap) {
                        return (T)innerMap;
                    }
                    final LinkedHashMap linkedHashMap = new LinkedHashMap();
                    linkedHashMap.putAll(innerMap);
                }
                if (clazz.isInstance(map)) {
                    return (T)map;
                }
                if (clazz == JSONObject.class) {
                    return (T)new JSONObject(map);
                }
                if (config == null) {
                    config = ParserConfig.getGlobalInstance();
                }
                JavaBeanDeserializer javaBeanDeser = null;
                final ObjectDeserializer deserializer = config.getDeserializer(clazz);
                if (deserializer instanceof JavaBeanDeserializer) {
                    javaBeanDeser = (JavaBeanDeserializer)deserializer;
                }
                if (javaBeanDeser == null) {
                    throw new JSONException("can not get javaBeanDeserializer. " + clazz.getName());
                }
                return (T)javaBeanDeser.createInstance(map, config);
            }
        }
        catch (Exception e) {
            throw new JSONException(e.getMessage(), e);
        }
    }
    
    private static void addBaseClassMappings() {
        TypeUtils.mappings.put("byte", Byte.TYPE);
        TypeUtils.mappings.put("short", Short.TYPE);
        TypeUtils.mappings.put("int", Integer.TYPE);
        TypeUtils.mappings.put("long", Long.TYPE);
        TypeUtils.mappings.put("float", Float.TYPE);
        TypeUtils.mappings.put("double", Double.TYPE);
        TypeUtils.mappings.put("boolean", Boolean.TYPE);
        TypeUtils.mappings.put("char", Character.TYPE);
        TypeUtils.mappings.put("[byte", byte[].class);
        TypeUtils.mappings.put("[short", short[].class);
        TypeUtils.mappings.put("[int", int[].class);
        TypeUtils.mappings.put("[long", long[].class);
        TypeUtils.mappings.put("[float", float[].class);
        TypeUtils.mappings.put("[double", double[].class);
        TypeUtils.mappings.put("[boolean", boolean[].class);
        TypeUtils.mappings.put("[char", char[].class);
        TypeUtils.mappings.put("[B", byte[].class);
        TypeUtils.mappings.put("[S", short[].class);
        TypeUtils.mappings.put("[I", int[].class);
        TypeUtils.mappings.put("[J", long[].class);
        TypeUtils.mappings.put("[F", float[].class);
        TypeUtils.mappings.put("[D", double[].class);
        TypeUtils.mappings.put("[C", char[].class);
        TypeUtils.mappings.put("[Z", boolean[].class);
        final Class[] array;
        final Class<?>[] classes = (Class<?>[])(array = new Class[] { Object.class, Cloneable.class, loadClass("java.lang.AutoCloseable"), Exception.class, RuntimeException.class, IllegalAccessError.class, IllegalAccessException.class, IllegalArgumentException.class, IllegalMonitorStateException.class, IllegalStateException.class, IllegalThreadStateException.class, IndexOutOfBoundsException.class, InstantiationError.class, InstantiationException.class, InternalError.class, InterruptedException.class, LinkageError.class, NegativeArraySizeException.class, NoClassDefFoundError.class, NoSuchFieldError.class, NoSuchFieldException.class, NoSuchMethodError.class, NoSuchMethodException.class, NullPointerException.class, NumberFormatException.class, OutOfMemoryError.class, SecurityException.class, StackOverflowError.class, StringIndexOutOfBoundsException.class, TypeNotPresentException.class, VerifyError.class, StackTraceElement.class, HashMap.class, Hashtable.class, TreeMap.class, IdentityHashMap.class, WeakHashMap.class, LinkedHashMap.class, HashSet.class, LinkedHashSet.class, TreeSet.class, ArrayList.class, TimeUnit.class, ConcurrentHashMap.class, AtomicInteger.class, AtomicLong.class, Collections.EMPTY_MAP.getClass(), Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Number.class, String.class, BigDecimal.class, BigInteger.class, BitSet.class, Calendar.class, Date.class, Locale.class, UUID.class, Time.class, java.sql.Date.class, Timestamp.class, SimpleDateFormat.class, JSONObject.class, JSONPObject.class, JSONArray.class });
        for (final Class clazz : array) {
            if (clazz != null) {
                TypeUtils.mappings.put(clazz.getName(), clazz);
            }
        }
    }
    
    public static void clearClassMapping() {
        TypeUtils.mappings.clear();
        addBaseClassMappings();
    }
    
    public static void addMapping(final String className, final Class<?> clazz) {
        TypeUtils.mappings.put(className, clazz);
    }
    
    public static Class<?> loadClass(final String className) {
        return loadClass(className, null);
    }
    
    public static boolean isPath(final Class<?> clazz) {
        if (TypeUtils.pathClass == null && !TypeUtils.pathClass_error) {
            try {
                TypeUtils.pathClass = Class.forName("java.nio.file.Path");
            }
            catch (Throwable ex) {
                TypeUtils.pathClass_error = true;
            }
        }
        return TypeUtils.pathClass != null && TypeUtils.pathClass.isAssignableFrom(clazz);
    }
    
    public static Class<?> getClassFromMapping(final String className) {
        return TypeUtils.mappings.get(className);
    }
    
    public static Class<?> loadClass(final String className, final ClassLoader classLoader) {
        return loadClass(className, classLoader, false);
    }
    
    public static Class<?> loadClass(final String className, final ClassLoader classLoader, final boolean cache) {
        if (className == null || className.length() == 0 || className.length() > 128) {
            return null;
        }
        Class<?> clazz = TypeUtils.mappings.get(className);
        if (clazz != null) {
            return clazz;
        }
        if (className.charAt(0) == '[') {
            final Class<?> componentType = loadClass(className.substring(1), classLoader);
            return Array.newInstance(componentType, 0).getClass();
        }
        if (className.startsWith("L") && className.endsWith(";")) {
            final String newClassName = className.substring(1, className.length() - 1);
            return loadClass(newClassName, classLoader);
        }
        try {
            if (classLoader != null) {
                clazz = classLoader.loadClass(className);
                if (cache) {
                    TypeUtils.mappings.put(className, clazz);
                }
                return clazz;
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader != null && contextClassLoader != classLoader) {
                clazz = contextClassLoader.loadClass(className);
                if (cache) {
                    TypeUtils.mappings.put(className, clazz);
                }
                return clazz;
            }
        }
        catch (Throwable t) {}
        try {
            clazz = Class.forName(className);
            if (cache) {
                TypeUtils.mappings.put(className, clazz);
            }
            return clazz;
        }
        catch (Throwable t2) {
            return clazz;
        }
    }
    
    public static SerializeBeanInfo buildBeanInfo(final Class<?> beanType, final Map<String, String> aliasMap, final PropertyNamingStrategy propertyNamingStrategy) {
        return buildBeanInfo(beanType, aliasMap, propertyNamingStrategy, false);
    }
    
    public static SerializeBeanInfo buildBeanInfo(final Class<?> beanType, final Map<String, String> aliasMap, PropertyNamingStrategy propertyNamingStrategy, final boolean fieldBased) {
        final JSONType jsonType = getAnnotation(beanType, JSONType.class);
        String[] orders = null;
        String typeName = null;
        String typeKey = null;
        int features;
        if (jsonType != null) {
            orders = jsonType.orders();
            typeName = jsonType.typeName();
            if (typeName.length() == 0) {
                typeName = null;
            }
            final PropertyNamingStrategy jsonTypeNaming = jsonType.naming();
            if (jsonTypeNaming != PropertyNamingStrategy.CamelCase) {
                propertyNamingStrategy = jsonTypeNaming;
            }
            features = SerializerFeature.of(jsonType.serialzeFeatures());
            for (Class<?> supperClass = beanType.getSuperclass(); supperClass != null && supperClass != Object.class; supperClass = supperClass.getSuperclass()) {
                final JSONType superJsonType = getAnnotation(supperClass, JSONType.class);
                if (superJsonType == null) {
                    break;
                }
                typeKey = superJsonType.typeKey();
                if (typeKey.length() != 0) {
                    break;
                }
            }
            for (final Class<?> interfaceClass : beanType.getInterfaces()) {
                final JSONType superJsonType2 = getAnnotation(interfaceClass, JSONType.class);
                if (superJsonType2 != null) {
                    typeKey = superJsonType2.typeKey();
                    if (typeKey.length() != 0) {
                        break;
                    }
                }
            }
            if (typeKey != null && typeKey.length() == 0) {
                typeKey = null;
            }
        }
        else {
            features = 0;
        }
        final Map<String, Field> fieldCacheMap = new HashMap<String, Field>();
        ParserConfig.parserAllFieldToCache(beanType, fieldCacheMap);
        final List<FieldInfo> fieldInfoList = fieldBased ? computeGettersWithFieldBase(beanType, aliasMap, false, propertyNamingStrategy) : computeGetters(beanType, jsonType, aliasMap, fieldCacheMap, false, propertyNamingStrategy);
        final FieldInfo[] fields = new FieldInfo[fieldInfoList.size()];
        fieldInfoList.toArray(fields);
        List<FieldInfo> sortedFieldList;
        if (orders != null && orders.length != 0) {
            sortedFieldList = (fieldBased ? computeGettersWithFieldBase(beanType, aliasMap, true, propertyNamingStrategy) : computeGetters(beanType, jsonType, aliasMap, fieldCacheMap, true, propertyNamingStrategy));
        }
        else {
            sortedFieldList = new ArrayList<FieldInfo>(fieldInfoList);
            Collections.sort(sortedFieldList);
        }
        FieldInfo[] sortedFields = new FieldInfo[sortedFieldList.size()];
        sortedFieldList.toArray(sortedFields);
        if (Arrays.equals(sortedFields, fields)) {
            sortedFields = fields;
        }
        return new SerializeBeanInfo(beanType, jsonType, typeName, typeKey, features, fields, sortedFields);
    }
    
    public static List<FieldInfo> computeGettersWithFieldBase(final Class<?> clazz, final Map<String, String> aliasMap, final boolean sorted, final PropertyNamingStrategy propertyNamingStrategy) {
        final Map<String, FieldInfo> fieldInfoMap = new LinkedHashMap<String, FieldInfo>();
        for (Class<?> currentClass = clazz; currentClass != null; currentClass = currentClass.getSuperclass()) {
            final Field[] fields = currentClass.getDeclaredFields();
            computeFields(currentClass, aliasMap, propertyNamingStrategy, fieldInfoMap, fields);
        }
        return getFieldInfos(clazz, sorted, fieldInfoMap);
    }
    
    public static List<FieldInfo> computeGetters(final Class<?> clazz, final Map<String, String> aliasMap) {
        return computeGetters(clazz, aliasMap, true);
    }
    
    public static List<FieldInfo> computeGetters(final Class<?> clazz, final Map<String, String> aliasMap, final boolean sorted) {
        final JSONType jsonType = getAnnotation(clazz, JSONType.class);
        final Map<String, Field> fieldCacheMap = new HashMap<String, Field>();
        ParserConfig.parserAllFieldToCache(clazz, fieldCacheMap);
        return computeGetters(clazz, jsonType, aliasMap, fieldCacheMap, sorted, PropertyNamingStrategy.CamelCase);
    }
    
    public static List<FieldInfo> computeGetters(final Class<?> clazz, final JSONType jsonType, final Map<String, String> aliasMap, final Map<String, Field> fieldCacheMap, final boolean sorted, final PropertyNamingStrategy propertyNamingStrategy) {
        final Map<String, FieldInfo> fieldInfoMap = new LinkedHashMap<String, FieldInfo>();
        final boolean kotlin = isKotlin(clazz);
        Constructor[] constructors = null;
        Annotation[][] paramAnnotationArrays = null;
        String[] paramNames = null;
        short[] paramNameMapping = null;
        final Method[] methods2;
        final Method[] methods = methods2 = clazz.getMethods();
        for (final Method method : methods2) {
            final String methodName = method.getName();
            int ordinal = 0;
            int serialzeFeatures = 0;
            int parserFeatures = 0;
            String label = null;
            Label_1850: {
                if (!Modifier.isStatic(method.getModifiers())) {
                    if (!method.getReturnType().equals(Void.TYPE)) {
                        if (method.getParameterTypes().length == 0) {
                            if (method.getReturnType() != ClassLoader.class) {
                                if (!methodName.equals("getMetaClass") || !method.getReturnType().getName().equals("groovy.lang.MetaClass")) {
                                    if (!methodName.equals("getSuppressed") || method.getDeclaringClass() != Throwable.class) {
                                        if (!kotlin || !isKotlinIgnore(clazz, methodName)) {
                                            Boolean fieldAnnotationAndNameExists = false;
                                            JSONField annotation = getAnnotation(method, JSONField.class);
                                            if (annotation == null) {
                                                annotation = getSuperMethodAnnotation(clazz, method);
                                            }
                                            if (annotation == null && kotlin) {
                                                if (constructors == null) {
                                                    constructors = clazz.getDeclaredConstructors();
                                                    final Constructor creatorConstructor = getKoltinConstructor(constructors);
                                                    if (creatorConstructor != null) {
                                                        paramAnnotationArrays = getParameterAnnotations(creatorConstructor);
                                                        paramNames = getKoltinConstructorParameters(clazz);
                                                        if (paramNames != null) {
                                                            final String[] paramNames_sorted = new String[paramNames.length];
                                                            System.arraycopy(paramNames, 0, paramNames_sorted, 0, paramNames.length);
                                                            Arrays.sort(paramNames_sorted);
                                                            paramNameMapping = new short[paramNames.length];
                                                            for (short p = 0; p < paramNames.length; ++p) {
                                                                final int index = Arrays.binarySearch(paramNames_sorted, paramNames[p]);
                                                                paramNameMapping[index] = p;
                                                            }
                                                            paramNames = paramNames_sorted;
                                                        }
                                                    }
                                                }
                                                if (paramNames != null && paramNameMapping != null && methodName.startsWith("get")) {
                                                    final String propertyName = decapitalize(methodName.substring(3));
                                                    int p2 = Arrays.binarySearch(paramNames, propertyName);
                                                    if (p2 < 0) {
                                                        for (int i = 0; i < paramNames.length; ++i) {
                                                            if (propertyName.equalsIgnoreCase(paramNames[i])) {
                                                                p2 = i;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if (p2 >= 0) {
                                                        final short index2 = paramNameMapping[p2];
                                                        final Annotation[] paramAnnotations = paramAnnotationArrays[index2];
                                                        if (paramAnnotations != null) {
                                                            for (final Annotation paramAnnotation : paramAnnotations) {
                                                                if (paramAnnotation instanceof JSONField) {
                                                                    annotation = (JSONField)paramAnnotation;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                        if (annotation == null) {
                                                            final Field field = ParserConfig.getFieldFromCache(propertyName, fieldCacheMap);
                                                            if (field != null) {
                                                                annotation = getAnnotation(field, JSONField.class);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (annotation != null) {
                                                if (!annotation.serialize()) {
                                                    break Label_1850;
                                                }
                                                ordinal = annotation.ordinal();
                                                serialzeFeatures = SerializerFeature.of(annotation.serialzeFeatures());
                                                parserFeatures = Feature.of(annotation.parseFeatures());
                                                if (annotation.name().length() != 0) {
                                                    String propertyName = annotation.name();
                                                    if (aliasMap != null) {
                                                        propertyName = aliasMap.get(propertyName);
                                                        if (propertyName == null) {
                                                            break Label_1850;
                                                        }
                                                    }
                                                    final FieldInfo fieldInfo = new FieldInfo(propertyName, method, null, clazz, null, ordinal, serialzeFeatures, parserFeatures, annotation, null, label);
                                                    fieldInfoMap.put(propertyName, fieldInfo);
                                                    break Label_1850;
                                                }
                                                if (annotation.label().length() != 0) {
                                                    label = annotation.label();
                                                }
                                            }
                                            if (methodName.startsWith("get")) {
                                                if (methodName.length() < 4) {
                                                    break Label_1850;
                                                }
                                                if (methodName.equals("getClass")) {
                                                    break Label_1850;
                                                }
                                                if (methodName.equals("getDeclaringClass") && clazz.isEnum()) {
                                                    break Label_1850;
                                                }
                                                final char c3 = methodName.charAt(3);
                                                Field field2 = null;
                                                String propertyName2;
                                                if (Character.isUpperCase(c3) || c3 > '\u0200') {
                                                    if (TypeUtils.compatibleWithJavaBean) {
                                                        propertyName2 = decapitalize(methodName.substring(3));
                                                    }
                                                    else {
                                                        propertyName2 = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                                                    }
                                                    propertyName2 = getPropertyNameByCompatibleFieldName(fieldCacheMap, methodName, propertyName2, 3);
                                                }
                                                else if (c3 == '_') {
                                                    propertyName2 = methodName.substring(4);
                                                    field2 = fieldCacheMap.get(propertyName2);
                                                    if (field2 == null) {
                                                        final String temp = propertyName2;
                                                        propertyName2 = methodName.substring(3);
                                                        field2 = ParserConfig.getFieldFromCache(propertyName2, fieldCacheMap);
                                                        if (field2 == null) {
                                                            propertyName2 = temp;
                                                        }
                                                    }
                                                }
                                                else if (c3 == 'f') {
                                                    propertyName2 = methodName.substring(3);
                                                }
                                                else if (methodName.length() >= 5 && Character.isUpperCase(methodName.charAt(4))) {
                                                    propertyName2 = decapitalize(methodName.substring(3));
                                                }
                                                else {
                                                    propertyName2 = methodName.substring(3);
                                                    field2 = ParserConfig.getFieldFromCache(propertyName2, fieldCacheMap);
                                                    if (field2 == null) {
                                                        break Label_1850;
                                                    }
                                                }
                                                final boolean ignore = isJSONTypeIgnore(clazz, propertyName2);
                                                if (ignore) {
                                                    break Label_1850;
                                                }
                                                if (field2 == null) {
                                                    field2 = ParserConfig.getFieldFromCache(propertyName2, fieldCacheMap);
                                                }
                                                if (field2 == null && propertyName2.length() > 1) {
                                                    final char ch = propertyName2.charAt(1);
                                                    if (ch >= 'A' && ch <= 'Z') {
                                                        final String javaBeanCompatiblePropertyName = decapitalize(methodName.substring(3));
                                                        field2 = ParserConfig.getFieldFromCache(javaBeanCompatiblePropertyName, fieldCacheMap);
                                                    }
                                                }
                                                JSONField fieldAnnotation = null;
                                                if (field2 != null) {
                                                    fieldAnnotation = getAnnotation(field2, JSONField.class);
                                                    if (fieldAnnotation != null) {
                                                        if (!fieldAnnotation.serialize()) {
                                                            break Label_1850;
                                                        }
                                                        ordinal = fieldAnnotation.ordinal();
                                                        serialzeFeatures = SerializerFeature.of(fieldAnnotation.serialzeFeatures());
                                                        parserFeatures = Feature.of(fieldAnnotation.parseFeatures());
                                                        if (fieldAnnotation.name().length() != 0) {
                                                            fieldAnnotationAndNameExists = true;
                                                            propertyName2 = fieldAnnotation.name();
                                                            if (aliasMap != null) {
                                                                propertyName2 = aliasMap.get(propertyName2);
                                                                if (propertyName2 == null) {
                                                                    break Label_1850;
                                                                }
                                                            }
                                                        }
                                                        if (fieldAnnotation.label().length() != 0) {
                                                            label = fieldAnnotation.label();
                                                        }
                                                    }
                                                }
                                                if (aliasMap != null) {
                                                    propertyName2 = aliasMap.get(propertyName2);
                                                    if (propertyName2 == null) {
                                                        break Label_1850;
                                                    }
                                                }
                                                if (propertyNamingStrategy != null && !fieldAnnotationAndNameExists) {
                                                    propertyName2 = propertyNamingStrategy.translate(propertyName2);
                                                }
                                                final FieldInfo fieldInfo2 = new FieldInfo(propertyName2, method, field2, clazz, null, ordinal, serialzeFeatures, parserFeatures, annotation, fieldAnnotation, label);
                                                fieldInfoMap.put(propertyName2, fieldInfo2);
                                            }
                                            if (methodName.startsWith("is")) {
                                                if (methodName.length() >= 3) {
                                                    if (method.getReturnType() == Boolean.TYPE || method.getReturnType() == Boolean.class) {
                                                        final char c4 = methodName.charAt(2);
                                                        Field field2 = null;
                                                        String propertyName2;
                                                        if (Character.isUpperCase(c4)) {
                                                            if (TypeUtils.compatibleWithJavaBean) {
                                                                propertyName2 = decapitalize(methodName.substring(2));
                                                            }
                                                            else {
                                                                propertyName2 = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
                                                            }
                                                            propertyName2 = getPropertyNameByCompatibleFieldName(fieldCacheMap, methodName, propertyName2, 2);
                                                        }
                                                        else if (c4 == '_') {
                                                            propertyName2 = methodName.substring(3);
                                                            field2 = fieldCacheMap.get(propertyName2);
                                                            if (field2 == null) {
                                                                final String temp = propertyName2;
                                                                propertyName2 = methodName.substring(2);
                                                                field2 = ParserConfig.getFieldFromCache(propertyName2, fieldCacheMap);
                                                                if (field2 == null) {
                                                                    propertyName2 = temp;
                                                                }
                                                            }
                                                        }
                                                        else if (c4 == 'f') {
                                                            propertyName2 = methodName.substring(2);
                                                        }
                                                        else {
                                                            propertyName2 = methodName.substring(2);
                                                            field2 = ParserConfig.getFieldFromCache(propertyName2, fieldCacheMap);
                                                            if (field2 == null) {
                                                                break Label_1850;
                                                            }
                                                        }
                                                        final boolean ignore = isJSONTypeIgnore(clazz, propertyName2);
                                                        if (!ignore) {
                                                            if (field2 == null) {
                                                                field2 = ParserConfig.getFieldFromCache(propertyName2, fieldCacheMap);
                                                            }
                                                            if (field2 == null) {
                                                                field2 = ParserConfig.getFieldFromCache(methodName, fieldCacheMap);
                                                            }
                                                            JSONField fieldAnnotation = null;
                                                            if (field2 != null) {
                                                                fieldAnnotation = getAnnotation(field2, JSONField.class);
                                                                if (fieldAnnotation != null) {
                                                                    if (!fieldAnnotation.serialize()) {
                                                                        break Label_1850;
                                                                    }
                                                                    ordinal = fieldAnnotation.ordinal();
                                                                    serialzeFeatures = SerializerFeature.of(fieldAnnotation.serialzeFeatures());
                                                                    parserFeatures = Feature.of(fieldAnnotation.parseFeatures());
                                                                    if (fieldAnnotation.name().length() != 0) {
                                                                        propertyName2 = fieldAnnotation.name();
                                                                        if (aliasMap != null) {
                                                                            propertyName2 = aliasMap.get(propertyName2);
                                                                            if (propertyName2 == null) {
                                                                                break Label_1850;
                                                                            }
                                                                        }
                                                                    }
                                                                    if (fieldAnnotation.label().length() != 0) {
                                                                        label = fieldAnnotation.label();
                                                                    }
                                                                }
                                                            }
                                                            if (aliasMap != null) {
                                                                propertyName2 = aliasMap.get(propertyName2);
                                                                if (propertyName2 == null) {
                                                                    break Label_1850;
                                                                }
                                                            }
                                                            if (propertyNamingStrategy != null) {
                                                                propertyName2 = propertyNamingStrategy.translate(propertyName2);
                                                            }
                                                            if (!fieldInfoMap.containsKey(propertyName2)) {
                                                                final FieldInfo fieldInfo2 = new FieldInfo(propertyName2, method, field2, clazz, null, ordinal, serialzeFeatures, parserFeatures, annotation, fieldAnnotation, label);
                                                                fieldInfoMap.put(propertyName2, fieldInfo2);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        final Field[] fields = clazz.getFields();
        computeFields(clazz, aliasMap, propertyNamingStrategy, fieldInfoMap, fields);
        return getFieldInfos(clazz, sorted, fieldInfoMap);
    }
    
    private static List<FieldInfo> getFieldInfos(final Class<?> clazz, final boolean sorted, final Map<String, FieldInfo> fieldInfoMap) {
        final List<FieldInfo> fieldInfoList = new ArrayList<FieldInfo>();
        String[] orders = null;
        final JSONType annotation = getAnnotation(clazz, JSONType.class);
        if (annotation != null) {
            orders = annotation.orders();
        }
        if (orders != null && orders.length > 0) {
            final LinkedHashMap<String, FieldInfo> map = new LinkedHashMap<String, FieldInfo>(fieldInfoList.size());
            for (final FieldInfo field : fieldInfoMap.values()) {
                map.put(field.name, field);
            }
            final int i = 0;
            for (final String item : orders) {
                final FieldInfo field2 = map.get(item);
                if (field2 != null) {
                    fieldInfoList.add(field2);
                    map.remove(item);
                }
            }
            for (final FieldInfo field3 : map.values()) {
                fieldInfoList.add(field3);
            }
        }
        else {
            for (final FieldInfo fieldInfo : fieldInfoMap.values()) {
                fieldInfoList.add(fieldInfo);
            }
            if (sorted) {
                Collections.sort(fieldInfoList);
            }
        }
        return fieldInfoList;
    }
    
    private static void computeFields(final Class<?> clazz, final Map<String, String> aliasMap, final PropertyNamingStrategy propertyNamingStrategy, final Map<String, FieldInfo> fieldInfoMap, final Field[] fields) {
        for (final Field field : fields) {
            Label_0253: {
                if (!Modifier.isStatic(field.getModifiers())) {
                    final JSONField fieldAnnotation = getAnnotation(field, JSONField.class);
                    int ordinal = 0;
                    int serialzeFeatures = 0;
                    int parserFeatures = 0;
                    String propertyName = field.getName();
                    String label = null;
                    if (fieldAnnotation != null) {
                        if (!fieldAnnotation.serialize()) {
                            break Label_0253;
                        }
                        ordinal = fieldAnnotation.ordinal();
                        serialzeFeatures = SerializerFeature.of(fieldAnnotation.serialzeFeatures());
                        parserFeatures = Feature.of(fieldAnnotation.parseFeatures());
                        if (fieldAnnotation.name().length() != 0) {
                            propertyName = fieldAnnotation.name();
                        }
                        if (fieldAnnotation.label().length() != 0) {
                            label = fieldAnnotation.label();
                        }
                    }
                    if (aliasMap != null) {
                        propertyName = aliasMap.get(propertyName);
                        if (propertyName == null) {
                            break Label_0253;
                        }
                    }
                    if (propertyNamingStrategy != null) {
                        propertyName = propertyNamingStrategy.translate(propertyName);
                    }
                    if (!fieldInfoMap.containsKey(propertyName)) {
                        final FieldInfo fieldInfo = new FieldInfo(propertyName, null, field, clazz, null, ordinal, serialzeFeatures, parserFeatures, null, fieldAnnotation, label);
                        fieldInfoMap.put(propertyName, fieldInfo);
                    }
                }
            }
        }
    }
    
    private static String getPropertyNameByCompatibleFieldName(final Map<String, Field> fieldCacheMap, final String methodName, final String propertyName, final int fromIdx) {
        if (TypeUtils.compatibleWithFieldName && !fieldCacheMap.containsKey(propertyName)) {
            final String tempPropertyName = methodName.substring(fromIdx);
            return fieldCacheMap.containsKey(tempPropertyName) ? tempPropertyName : propertyName;
        }
        return propertyName;
    }
    
    public static JSONField getSuperMethodAnnotation(final Class<?> clazz, final Method method) {
        final Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            final Class<?>[] types = method.getParameterTypes();
            for (final Class<?> interfaceClass : interfaces) {
                for (final Method interfaceMethod : interfaceClass.getMethods()) {
                    final Class<?>[] interfaceTypes = interfaceMethod.getParameterTypes();
                    if (interfaceTypes.length == types.length) {
                        if (interfaceMethod.getName().equals(method.getName())) {
                            boolean match = true;
                            for (int i = 0; i < types.length; ++i) {
                                if (!interfaceTypes[i].equals(types[i])) {
                                    match = false;
                                    break;
                                }
                            }
                            if (match) {
                                final JSONField annotation = getAnnotation(interfaceMethod, JSONField.class);
                                if (annotation != null) {
                                    return annotation;
                                }
                            }
                        }
                    }
                }
            }
        }
        final Class<?> superClass = clazz.getSuperclass();
        if (superClass == null) {
            return null;
        }
        if (Modifier.isAbstract(superClass.getModifiers())) {
            final Class<?>[] types2 = method.getParameterTypes();
            for (final Method interfaceMethod2 : superClass.getMethods()) {
                final Class<?>[] interfaceTypes2 = interfaceMethod2.getParameterTypes();
                if (interfaceTypes2.length == types2.length) {
                    if (interfaceMethod2.getName().equals(method.getName())) {
                        boolean match2 = true;
                        for (int j = 0; j < types2.length; ++j) {
                            if (!interfaceTypes2[j].equals(types2[j])) {
                                match2 = false;
                                break;
                            }
                        }
                        if (match2) {
                            final JSONField annotation2 = getAnnotation(interfaceMethod2, JSONField.class);
                            if (annotation2 != null) {
                                return annotation2;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private static boolean isJSONTypeIgnore(final Class<?> clazz, final String propertyName) {
        final JSONType jsonType = getAnnotation(clazz, JSONType.class);
        if (jsonType != null) {
            String[] fields = jsonType.includes();
            if (fields.length > 0) {
                for (int i = 0; i < fields.length; ++i) {
                    if (propertyName.equals(fields[i])) {
                        return false;
                    }
                }
                return true;
            }
            fields = jsonType.ignores();
            for (int i = 0; i < fields.length; ++i) {
                if (propertyName.equals(fields[i])) {
                    return true;
                }
            }
        }
        return clazz.getSuperclass() != Object.class && clazz.getSuperclass() != null && isJSONTypeIgnore(clazz.getSuperclass(), propertyName);
    }
    
    public static boolean isGenericParamType(final Type type) {
        if (type instanceof ParameterizedType) {
            return true;
        }
        if (type instanceof Class) {
            final Type superType = ((Class)type).getGenericSuperclass();
            return superType != Object.class && isGenericParamType(superType);
        }
        return false;
    }
    
    public static Type getGenericParamType(final Type type) {
        if (type instanceof ParameterizedType) {
            return type;
        }
        if (type instanceof Class) {
            return getGenericParamType(((Class)type).getGenericSuperclass());
        }
        return type;
    }
    
    public static Type unwrapOptional(final Type type) {
        if (!TypeUtils.optionalClassInited) {
            try {
                TypeUtils.optionalClass = Class.forName("java.util.Optional");
            }
            catch (Exception ex) {}
            finally {
                TypeUtils.optionalClassInited = true;
            }
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType)type;
            if (parameterizedType.getRawType() == TypeUtils.optionalClass) {
                return parameterizedType.getActualTypeArguments()[0];
            }
        }
        return type;
    }
    
    public static Class<?> getClass(final Type type) {
        if (type.getClass() == Class.class) {
            return (Class<?>)type;
        }
        if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType)type).getRawType());
        }
        if (!(type instanceof TypeVariable)) {
            if (type instanceof WildcardType) {
                final Type[] upperBounds = ((WildcardType)type).getUpperBounds();
                if (upperBounds.length == 1) {
                    return getClass(upperBounds[0]);
                }
            }
            return Object.class;
        }
        final Type boundType = ((TypeVariable)type).getBounds()[0];
        if (boundType instanceof Class) {
            return (Class<?>)boundType;
        }
        return getClass(boundType);
    }
    
    public static Field getField(final Class<?> clazz, final String fieldName, final Field[] declaredFields) {
        for (final Field field : declaredFields) {
            final String itemName = field.getName();
            if (fieldName.equals(itemName)) {
                return field;
            }
            final char c0;
            final char c2;
            if (fieldName.length() > 2 && (c0 = fieldName.charAt(0)) >= 'a' && c0 <= 'z' && (c2 = fieldName.charAt(1)) >= 'A' && c2 <= 'Z' && fieldName.equalsIgnoreCase(itemName)) {
                return field;
            }
        }
        final Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            return getField(superClass, fieldName, superClass.getDeclaredFields());
        }
        return null;
    }
    
    @Deprecated
    public static int getSerializeFeatures(final Class<?> clazz) {
        final JSONType annotation = getAnnotation(clazz, JSONType.class);
        if (annotation == null) {
            return 0;
        }
        return SerializerFeature.of(annotation.serialzeFeatures());
    }
    
    public static int getParserFeatures(final Class<?> clazz) {
        final JSONType annotation = getAnnotation(clazz, JSONType.class);
        if (annotation == null) {
            return 0;
        }
        return Feature.of(annotation.parseFeatures());
    }
    
    public static String decapitalize(final String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {
            return name;
        }
        final char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
    
    static void setAccessible(final AccessibleObject obj) {
        if (!TypeUtils.setAccessibleEnable) {
            return;
        }
        if (obj.isAccessible()) {
            return;
        }
        try {
            obj.setAccessible(true);
        }
        catch (AccessControlException error) {
            TypeUtils.setAccessibleEnable = false;
        }
    }
    
    public static Type getCollectionItemType(final Type fieldType) {
        if (fieldType instanceof ParameterizedType) {
            return getCollectionItemType((ParameterizedType)fieldType);
        }
        if (fieldType instanceof Class) {
            return getCollectionItemType((Class<?>)fieldType);
        }
        return Object.class;
    }
    
    private static Type getCollectionItemType(final Class<?> clazz) {
        return clazz.getName().startsWith("java.") ? Object.class : getCollectionItemType(getCollectionSuperType(clazz));
    }
    
    private static Type getCollectionItemType(final ParameterizedType parameterizedType) {
        final Type rawType = parameterizedType.getRawType();
        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (rawType == Collection.class) {
            return getWildcardTypeUpperBounds(actualTypeArguments[0]);
        }
        final Class<?> rawClass = (Class<?>)rawType;
        final Map<TypeVariable, Type> actualTypeMap = createActualTypeMap(rawClass.getTypeParameters(), actualTypeArguments);
        final Type superType = getCollectionSuperType(rawClass);
        if (superType instanceof ParameterizedType) {
            final Class<?> superClass = getRawClass(superType);
            final Type[] superClassTypeParameters = ((ParameterizedType)superType).getActualTypeArguments();
            return (superClassTypeParameters.length > 0) ? getCollectionItemType(makeParameterizedType(superClass, superClassTypeParameters, actualTypeMap)) : getCollectionItemType(superClass);
        }
        return getCollectionItemType((Class<?>)superType);
    }
    
    private static Type getCollectionSuperType(final Class<?> clazz) {
        Type assignable = null;
        for (final Type type : clazz.getGenericInterfaces()) {
            final Class<?> rawClass = getRawClass(type);
            if (rawClass == Collection.class) {
                return type;
            }
            if (Collection.class.isAssignableFrom(rawClass)) {
                assignable = type;
            }
        }
        return (assignable == null) ? clazz.getGenericSuperclass() : assignable;
    }
    
    private static Map<TypeVariable, Type> createActualTypeMap(final TypeVariable[] typeParameters, final Type[] actualTypeArguments) {
        final int length = typeParameters.length;
        final Map<TypeVariable, Type> actualTypeMap = new HashMap<TypeVariable, Type>(length);
        for (int i = 0; i < length; ++i) {
            actualTypeMap.put(typeParameters[i], actualTypeArguments[i]);
        }
        return actualTypeMap;
    }
    
    private static ParameterizedType makeParameterizedType(final Class<?> rawClass, final Type[] typeParameters, final Map<TypeVariable, Type> actualTypeMap) {
        final int length = typeParameters.length;
        final Type[] actualTypeArguments = new Type[length];
        for (int i = 0; i < length; ++i) {
            actualTypeArguments[i] = getActualType(typeParameters[i], actualTypeMap);
        }
        return new ParameterizedTypeImpl(actualTypeArguments, null, rawClass);
    }
    
    private static Type getActualType(final Type typeParameter, final Map<TypeVariable, Type> actualTypeMap) {
        if (typeParameter instanceof TypeVariable) {
            return actualTypeMap.get(typeParameter);
        }
        if (typeParameter instanceof ParameterizedType) {
            return makeParameterizedType(getRawClass(typeParameter), ((ParameterizedType)typeParameter).getActualTypeArguments(), actualTypeMap);
        }
        if (typeParameter instanceof GenericArrayType) {
            return new GenericArrayTypeImpl(getActualType(((GenericArrayType)typeParameter).getGenericComponentType(), actualTypeMap));
        }
        return typeParameter;
    }
    
    private static Type getWildcardTypeUpperBounds(final Type type) {
        if (type instanceof WildcardType) {
            final WildcardType wildcardType = (WildcardType)type;
            final Type[] upperBounds = wildcardType.getUpperBounds();
            return (upperBounds.length > 0) ? upperBounds[0] : Object.class;
        }
        return type;
    }
    
    public static Class<?> getCollectionItemClass(final Type fieldType) {
        if (!(fieldType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type actualTypeArgument = ((ParameterizedType)fieldType).getActualTypeArguments()[0];
        if (actualTypeArgument instanceof WildcardType) {
            final WildcardType wildcardType = (WildcardType)actualTypeArgument;
            final Type[] upperBounds = wildcardType.getUpperBounds();
            if (upperBounds.length == 1) {
                actualTypeArgument = upperBounds[0];
            }
        }
        if (!(actualTypeArgument instanceof Class)) {
            throw new JSONException("can not create ASMParser");
        }
        final Class<?> itemClass = (Class<?>)actualTypeArgument;
        if (!Modifier.isPublic(itemClass.getModifiers())) {
            throw new JSONException("can not create ASMParser");
        }
        return itemClass;
    }
    
    public static Type checkPrimitiveArray(final GenericArrayType genericArrayType) {
        Type clz = genericArrayType;
        Type genericComponentType;
        String prefix;
        for (genericComponentType = genericArrayType.getGenericComponentType(), prefix = "["; genericComponentType instanceof GenericArrayType; genericComponentType = ((GenericArrayType)genericComponentType).getGenericComponentType(), prefix += prefix) {}
        if (genericComponentType instanceof Class) {
            final Class<?> ck = (Class<?>)genericComponentType;
            if (ck.isPrimitive()) {
                try {
                    if (ck == Boolean.TYPE) {
                        clz = Class.forName(prefix + "Z");
                    }
                    else if (ck == Character.TYPE) {
                        clz = Class.forName(prefix + "C");
                    }
                    else if (ck == Byte.TYPE) {
                        clz = Class.forName(prefix + "B");
                    }
                    else if (ck == Short.TYPE) {
                        clz = Class.forName(prefix + "S");
                    }
                    else if (ck == Integer.TYPE) {
                        clz = Class.forName(prefix + "I");
                    }
                    else if (ck == Long.TYPE) {
                        clz = Class.forName(prefix + "J");
                    }
                    else if (ck == Float.TYPE) {
                        clz = Class.forName(prefix + "F");
                    }
                    else if (ck == Double.TYPE) {
                        clz = Class.forName(prefix + "D");
                    }
                }
                catch (ClassNotFoundException ex) {}
            }
        }
        return clz;
    }
    
    public static Collection createCollection(final Type type) {
        final Class<?> rawClass = getRawClass(type);
        Collection list;
        if (rawClass == AbstractCollection.class || rawClass == Collection.class) {
            list = new ArrayList();
        }
        else if (rawClass.isAssignableFrom(HashSet.class)) {
            list = new HashSet();
        }
        else if (rawClass.isAssignableFrom(LinkedHashSet.class)) {
            list = new LinkedHashSet();
        }
        else if (rawClass.isAssignableFrom(TreeSet.class)) {
            list = new TreeSet();
        }
        else if (rawClass.isAssignableFrom(ArrayList.class)) {
            list = new ArrayList();
        }
        else if (rawClass.isAssignableFrom(EnumSet.class)) {
            Type itemType;
            if (type instanceof ParameterizedType) {
                itemType = ((ParameterizedType)type).getActualTypeArguments()[0];
            }
            else {
                itemType = Object.class;
            }
            list = EnumSet.noneOf((Class<Enum>)itemType);
        }
        else if (rawClass.isAssignableFrom(Queue.class) || rawClass.isAssignableFrom(Deque.class)) {
            list = new LinkedList();
        }
        else {
            try {
                list = (Collection)rawClass.newInstance();
            }
            catch (Exception e) {
                throw new JSONException("create instance error, class " + rawClass.getName());
            }
        }
        return list;
    }
    
    public static Class<?> getRawClass(final Type type) {
        if (type instanceof Class) {
            return (Class<?>)type;
        }
        if (type instanceof ParameterizedType) {
            return getRawClass(((ParameterizedType)type).getRawType());
        }
        if (!(type instanceof WildcardType)) {
            throw new JSONException("TODO");
        }
        final WildcardType wildcardType = (WildcardType)type;
        final Type[] upperBounds = wildcardType.getUpperBounds();
        if (upperBounds.length == 1) {
            return getRawClass(upperBounds[0]);
        }
        throw new JSONException("TODO");
    }
    
    public static boolean isProxy(final Class<?> clazz) {
        for (final Class<?> item : clazz.getInterfaces()) {
            final String interfaceName = item.getName();
            if (interfaceName.equals("net.sf.cglib.proxy.Factory") || interfaceName.equals("org.springframework.cglib.proxy.Factory")) {
                return true;
            }
            if (interfaceName.equals("javassist.util.proxy.ProxyObject") || interfaceName.equals("org.apache.ibatis.javassist.util.proxy.ProxyObject")) {
                return true;
            }
            if (interfaceName.equals("org.hibernate.proxy.HibernateProxy")) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isTransient(final Method method) {
        if (method == null) {
            return false;
        }
        if (!TypeUtils.transientClassInited) {
            try {
                TypeUtils.transientClass = (Class<? extends Annotation>)Class.forName("java.beans.Transient");
            }
            catch (Exception ex) {}
            finally {
                TypeUtils.transientClassInited = true;
            }
        }
        if (TypeUtils.transientClass != null) {
            final Annotation annotation = getAnnotation(method, TypeUtils.transientClass);
            return annotation != null;
        }
        return false;
    }
    
    public static boolean isAnnotationPresentOneToMany(final Method method) {
        if (method == null) {
            return false;
        }
        if (TypeUtils.class_OneToMany == null && !TypeUtils.class_OneToMany_error) {
            try {
                TypeUtils.class_OneToMany = (Class<? extends Annotation>)Class.forName("javax.persistence.OneToMany");
            }
            catch (Throwable e) {
                TypeUtils.class_OneToMany_error = true;
            }
        }
        return TypeUtils.class_OneToMany != null && method.isAnnotationPresent(TypeUtils.class_OneToMany);
    }
    
    public static boolean isAnnotationPresentManyToMany(final Method method) {
        if (method == null) {
            return false;
        }
        if (TypeUtils.class_ManyToMany == null && !TypeUtils.class_ManyToMany_error) {
            try {
                TypeUtils.class_ManyToMany = (Class<? extends Annotation>)Class.forName("javax.persistence.ManyToMany");
            }
            catch (Throwable e) {
                TypeUtils.class_ManyToMany_error = true;
            }
        }
        return TypeUtils.class_ManyToMany != null && (method.isAnnotationPresent(TypeUtils.class_OneToMany) || method.isAnnotationPresent(TypeUtils.class_ManyToMany));
    }
    
    public static boolean isHibernateInitialized(final Object object) {
        if (object == null) {
            return false;
        }
        if (TypeUtils.method_HibernateIsInitialized == null && !TypeUtils.method_HibernateIsInitialized_error) {
            try {
                final Class<?> class_Hibernate = Class.forName("org.hibernate.Hibernate");
                TypeUtils.method_HibernateIsInitialized = class_Hibernate.getMethod("isInitialized", Object.class);
            }
            catch (Throwable e) {
                TypeUtils.method_HibernateIsInitialized_error = true;
            }
        }
        if (TypeUtils.method_HibernateIsInitialized != null) {
            try {
                final Boolean initialized = (Boolean)TypeUtils.method_HibernateIsInitialized.invoke(null, object);
                return initialized;
            }
            catch (Throwable t) {}
        }
        return true;
    }
    
    public static double parseDouble(final String str) {
        final int len = str.length();
        if (len > 10) {
            return Double.parseDouble(str);
        }
        boolean negative = false;
        long longValue = 0L;
        int scale = 0;
        for (int i = 0; i < len; ++i) {
            final char ch = str.charAt(i);
            if (ch == '-' && i == 0) {
                negative = true;
            }
            else if (ch == '.') {
                if (scale != 0) {
                    return Double.parseDouble(str);
                }
                scale = len - i - 1;
            }
            else {
                if (ch < '0' || ch > '9') {
                    return Double.parseDouble(str);
                }
                final int digit = ch - '0';
                longValue = longValue * 10L + digit;
            }
        }
        if (negative) {
            longValue = -longValue;
        }
        switch (scale) {
            case 0: {
                return (double)longValue;
            }
            case 1: {
                return longValue / 10.0;
            }
            case 2: {
                return longValue / 100.0;
            }
            case 3: {
                return longValue / 1000.0;
            }
            case 4: {
                return longValue / 10000.0;
            }
            case 5: {
                return longValue / 100000.0;
            }
            case 6: {
                return longValue / 1000000.0;
            }
            case 7: {
                return longValue / 1.0E7;
            }
            case 8: {
                return longValue / 1.0E8;
            }
            case 9: {
                return longValue / 1.0E9;
            }
            default: {
                return Double.parseDouble(str);
            }
        }
    }
    
    public static float parseFloat(final String str) {
        final int len = str.length();
        if (len >= 10) {
            return Float.parseFloat(str);
        }
        boolean negative = false;
        long longValue = 0L;
        int scale = 0;
        for (int i = 0; i < len; ++i) {
            final char ch = str.charAt(i);
            if (ch == '-' && i == 0) {
                negative = true;
            }
            else if (ch == '.') {
                if (scale != 0) {
                    return Float.parseFloat(str);
                }
                scale = len - i - 1;
            }
            else {
                if (ch < '0' || ch > '9') {
                    return Float.parseFloat(str);
                }
                final int digit = ch - '0';
                longValue = longValue * 10L + digit;
            }
        }
        if (negative) {
            longValue = -longValue;
        }
        switch (scale) {
            case 0: {
                return (float)longValue;
            }
            case 1: {
                return longValue / 10.0f;
            }
            case 2: {
                return longValue / 100.0f;
            }
            case 3: {
                return longValue / 1000.0f;
            }
            case 4: {
                return longValue / 10000.0f;
            }
            case 5: {
                return longValue / 100000.0f;
            }
            case 6: {
                return longValue / 1000000.0f;
            }
            case 7: {
                return longValue / 1.0E7f;
            }
            case 8: {
                return longValue / 1.0E8f;
            }
            case 9: {
                return longValue / 1.0E9f;
            }
            default: {
                return Float.parseFloat(str);
            }
        }
    }
    
    public static long fnv1a_64_lower(final String key) {
        long hashCode = -3750763034362895579L;
        for (int i = 0; i < key.length(); ++i) {
            char ch = key.charAt(i);
            if (ch != '_') {
                if (ch != '-') {
                    if (ch >= 'A' && ch <= 'Z') {
                        ch += ' ';
                    }
                    hashCode ^= ch;
                    hashCode *= 1099511628211L;
                }
            }
        }
        return hashCode;
    }
    
    public static long fnv1a_64(final String key) {
        long hashCode = -3750763034362895579L;
        for (int i = 0; i < key.length(); ++i) {
            final char ch = key.charAt(i);
            hashCode ^= ch;
            hashCode *= 1099511628211L;
        }
        return hashCode;
    }
    
    public static boolean isKotlin(final Class clazz) {
        if (TypeUtils.kotlin_metadata == null && !TypeUtils.kotlin_metadata_error) {
            try {
                TypeUtils.kotlin_metadata = Class.forName("kotlin.Metadata");
            }
            catch (Throwable e) {
                TypeUtils.kotlin_metadata_error = true;
            }
        }
        return TypeUtils.kotlin_metadata != null && clazz.isAnnotationPresent(TypeUtils.kotlin_metadata);
    }
    
    public static Constructor getKoltinConstructor(final Constructor[] constructors) {
        return getKoltinConstructor(constructors, null);
    }
    
    public static Constructor getKoltinConstructor(final Constructor[] constructors, final String[] paramNames) {
        Constructor creatorConstructor = null;
        for (final Constructor<?> constructor : constructors) {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (paramNames == null || parameterTypes.length == paramNames.length) {
                if (parameterTypes.length <= 0 || !parameterTypes[parameterTypes.length - 1].getName().equals("kotlin.jvm.internal.DefaultConstructorMarker")) {
                    if (creatorConstructor == null || creatorConstructor.getParameterTypes().length < parameterTypes.length) {
                        creatorConstructor = constructor;
                    }
                }
            }
        }
        return creatorConstructor;
    }
    
    public static String[] getKoltinConstructorParameters(final Class clazz) {
        if (TypeUtils.kotlin_kclass_constructor == null && !TypeUtils.kotlin_class_klass_error) {
            try {
                final Class class_kotlin_kclass = Class.forName("kotlin.reflect.jvm.internal.KClassImpl");
                TypeUtils.kotlin_kclass_constructor = class_kotlin_kclass.getConstructor(Class.class);
            }
            catch (Throwable e) {
                TypeUtils.kotlin_class_klass_error = true;
            }
        }
        if (TypeUtils.kotlin_kclass_constructor == null) {
            return null;
        }
        if (TypeUtils.kotlin_kclass_getConstructors == null && !TypeUtils.kotlin_class_klass_error) {
            try {
                final Class class_kotlin_kclass = Class.forName("kotlin.reflect.jvm.internal.KClassImpl");
                TypeUtils.kotlin_kclass_getConstructors = class_kotlin_kclass.getMethod("getConstructors", (Class[])new Class[0]);
            }
            catch (Throwable e) {
                TypeUtils.kotlin_class_klass_error = true;
            }
        }
        if (TypeUtils.kotlin_kfunction_getParameters == null && !TypeUtils.kotlin_class_klass_error) {
            try {
                final Class class_kotlin_kfunction = Class.forName("kotlin.reflect.KFunction");
                TypeUtils.kotlin_kfunction_getParameters = class_kotlin_kfunction.getMethod("getParameters", (Class[])new Class[0]);
            }
            catch (Throwable e) {
                TypeUtils.kotlin_class_klass_error = true;
            }
        }
        if (TypeUtils.kotlin_kparameter_getName == null && !TypeUtils.kotlin_class_klass_error) {
            try {
                final Class class_kotlinn_kparameter = Class.forName("kotlin.reflect.KParameter");
                TypeUtils.kotlin_kparameter_getName = class_kotlinn_kparameter.getMethod("getName", (Class[])new Class[0]);
            }
            catch (Throwable e) {
                TypeUtils.kotlin_class_klass_error = true;
            }
        }
        if (TypeUtils.kotlin_error) {
            return null;
        }
        try {
            Object constructor = null;
            final Object kclassImpl = TypeUtils.kotlin_kclass_constructor.newInstance(clazz);
            final Iterable it = (Iterable)TypeUtils.kotlin_kclass_getConstructors.invoke(kclassImpl, new Object[0]);
            final Iterator iterator = it.iterator();
            while (iterator.hasNext()) {
                final Object item = iterator.next();
                final List parameters = (List)TypeUtils.kotlin_kfunction_getParameters.invoke(item, new Object[0]);
                if (constructor == null || parameters.size() != 0) {
                    constructor = item;
                }
                iterator.hasNext();
            }
            if (constructor == null) {
                return null;
            }
            final List parameters2 = (List)TypeUtils.kotlin_kfunction_getParameters.invoke(constructor, new Object[0]);
            final String[] names = new String[parameters2.size()];
            for (int i = 0; i < parameters2.size(); ++i) {
                final Object param = parameters2.get(i);
                names[i] = (String)TypeUtils.kotlin_kparameter_getName.invoke(param, new Object[0]);
            }
            return names;
        }
        catch (Throwable e) {
            e.printStackTrace();
            TypeUtils.kotlin_error = true;
            return null;
        }
    }
    
    private static boolean isKotlinIgnore(final Class clazz, final String methodName) {
        if (TypeUtils.kotlinIgnores == null && !TypeUtils.kotlinIgnores_error) {
            try {
                final Map<Class, String[]> map = new HashMap<Class, String[]>();
                final Class charRangeClass = Class.forName("kotlin.ranges.CharRange");
                map.put(charRangeClass, new String[] { "getEndInclusive", "isEmpty" });
                final Class intRangeClass = Class.forName("kotlin.ranges.IntRange");
                map.put(intRangeClass, new String[] { "getEndInclusive", "isEmpty" });
                final Class longRangeClass = Class.forName("kotlin.ranges.LongRange");
                map.put(longRangeClass, new String[] { "getEndInclusive", "isEmpty" });
                final Class floatRangeClass = Class.forName("kotlin.ranges.ClosedFloatRange");
                map.put(floatRangeClass, new String[] { "getEndInclusive", "isEmpty" });
                final Class doubleRangeClass = Class.forName("kotlin.ranges.ClosedDoubleRange");
                map.put(doubleRangeClass, new String[] { "getEndInclusive", "isEmpty" });
                TypeUtils.kotlinIgnores = map;
            }
            catch (Throwable error) {
                TypeUtils.kotlinIgnores_error = true;
            }
        }
        if (TypeUtils.kotlinIgnores == null) {
            return false;
        }
        final String[] ignores = TypeUtils.kotlinIgnores.get(clazz);
        return ignores != null && Arrays.binarySearch(ignores, methodName) >= 0;
    }
    
    public static <A extends Annotation> A getAnnotation(final Class<?> targetClass, final Class<A> annotationClass) {
        A targetAnnotation = targetClass.getAnnotation(annotationClass);
        Class<?> mixInClass = null;
        final Type type = JSON.getMixInAnnotations(targetClass);
        if (type instanceof Class) {
            mixInClass = (Class<?>)type;
        }
        if (mixInClass != null) {
            A mixInAnnotation = mixInClass.getAnnotation(annotationClass);
            if (mixInAnnotation == null && mixInClass.getAnnotations().length > 0) {
                for (final Annotation annotation : mixInClass.getAnnotations()) {
                    mixInAnnotation = annotation.annotationType().getAnnotation(annotationClass);
                    if (mixInAnnotation != null) {
                        break;
                    }
                }
            }
            if (mixInAnnotation != null) {
                return mixInAnnotation;
            }
        }
        if (targetAnnotation == null && targetClass.getAnnotations().length > 0) {
            for (final Annotation annotation2 : targetClass.getAnnotations()) {
                targetAnnotation = annotation2.annotationType().getAnnotation(annotationClass);
                if (targetAnnotation != null) {
                    break;
                }
            }
        }
        return targetAnnotation;
    }
    
    public static <A extends Annotation> A getAnnotation(final Field field, final Class<A> annotationClass) {
        final A targetAnnotation = field.getAnnotation(annotationClass);
        final Class<?> clazz = field.getDeclaringClass();
        Class<?> mixInClass = null;
        final Type type = JSON.getMixInAnnotations(clazz);
        if (type instanceof Class) {
            mixInClass = (Class<?>)type;
        }
        if (mixInClass != null) {
            Field mixInField = null;
            final String fieldName = field.getName();
            Class<?> currClass = mixInClass;
            while (currClass != null && currClass != Object.class) {
                try {
                    mixInField = currClass.getDeclaredField(fieldName);
                }
                catch (NoSuchFieldException ex) {
                    currClass = currClass.getSuperclass();
                    continue;
                }
                break;
            }
            if (mixInField == null) {
                return targetAnnotation;
            }
            final A mixInAnnotation = mixInField.getAnnotation(annotationClass);
            if (mixInAnnotation != null) {
                return mixInAnnotation;
            }
        }
        return targetAnnotation;
    }
    
    public static <A extends Annotation> A getAnnotation(final Method method, final Class<A> annotationClass) {
        final A targetAnnotation = method.getAnnotation(annotationClass);
        final Class<?> clazz = method.getDeclaringClass();
        Class<?> mixInClass = null;
        final Type type = JSON.getMixInAnnotations(clazz);
        if (type instanceof Class) {
            mixInClass = (Class<?>)type;
        }
        if (mixInClass != null) {
            Method mixInMethod = null;
            final String methodName = method.getName();
            final Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> currClass = mixInClass;
            while (currClass != null && currClass != Object.class) {
                try {
                    mixInMethod = currClass.getDeclaredMethod(methodName, parameterTypes);
                }
                catch (NoSuchMethodException ex) {
                    currClass = currClass.getSuperclass();
                    continue;
                }
                break;
            }
            if (mixInMethod == null) {
                return targetAnnotation;
            }
            final A mixInAnnotation = mixInMethod.getAnnotation(annotationClass);
            if (mixInAnnotation != null) {
                return mixInAnnotation;
            }
        }
        return targetAnnotation;
    }
    
    public static Annotation[][] getParameterAnnotations(final Method method) {
        final Annotation[][] targetAnnotations = method.getParameterAnnotations();
        final Class<?> clazz = method.getDeclaringClass();
        Class<?> mixInClass = null;
        final Type type = JSON.getMixInAnnotations(clazz);
        if (type instanceof Class) {
            mixInClass = (Class<?>)type;
        }
        if (mixInClass != null) {
            Method mixInMethod = null;
            final String methodName = method.getName();
            final Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> currClass = mixInClass;
            while (currClass != null && currClass != Object.class) {
                try {
                    mixInMethod = currClass.getDeclaredMethod(methodName, parameterTypes);
                }
                catch (NoSuchMethodException e) {
                    currClass = currClass.getSuperclass();
                    continue;
                }
                break;
            }
            if (mixInMethod == null) {
                return targetAnnotations;
            }
            final Annotation[][] mixInAnnotations = mixInMethod.getParameterAnnotations();
            if (mixInAnnotations != null) {
                return mixInAnnotations;
            }
        }
        return targetAnnotations;
    }
    
    public static Annotation[][] getParameterAnnotations(final Constructor constructor) {
        final Annotation[][] targetAnnotations = constructor.getParameterAnnotations();
        final Class<?> clazz = constructor.getDeclaringClass();
        Class<?> mixInClass = null;
        final Type type = JSON.getMixInAnnotations(clazz);
        if (type instanceof Class) {
            mixInClass = (Class<?>)type;
        }
        if (mixInClass != null) {
            Constructor mixInConstructor = null;
            final Class<?>[] parameterTypes = (Class<?>[])constructor.getParameterTypes();
            final List<Class<?>> enclosingClasses = new ArrayList<Class<?>>(2);
            for (Class<?> enclosingClass = mixInClass.getEnclosingClass(); enclosingClass != null; enclosingClass = enclosingClass.getEnclosingClass()) {
                enclosingClasses.add(enclosingClass);
            }
            int level = enclosingClasses.size();
            Class<?> currClass = mixInClass;
            while (currClass != null && currClass != Object.class) {
                try {
                    if (level != 0) {
                        final Class<?>[] outerClassAndParameterTypes = (Class<?>[])new Class[level + parameterTypes.length];
                        System.arraycopy(parameterTypes, 0, outerClassAndParameterTypes, level, parameterTypes.length);
                        for (int i = level; i > 0; --i) {
                            outerClassAndParameterTypes[i - 1] = enclosingClasses.get(i - 1);
                        }
                        mixInConstructor = mixInClass.getDeclaredConstructor(outerClassAndParameterTypes);
                    }
                    else {
                        mixInConstructor = mixInClass.getDeclaredConstructor(parameterTypes);
                    }
                }
                catch (NoSuchMethodException e) {
                    --level;
                    currClass = currClass.getSuperclass();
                    continue;
                }
                break;
            }
            if (mixInConstructor == null) {
                return targetAnnotations;
            }
            final Annotation[][] mixInAnnotations = mixInConstructor.getParameterAnnotations();
            if (mixInAnnotations != null) {
                return mixInAnnotations;
            }
        }
        return targetAnnotations;
    }
    
    public static boolean isJacksonCreator(final Method method) {
        if (method == null) {
            return false;
        }
        if (TypeUtils.class_JacksonCreator == null && !TypeUtils.class_JacksonCreator_error) {
            try {
                TypeUtils.class_JacksonCreator = (Class<? extends Annotation>)Class.forName("com.fasterxml.jackson.annotation.JsonCreator");
            }
            catch (Throwable e) {
                TypeUtils.class_JacksonCreator_error = true;
            }
        }
        return TypeUtils.class_JacksonCreator != null && method.isAnnotationPresent(TypeUtils.class_JacksonCreator);
    }
    
    public static LocalDateTime castToLocalDateTime(final Object value, String format) {
        if (value == null) {
            return null;
        }
        if (format == null) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        final DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.parse(value.toString(), df);
    }
    
    static {
        NUMBER_WITH_TRAILING_ZEROS_PATTERN = Pattern.compile("\\.0*$");
        TypeUtils.compatibleWithJavaBean = false;
        TypeUtils.compatibleWithFieldName = false;
        TypeUtils.setAccessibleEnable = true;
        TypeUtils.oracleTimestampMethodInited = false;
        TypeUtils.oracleDateMethodInited = false;
        TypeUtils.optionalClassInited = false;
        TypeUtils.transientClassInited = false;
        TypeUtils.class_OneToMany = null;
        TypeUtils.class_OneToMany_error = false;
        TypeUtils.class_ManyToMany = null;
        TypeUtils.class_ManyToMany_error = false;
        TypeUtils.method_HibernateIsInitialized = null;
        TypeUtils.method_HibernateIsInitialized_error = false;
        TypeUtils.mappings = new ConcurrentHashMap<String, Class<?>>(256, 0.75f, 1);
        TypeUtils.pathClass_error = false;
        TypeUtils.class_JacksonCreator = null;
        TypeUtils.class_JacksonCreator_error = false;
        TypeUtils.class_Clob = null;
        TypeUtils.class_Clob_error = false;
        TypeUtils.class_XmlAccessType = null;
        TypeUtils.class_XmlAccessorType = null;
        TypeUtils.classXmlAccessorType_error = false;
        TypeUtils.method_XmlAccessorType_value = null;
        TypeUtils.field_XmlAccessType_FIELD = null;
        TypeUtils.field_XmlAccessType_FIELD_VALUE = null;
        try {
            TypeUtils.compatibleWithJavaBean = "true".equals(IOUtils.getStringProperty("fastjson.compatibleWithJavaBean"));
            TypeUtils.compatibleWithFieldName = "true".equals(IOUtils.getStringProperty("fastjson.compatibleWithFieldName"));
        }
        catch (Throwable t) {}
        addBaseClassMappings();
    }
}
