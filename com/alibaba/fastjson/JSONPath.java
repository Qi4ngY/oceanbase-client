package com.alibaba.fastjson;

import java.util.regex.Matcher;
import java.util.Arrays;
import com.alibaba.fastjson.parser.JSONLexerBase;
import java.util.ArrayList;
import com.alibaba.fastjson.util.IOUtils;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.FieldSerializer;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.math.BigInteger;
import com.alibaba.fastjson.util.TypeUtils;
import java.math.BigDecimal;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.JavaBeanSerializer;
import java.util.UUID;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.deserializer.FieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import java.lang.reflect.Type;
import java.util.Map;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import java.util.concurrent.ConcurrentMap;

public class JSONPath implements JSONAware
{
    private static ConcurrentMap<String, JSONPath> pathCache;
    private final String path;
    private Segment[] segments;
    private boolean hasRefSegment;
    private SerializeConfig serializeConfig;
    private ParserConfig parserConfig;
    static final long SIZE = 5614464919154503228L;
    static final long LENGTH = -1580386065683472715L;
    
    public JSONPath(final String path) {
        this(path, SerializeConfig.getGlobalInstance(), ParserConfig.getGlobalInstance());
    }
    
    public JSONPath(final String path, final SerializeConfig serializeConfig, final ParserConfig parserConfig) {
        if (path == null || path.length() == 0) {
            throw new JSONPathException("json-path can not be null or empty");
        }
        this.path = path;
        this.serializeConfig = serializeConfig;
        this.parserConfig = parserConfig;
    }
    
    protected void init() {
        if (this.segments != null) {
            return;
        }
        if ("*".equals(this.path)) {
            this.segments = new Segment[] { WildCardSegment.instance };
        }
        else {
            final JSONPathParser parser = new JSONPathParser(this.path);
            this.segments = parser.explain();
            this.hasRefSegment = parser.hasRefSegment;
        }
    }
    
    public boolean isRef() {
        this.init();
        for (int i = 0; i < this.segments.length; ++i) {
            final Segment segment = this.segments[i];
            final Class segmentType = segment.getClass();
            if (segmentType != ArrayAccessSegment.class && segmentType != PropertySegment.class) {
                return false;
            }
        }
        return true;
    }
    
    public Object eval(final Object rootObject) {
        if (rootObject == null) {
            return null;
        }
        this.init();
        Object currentObject = rootObject;
        for (int i = 0; i < this.segments.length; ++i) {
            final Segment segment = this.segments[i];
            currentObject = segment.eval(this, rootObject, currentObject);
        }
        return currentObject;
    }
    
    public Object extract(final DefaultJSONParser parser) {
        if (parser == null) {
            return null;
        }
        this.init();
        if (this.hasRefSegment) {
            final Object root = parser.parse();
            return this.eval(root);
        }
        if (this.segments.length == 0) {
            return parser.parse();
        }
        Context context = null;
        for (int i = 0; i < this.segments.length; ++i) {
            final Segment segment = this.segments[i];
            final boolean last = i == this.segments.length - 1;
            if (context != null && context.object != null) {
                context.object = segment.eval(this, null, context.object);
            }
            else {
                boolean eval;
                if (!last) {
                    final Segment nextSegment = this.segments[i + 1];
                    eval = ((segment instanceof PropertySegment && ((PropertySegment)segment).deep && (nextSegment instanceof ArrayAccessSegment || nextSegment instanceof MultiIndexSegment || nextSegment instanceof MultiPropertySegment || nextSegment instanceof SizeSegment || nextSegment instanceof PropertySegment || nextSegment instanceof FilterSegment)) || (nextSegment instanceof ArrayAccessSegment && ((ArrayAccessSegment)nextSegment).index < 0) || nextSegment instanceof FilterSegment || segment instanceof WildCardSegment);
                }
                else {
                    eval = true;
                }
                context = new Context(context, eval);
                segment.extract(this, parser, context);
            }
        }
        return context.object;
    }
    
    public boolean contains(final Object rootObject) {
        if (rootObject == null) {
            return false;
        }
        this.init();
        Object currentObject = rootObject;
        for (int i = 0; i < this.segments.length; ++i) {
            final Object parentObject = currentObject;
            currentObject = this.segments[i].eval(this, rootObject, currentObject);
            if (currentObject == null) {
                return false;
            }
            if (currentObject == Collections.EMPTY_LIST && parentObject instanceof List) {
                return ((List)parentObject).contains(currentObject);
            }
        }
        return true;
    }
    
    public boolean containsValue(final Object rootObject, final Object value) {
        final Object currentObject = this.eval(rootObject);
        if (currentObject == value) {
            return true;
        }
        if (currentObject == null) {
            return false;
        }
        if (currentObject instanceof Iterable) {
            for (final Object item : (Iterable)currentObject) {
                if (eq(item, value)) {
                    return true;
                }
            }
            return false;
        }
        return eq(currentObject, value);
    }
    
    public int size(final Object rootObject) {
        if (rootObject == null) {
            return -1;
        }
        this.init();
        Object currentObject = rootObject;
        for (int i = 0; i < this.segments.length; ++i) {
            currentObject = this.segments[i].eval(this, rootObject, currentObject);
        }
        return this.evalSize(currentObject);
    }
    
    public Set<?> keySet(final Object rootObject) {
        if (rootObject == null) {
            return null;
        }
        this.init();
        Object currentObject = rootObject;
        for (int i = 0; i < this.segments.length; ++i) {
            currentObject = this.segments[i].eval(this, rootObject, currentObject);
        }
        return this.evalKeySet(currentObject);
    }
    
    public void arrayAdd(final Object rootObject, final Object... values) {
        if (values == null || values.length == 0) {
            return;
        }
        if (rootObject == null) {
            return;
        }
        this.init();
        Object currentObject = rootObject;
        Object parentObject = null;
        for (int i = 0; i < this.segments.length; ++i) {
            if (i == this.segments.length - 1) {
                parentObject = currentObject;
            }
            currentObject = this.segments[i].eval(this, rootObject, currentObject);
        }
        final Object result = currentObject;
        if (result == null) {
            throw new JSONPathException("value not found in path " + this.path);
        }
        if (result instanceof Collection) {
            final Collection collection = (Collection)result;
            for (final Object value : values) {
                collection.add(value);
            }
            return;
        }
        final Class<?> resultClass = result.getClass();
        if (!resultClass.isArray()) {
            throw new JSONException("unsupported array put operation. " + resultClass);
        }
        final int length = Array.getLength(result);
        final Object descArray = Array.newInstance(resultClass.getComponentType(), length + values.length);
        System.arraycopy(result, 0, descArray, 0, length);
        for (int j = 0; j < values.length; ++j) {
            Array.set(descArray, length + j, values[j]);
        }
        final Object newResult = descArray;
        final Segment lastSegment = this.segments[this.segments.length - 1];
        if (lastSegment instanceof PropertySegment) {
            final PropertySegment propertySegment = (PropertySegment)lastSegment;
            propertySegment.setValue(this, parentObject, newResult);
            return;
        }
        if (lastSegment instanceof ArrayAccessSegment) {
            ((ArrayAccessSegment)lastSegment).setValue(this, parentObject, newResult);
            return;
        }
        throw new UnsupportedOperationException();
    }
    
    public boolean remove(final Object rootObject) {
        if (rootObject == null) {
            return false;
        }
        this.init();
        Object currentObject = rootObject;
        Object parentObject = null;
        final Segment lastSegment = this.segments[this.segments.length - 1];
        for (int i = 0; i < this.segments.length; ++i) {
            if (i == this.segments.length - 1) {
                parentObject = currentObject;
                break;
            }
            final Segment segement = this.segments[i];
            if (i == this.segments.length - 2 && lastSegment instanceof FilterSegment && segement instanceof PropertySegment) {
                final FilterSegment filterSegment = (FilterSegment)lastSegment;
                if (currentObject instanceof List) {
                    final PropertySegment propertySegment = (PropertySegment)segement;
                    final List list = (List)currentObject;
                    final Iterator it = list.iterator();
                    while (it.hasNext()) {
                        final Object item = it.next();
                        final Object result = propertySegment.eval(this, rootObject, item);
                        if (result instanceof Iterable) {
                            filterSegment.remove(this, rootObject, result);
                        }
                        else {
                            if (!(result instanceof Map) || !filterSegment.filter.apply(this, rootObject, currentObject, result)) {
                                continue;
                            }
                            it.remove();
                        }
                    }
                    return true;
                }
                if (currentObject instanceof Map) {
                    final PropertySegment propertySegment = (PropertySegment)segement;
                    final Object result2 = propertySegment.eval(this, rootObject, currentObject);
                    if (result2 == null) {
                        return false;
                    }
                    if (result2 instanceof Map && filterSegment.filter.apply(this, rootObject, currentObject, result2)) {
                        propertySegment.remove(this, currentObject);
                        return true;
                    }
                }
            }
            currentObject = segement.eval(this, rootObject, currentObject);
            if (currentObject == null) {
                break;
            }
        }
        if (parentObject == null) {
            return false;
        }
        if (lastSegment instanceof PropertySegment) {
            final PropertySegment propertySegment2 = (PropertySegment)lastSegment;
            if (parentObject instanceof Collection && this.segments.length > 1) {
                final Segment parentSegment = this.segments[this.segments.length - 2];
                if (parentSegment instanceof RangeSegment || parentSegment instanceof MultiIndexSegment) {
                    final Collection collection = (Collection)parentObject;
                    boolean removedOnce = false;
                    for (final Object item2 : collection) {
                        final boolean removed = propertySegment2.remove(this, item2);
                        if (removed) {
                            removedOnce = true;
                        }
                    }
                    return removedOnce;
                }
            }
            return propertySegment2.remove(this, parentObject);
        }
        if (lastSegment instanceof ArrayAccessSegment) {
            return ((ArrayAccessSegment)lastSegment).remove(this, parentObject);
        }
        if (lastSegment instanceof FilterSegment) {
            final FilterSegment filterSegment2 = (FilterSegment)lastSegment;
            return filterSegment2.remove(this, rootObject, parentObject);
        }
        throw new UnsupportedOperationException();
    }
    
    public boolean set(final Object rootObject, final Object value) {
        return this.set(rootObject, value, true);
    }
    
    public boolean set(final Object rootObject, final Object value, final boolean p) {
        if (rootObject == null) {
            return false;
        }
        this.init();
        Object currentObject = rootObject;
        Object parentObject = null;
        for (int i = 0; i < this.segments.length; ++i) {
            parentObject = currentObject;
            final Segment segment = this.segments[i];
            currentObject = segment.eval(this, rootObject, currentObject);
            if (currentObject == null) {
                Segment nextSegment = null;
                if (i < this.segments.length - 1) {
                    nextSegment = this.segments[i + 1];
                }
                Object newObj = null;
                if (nextSegment instanceof PropertySegment) {
                    JavaBeanDeserializer beanDeserializer = null;
                    Class<?> fieldClass = null;
                    if (segment instanceof PropertySegment) {
                        final String propertyName = ((PropertySegment)segment).propertyName;
                        final Class<?> parentClass = parentObject.getClass();
                        final JavaBeanDeserializer parentBeanDeserializer = this.getJavaBeanDeserializer(parentClass);
                        if (parentBeanDeserializer != null) {
                            final FieldDeserializer fieldDeserializer = parentBeanDeserializer.getFieldDeserializer(propertyName);
                            fieldClass = fieldDeserializer.fieldInfo.fieldClass;
                            beanDeserializer = this.getJavaBeanDeserializer(fieldClass);
                        }
                    }
                    if (beanDeserializer != null) {
                        if (beanDeserializer.beanInfo.defaultConstructor == null) {
                            return false;
                        }
                        newObj = beanDeserializer.createInstance(null, fieldClass);
                    }
                    else {
                        newObj = new JSONObject();
                    }
                }
                else if (nextSegment instanceof ArrayAccessSegment) {
                    newObj = new JSONArray();
                }
                if (newObj == null) {
                    break;
                }
                if (segment instanceof PropertySegment) {
                    final PropertySegment propSegement = (PropertySegment)segment;
                    propSegement.setValue(this, parentObject, newObj);
                    currentObject = newObj;
                }
                else {
                    if (!(segment instanceof ArrayAccessSegment)) {
                        break;
                    }
                    final ArrayAccessSegment arrayAccessSegement = (ArrayAccessSegment)segment;
                    arrayAccessSegement.setValue(this, parentObject, newObj);
                    currentObject = newObj;
                }
            }
        }
        if (parentObject == null) {
            return false;
        }
        final Segment lastSegment = this.segments[this.segments.length - 1];
        if (lastSegment instanceof PropertySegment) {
            final PropertySegment propertySegment = (PropertySegment)lastSegment;
            propertySegment.setValue(this, parentObject, value);
            return true;
        }
        if (lastSegment instanceof ArrayAccessSegment) {
            return ((ArrayAccessSegment)lastSegment).setValue(this, parentObject, value);
        }
        throw new UnsupportedOperationException();
    }
    
    public static Object eval(final Object rootObject, final String path) {
        final JSONPath jsonpath = compile(path);
        return jsonpath.eval(rootObject);
    }
    
    public static int size(final Object rootObject, final String path) {
        final JSONPath jsonpath = compile(path);
        final Object result = jsonpath.eval(rootObject);
        return jsonpath.evalSize(result);
    }
    
    public static Set<?> keySet(final Object rootObject, final String path) {
        final JSONPath jsonpath = compile(path);
        final Object result = jsonpath.eval(rootObject);
        return jsonpath.evalKeySet(result);
    }
    
    public static boolean contains(final Object rootObject, final String path) {
        if (rootObject == null) {
            return false;
        }
        final JSONPath jsonpath = compile(path);
        return jsonpath.contains(rootObject);
    }
    
    public static boolean containsValue(final Object rootObject, final String path, final Object value) {
        final JSONPath jsonpath = compile(path);
        return jsonpath.containsValue(rootObject, value);
    }
    
    public static void arrayAdd(final Object rootObject, final String path, final Object... values) {
        final JSONPath jsonpath = compile(path);
        jsonpath.arrayAdd(rootObject, values);
    }
    
    public static boolean set(final Object rootObject, final String path, final Object value) {
        final JSONPath jsonpath = compile(path);
        return jsonpath.set(rootObject, value);
    }
    
    public static boolean remove(final Object root, final String path) {
        final JSONPath jsonpath = compile(path);
        return jsonpath.remove(root);
    }
    
    public static JSONPath compile(final String path) {
        if (path == null) {
            throw new JSONPathException("jsonpath can not be null");
        }
        JSONPath jsonpath = JSONPath.pathCache.get(path);
        if (jsonpath == null) {
            jsonpath = new JSONPath(path);
            if (JSONPath.pathCache.size() < 1024) {
                JSONPath.pathCache.putIfAbsent(path, jsonpath);
                jsonpath = JSONPath.pathCache.get(path);
            }
        }
        return jsonpath;
    }
    
    public static Object read(final String json, final String path) {
        return compile(path).eval(JSON.parse(json));
    }
    
    public static Object extract(final String json, final String path, final ParserConfig config, int features, final Feature... optionFeatures) {
        features |= Feature.OrderedField.mask;
        final DefaultJSONParser parser = new DefaultJSONParser(json, config, features);
        final JSONPath jsonPath = compile(path);
        final Object result = jsonPath.extract(parser);
        parser.lexer.close();
        return result;
    }
    
    public static Object extract(final String json, final String path) {
        return extract(json, path, ParserConfig.global, JSON.DEFAULT_PARSER_FEATURE, new Feature[0]);
    }
    
    public static Map<String, Object> paths(final Object javaObject) {
        return paths(javaObject, SerializeConfig.globalInstance);
    }
    
    public static Map<String, Object> paths(final Object javaObject, final SerializeConfig config) {
        final Map<Object, String> values = new IdentityHashMap<Object, String>();
        final Map<String, Object> paths = new HashMap<String, Object>();
        paths(values, paths, "/", javaObject, config);
        return paths;
    }
    
    private static void paths(final Map<Object, String> values, final Map<String, Object> paths, final String parent, final Object javaObject, final SerializeConfig config) {
        if (javaObject == null) {
            return;
        }
        final String p = values.put(javaObject, parent);
        if (p != null) {
            final boolean basicType = javaObject instanceof String || javaObject instanceof Number || javaObject instanceof Date || javaObject instanceof UUID;
            if (!basicType) {
                return;
            }
        }
        paths.put(parent, javaObject);
        if (javaObject instanceof Map) {
            final Map map = (Map)javaObject;
            for (final Object entryObj : map.entrySet()) {
                final Map.Entry entry = (Map.Entry)entryObj;
                final Object key = entry.getKey();
                if (key instanceof String) {
                    final String path = parent.equals("/") ? ("/" + key) : (parent + "/" + key);
                    paths(values, paths, path, entry.getValue(), config);
                }
            }
            return;
        }
        if (javaObject instanceof Collection) {
            final Collection collection = (Collection)javaObject;
            int i = 0;
            for (final Object item : collection) {
                final String path2 = parent.equals("/") ? ("/" + i) : (parent + "/" + i);
                paths(values, paths, path2, item, config);
                ++i;
            }
            return;
        }
        final Class<?> clazz = javaObject.getClass();
        if (clazz.isArray()) {
            for (int len = Array.getLength(javaObject), j = 0; j < len; ++j) {
                final Object item = Array.get(javaObject, j);
                final String path2 = parent.equals("/") ? ("/" + j) : (parent + "/" + j);
                paths(values, paths, path2, item, config);
            }
            return;
        }
        if (ParserConfig.isPrimitive2(clazz) || clazz.isEnum()) {
            return;
        }
        final ObjectSerializer serializer = config.getObjectWriter(clazz);
        if (serializer instanceof JavaBeanSerializer) {
            final JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer)serializer;
            try {
                final Map<String, Object> fieldValues = javaBeanSerializer.getFieldValuesMap(javaObject);
                for (final Map.Entry<String, Object> entry2 : fieldValues.entrySet()) {
                    final String key2 = entry2.getKey();
                    if (key2 instanceof String) {
                        final String path3 = parent.equals("/") ? ("/" + key2) : (parent + "/" + key2);
                        paths(values, paths, path3, entry2.getValue(), config);
                    }
                }
            }
            catch (Exception e) {
                throw new JSONException("toJSON error", e);
            }
        }
    }
    
    public String getPath() {
        return this.path;
    }
    
    static int compare(Object a, Object b) {
        if (a.getClass() == b.getClass()) {
            return ((Comparable)a).compareTo(b);
        }
        final Class typeA = a.getClass();
        final Class typeB = b.getClass();
        if (typeA == BigDecimal.class) {
            if (typeB == Integer.class) {
                b = new BigDecimal((int)b);
            }
            else if (typeB == Long.class) {
                b = new BigDecimal((long)b);
            }
            else if (typeB == Float.class) {
                b = new BigDecimal((float)b);
            }
            else if (typeB == Double.class) {
                b = new BigDecimal((double)b);
            }
        }
        else if (typeA == Long.class) {
            if (typeB == Integer.class) {
                b = new Long((int)b);
            }
            else if (typeB == BigDecimal.class) {
                a = new BigDecimal((long)a);
            }
            else if (typeB == Float.class) {
                a = new Float((float)(long)a);
            }
            else if (typeB == Double.class) {
                a = new Double((double)(long)a);
            }
        }
        else if (typeA == Integer.class) {
            if (typeB == Long.class) {
                a = new Long((int)a);
            }
            else if (typeB == BigDecimal.class) {
                a = new BigDecimal((int)a);
            }
            else if (typeB == Float.class) {
                a = new Float((float)(int)a);
            }
            else if (typeB == Double.class) {
                a = new Double((int)a);
            }
        }
        else if (typeA == Double.class) {
            if (typeB == Integer.class) {
                b = new Double((int)b);
            }
            else if (typeB == Long.class) {
                b = new Double((double)(long)b);
            }
            else if (typeB == Float.class) {
                b = new Double((float)b);
            }
        }
        else if (typeA == Float.class) {
            if (typeB == Integer.class) {
                b = new Float((float)(int)b);
            }
            else if (typeB == Long.class) {
                b = new Float((float)(long)b);
            }
            else if (typeB == Double.class) {
                a = new Double((float)a);
            }
        }
        return ((Comparable)a).compareTo(b);
    }
    
    protected Object getArrayItem(final Object currentObject, final int index) {
        if (currentObject == null) {
            return null;
        }
        if (currentObject instanceof List) {
            final List list = (List)currentObject;
            if (index >= 0) {
                if (index < list.size()) {
                    return list.get(index);
                }
                return null;
            }
            else {
                if (Math.abs(index) <= list.size()) {
                    return list.get(list.size() + index);
                }
                return null;
            }
        }
        else if (currentObject.getClass().isArray()) {
            final int arrayLenth = Array.getLength(currentObject);
            if (index >= 0) {
                if (index < arrayLenth) {
                    return Array.get(currentObject, index);
                }
                return null;
            }
            else {
                if (Math.abs(index) <= arrayLenth) {
                    return Array.get(currentObject, arrayLenth + index);
                }
                return null;
            }
        }
        else {
            if (currentObject instanceof Map) {
                final Map map = (Map)currentObject;
                Object value = map.get(index);
                if (value == null) {
                    value = map.get(Integer.toString(index));
                }
                return value;
            }
            if (currentObject instanceof Collection) {
                final Collection collection = (Collection)currentObject;
                int i = 0;
                for (final Object item : collection) {
                    if (i == index) {
                        return item;
                    }
                    ++i;
                }
                return null;
            }
            if (index == 0) {
                return currentObject;
            }
            throw new UnsupportedOperationException();
        }
    }
    
    public boolean setArrayItem(final JSONPath path, final Object currentObject, final int index, final Object value) {
        if (currentObject instanceof List) {
            final List list = (List)currentObject;
            if (index >= 0) {
                list.set(index, value);
            }
            else {
                list.set(list.size() + index, value);
            }
            return true;
        }
        final Class<?> clazz = currentObject.getClass();
        if (clazz.isArray()) {
            final int arrayLenth = Array.getLength(currentObject);
            if (index >= 0) {
                if (index < arrayLenth) {
                    Array.set(currentObject, index, value);
                }
            }
            else if (Math.abs(index) <= arrayLenth) {
                Array.set(currentObject, arrayLenth + index, value);
            }
            return true;
        }
        throw new JSONPathException("unsupported set operation." + clazz);
    }
    
    public boolean removeArrayItem(final JSONPath path, final Object currentObject, final int index) {
        if (currentObject instanceof List) {
            final List list = (List)currentObject;
            if (index >= 0) {
                if (index >= list.size()) {
                    return false;
                }
                list.remove(index);
            }
            else {
                final int newIndex = list.size() + index;
                if (newIndex < 0) {
                    return false;
                }
                list.remove(newIndex);
            }
            return true;
        }
        final Class<?> clazz = currentObject.getClass();
        throw new JSONPathException("unsupported set operation." + clazz);
    }
    
    protected Collection<Object> getPropertyValues(final Object currentObject) {
        if (currentObject == null) {
            return null;
        }
        final Class<?> currentClass = currentObject.getClass();
        final JavaBeanSerializer beanSerializer = this.getJavaBeanSerializer(currentClass);
        if (beanSerializer != null) {
            try {
                return beanSerializer.getFieldValues(currentObject);
            }
            catch (Exception e) {
                throw new JSONPathException("jsonpath error, path " + this.path, e);
            }
        }
        if (currentObject instanceof Map) {
            final Map map = (Map)currentObject;
            return map.values();
        }
        if (currentObject instanceof Collection) {
            return (Collection<Object>)currentObject;
        }
        throw new UnsupportedOperationException();
    }
    
    protected void deepGetPropertyValues(final Object currentObject, final List<Object> outValues) {
        final Class<?> currentClass = currentObject.getClass();
        final JavaBeanSerializer beanSerializer = this.getJavaBeanSerializer(currentClass);
        Collection collection = null;
        Label_0103: {
            if (beanSerializer != null) {
                try {
                    collection = beanSerializer.getFieldValues(currentObject);
                    break Label_0103;
                }
                catch (Exception e) {
                    throw new JSONPathException("jsonpath error, path " + this.path, e);
                }
            }
            if (currentObject instanceof Map) {
                final Map map = (Map)currentObject;
                collection = map.values();
            }
            else if (currentObject instanceof Collection) {
                collection = (Collection)currentObject;
            }
        }
        if (collection != null) {
            for (final Object fieldValue : collection) {
                if (fieldValue == null || ParserConfig.isPrimitive2(fieldValue.getClass())) {
                    outValues.add(fieldValue);
                }
                else {
                    this.deepGetPropertyValues(fieldValue, outValues);
                }
            }
            return;
        }
        throw new UnsupportedOperationException(currentClass.getName());
    }
    
    static boolean eq(final Object a, final Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.getClass() == b.getClass()) {
            return a.equals(b);
        }
        if (a instanceof Number) {
            return b instanceof Number && eqNotNull((Number)a, (Number)b);
        }
        return a.equals(b);
    }
    
    static boolean eqNotNull(final Number a, final Number b) {
        final Class clazzA = a.getClass();
        final boolean isIntA = isInt(clazzA);
        final Class clazzB = b.getClass();
        final boolean isIntB = isInt(clazzB);
        if (a instanceof BigDecimal) {
            final BigDecimal decimalA = (BigDecimal)a;
            if (isIntB) {
                return decimalA.equals(BigDecimal.valueOf(TypeUtils.longExtractValue(b)));
            }
        }
        if (isIntA) {
            if (isIntB) {
                return a.longValue() == b.longValue();
            }
            if (b instanceof BigInteger) {
                final BigInteger bigIntB = (BigInteger)a;
                final BigInteger bigIntA = BigInteger.valueOf(a.longValue());
                return bigIntA.equals(bigIntB);
            }
        }
        if (isIntB && a instanceof BigInteger) {
            final BigInteger bigIntA2 = (BigInteger)a;
            final BigInteger bigIntB2 = BigInteger.valueOf(TypeUtils.longExtractValue(b));
            return bigIntA2.equals(bigIntB2);
        }
        final boolean isDoubleA = isDouble(clazzA);
        final boolean isDoubleB = isDouble(clazzB);
        return ((isDoubleA && isDoubleB) || (isDoubleA && isIntB) || (isDoubleB && isIntA)) && a.doubleValue() == b.doubleValue();
    }
    
    protected static boolean isDouble(final Class<?> clazzA) {
        return clazzA == Float.class || clazzA == Double.class;
    }
    
    protected static boolean isInt(final Class<?> clazzA) {
        return clazzA == Byte.class || clazzA == Short.class || clazzA == Integer.class || clazzA == Long.class;
    }
    
    protected Object getPropertyValue(Object currentObject, final String propertyName, final long propertyNameHash) {
        if (currentObject == null) {
            return null;
        }
        if (currentObject instanceof String) {
            try {
                final JSONObject object = (JSONObject)(currentObject = JSON.parseObject((String)currentObject));
            }
            catch (Exception ex) {}
        }
        if (currentObject instanceof Map) {
            final Map map = (Map)currentObject;
            Object val = map.get(propertyName);
            if (val == null && (5614464919154503228L == propertyNameHash || -1580386065683472715L == propertyNameHash)) {
                val = map.size();
            }
            return val;
        }
        final Class<?> currentClass = currentObject.getClass();
        final JavaBeanSerializer beanSerializer = this.getJavaBeanSerializer(currentClass);
        if (beanSerializer != null) {
            try {
                return beanSerializer.getFieldValue(currentObject, propertyName, propertyNameHash, false);
            }
            catch (Exception e) {
                throw new JSONPathException("jsonpath error, path " + this.path + ", segement " + propertyName, e);
            }
        }
        if (currentObject instanceof List) {
            final List list = (List)currentObject;
            if (5614464919154503228L == propertyNameHash || -1580386065683472715L == propertyNameHash) {
                return list.size();
            }
            List<Object> fieldValues = null;
            for (int i = 0; i < list.size(); ++i) {
                final Object obj = list.get(i);
                if (obj == list) {
                    if (fieldValues == null) {
                        fieldValues = new JSONArray(list.size());
                    }
                    fieldValues.add(obj);
                }
                else {
                    final Object itemValue = this.getPropertyValue(obj, propertyName, propertyNameHash);
                    if (itemValue instanceof Collection) {
                        final Collection collection = (Collection)itemValue;
                        if (fieldValues == null) {
                            fieldValues = new JSONArray(list.size());
                        }
                        fieldValues.addAll(collection);
                    }
                    else if (itemValue != null) {
                        if (fieldValues == null) {
                            fieldValues = new JSONArray(list.size());
                        }
                        fieldValues.add(itemValue);
                    }
                }
            }
            if (fieldValues == null) {
                fieldValues = Collections.emptyList();
            }
            return fieldValues;
        }
        else {
            if (!(currentObject instanceof Object[])) {
                if (currentObject instanceof Enum) {
                    final long NAME = -4270347329889690746L;
                    final long ORDINAL = -1014497654951707614L;
                    final Enum e2 = (Enum)currentObject;
                    if (-4270347329889690746L == propertyNameHash) {
                        return e2.name();
                    }
                    if (-1014497654951707614L == propertyNameHash) {
                        return e2.ordinal();
                    }
                }
                if (currentObject instanceof Calendar) {
                    final long YEAR = 8963398325558730460L;
                    final long MONTH = -811277319855450459L;
                    final long DAY = -3851359326990528739L;
                    final long HOUR = 4647432019745535567L;
                    final long MINUTE = 6607618197526598121L;
                    final long SECOND = -6586085717218287427L;
                    final Calendar e3 = (Calendar)currentObject;
                    if (8963398325558730460L == propertyNameHash) {
                        return e3.get(1);
                    }
                    if (-811277319855450459L == propertyNameHash) {
                        return e3.get(2);
                    }
                    if (-3851359326990528739L == propertyNameHash) {
                        return e3.get(5);
                    }
                    if (4647432019745535567L == propertyNameHash) {
                        return e3.get(11);
                    }
                    if (6607618197526598121L == propertyNameHash) {
                        return e3.get(12);
                    }
                    if (-6586085717218287427L == propertyNameHash) {
                        return e3.get(13);
                    }
                }
                return null;
            }
            final Object[] array = (Object[])currentObject;
            if (5614464919154503228L == propertyNameHash || -1580386065683472715L == propertyNameHash) {
                return array.length;
            }
            final List<Object> fieldValues = new JSONArray(array.length);
            for (int i = 0; i < array.length; ++i) {
                final Object obj = array[i];
                if (obj == array) {
                    fieldValues.add(obj);
                }
                else {
                    final Object itemValue = this.getPropertyValue(obj, propertyName, propertyNameHash);
                    if (itemValue instanceof Collection) {
                        final Collection collection = (Collection)itemValue;
                        fieldValues.addAll(collection);
                    }
                    else if (itemValue != null) {
                        fieldValues.add(itemValue);
                    }
                }
            }
            return fieldValues;
        }
    }
    
    protected void deepScan(final Object currentObject, final String propertyName, final List<Object> results) {
        if (currentObject == null) {
            return;
        }
        if (currentObject instanceof Map) {
            final Map<?, ?> map = (Map<?, ?>)currentObject;
            for (final Map.Entry entry : map.entrySet()) {
                final Object val = entry.getValue();
                if (propertyName.equals(entry.getKey())) {
                    if (val instanceof Collection) {
                        results.addAll((Collection<?>)val);
                    }
                    else {
                        results.add(val);
                    }
                }
                else {
                    if (val == null) {
                        continue;
                    }
                    if (ParserConfig.isPrimitive2(val.getClass())) {
                        continue;
                    }
                    this.deepScan(val, propertyName, results);
                }
            }
            return;
        }
        if (currentObject instanceof Collection) {
            for (final Object next : (Collection)currentObject) {
                if (ParserConfig.isPrimitive2(next.getClass())) {
                    continue;
                }
                this.deepScan(next, propertyName, results);
            }
            return;
        }
        final Class<?> currentClass = currentObject.getClass();
        final JavaBeanSerializer beanSerializer = this.getJavaBeanSerializer(currentClass);
        if (beanSerializer != null) {
            try {
                final FieldSerializer fieldDeser = beanSerializer.getFieldSerializer(propertyName);
                if (fieldDeser != null) {
                    try {
                        final Object val = fieldDeser.getPropertyValueDirect(currentObject);
                        results.add(val);
                    }
                    catch (InvocationTargetException ex) {
                        throw new JSONException("getFieldValue error." + propertyName, ex);
                    }
                    catch (IllegalAccessException ex2) {
                        throw new JSONException("getFieldValue error." + propertyName, ex2);
                    }
                    return;
                }
                final List<Object> fieldValues = beanSerializer.getFieldValues(currentObject);
                for (final Object val2 : fieldValues) {
                    this.deepScan(val2, propertyName, results);
                }
                return;
            }
            catch (Exception e) {
                throw new JSONPathException("jsonpath error, path " + this.path + ", segement " + propertyName, e);
            }
        }
        if (currentObject instanceof List) {
            final List list = (List)currentObject;
            for (int i = 0; i < list.size(); ++i) {
                final Object val3 = list.get(i);
                this.deepScan(val3, propertyName, results);
            }
        }
    }
    
    protected void deepSet(final Object currentObject, final String propertyName, final long propertyNameHash, final Object value) {
        if (currentObject == null) {
            return;
        }
        if (currentObject instanceof Map) {
            final Map map = (Map)currentObject;
            if (map.containsKey(propertyName)) {
                final Object val = map.get(propertyName);
                map.put(propertyName, value);
                return;
            }
            for (final Object val2 : map.values()) {
                this.deepSet(val2, propertyName, propertyNameHash, value);
            }
        }
        else {
            final Class<?> currentClass = currentObject.getClass();
            final JavaBeanDeserializer beanDeserializer = this.getJavaBeanDeserializer(currentClass);
            if (beanDeserializer != null) {
                try {
                    final FieldDeserializer fieldDeser = beanDeserializer.getFieldDeserializer(propertyName);
                    if (fieldDeser != null) {
                        fieldDeser.setValue(currentObject, value);
                        return;
                    }
                    final JavaBeanSerializer beanSerializer = this.getJavaBeanSerializer(currentClass);
                    final List<Object> fieldValues = beanSerializer.getObjectFieldValues(currentObject);
                    for (final Object val3 : fieldValues) {
                        this.deepSet(val3, propertyName, propertyNameHash, value);
                    }
                    return;
                }
                catch (Exception e) {
                    throw new JSONPathException("jsonpath error, path " + this.path + ", segement " + propertyName, e);
                }
            }
            if (currentObject instanceof List) {
                final List list = (List)currentObject;
                for (int i = 0; i < list.size(); ++i) {
                    final Object val4 = list.get(i);
                    this.deepSet(val4, propertyName, propertyNameHash, value);
                }
            }
        }
    }
    
    protected boolean setPropertyValue(final Object parent, final String name, final long propertyNameHash, final Object value) {
        if (parent instanceof Map) {
            ((Map)parent).put(name, value);
            return true;
        }
        if (parent instanceof List) {
            for (final Object element : (List)parent) {
                if (element == null) {
                    continue;
                }
                this.setPropertyValue(element, name, propertyNameHash, value);
            }
            return true;
        }
        final ObjectDeserializer deserializer = this.parserConfig.getDeserializer(parent.getClass());
        JavaBeanDeserializer beanDeserializer = null;
        if (deserializer instanceof JavaBeanDeserializer) {
            beanDeserializer = (JavaBeanDeserializer)deserializer;
        }
        if (beanDeserializer == null) {
            throw new UnsupportedOperationException();
        }
        final FieldDeserializer fieldDeserializer = beanDeserializer.getFieldDeserializer(propertyNameHash);
        if (fieldDeserializer == null) {
            return false;
        }
        fieldDeserializer.setValue(parent, value);
        return true;
    }
    
    protected boolean removePropertyValue(final Object parent, final String name, final boolean deep) {
        if (parent instanceof Map) {
            final Object origin = ((Map)parent).remove(name);
            final boolean found = origin != null;
            if (deep) {
                for (final Object item : ((Map)parent).values()) {
                    this.removePropertyValue(item, name, deep);
                }
            }
            return found;
        }
        final ObjectDeserializer deserializer = this.parserConfig.getDeserializer(parent.getClass());
        JavaBeanDeserializer beanDeserializer = null;
        if (deserializer instanceof JavaBeanDeserializer) {
            beanDeserializer = (JavaBeanDeserializer)deserializer;
        }
        if (beanDeserializer != null) {
            final FieldDeserializer fieldDeserializer = beanDeserializer.getFieldDeserializer(name);
            boolean found2 = false;
            if (fieldDeserializer != null) {
                fieldDeserializer.setValue(parent, null);
                found2 = true;
            }
            if (deep) {
                final Collection<Object> propertyValues = this.getPropertyValues(parent);
                for (final Object item2 : propertyValues) {
                    if (item2 == null) {
                        continue;
                    }
                    this.removePropertyValue(item2, name, deep);
                }
            }
            return found2;
        }
        if (deep) {
            return false;
        }
        throw new UnsupportedOperationException();
    }
    
    protected JavaBeanSerializer getJavaBeanSerializer(final Class<?> currentClass) {
        JavaBeanSerializer beanSerializer = null;
        final ObjectSerializer serializer = this.serializeConfig.getObjectWriter(currentClass);
        if (serializer instanceof JavaBeanSerializer) {
            beanSerializer = (JavaBeanSerializer)serializer;
        }
        return beanSerializer;
    }
    
    protected JavaBeanDeserializer getJavaBeanDeserializer(final Class<?> currentClass) {
        JavaBeanDeserializer beanDeserializer = null;
        final ObjectDeserializer deserializer = this.parserConfig.getDeserializer(currentClass);
        if (deserializer instanceof JavaBeanDeserializer) {
            beanDeserializer = (JavaBeanDeserializer)deserializer;
        }
        return beanDeserializer;
    }
    
    int evalSize(final Object currentObject) {
        if (currentObject == null) {
            return -1;
        }
        if (currentObject instanceof Collection) {
            return ((Collection)currentObject).size();
        }
        if (currentObject instanceof Object[]) {
            return ((Object[])currentObject).length;
        }
        if (currentObject.getClass().isArray()) {
            return Array.getLength(currentObject);
        }
        if (currentObject instanceof Map) {
            int count = 0;
            for (final Object value : ((Map)currentObject).values()) {
                if (value != null) {
                    ++count;
                }
            }
            return count;
        }
        final JavaBeanSerializer beanSerializer = this.getJavaBeanSerializer(currentObject.getClass());
        if (beanSerializer == null) {
            return -1;
        }
        try {
            return beanSerializer.getSize(currentObject);
        }
        catch (Exception e) {
            throw new JSONPathException("evalSize error : " + this.path, e);
        }
    }
    
    Set<?> evalKeySet(final Object currentObject) {
        if (currentObject == null) {
            return null;
        }
        if (currentObject instanceof Map) {
            return ((Map)currentObject).keySet();
        }
        if (currentObject instanceof Collection || currentObject instanceof Object[] || currentObject.getClass().isArray()) {
            return null;
        }
        final JavaBeanSerializer beanSerializer = this.getJavaBeanSerializer(currentObject.getClass());
        if (beanSerializer == null) {
            return null;
        }
        try {
            return beanSerializer.getFieldNames(currentObject);
        }
        catch (Exception e) {
            throw new JSONPathException("evalKeySet error : " + this.path, e);
        }
    }
    
    @Override
    public String toJSONString() {
        return JSON.toJSONString(this.path);
    }
    
    public static Object reserveToArray(final Object object, final String... paths) {
        final JSONArray reserved = new JSONArray();
        if (paths == null || paths.length == 0) {
            return reserved;
        }
        for (final String item : paths) {
            final JSONPath path = compile(item);
            path.init();
            final Object value = path.eval(object);
            reserved.add(value);
        }
        return reserved;
    }
    
    public static Object reserveToObject(final Object object, final String... paths) {
        if (paths == null || paths.length == 0) {
            return object;
        }
        final JSONObject reserved = new JSONObject(true);
        for (final String item : paths) {
            final JSONPath path = compile(item);
            path.init();
            final Segment lastSegement = path.segments[path.segments.length - 1];
            if (lastSegement instanceof PropertySegment) {
                final Object value = path.eval(object);
                if (value != null) {
                    path.set(reserved, value);
                }
            }
        }
        return reserved;
    }
    
    static {
        JSONPath.pathCache = new ConcurrentHashMap<String, JSONPath>(128, 0.75f, 1);
    }
    
    private static class Context
    {
        final Context parent;
        final boolean eval;
        Object object;
        
        public Context(final Context parent, final boolean eval) {
            this.parent = parent;
            this.eval = eval;
        }
    }
    
    static class JSONPathParser
    {
        private final String path;
        private int pos;
        private char ch;
        private int level;
        private boolean hasRefSegment;
        private static final String strArrayRegex = "'\\s*,\\s*'";
        private static final Pattern strArrayPatternx;
        
        public JSONPathParser(final String path) {
            this.path = path;
            this.next();
        }
        
        void next() {
            this.ch = this.path.charAt(this.pos++);
        }
        
        char getNextChar() {
            return this.path.charAt(this.pos);
        }
        
        boolean isEOF() {
            return this.pos >= this.path.length();
        }
        
        Segment readSegement() {
            if (this.level == 0 && this.path.length() == 1) {
                if (isDigitFirst(this.ch)) {
                    final int index = this.ch - '0';
                    return new ArrayAccessSegment(index);
                }
                if ((this.ch >= 'a' && this.ch <= 'z') || (this.ch >= 'A' && this.ch <= 'Z')) {
                    return new PropertySegment(Character.toString(this.ch), false);
                }
            }
            while (!this.isEOF()) {
                this.skipWhitespace();
                if (this.ch == '$') {
                    this.next();
                }
                else if (this.ch == '.' || this.ch == '/') {
                    final int c0 = this.ch;
                    boolean deep = false;
                    this.next();
                    if (c0 == 46 && this.ch == '.') {
                        this.next();
                        deep = true;
                        if (this.path.length() > this.pos + 3 && this.ch == '[' && this.path.charAt(this.pos) == '*' && this.path.charAt(this.pos + 1) == ']' && this.path.charAt(this.pos + 2) == '.') {
                            this.next();
                            this.next();
                            this.next();
                            this.next();
                        }
                    }
                    if (this.ch == '*') {
                        if (!this.isEOF()) {
                            this.next();
                        }
                        return deep ? WildCardSegment.instance_deep : WildCardSegment.instance;
                    }
                    if (isDigitFirst(this.ch)) {
                        return this.parseArrayAccess(false);
                    }
                    final String propertyName = this.readName();
                    if (this.ch != '(') {
                        return new PropertySegment(propertyName, deep);
                    }
                    this.next();
                    if (this.ch != ')') {
                        throw new JSONPathException("not support jsonpath : " + this.path);
                    }
                    if (!this.isEOF()) {
                        this.next();
                    }
                    if ("size".equals(propertyName) || "length".equals(propertyName)) {
                        return SizeSegment.instance;
                    }
                    if ("max".equals(propertyName)) {
                        return MaxSegment.instance;
                    }
                    if ("min".equals(propertyName)) {
                        return MinSegment.instance;
                    }
                    if ("keySet".equals(propertyName)) {
                        return KeySetSegment.instance;
                    }
                    throw new JSONPathException("not support jsonpath : " + this.path);
                }
                else {
                    if (this.ch == '[') {
                        return this.parseArrayAccess(true);
                    }
                    if (this.level == 0) {
                        final String propertyName2 = this.readName();
                        return new PropertySegment(propertyName2, false);
                    }
                    throw new JSONPathException("not support jsonpath : " + this.path);
                }
            }
            return null;
        }
        
        public final void skipWhitespace() {
            while (this.ch <= ' ' && (this.ch == ' ' || this.ch == '\r' || this.ch == '\n' || this.ch == '\t' || this.ch == '\f' || this.ch == '\b')) {
                this.next();
            }
        }
        
        Segment parseArrayAccess(final boolean acceptBracket) {
            final Object object = this.parseArrayAccessFilter(acceptBracket);
            if (object instanceof Segment) {
                return (Segment)object;
            }
            return new FilterSegment((Filter)object);
        }
        
        Object parseArrayAccessFilter(final boolean acceptBracket) {
            if (acceptBracket) {
                this.accept('[');
            }
            boolean predicateFlag = false;
            int lparanCount = 0;
            if (this.ch == '?') {
                this.next();
                this.accept('(');
                ++lparanCount;
                while (this.ch == '(') {
                    this.next();
                    ++lparanCount;
                }
                predicateFlag = true;
            }
            if (predicateFlag || IOUtils.firstIdentifier(this.ch) || this.ch == '\\' || this.ch == '@') {
                boolean self = false;
                if (this.ch == '@') {
                    this.next();
                    this.accept('.');
                    self = true;
                }
                final String propertyName = this.readName();
                this.skipWhitespace();
                if (predicateFlag && this.ch == ')') {
                    this.next();
                    Filter filter = new NotNullSegement(propertyName);
                    while (this.ch == ' ') {
                        this.next();
                    }
                    if (this.ch == '&' || this.ch == '|') {
                        filter = this.filterRest(filter);
                    }
                    if (acceptBracket) {
                        this.accept(']');
                    }
                    return filter;
                }
                if (acceptBracket && this.ch == ']') {
                    this.next();
                    Filter filter = new NotNullSegement(propertyName);
                    while (this.ch == ' ') {
                        this.next();
                    }
                    if (this.ch == '&' || this.ch == '|') {
                        filter = this.filterRest(filter);
                    }
                    this.accept(')');
                    if (predicateFlag) {
                        this.accept(')');
                    }
                    if (acceptBracket) {
                        this.accept(']');
                    }
                    return filter;
                }
                Operator op = this.readOp();
                this.skipWhitespace();
                if (op == Operator.BETWEEN || op == Operator.NOT_BETWEEN) {
                    final boolean not = op == Operator.NOT_BETWEEN;
                    final Object startValue = this.readValue();
                    final String name = this.readName();
                    if (!"and".equalsIgnoreCase(name)) {
                        throw new JSONPathException(this.path);
                    }
                    final Object endValue = this.readValue();
                    if (startValue == null || endValue == null) {
                        throw new JSONPathException(this.path);
                    }
                    if (JSONPath.isInt(startValue.getClass()) && JSONPath.isInt(endValue.getClass())) {
                        final Filter filter2 = new IntBetweenSegement(propertyName, TypeUtils.longExtractValue((Number)startValue), TypeUtils.longExtractValue((Number)endValue), not);
                        return filter2;
                    }
                    throw new JSONPathException(this.path);
                }
                else if (op == Operator.IN || op == Operator.NOT_IN) {
                    final boolean not = op == Operator.NOT_IN;
                    this.accept('(');
                    final List<Object> valueList = new JSONArray();
                    Object value = this.readValue();
                    valueList.add(value);
                    while (true) {
                        this.skipWhitespace();
                        if (this.ch != ',') {
                            break;
                        }
                        this.next();
                        value = this.readValue();
                        valueList.add(value);
                    }
                    boolean isInt = true;
                    boolean isIntObj = true;
                    boolean isString = true;
                    for (final Object item : valueList) {
                        if (item == null) {
                            if (!isInt) {
                                continue;
                            }
                            isInt = false;
                        }
                        else {
                            final Class<?> clazz = item.getClass();
                            if (isInt && clazz != Byte.class && clazz != Short.class && clazz != Integer.class && clazz != Long.class) {
                                isInt = false;
                                isIntObj = false;
                            }
                            if (!isString || clazz == String.class) {
                                continue;
                            }
                            isString = false;
                        }
                    }
                    if (valueList.size() == 1 && valueList.get(0) == null) {
                        Filter filter3;
                        if (not) {
                            filter3 = new NotNullSegement(propertyName);
                        }
                        else {
                            filter3 = new NullSegement(propertyName);
                        }
                        while (this.ch == ' ') {
                            this.next();
                        }
                        if (this.ch == '&' || this.ch == '|') {
                            filter3 = this.filterRest(filter3);
                        }
                        this.accept(')');
                        if (predicateFlag) {
                            this.accept(')');
                        }
                        if (acceptBracket) {
                            this.accept(']');
                        }
                        return filter3;
                    }
                    if (isInt) {
                        if (valueList.size() == 1) {
                            final long value2 = TypeUtils.longExtractValue(valueList.get(0));
                            final Operator intOp = not ? Operator.NE : Operator.EQ;
                            Filter filter4 = new IntOpSegement(propertyName, value2, intOp);
                            while (this.ch == ' ') {
                                this.next();
                            }
                            if (this.ch == '&' || this.ch == '|') {
                                filter4 = this.filterRest(filter4);
                            }
                            this.accept(')');
                            if (predicateFlag) {
                                this.accept(')');
                            }
                            if (acceptBracket) {
                                this.accept(']');
                            }
                            return filter4;
                        }
                        final long[] values = new long[valueList.size()];
                        for (int i = 0; i < values.length; ++i) {
                            values[i] = TypeUtils.longExtractValue(valueList.get(i));
                        }
                        Filter filter5 = new IntInSegement(propertyName, values, not);
                        while (this.ch == ' ') {
                            this.next();
                        }
                        if (this.ch == '&' || this.ch == '|') {
                            filter5 = this.filterRest(filter5);
                        }
                        this.accept(')');
                        if (predicateFlag) {
                            this.accept(')');
                        }
                        if (acceptBracket) {
                            this.accept(']');
                        }
                        return filter5;
                    }
                    else if (isString) {
                        if (valueList.size() == 1) {
                            final String value3 = valueList.get(0);
                            final Operator intOp2 = not ? Operator.NE : Operator.EQ;
                            Filter filter6 = new StringOpSegement(propertyName, value3, intOp2);
                            while (this.ch == ' ') {
                                this.next();
                            }
                            if (this.ch == '&' || this.ch == '|') {
                                filter6 = this.filterRest(filter6);
                            }
                            this.accept(')');
                            if (predicateFlag) {
                                this.accept(')');
                            }
                            if (acceptBracket) {
                                this.accept(']');
                            }
                            return filter6;
                        }
                        final String[] values2 = new String[valueList.size()];
                        valueList.toArray(values2);
                        Filter filter5 = new StringInSegement(propertyName, values2, not);
                        while (this.ch == ' ') {
                            this.next();
                        }
                        if (this.ch == '&' || this.ch == '|') {
                            filter5 = this.filterRest(filter5);
                        }
                        this.accept(')');
                        if (predicateFlag) {
                            this.accept(')');
                        }
                        if (acceptBracket) {
                            this.accept(']');
                        }
                        return filter5;
                    }
                    else {
                        if (isIntObj) {
                            final Long[] values3 = new Long[valueList.size()];
                            for (int i = 0; i < values3.length; ++i) {
                                final Number item2 = valueList.get(i);
                                if (item2 != null) {
                                    values3[i] = TypeUtils.longExtractValue(item2);
                                }
                            }
                            Filter filter5 = new IntObjInSegement(propertyName, values3, not);
                            while (this.ch == ' ') {
                                this.next();
                            }
                            if (this.ch == '&' || this.ch == '|') {
                                filter5 = this.filterRest(filter5);
                            }
                            this.accept(')');
                            if (predicateFlag) {
                                this.accept(')');
                            }
                            if (acceptBracket) {
                                this.accept(']');
                            }
                            return filter5;
                        }
                        throw new UnsupportedOperationException();
                    }
                }
                else {
                    if (this.ch == '\'' || this.ch == '\"') {
                        String strValue = this.readString();
                        Filter filter7 = null;
                        if (op == Operator.RLIKE) {
                            filter7 = new RlikeSegement(propertyName, strValue, false);
                        }
                        else if (op == Operator.NOT_RLIKE) {
                            filter7 = new RlikeSegement(propertyName, strValue, true);
                        }
                        else if (op == Operator.LIKE || op == Operator.NOT_LIKE) {
                            while (strValue.indexOf("%%") != -1) {
                                strValue = strValue.replaceAll("%%", "%");
                            }
                            final boolean not2 = op == Operator.NOT_LIKE;
                            final int p0 = strValue.indexOf(37);
                            if (p0 == -1) {
                                if (op == Operator.LIKE) {
                                    op = Operator.EQ;
                                }
                                else {
                                    op = Operator.NE;
                                }
                                filter7 = new StringOpSegement(propertyName, strValue, op);
                            }
                            else {
                                final String[] items = strValue.split("%");
                                String startsWithValue = null;
                                String endsWithValue = null;
                                String[] containsValues = null;
                                if (p0 == 0) {
                                    if (strValue.charAt(strValue.length() - 1) == '%') {
                                        containsValues = new String[items.length - 1];
                                        System.arraycopy(items, 1, containsValues, 0, containsValues.length);
                                    }
                                    else {
                                        endsWithValue = items[items.length - 1];
                                        if (items.length > 2) {
                                            containsValues = new String[items.length - 2];
                                            System.arraycopy(items, 1, containsValues, 0, containsValues.length);
                                        }
                                    }
                                }
                                else if (strValue.charAt(strValue.length() - 1) == '%') {
                                    if (items.length == 1) {
                                        startsWithValue = items[0];
                                    }
                                    else {
                                        containsValues = items;
                                    }
                                }
                                else if (items.length == 1) {
                                    startsWithValue = items[0];
                                }
                                else if (items.length == 2) {
                                    startsWithValue = items[0];
                                    endsWithValue = items[1];
                                }
                                else {
                                    startsWithValue = items[0];
                                    endsWithValue = items[items.length - 1];
                                    containsValues = new String[items.length - 2];
                                    System.arraycopy(items, 1, containsValues, 0, containsValues.length);
                                }
                                filter7 = new MatchSegement(propertyName, startsWithValue, endsWithValue, containsValues, not2);
                            }
                        }
                        else {
                            filter7 = new StringOpSegement(propertyName, strValue, op);
                        }
                        while (this.ch == ' ') {
                            this.next();
                        }
                        if (this.ch == '&' || this.ch == '|') {
                            filter7 = this.filterRest(filter7);
                        }
                        if (predicateFlag) {
                            this.accept(')');
                        }
                        if (acceptBracket) {
                            this.accept(']');
                        }
                        return filter7;
                    }
                    if (isDigitFirst(this.ch)) {
                        final long value4 = this.readLongValue();
                        double doubleValue = 0.0;
                        if (this.ch == '.') {
                            doubleValue = this.readDoubleValue(value4);
                        }
                        Filter filter2;
                        if (doubleValue == 0.0) {
                            filter2 = new IntOpSegement(propertyName, value4, op);
                        }
                        else {
                            filter2 = new DoubleOpSegement(propertyName, doubleValue, op);
                        }
                        while (this.ch == ' ') {
                            this.next();
                        }
                        if (lparanCount > 1 && this.ch == ')') {
                            this.next();
                            --lparanCount;
                        }
                        if (this.ch == '&' || this.ch == '|') {
                            filter2 = this.filterRest(filter2);
                        }
                        if (predicateFlag) {
                            --lparanCount;
                            this.accept(')');
                        }
                        if (acceptBracket) {
                            this.accept(']');
                        }
                        return filter2;
                    }
                    if (this.ch == '$') {
                        final Segment segment = this.readSegement();
                        final RefOpSegement filter8 = new RefOpSegement(propertyName, segment, op);
                        this.hasRefSegment = true;
                        while (this.ch == ' ') {
                            this.next();
                        }
                        if (predicateFlag) {
                            this.accept(')');
                        }
                        if (acceptBracket) {
                            this.accept(']');
                        }
                        return filter8;
                    }
                    if (this.ch == '/') {
                        int flags = 0;
                        final StringBuilder regBuf = new StringBuilder();
                        while (true) {
                            this.next();
                            if (this.ch == '/') {
                                break;
                            }
                            if (this.ch == '\\') {
                                this.next();
                                regBuf.append(this.ch);
                            }
                            else {
                                regBuf.append(this.ch);
                            }
                        }
                        this.next();
                        if (this.ch == 'i') {
                            this.next();
                            flags |= 0x2;
                        }
                        final Pattern pattern = Pattern.compile(regBuf.toString(), flags);
                        final RegMatchSegement filter9 = new RegMatchSegement(propertyName, pattern, op);
                        if (predicateFlag) {
                            this.accept(')');
                        }
                        if (acceptBracket) {
                            this.accept(']');
                        }
                        return filter9;
                    }
                    if (this.ch == 'n') {
                        final String name2 = this.readName();
                        if ("null".equals(name2)) {
                            Filter filter7 = null;
                            if (op == Operator.EQ) {
                                filter7 = new NullSegement(propertyName);
                            }
                            else if (op == Operator.NE) {
                                filter7 = new NotNullSegement(propertyName);
                            }
                            if (filter7 != null) {
                                while (this.ch == ' ') {
                                    this.next();
                                }
                                if (this.ch == '&' || this.ch == '|') {
                                    filter7 = this.filterRest(filter7);
                                }
                            }
                            if (predicateFlag) {
                                this.accept(')');
                            }
                            this.accept(']');
                            if (filter7 != null) {
                                return filter7;
                            }
                            throw new UnsupportedOperationException();
                        }
                    }
                    else if (this.ch == 't') {
                        final String name2 = this.readName();
                        if ("true".equals(name2)) {
                            Filter filter7 = null;
                            if (op == Operator.EQ) {
                                filter7 = new ValueSegment(propertyName, Boolean.TRUE, true);
                            }
                            else if (op == Operator.NE) {
                                filter7 = new ValueSegment(propertyName, Boolean.TRUE, false);
                            }
                            if (filter7 != null) {
                                while (this.ch == ' ') {
                                    this.next();
                                }
                                if (this.ch == '&' || this.ch == '|') {
                                    filter7 = this.filterRest(filter7);
                                }
                            }
                            if (predicateFlag) {
                                this.accept(')');
                            }
                            this.accept(']');
                            if (filter7 != null) {
                                return filter7;
                            }
                            throw new UnsupportedOperationException();
                        }
                    }
                    else if (this.ch == 'f') {
                        final String name2 = this.readName();
                        if ("false".equals(name2)) {
                            Filter filter7 = null;
                            if (op == Operator.EQ) {
                                filter7 = new ValueSegment(propertyName, Boolean.FALSE, true);
                            }
                            else if (op == Operator.NE) {
                                filter7 = new ValueSegment(propertyName, Boolean.FALSE, false);
                            }
                            if (filter7 != null) {
                                while (this.ch == ' ') {
                                    this.next();
                                }
                                if (this.ch == '&' || this.ch == '|') {
                                    filter7 = this.filterRest(filter7);
                                }
                            }
                            if (predicateFlag) {
                                this.accept(')');
                            }
                            this.accept(']');
                            if (filter7 != null) {
                                return filter7;
                            }
                            throw new UnsupportedOperationException();
                        }
                    }
                    throw new UnsupportedOperationException();
                }
            }
            else {
                final int start = this.pos - 1;
                final char startCh = this.ch;
                while (this.ch != ']' && this.ch != '/' && !this.isEOF() && (this.ch != '.' || predicateFlag || predicateFlag || startCh == '\'')) {
                    if (this.ch == '\\') {
                        this.next();
                    }
                    this.next();
                }
                int end;
                if (acceptBracket) {
                    end = this.pos - 1;
                }
                else if (this.ch == '/' || this.ch == '.') {
                    end = this.pos - 1;
                }
                else {
                    end = this.pos;
                }
                final String text = this.path.substring(start, end);
                if (text.indexOf("\\.") != -1) {
                    String propName;
                    if (startCh == '\'' && text.length() > 2 && text.charAt(text.length() - 1) == startCh) {
                        propName = text.substring(1, text.length() - 1);
                    }
                    else {
                        propName = text.replaceAll("\\\\\\.", "\\.");
                        if (propName.indexOf("\\-") != -1) {
                            propName = propName.replaceAll("\\\\-", "-");
                        }
                    }
                    if (predicateFlag) {
                        this.accept(')');
                    }
                    return new PropertySegment(propName, false);
                }
                final Segment segment2 = this.buildArraySegement(text);
                if (acceptBracket && !this.isEOF()) {
                    this.accept(']');
                }
                return segment2;
            }
        }
        
        Filter filterRest(Filter filter) {
            final boolean and = this.ch == '&';
            if ((this.ch == '&' && this.getNextChar() == '&') || (this.ch == '|' && this.getNextChar() == '|')) {
                this.next();
                this.next();
                boolean paren = false;
                if (this.ch == '(') {
                    paren = true;
                    this.next();
                }
                while (this.ch == ' ') {
                    this.next();
                }
                final Filter right = (Filter)this.parseArrayAccessFilter(false);
                filter = new FilterGroup(filter, right, and);
                if (paren && this.ch == ')') {
                    this.next();
                }
            }
            return filter;
        }
        
        protected long readLongValue() {
            final int beginIndex = this.pos - 1;
            if (this.ch == '+' || this.ch == '-') {
                this.next();
            }
            while (this.ch >= '0' && this.ch <= '9') {
                this.next();
            }
            final int endIndex = this.pos - 1;
            final String text = this.path.substring(beginIndex, endIndex);
            final long value = Long.parseLong(text);
            return value;
        }
        
        protected double readDoubleValue(final long longValue) {
            final int beginIndex = this.pos - 1;
            this.next();
            while (this.ch >= '0' && this.ch <= '9') {
                this.next();
            }
            final int endIndex = this.pos - 1;
            final String text = this.path.substring(beginIndex, endIndex);
            double value = Double.parseDouble(text);
            value += longValue;
            return value;
        }
        
        protected Object readValue() {
            this.skipWhitespace();
            if (isDigitFirst(this.ch)) {
                return this.readLongValue();
            }
            if (this.ch == '\"' || this.ch == '\'') {
                return this.readString();
            }
            if (this.ch != 'n') {
                throw new UnsupportedOperationException();
            }
            final String name = this.readName();
            if ("null".equals(name)) {
                return null;
            }
            throw new JSONPathException(this.path);
        }
        
        static boolean isDigitFirst(final char ch) {
            return ch == '-' || ch == '+' || (ch >= '0' && ch <= '9');
        }
        
        protected Operator readOp() {
            Operator op = null;
            if (this.ch == '=') {
                this.next();
                if (this.ch == '~') {
                    this.next();
                    op = Operator.REG_MATCH;
                }
                else if (this.ch == '=') {
                    this.next();
                    op = Operator.EQ;
                }
                else {
                    op = Operator.EQ;
                }
            }
            else if (this.ch == '!') {
                this.next();
                this.accept('=');
                op = Operator.NE;
            }
            else if (this.ch == '<') {
                this.next();
                if (this.ch == '=') {
                    this.next();
                    op = Operator.LE;
                }
                else {
                    op = Operator.LT;
                }
            }
            else if (this.ch == '>') {
                this.next();
                if (this.ch == '=') {
                    this.next();
                    op = Operator.GE;
                }
                else {
                    op = Operator.GT;
                }
            }
            if (op == null) {
                String name = this.readName();
                if ("not".equalsIgnoreCase(name)) {
                    this.skipWhitespace();
                    name = this.readName();
                    if ("like".equalsIgnoreCase(name)) {
                        op = Operator.NOT_LIKE;
                    }
                    else if ("rlike".equalsIgnoreCase(name)) {
                        op = Operator.NOT_RLIKE;
                    }
                    else if ("in".equalsIgnoreCase(name)) {
                        op = Operator.NOT_IN;
                    }
                    else {
                        if (!"between".equalsIgnoreCase(name)) {
                            throw new UnsupportedOperationException();
                        }
                        op = Operator.NOT_BETWEEN;
                    }
                }
                else if ("nin".equalsIgnoreCase(name)) {
                    op = Operator.NOT_IN;
                }
                else if ("like".equalsIgnoreCase(name)) {
                    op = Operator.LIKE;
                }
                else if ("rlike".equalsIgnoreCase(name)) {
                    op = Operator.RLIKE;
                }
                else if ("in".equalsIgnoreCase(name)) {
                    op = Operator.IN;
                }
                else {
                    if (!"between".equalsIgnoreCase(name)) {
                        throw new UnsupportedOperationException();
                    }
                    op = Operator.BETWEEN;
                }
            }
            return op;
        }
        
        String readName() {
            this.skipWhitespace();
            if (this.ch != '\\' && !Character.isJavaIdentifierStart(this.ch)) {
                throw new JSONPathException("illeal jsonpath syntax. " + this.path);
            }
            final StringBuilder buf = new StringBuilder();
            while (!this.isEOF()) {
                if (this.ch == '\\') {
                    this.next();
                    buf.append(this.ch);
                    if (this.isEOF()) {
                        return buf.toString();
                    }
                    this.next();
                }
                else {
                    final boolean identifierFlag = Character.isJavaIdentifierPart(this.ch);
                    if (!identifierFlag) {
                        break;
                    }
                    buf.append(this.ch);
                    this.next();
                }
            }
            if (this.isEOF() && Character.isJavaIdentifierPart(this.ch)) {
                buf.append(this.ch);
            }
            return buf.toString();
        }
        
        String readString() {
            final char quoate = this.ch;
            this.next();
            final int beginIndex = this.pos - 1;
            while (this.ch != quoate && !this.isEOF()) {
                this.next();
            }
            final String strValue = this.path.substring(beginIndex, this.isEOF() ? this.pos : (this.pos - 1));
            this.accept(quoate);
            return strValue;
        }
        
        void accept(final char expect) {
            if (this.ch != expect) {
                throw new JSONPathException("expect '" + expect + ", but '" + this.ch + "'");
            }
            if (!this.isEOF()) {
                this.next();
            }
        }
        
        public Segment[] explain() {
            if (this.path == null || this.path.length() == 0) {
                throw new IllegalArgumentException();
            }
            Segment[] segments = new Segment[8];
            while (true) {
                final Segment segment = this.readSegement();
                if (segment == null) {
                    break;
                }
                if (segment instanceof PropertySegment) {
                    final PropertySegment propertySegment = (PropertySegment)segment;
                    if (!propertySegment.deep && propertySegment.propertyName.equals("*")) {
                        continue;
                    }
                }
                if (this.level == segments.length) {
                    final Segment[] t = new Segment[this.level * 3 / 2];
                    System.arraycopy(segments, 0, t, 0, this.level);
                    segments = t;
                }
                segments[this.level++] = segment;
            }
            if (this.level == segments.length) {
                return segments;
            }
            final Segment[] result = new Segment[this.level];
            System.arraycopy(segments, 0, result, 0, this.level);
            return result;
        }
        
        Segment buildArraySegement(String indexText) {
            final int indexTextLen = indexText.length();
            final char firstChar = indexText.charAt(0);
            final char lastChar = indexText.charAt(indexTextLen - 1);
            final int commaIndex = indexText.indexOf(44);
            if (indexText.length() > 2 && firstChar == '\'' && lastChar == '\'') {
                final String propertyName = indexText.substring(1, indexTextLen - 1);
                if (commaIndex == -1 || !JSONPathParser.strArrayPatternx.matcher(indexText).find()) {
                    return new PropertySegment(propertyName, false);
                }
                final String[] propertyNames = propertyName.split("'\\s*,\\s*'");
                return new MultiPropertySegment(propertyNames);
            }
            else {
                final int colonIndex = indexText.indexOf(58);
                if (commaIndex == -1 && colonIndex == -1) {
                    if (TypeUtils.isNumber(indexText)) {
                        try {
                            final int index = Integer.parseInt(indexText);
                            return new ArrayAccessSegment(index);
                        }
                        catch (NumberFormatException ex) {
                            return new PropertySegment(indexText, false);
                        }
                    }
                    if (indexText.charAt(0) == '\"' && indexText.charAt(indexText.length() - 1) == '\"') {
                        indexText = indexText.substring(1, indexText.length() - 1);
                    }
                    return new PropertySegment(indexText, false);
                }
                if (commaIndex != -1) {
                    final String[] indexesText = indexText.split(",");
                    final int[] indexes = new int[indexesText.length];
                    for (int i = 0; i < indexesText.length; ++i) {
                        indexes[i] = Integer.parseInt(indexesText[i]);
                    }
                    return new MultiIndexSegment(indexes);
                }
                if (colonIndex == -1) {
                    throw new UnsupportedOperationException();
                }
                final String[] indexesText = indexText.split(":");
                final int[] indexes = new int[indexesText.length];
                for (int i = 0; i < indexesText.length; ++i) {
                    final String str = indexesText[i];
                    if (str.length() == 0) {
                        if (i != 0) {
                            throw new UnsupportedOperationException();
                        }
                        indexes[i] = 0;
                    }
                    else {
                        indexes[i] = Integer.parseInt(str);
                    }
                }
                final int start = indexes[0];
                int end;
                if (indexes.length > 1) {
                    end = indexes[1];
                }
                else {
                    end = -1;
                }
                int step;
                if (indexes.length == 3) {
                    step = indexes[2];
                }
                else {
                    step = 1;
                }
                if (end >= 0 && end < start) {
                    throw new UnsupportedOperationException("end must greater than or equals start. start " + start + ",  end " + end);
                }
                if (step <= 0) {
                    throw new UnsupportedOperationException("step must greater than zero : " + step);
                }
                return new RangeSegment(start, end, step);
            }
        }
        
        static {
            strArrayPatternx = Pattern.compile("'\\s*,\\s*'");
        }
    }
    
    static class SizeSegment implements Segment
    {
        public static final SizeSegment instance;
        
        @Override
        public Integer eval(final JSONPath path, final Object rootObject, final Object currentObject) {
            return path.evalSize(currentObject);
        }
        
        @Override
        public void extract(final JSONPath path, final DefaultJSONParser parser, final Context context) {
            throw new UnsupportedOperationException();
        }
        
        static {
            instance = new SizeSegment();
        }
    }
    
    static class MaxSegment implements Segment
    {
        public static final MaxSegment instance;
        
        @Override
        public Object eval(final JSONPath path, final Object rootObject, final Object currentObject) {
            Object max = null;
            if (currentObject instanceof Collection) {
                for (final Object next : (Collection)currentObject) {
                    if (next == null) {
                        continue;
                    }
                    if (max == null) {
                        max = next;
                    }
                    else {
                        if (JSONPath.compare(max, next) >= 0) {
                            continue;
                        }
                        max = next;
                    }
                }
                return max;
            }
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void extract(final JSONPath path, final DefaultJSONParser parser, final Context context) {
            throw new UnsupportedOperationException();
        }
        
        static {
            instance = new MaxSegment();
        }
    }
    
    static class MinSegment implements Segment
    {
        public static final MinSegment instance;
        
        @Override
        public Object eval(final JSONPath path, final Object rootObject, final Object currentObject) {
            Object min = null;
            if (currentObject instanceof Collection) {
                for (final Object next : (Collection)currentObject) {
                    if (next == null) {
                        continue;
                    }
                    if (min == null) {
                        min = next;
                    }
                    else {
                        if (JSONPath.compare(min, next) <= 0) {
                            continue;
                        }
                        min = next;
                    }
                }
                return min;
            }
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void extract(final JSONPath path, final DefaultJSONParser parser, final Context context) {
            throw new UnsupportedOperationException();
        }
        
        static {
            instance = new MinSegment();
        }
    }
    
    static class KeySetSegment implements Segment
    {
        public static final KeySetSegment instance;
        
        @Override
        public Object eval(final JSONPath path, final Object rootObject, final Object currentObject) {
            return path.evalKeySet(currentObject);
        }
        
        @Override
        public void extract(final JSONPath path, final DefaultJSONParser parser, final Context context) {
            throw new UnsupportedOperationException();
        }
        
        static {
            instance = new KeySetSegment();
        }
    }
    
    static class PropertySegment implements Segment
    {
        private final String propertyName;
        private final long propertyNameHash;
        private final boolean deep;
        
        public PropertySegment(final String propertyName, final boolean deep) {
            this.propertyName = propertyName;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName);
            this.deep = deep;
        }
        
        @Override
        public Object eval(final JSONPath path, final Object rootObject, final Object currentObject) {
            if (this.deep) {
                final List<Object> results = new ArrayList<Object>();
                path.deepScan(currentObject, this.propertyName, results);
                return results;
            }
            return path.getPropertyValue(currentObject, this.propertyName, this.propertyNameHash);
        }
        
        @Override
        public void extract(final JSONPath path, final DefaultJSONParser parser, final Context context) {
            final JSONLexerBase lexer = (JSONLexerBase)parser.lexer;
            if (this.deep && context.object == null) {
                context.object = new JSONArray();
            }
            if (lexer.token() == 14) {
                if ("*".equals(this.propertyName)) {
                    return;
                }
                lexer.nextToken();
                JSONArray array;
                if (this.deep) {
                    array = (JSONArray)context.object;
                }
                else {
                    array = new JSONArray();
                }
                while (true) {
                    switch (lexer.token()) {
                        case 12: {
                            if (this.deep) {
                                this.extract(path, parser, context);
                                break;
                            }
                            final int matchStat = lexer.seekObjectToField(this.propertyNameHash, this.deep);
                            if (matchStat == 3) {
                                Object value = null;
                                switch (lexer.token()) {
                                    case 2: {
                                        value = lexer.integerValue();
                                        lexer.nextToken();
                                        break;
                                    }
                                    case 4: {
                                        value = lexer.stringVal();
                                        lexer.nextToken();
                                        break;
                                    }
                                    default: {
                                        value = parser.parse();
                                        break;
                                    }
                                }
                                array.add(value);
                                if (lexer.token() == 13) {
                                    lexer.nextToken();
                                    continue;
                                }
                                lexer.skipObject(false);
                                break;
                            }
                            else {
                                if (matchStat == -1) {
                                    continue;
                                }
                                if (this.deep) {
                                    throw new UnsupportedOperationException(lexer.info());
                                }
                                lexer.skipObject(false);
                                break;
                            }
                            break;
                        }
                        case 14: {
                            if (this.deep) {
                                this.extract(path, parser, context);
                                break;
                            }
                            lexer.skipObject(false);
                            break;
                        }
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8: {
                            lexer.nextToken();
                            break;
                        }
                    }
                    if (lexer.token() == 15) {
                        lexer.nextToken();
                        if (!this.deep && array.size() > 0) {
                            context.object = array;
                        }
                        return;
                    }
                    if (lexer.token() != 16) {
                        throw new JSONException("illegal json : " + lexer.info());
                    }
                    lexer.nextToken();
                }
            }
            else {
                Object value2 = null;
                if (!this.deep) {
                    final int matchStat2 = lexer.seekObjectToField(this.propertyNameHash, this.deep);
                    if (matchStat2 == 3 && context.eval) {
                        switch (lexer.token()) {
                            case 2: {
                                value2 = lexer.integerValue();
                                lexer.nextToken(16);
                                break;
                            }
                            case 3: {
                                value2 = lexer.decimalValue();
                                lexer.nextToken(16);
                                break;
                            }
                            case 4: {
                                value2 = lexer.stringVal();
                                lexer.nextToken(16);
                                break;
                            }
                            default: {
                                value2 = parser.parse();
                                break;
                            }
                        }
                        if (context.eval) {
                            context.object = value2;
                        }
                    }
                    return;
                }
                while (true) {
                    final int matchStat2 = lexer.seekObjectToField(this.propertyNameHash, this.deep);
                    if (matchStat2 == -1) {
                        break;
                    }
                    if (matchStat2 == 3) {
                        if (!context.eval) {
                            continue;
                        }
                        switch (lexer.token()) {
                            case 2: {
                                value2 = lexer.integerValue();
                                lexer.nextToken(16);
                                break;
                            }
                            case 3: {
                                value2 = lexer.decimalValue();
                                lexer.nextToken(16);
                                break;
                            }
                            case 4: {
                                value2 = lexer.stringVal();
                                lexer.nextToken(16);
                                break;
                            }
                            default: {
                                value2 = parser.parse();
                                break;
                            }
                        }
                        if (!context.eval) {
                            continue;
                        }
                        if (context.object instanceof List) {
                            final List list = (List)context.object;
                            if (list.size() == 0 && value2 instanceof List) {
                                context.object = value2;
                            }
                            else {
                                list.add(value2);
                            }
                        }
                        else {
                            context.object = value2;
                        }
                    }
                    else {
                        if (matchStat2 != 1 && matchStat2 != 2) {
                            continue;
                        }
                        this.extract(path, parser, context);
                    }
                }
            }
        }
        
        public void setValue(final JSONPath path, final Object parent, final Object value) {
            if (this.deep) {
                path.deepSet(parent, this.propertyName, this.propertyNameHash, value);
            }
            else {
                path.setPropertyValue(parent, this.propertyName, this.propertyNameHash, value);
            }
        }
        
        public boolean remove(final JSONPath path, final Object parent) {
            return path.removePropertyValue(parent, this.propertyName, this.deep);
        }
    }
    
    static class MultiPropertySegment implements Segment
    {
        private final String[] propertyNames;
        private final long[] propertyNamesHash;
        
        public MultiPropertySegment(final String[] propertyNames) {
            this.propertyNames = propertyNames;
            this.propertyNamesHash = new long[propertyNames.length];
            for (int i = 0; i < this.propertyNamesHash.length; ++i) {
                this.propertyNamesHash[i] = TypeUtils.fnv1a_64(propertyNames[i]);
            }
        }
        
        @Override
        public Object eval(final JSONPath path, final Object rootObject, final Object currentObject) {
            final List<Object> fieldValues = new ArrayList<Object>(this.propertyNames.length);
            for (int i = 0; i < this.propertyNames.length; ++i) {
                final Object fieldValue = path.getPropertyValue(currentObject, this.propertyNames[i], this.propertyNamesHash[i]);
                fieldValues.add(fieldValue);
            }
            return fieldValues;
        }
        
        @Override
        public void extract(final JSONPath path, final DefaultJSONParser parser, final Context context) {
            final JSONLexerBase lexer = (JSONLexerBase)parser.lexer;
            JSONArray array;
            if (context.object == null) {
                array = (JSONArray)(context.object = new JSONArray());
            }
            else {
                array = (JSONArray)context.object;
            }
            for (int i = array.size(); i < this.propertyNamesHash.length; ++i) {
                array.add(null);
            }
            do {
                final int index = lexer.seekObjectToField(this.propertyNamesHash);
                final int matchStat = lexer.matchStat;
                if (matchStat != 3) {
                    break;
                }
                Object value = null;
                switch (lexer.token()) {
                    case 2: {
                        value = lexer.integerValue();
                        lexer.nextToken(16);
                        break;
                    }
                    case 3: {
                        value = lexer.decimalValue();
                        lexer.nextToken(16);
                        break;
                    }
                    case 4: {
                        value = lexer.stringVal();
                        lexer.nextToken(16);
                        break;
                    }
                    default: {
                        value = parser.parse();
                        break;
                    }
                }
                array.set(index, value);
            } while (lexer.token() == 16);
        }
    }
    
    static class WildCardSegment implements Segment
    {
        private boolean deep;
        public static final WildCardSegment instance;
        public static final WildCardSegment instance_deep;
        
        private WildCardSegment(final boolean deep) {
            this.deep = deep;
        }
        
        @Override
        public Object eval(final JSONPath path, final Object rootObject, final Object currentObject) {
            if (!this.deep) {
                return path.getPropertyValues(currentObject);
            }
            final List<Object> values = new ArrayList<Object>();
            path.deepGetPropertyValues(currentObject, values);
            return values;
        }
        
        @Override
        public void extract(final JSONPath path, final DefaultJSONParser parser, final Context context) {
            if (context.eval) {
                final Object object = parser.parse();
                if (this.deep) {
                    final List<Object> values = new ArrayList<Object>();
                    path.deepGetPropertyValues(object, values);
                    context.object = values;
                    return;
                }
                if (object instanceof JSONObject) {
                    final Collection<Object> values2 = ((JSONObject)object).values();
                    final JSONArray array = new JSONArray(values2.size());
                    for (final Object value : values2) {
                        array.add(value);
                    }
                    context.object = array;
                    return;
                }
                if (object instanceof JSONArray) {
                    context.object = object;
                    return;
                }
            }
            throw new JSONException("TODO");
        }
        
        static {
            instance = new WildCardSegment(false);
            instance_deep = new WildCardSegment(true);
        }
    }
    
    static class ArrayAccessSegment implements Segment
    {
        private final int index;
        
        public ArrayAccessSegment(final int index) {
            this.index = index;
        }
        
        @Override
        public Object eval(final JSONPath path, final Object rootObject, final Object currentObject) {
            return path.getArrayItem(currentObject, this.index);
        }
        
        public boolean setValue(final JSONPath path, final Object currentObject, final Object value) {
            return path.setArrayItem(path, currentObject, this.index, value);
        }
        
        public boolean remove(final JSONPath path, final Object currentObject) {
            return path.removeArrayItem(path, currentObject, this.index);
        }
        
        @Override
        public void extract(final JSONPath path, final DefaultJSONParser parser, final Context context) {
            final JSONLexerBase lexer = (JSONLexerBase)parser.lexer;
            if (lexer.seekArrayToItem(this.index) && context.eval) {
                context.object = parser.parse();
            }
        }
    }
    
    static class MultiIndexSegment implements Segment
    {
        private final int[] indexes;
        
        public MultiIndexSegment(final int[] indexes) {
            this.indexes = indexes;
        }
        
        @Override
        public Object eval(final JSONPath path, final Object rootObject, final Object currentObject) {
            final List<Object> items = new JSONArray(this.indexes.length);
            for (int i = 0; i < this.indexes.length; ++i) {
                final Object item = path.getArrayItem(currentObject, this.indexes[i]);
                items.add(item);
            }
            return items;
        }
        
        @Override
        public void extract(final JSONPath path, final DefaultJSONParser parser, final Context context) {
            if (context.eval) {
                final Object object = parser.parse();
                if (object instanceof List) {
                    final int[] indexes = new int[this.indexes.length];
                    System.arraycopy(this.indexes, 0, indexes, 0, indexes.length);
                    final boolean noneNegative = indexes[0] >= 0;
                    final List list = (List)object;
                    if (noneNegative) {
                        for (int i = list.size() - 1; i >= 0; --i) {
                            if (Arrays.binarySearch(indexes, i) < 0) {
                                list.remove(i);
                            }
                        }
                        context.object = list;
                        return;
                    }
                }
            }
            throw new UnsupportedOperationException();
        }
    }
    
    static class RangeSegment implements Segment
    {
        private final int start;
        private final int end;
        private final int step;
        
        public RangeSegment(final int start, final int end, final int step) {
            this.start = start;
            this.end = end;
            this.step = step;
        }
        
        @Override
        public Object eval(final JSONPath path, final Object rootObject, final Object currentObject) {
            final int size = SizeSegment.instance.eval(path, rootObject, currentObject);
            final int start = (this.start >= 0) ? this.start : (this.start + size);
            final int end = (this.end >= 0) ? this.end : (this.end + size);
            final int array_size = (end - start) / this.step + 1;
            if (array_size == -1) {
                return null;
            }
            final List<Object> items = new ArrayList<Object>(array_size);
            for (int i = start; i <= end && i < size; i += this.step) {
                final Object item = path.getArrayItem(currentObject, i);
                items.add(item);
            }
            return items;
        }
        
        @Override
        public void extract(final JSONPath path, final DefaultJSONParser parser, final Context context) {
            throw new UnsupportedOperationException();
        }
    }
    
    static class NotNullSegement implements Filter
    {
        private final String propertyName;
        private final long propertyNameHash;
        
        public NotNullSegement(final String propertyName) {
            this.propertyName = propertyName;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName);
        }
        
        @Override
        public boolean apply(final JSONPath path, final Object rootObject, final Object currentObject, final Object item) {
            final Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            return propertyValue != null;
        }
    }
    
    static class NullSegement implements Filter
    {
        private final String propertyName;
        private final long propertyNameHash;
        
        public NullSegement(final String propertyName) {
            this.propertyName = propertyName;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName);
        }
        
        @Override
        public boolean apply(final JSONPath path, final Object rootObject, final Object currentObject, final Object item) {
            final Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            return propertyValue == null;
        }
    }
    
    static class ValueSegment implements Filter
    {
        private final String propertyName;
        private final long propertyNameHash;
        private final Object value;
        private boolean eq;
        
        public ValueSegment(final String propertyName, final Object value, final boolean eq) {
            this.eq = true;
            if (value == null) {
                throw new IllegalArgumentException("value is null");
            }
            this.propertyName = propertyName;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName);
            this.value = value;
            this.eq = eq;
        }
        
        @Override
        public boolean apply(final JSONPath path, final Object rootObject, final Object currentObject, final Object item) {
            final Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            boolean result = this.value.equals(propertyValue);
            if (!this.eq) {
                result = !result;
            }
            return result;
        }
    }
    
    static class IntInSegement implements Filter
    {
        private final String propertyName;
        private final long propertyNameHash;
        private final long[] values;
        private final boolean not;
        
        public IntInSegement(final String propertyName, final long[] values, final boolean not) {
            this.propertyName = propertyName;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName);
            this.values = values;
            this.not = not;
        }
        
        @Override
        public boolean apply(final JSONPath path, final Object rootObject, final Object currentObject, final Object item) {
            final Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null) {
                return false;
            }
            if (propertyValue instanceof Number) {
                final long longPropertyValue = TypeUtils.longExtractValue((Number)propertyValue);
                for (final long value : this.values) {
                    if (value == longPropertyValue) {
                        return !this.not;
                    }
                }
            }
            return this.not;
        }
    }
    
    static class IntBetweenSegement implements Filter
    {
        private final String propertyName;
        private final long propertyNameHash;
        private final long startValue;
        private final long endValue;
        private final boolean not;
        
        public IntBetweenSegement(final String propertyName, final long startValue, final long endValue, final boolean not) {
            this.propertyName = propertyName;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName);
            this.startValue = startValue;
            this.endValue = endValue;
            this.not = not;
        }
        
        @Override
        public boolean apply(final JSONPath path, final Object rootObject, final Object currentObject, final Object item) {
            final Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null) {
                return false;
            }
            if (propertyValue instanceof Number) {
                final long longPropertyValue = TypeUtils.longExtractValue((Number)propertyValue);
                if (longPropertyValue >= this.startValue && longPropertyValue <= this.endValue) {
                    return !this.not;
                }
            }
            return this.not;
        }
    }
    
    static class IntObjInSegement implements Filter
    {
        private final String propertyName;
        private final long propertyNameHash;
        private final Long[] values;
        private final boolean not;
        
        public IntObjInSegement(final String propertyName, final Long[] values, final boolean not) {
            this.propertyName = propertyName;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName);
            this.values = values;
            this.not = not;
        }
        
        @Override
        public boolean apply(final JSONPath path, final Object rootObject, final Object currentObject, final Object item) {
            final Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null) {
                for (final Long value : this.values) {
                    if (value == null) {
                        return !this.not;
                    }
                }
                return this.not;
            }
            if (propertyValue instanceof Number) {
                final long longPropertyValue = TypeUtils.longExtractValue((Number)propertyValue);
                for (final Long value2 : this.values) {
                    if (value2 != null) {
                        if (value2 == longPropertyValue) {
                            return !this.not;
                        }
                    }
                }
            }
            return this.not;
        }
    }
    
    static class StringInSegement implements Filter
    {
        private final String propertyName;
        private final long propertyNameHash;
        private final String[] values;
        private final boolean not;
        
        public StringInSegement(final String propertyName, final String[] values, final boolean not) {
            this.propertyName = propertyName;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName);
            this.values = values;
            this.not = not;
        }
        
        @Override
        public boolean apply(final JSONPath path, final Object rootObject, final Object currentObject, final Object item) {
            final Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            for (final String value : this.values) {
                if (value == propertyValue) {
                    return !this.not;
                }
                if (value != null && value.equals(propertyValue)) {
                    return !this.not;
                }
            }
            return this.not;
        }
    }
    
    static class IntOpSegement implements Filter
    {
        private final String propertyName;
        private final long propertyNameHash;
        private final long value;
        private final Operator op;
        private BigDecimal valueDecimal;
        private Float valueFloat;
        private Double valueDouble;
        
        public IntOpSegement(final String propertyName, final long value, final Operator op) {
            this.propertyName = propertyName;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName);
            this.value = value;
            this.op = op;
        }
        
        @Override
        public boolean apply(final JSONPath path, final Object rootObject, final Object currentObject, final Object item) {
            final Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null) {
                return false;
            }
            if (!(propertyValue instanceof Number)) {
                return false;
            }
            if (propertyValue instanceof BigDecimal) {
                if (this.valueDecimal == null) {
                    this.valueDecimal = BigDecimal.valueOf(this.value);
                }
                final int result = this.valueDecimal.compareTo((BigDecimal)propertyValue);
                switch (this.op) {
                    case EQ: {
                        return result == 0;
                    }
                    case NE: {
                        return result != 0;
                    }
                    case GE: {
                        return 0 >= result;
                    }
                    case GT: {
                        return 0 > result;
                    }
                    case LE: {
                        return 0 <= result;
                    }
                    case LT: {
                        return 0 < result;
                    }
                    default: {
                        return false;
                    }
                }
            }
            else if (propertyValue instanceof Float) {
                if (this.valueFloat == null) {
                    this.valueFloat = Float.valueOf(this.value);
                }
                final int result = this.valueFloat.compareTo((Float)propertyValue);
                switch (this.op) {
                    case EQ: {
                        return result == 0;
                    }
                    case NE: {
                        return result != 0;
                    }
                    case GE: {
                        return 0 >= result;
                    }
                    case GT: {
                        return 0 > result;
                    }
                    case LE: {
                        return 0 <= result;
                    }
                    case LT: {
                        return 0 < result;
                    }
                    default: {
                        return false;
                    }
                }
            }
            else if (propertyValue instanceof Double) {
                if (this.valueDouble == null) {
                    this.valueDouble = Double.valueOf(this.value);
                }
                final int result = this.valueDouble.compareTo((Double)propertyValue);
                switch (this.op) {
                    case EQ: {
                        return result == 0;
                    }
                    case NE: {
                        return result != 0;
                    }
                    case GE: {
                        return 0 >= result;
                    }
                    case GT: {
                        return 0 > result;
                    }
                    case LE: {
                        return 0 <= result;
                    }
                    case LT: {
                        return 0 < result;
                    }
                    default: {
                        return false;
                    }
                }
            }
            else {
                final long longValue = TypeUtils.longExtractValue((Number)propertyValue);
                switch (this.op) {
                    case EQ: {
                        return longValue == this.value;
                    }
                    case NE: {
                        return longValue != this.value;
                    }
                    case GE: {
                        return longValue >= this.value;
                    }
                    case GT: {
                        return longValue > this.value;
                    }
                    case LE: {
                        return longValue <= this.value;
                    }
                    case LT: {
                        return longValue < this.value;
                    }
                    default: {
                        return false;
                    }
                }
            }
        }
    }
    
    static class DoubleOpSegement implements Filter
    {
        private final String propertyName;
        private final double value;
        private final Operator op;
        private final long propertyNameHash;
        
        public DoubleOpSegement(final String propertyName, final double value, final Operator op) {
            this.propertyName = propertyName;
            this.value = value;
            this.op = op;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName);
        }
        
        @Override
        public boolean apply(final JSONPath path, final Object rootObject, final Object currentObject, final Object item) {
            final Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null) {
                return false;
            }
            if (!(propertyValue instanceof Number)) {
                return false;
            }
            final double doubleValue = ((Number)propertyValue).doubleValue();
            switch (this.op) {
                case EQ: {
                    return doubleValue == this.value;
                }
                case NE: {
                    return doubleValue != this.value;
                }
                case GE: {
                    return doubleValue >= this.value;
                }
                case GT: {
                    return doubleValue > this.value;
                }
                case LE: {
                    return doubleValue <= this.value;
                }
                case LT: {
                    return doubleValue < this.value;
                }
                default: {
                    return false;
                }
            }
        }
    }
    
    static class RefOpSegement implements Filter
    {
        private final String propertyName;
        private final Segment refSgement;
        private final Operator op;
        private final long propertyNameHash;
        
        public RefOpSegement(final String propertyName, final Segment refSgement, final Operator op) {
            this.propertyName = propertyName;
            this.refSgement = refSgement;
            this.op = op;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName);
        }
        
        @Override
        public boolean apply(final JSONPath path, final Object rootObject, final Object currentObject, final Object item) {
            final Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null) {
                return false;
            }
            if (!(propertyValue instanceof Number)) {
                return false;
            }
            final Object refValue = this.refSgement.eval(path, rootObject, rootObject);
            if (refValue instanceof Integer || refValue instanceof Long || refValue instanceof Short || refValue instanceof Byte) {
                final long value = TypeUtils.longExtractValue((Number)refValue);
                if (propertyValue instanceof Integer || propertyValue instanceof Long || propertyValue instanceof Short || propertyValue instanceof Byte) {
                    final long longValue = TypeUtils.longExtractValue((Number)propertyValue);
                    switch (this.op) {
                        case EQ: {
                            return longValue == value;
                        }
                        case NE: {
                            return longValue != value;
                        }
                        case GE: {
                            return longValue >= value;
                        }
                        case GT: {
                            return longValue > value;
                        }
                        case LE: {
                            return longValue <= value;
                        }
                        case LT: {
                            return longValue < value;
                        }
                    }
                }
                else if (propertyValue instanceof BigDecimal) {
                    final BigDecimal valueDecimal = BigDecimal.valueOf(value);
                    final int result = valueDecimal.compareTo((BigDecimal)propertyValue);
                    switch (this.op) {
                        case EQ: {
                            return result == 0;
                        }
                        case NE: {
                            return result != 0;
                        }
                        case GE: {
                            return 0 >= result;
                        }
                        case GT: {
                            return 0 > result;
                        }
                        case LE: {
                            return 0 <= result;
                        }
                        case LT: {
                            return 0 < result;
                        }
                        default: {
                            return false;
                        }
                    }
                }
            }
            throw new UnsupportedOperationException();
        }
    }
    
    static class MatchSegement implements Filter
    {
        private final String propertyName;
        private final long propertyNameHash;
        private final String startsWithValue;
        private final String endsWithValue;
        private final String[] containsValues;
        private final int minLength;
        private final boolean not;
        
        public MatchSegement(final String propertyName, final String startsWithValue, final String endsWithValue, final String[] containsValues, final boolean not) {
            this.propertyName = propertyName;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName);
            this.startsWithValue = startsWithValue;
            this.endsWithValue = endsWithValue;
            this.containsValues = containsValues;
            this.not = not;
            int len = 0;
            if (startsWithValue != null) {
                len += startsWithValue.length();
            }
            if (endsWithValue != null) {
                len += endsWithValue.length();
            }
            if (containsValues != null) {
                for (final String item : containsValues) {
                    len += item.length();
                }
            }
            this.minLength = len;
        }
        
        @Override
        public boolean apply(final JSONPath path, final Object rootObject, final Object currentObject, final Object item) {
            final Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null) {
                return false;
            }
            final String strPropertyValue = propertyValue.toString();
            if (strPropertyValue.length() < this.minLength) {
                return this.not;
            }
            int start = 0;
            if (this.startsWithValue != null) {
                if (!strPropertyValue.startsWith(this.startsWithValue)) {
                    return this.not;
                }
                start += this.startsWithValue.length();
            }
            if (this.containsValues != null) {
                for (final String containsValue : this.containsValues) {
                    final int index = strPropertyValue.indexOf(containsValue, start);
                    if (index == -1) {
                        return this.not;
                    }
                    start = index + containsValue.length();
                }
            }
            if (this.endsWithValue != null && !strPropertyValue.endsWith(this.endsWithValue)) {
                return this.not;
            }
            return !this.not;
        }
    }
    
    static class RlikeSegement implements Filter
    {
        private final String propertyName;
        private final long propertyNameHash;
        private final Pattern pattern;
        private final boolean not;
        
        public RlikeSegement(final String propertyName, final String pattern, final boolean not) {
            this.propertyName = propertyName;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName);
            this.pattern = Pattern.compile(pattern);
            this.not = not;
        }
        
        @Override
        public boolean apply(final JSONPath path, final Object rootObject, final Object currentObject, final Object item) {
            final Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null) {
                return false;
            }
            final String strPropertyValue = propertyValue.toString();
            final Matcher m = this.pattern.matcher(strPropertyValue);
            boolean match = m.matches();
            if (this.not) {
                match = !match;
            }
            return match;
        }
    }
    
    static class StringOpSegement implements Filter
    {
        private final String propertyName;
        private final long propertyNameHash;
        private final String value;
        private final Operator op;
        
        public StringOpSegement(final String propertyName, final String value, final Operator op) {
            this.propertyName = propertyName;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName);
            this.value = value;
            this.op = op;
        }
        
        @Override
        public boolean apply(final JSONPath path, final Object rootObject, final Object currentObject, final Object item) {
            final Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (this.op == Operator.EQ) {
                return this.value.equals(propertyValue);
            }
            if (this.op == Operator.NE) {
                return !this.value.equals(propertyValue);
            }
            if (propertyValue == null) {
                return false;
            }
            final int compareResult = this.value.compareTo(propertyValue.toString());
            if (this.op == Operator.GE) {
                return compareResult <= 0;
            }
            if (this.op == Operator.GT) {
                return compareResult < 0;
            }
            if (this.op == Operator.LE) {
                return compareResult >= 0;
            }
            return this.op == Operator.LT && compareResult > 0;
        }
    }
    
    static class RegMatchSegement implements Filter
    {
        private final String propertyName;
        private final long propertyNameHash;
        private final Pattern pattern;
        private final Operator op;
        
        public RegMatchSegement(final String propertyName, final Pattern pattern, final Operator op) {
            this.propertyName = propertyName;
            this.propertyNameHash = TypeUtils.fnv1a_64(propertyName);
            this.pattern = pattern;
            this.op = op;
        }
        
        @Override
        public boolean apply(final JSONPath path, final Object rootObject, final Object currentObject, final Object item) {
            final Object propertyValue = path.getPropertyValue(item, this.propertyName, this.propertyNameHash);
            if (propertyValue == null) {
                return false;
            }
            final String str = propertyValue.toString();
            final Matcher m = this.pattern.matcher(str);
            return m.matches();
        }
    }
    
    enum Operator
    {
        EQ, 
        NE, 
        GT, 
        GE, 
        LT, 
        LE, 
        LIKE, 
        NOT_LIKE, 
        RLIKE, 
        NOT_RLIKE, 
        IN, 
        NOT_IN, 
        BETWEEN, 
        NOT_BETWEEN, 
        And, 
        Or, 
        REG_MATCH;
    }
    
    public static class FilterSegment implements Segment
    {
        private final Filter filter;
        
        public FilterSegment(final Filter filter) {
            this.filter = filter;
        }
        
        @Override
        public Object eval(final JSONPath path, final Object rootObject, final Object currentObject) {
            if (currentObject == null) {
                return null;
            }
            final List<Object> items = new JSONArray();
            if (currentObject instanceof Iterable) {
                for (final Object item : (Iterable)currentObject) {
                    if (this.filter.apply(path, rootObject, currentObject, item)) {
                        items.add(item);
                    }
                }
                return items;
            }
            if (this.filter.apply(path, rootObject, currentObject, currentObject)) {
                return currentObject;
            }
            return null;
        }
        
        @Override
        public void extract(final JSONPath path, final DefaultJSONParser parser, final Context context) {
            throw new UnsupportedOperationException();
        }
        
        public boolean remove(final JSONPath path, final Object rootObject, final Object currentObject) {
            if (currentObject == null) {
                return false;
            }
            if (currentObject instanceof Iterable) {
                final Iterator it = ((Iterable)currentObject).iterator();
                while (it.hasNext()) {
                    final Object item = it.next();
                    if (this.filter.apply(path, rootObject, currentObject, item)) {
                        it.remove();
                    }
                }
                return true;
            }
            return false;
        }
    }
    
    static class FilterGroup implements Filter
    {
        private boolean and;
        private List<Filter> fitlers;
        
        public FilterGroup(final Filter left, final Filter right, final boolean and) {
            (this.fitlers = new ArrayList<Filter>(2)).add(left);
            this.fitlers.add(right);
            this.and = and;
        }
        
        @Override
        public boolean apply(final JSONPath path, final Object rootObject, final Object currentObject, final Object item) {
            if (this.and) {
                for (final Filter fitler : this.fitlers) {
                    if (!fitler.apply(path, rootObject, currentObject, item)) {
                        return false;
                    }
                }
                return true;
            }
            for (final Filter fitler : this.fitlers) {
                if (fitler.apply(path, rootObject, currentObject, item)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    interface Filter
    {
        boolean apply(final JSONPath p0, final Object p1, final Object p2, final Object p3);
    }
    
    interface Segment
    {
        Object eval(final JSONPath p0, final Object p1, final Object p2);
        
        void extract(final JSONPath p0, final DefaultJSONParser p1, final Context p2);
    }
}
