package com.alibaba.fastjson.util;

import com.alibaba.fastjson.annotation.JSONCreator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import com.alibaba.fastjson.annotation.JSONPOJOBuilder;
import java.util.Collection;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import com.alibaba.fastjson.PropertyNamingStrategy;
import java.lang.reflect.Field;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.lang.reflect.AccessibleObject;
import com.alibaba.fastjson.annotation.JSONField;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.lang.reflect.Type;
import com.alibaba.fastjson.annotation.JSONType;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class JavaBeanInfo
{
    public final Class<?> clazz;
    public final Class<?> builderClass;
    public final Constructor<?> defaultConstructor;
    public final Constructor<?> creatorConstructor;
    public final Method factoryMethod;
    public final Method buildMethod;
    public final int defaultConstructorParameterSize;
    public final FieldInfo[] fields;
    public final FieldInfo[] sortedFields;
    public final int parserFeatures;
    public final JSONType jsonType;
    public final String typeName;
    public final String typeKey;
    public String[] orders;
    public Type[] creatorConstructorParameterTypes;
    public String[] creatorConstructorParameters;
    public boolean kotlin;
    public Constructor<?> kotlinDefaultConstructor;
    
    public JavaBeanInfo(final Class<?> clazz, final Class<?> builderClass, final Constructor<?> defaultConstructor, final Constructor<?> creatorConstructor, final Method factoryMethod, final Method buildMethod, final JSONType jsonType, final List<FieldInfo> fieldList) {
        this.clazz = clazz;
        this.builderClass = builderClass;
        this.defaultConstructor = defaultConstructor;
        this.creatorConstructor = creatorConstructor;
        this.factoryMethod = factoryMethod;
        this.parserFeatures = TypeUtils.getParserFeatures(clazz);
        this.buildMethod = buildMethod;
        this.jsonType = jsonType;
        if (jsonType != null) {
            final String typeName = jsonType.typeName();
            final String typeKey = jsonType.typeKey();
            this.typeKey = ((typeKey.length() > 0) ? typeKey : null);
            if (typeName.length() != 0) {
                this.typeName = typeName;
            }
            else {
                this.typeName = clazz.getName();
            }
            final String[] orders = jsonType.orders();
            this.orders = (String[])((orders.length == 0) ? null : orders);
        }
        else {
            this.typeName = clazz.getName();
            this.typeKey = null;
            this.orders = null;
        }
        fieldList.toArray(this.fields = new FieldInfo[fieldList.size()]);
        FieldInfo[] sortedFields = new FieldInfo[this.fields.length];
        if (this.orders != null) {
            final LinkedHashMap<String, FieldInfo> map = new LinkedHashMap<String, FieldInfo>(fieldList.size());
            for (final FieldInfo field : this.fields) {
                map.put(field.name, field);
            }
            int i = 0;
            for (final String item : this.orders) {
                final FieldInfo field2 = map.get(item);
                if (field2 != null) {
                    sortedFields[i++] = field2;
                    map.remove(item);
                }
            }
            for (final FieldInfo field3 : map.values()) {
                sortedFields[i++] = field3;
            }
        }
        else {
            System.arraycopy(this.fields, 0, sortedFields, 0, this.fields.length);
            Arrays.sort(sortedFields);
        }
        if (Arrays.equals(this.fields, sortedFields)) {
            sortedFields = this.fields;
        }
        this.sortedFields = sortedFields;
        if (defaultConstructor != null) {
            this.defaultConstructorParameterSize = defaultConstructor.getParameterTypes().length;
        }
        else if (factoryMethod != null) {
            this.defaultConstructorParameterSize = factoryMethod.getParameterTypes().length;
        }
        else {
            this.defaultConstructorParameterSize = 0;
        }
        if (creatorConstructor != null) {
            this.creatorConstructorParameterTypes = creatorConstructor.getParameterTypes();
            this.kotlin = TypeUtils.isKotlin(clazz);
            if (this.kotlin) {
                this.creatorConstructorParameters = TypeUtils.getKoltinConstructorParameters(clazz);
                try {
                    this.kotlinDefaultConstructor = clazz.getConstructor((Class<?>[])new Class[0]);
                }
                catch (Throwable t) {}
                final Annotation[][] paramAnnotationArrays = TypeUtils.getParameterAnnotations(creatorConstructor);
                for (int i = 0; i < this.creatorConstructorParameters.length && i < paramAnnotationArrays.length; ++i) {
                    final Annotation[] paramAnnotations = paramAnnotationArrays[i];
                    JSONField fieldAnnotation = null;
                    for (final Annotation paramAnnotation : paramAnnotations) {
                        if (paramAnnotation instanceof JSONField) {
                            fieldAnnotation = (JSONField)paramAnnotation;
                            break;
                        }
                    }
                    if (fieldAnnotation != null) {
                        final String fieldAnnotationName = fieldAnnotation.name();
                        if (fieldAnnotationName.length() > 0) {
                            this.creatorConstructorParameters[i] = fieldAnnotationName;
                        }
                    }
                }
            }
            else {
                boolean match;
                if (this.creatorConstructorParameterTypes.length != this.fields.length) {
                    match = false;
                }
                else {
                    match = true;
                    for (int i = 0; i < this.creatorConstructorParameterTypes.length; ++i) {
                        if (this.creatorConstructorParameterTypes[i] != this.fields[i].fieldClass) {
                            match = false;
                            break;
                        }
                    }
                }
                if (!match) {
                    this.creatorConstructorParameters = ASMUtils.lookupParameterNames(creatorConstructor);
                }
            }
        }
    }
    
    private static FieldInfo getField(final List<FieldInfo> fieldList, final String propertyName) {
        for (final FieldInfo item : fieldList) {
            if (item.name.equals(propertyName)) {
                return item;
            }
            final Field field = item.field;
            if (field != null && item.getAnnotation() != null && field.getName().equals(propertyName)) {
                return item;
            }
        }
        return null;
    }
    
    static boolean add(final List<FieldInfo> fieldList, final FieldInfo field) {
        int i = fieldList.size() - 1;
        while (i >= 0) {
            final FieldInfo item = fieldList.get(i);
            if (item.name.equals(field.name) && (!item.getOnly || field.getOnly)) {
                if (item.fieldClass.isAssignableFrom(field.fieldClass)) {
                    fieldList.set(i, field);
                    return true;
                }
                final int result = item.compareTo(field);
                if (result < 0) {
                    fieldList.set(i, field);
                    return true;
                }
                return false;
            }
            else {
                --i;
            }
        }
        fieldList.add(field);
        return true;
    }
    
    public static JavaBeanInfo build(final Class<?> clazz, final Type type, final PropertyNamingStrategy propertyNamingStrategy) {
        return build(clazz, type, propertyNamingStrategy, false, TypeUtils.compatibleWithJavaBean, false);
    }
    
    private static Map<TypeVariable, Type> buildGenericInfo(final Class<?> clazz) {
        Class<?> childClass = clazz;
        Class<?> currentClass = clazz.getSuperclass();
        if (currentClass == null) {
            return null;
        }
        Map<TypeVariable, Type> typeVarMap = null;
        while (currentClass != null && currentClass != Object.class) {
            if (childClass.getGenericSuperclass() instanceof ParameterizedType) {
                final Type[] childGenericParentActualTypeArgs = ((ParameterizedType)childClass.getGenericSuperclass()).getActualTypeArguments();
                final TypeVariable[] currentTypeParameters = currentClass.getTypeParameters();
                for (int i = 0; i < childGenericParentActualTypeArgs.length; ++i) {
                    if (typeVarMap == null) {
                        typeVarMap = new HashMap<TypeVariable, Type>();
                    }
                    if (typeVarMap.containsKey(childGenericParentActualTypeArgs[i])) {
                        final Type actualArg = typeVarMap.get(childGenericParentActualTypeArgs[i]);
                        typeVarMap.put(currentTypeParameters[i], actualArg);
                    }
                    else {
                        typeVarMap.put(currentTypeParameters[i], childGenericParentActualTypeArgs[i]);
                    }
                }
            }
            childClass = currentClass;
            currentClass = currentClass.getSuperclass();
        }
        return typeVarMap;
    }
    
    public static JavaBeanInfo build(final Class<?> clazz, final Type type, final PropertyNamingStrategy propertyNamingStrategy, final boolean fieldBased, final boolean compatibleWithJavaBean) {
        return build(clazz, type, propertyNamingStrategy, fieldBased, compatibleWithJavaBean, false);
    }
    
    public static JavaBeanInfo build(final Class<?> clazz, final Type type, PropertyNamingStrategy propertyNamingStrategy, boolean fieldBased, final boolean compatibleWithJavaBean, final boolean jacksonCompatible) {
        final JSONType jsonType = TypeUtils.getAnnotation(clazz, JSONType.class);
        if (jsonType != null) {
            final PropertyNamingStrategy jsonTypeNaming = jsonType.naming();
            if (jsonTypeNaming != null && jsonTypeNaming != PropertyNamingStrategy.CamelCase) {
                propertyNamingStrategy = jsonTypeNaming;
            }
        }
        final Class<?> builderClass = getBuilderClass(clazz, jsonType);
        final Field[] declaredFields = clazz.getDeclaredFields();
        final Method[] methods = clazz.getMethods();
        final Map<TypeVariable, Type> genericInfo = buildGenericInfo(clazz);
        final boolean kotlin = TypeUtils.isKotlin(clazz);
        final Constructor[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> defaultConstructor = null;
        if (!kotlin || constructors.length == 1) {
            if (builderClass == null) {
                defaultConstructor = getDefaultConstructor(clazz, constructors);
            }
            else {
                defaultConstructor = getDefaultConstructor(builderClass, builderClass.getDeclaredConstructors());
            }
        }
        Constructor<?> creatorConstructor = null;
        Method buildMethod = null;
        Method factoryMethod = null;
        final List<FieldInfo> fieldList = new ArrayList<FieldInfo>();
        if (fieldBased) {
            for (Class<?> currentClass = clazz; currentClass != null; currentClass = currentClass.getSuperclass()) {
                final Field[] fields = currentClass.getDeclaredFields();
                computeFields(clazz, type, propertyNamingStrategy, fieldList, fields);
            }
            if (defaultConstructor != null) {
                TypeUtils.setAccessible(defaultConstructor);
            }
            return new JavaBeanInfo(clazz, builderClass, defaultConstructor, null, factoryMethod, buildMethod, jsonType, fieldList);
        }
        final boolean isInterfaceOrAbstract = clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers());
        if ((defaultConstructor == null && builderClass == null) || isInterfaceOrAbstract) {
            creatorConstructor = getCreatorConstructor(constructors);
            if (creatorConstructor != null && !isInterfaceOrAbstract) {
                TypeUtils.setAccessible(creatorConstructor);
                final Class<?>[] types = creatorConstructor.getParameterTypes();
                String[] lookupParameterNames = null;
                if (types.length > 0) {
                    final Annotation[][] paramAnnotationArrays = TypeUtils.getParameterAnnotations(creatorConstructor);
                    for (int i = 0; i < types.length && i < paramAnnotationArrays.length; ++i) {
                        final Annotation[] paramAnnotations = paramAnnotationArrays[i];
                        JSONField fieldAnnotation = null;
                        for (final Annotation paramAnnotation : paramAnnotations) {
                            if (paramAnnotation instanceof JSONField) {
                                fieldAnnotation = (JSONField)paramAnnotation;
                                break;
                            }
                        }
                        final Class<?> fieldClass = types[i];
                        final Type fieldType = creatorConstructor.getGenericParameterTypes()[i];
                        String fieldName = null;
                        Field field = null;
                        int ordinal = 0;
                        int serialzeFeatures = 0;
                        int parserFeatures = 0;
                        if (fieldAnnotation != null) {
                            field = TypeUtils.getField(clazz, fieldAnnotation.name(), declaredFields);
                            ordinal = fieldAnnotation.ordinal();
                            serialzeFeatures = SerializerFeature.of(fieldAnnotation.serialzeFeatures());
                            parserFeatures = Feature.of(fieldAnnotation.parseFeatures());
                            fieldName = fieldAnnotation.name();
                        }
                        if (fieldName == null || fieldName.length() == 0) {
                            if (lookupParameterNames == null) {
                                lookupParameterNames = ASMUtils.lookupParameterNames(creatorConstructor);
                            }
                            fieldName = lookupParameterNames[i];
                        }
                        if (field == null) {
                            if (lookupParameterNames == null) {
                                if (kotlin) {
                                    lookupParameterNames = TypeUtils.getKoltinConstructorParameters(clazz);
                                }
                                else {
                                    lookupParameterNames = ASMUtils.lookupParameterNames(creatorConstructor);
                                }
                            }
                            if (lookupParameterNames.length > i) {
                                final String parameterName = lookupParameterNames[i];
                                field = TypeUtils.getField(clazz, parameterName, declaredFields);
                            }
                        }
                        final FieldInfo fieldInfo = new FieldInfo(fieldName, clazz, fieldClass, fieldType, field, ordinal, serialzeFeatures, parserFeatures);
                        add(fieldList, fieldInfo);
                    }
                }
            }
            else if ((factoryMethod = getFactoryMethod(clazz, methods, jacksonCompatible)) != null) {
                TypeUtils.setAccessible(factoryMethod);
                String[] lookupParameterNames2 = null;
                final Class<?>[] types2 = factoryMethod.getParameterTypes();
                if (types2.length > 0) {
                    final Annotation[][] paramAnnotationArrays = TypeUtils.getParameterAnnotations(factoryMethod);
                    for (int i = 0; i < types2.length; ++i) {
                        final Annotation[] paramAnnotations = paramAnnotationArrays[i];
                        JSONField fieldAnnotation = null;
                        for (final Annotation paramAnnotation : paramAnnotations) {
                            if (paramAnnotation instanceof JSONField) {
                                fieldAnnotation = (JSONField)paramAnnotation;
                                break;
                            }
                        }
                        if (fieldAnnotation == null && (!jacksonCompatible || !TypeUtils.isJacksonCreator(factoryMethod))) {
                            throw new JSONException("illegal json creator");
                        }
                        String fieldName2 = null;
                        int ordinal2 = 0;
                        int serialzeFeatures2 = 0;
                        int parserFeatures2 = 0;
                        if (fieldAnnotation != null) {
                            fieldName2 = fieldAnnotation.name();
                            ordinal2 = fieldAnnotation.ordinal();
                            serialzeFeatures2 = SerializerFeature.of(fieldAnnotation.serialzeFeatures());
                            parserFeatures2 = Feature.of(fieldAnnotation.parseFeatures());
                        }
                        if (fieldName2 == null || fieldName2.length() == 0) {
                            if (lookupParameterNames2 == null) {
                                lookupParameterNames2 = ASMUtils.lookupParameterNames(factoryMethod);
                            }
                            fieldName2 = lookupParameterNames2[i];
                        }
                        final Class<?> fieldClass2 = types2[i];
                        final Type fieldType2 = factoryMethod.getGenericParameterTypes()[i];
                        final Field field2 = TypeUtils.getField(clazz, fieldName2, declaredFields);
                        final FieldInfo fieldInfo = new FieldInfo(fieldName2, clazz, fieldClass2, fieldType2, field2, ordinal2, serialzeFeatures2, parserFeatures2);
                        add(fieldList, fieldInfo);
                    }
                    return new JavaBeanInfo(clazz, builderClass, null, null, factoryMethod, null, jsonType, fieldList);
                }
            }
            else if (!isInterfaceOrAbstract) {
                final String className = clazz.getName();
                String[] paramNames = null;
                if (kotlin && constructors.length > 0) {
                    paramNames = TypeUtils.getKoltinConstructorParameters(clazz);
                    creatorConstructor = (Constructor<?>)TypeUtils.getKoltinConstructor(constructors, paramNames);
                    TypeUtils.setAccessible(creatorConstructor);
                }
                else {
                    for (final Constructor constructor : constructors) {
                        final Class<?>[] parameterTypes = (Class<?>[])constructor.getParameterTypes();
                        if (className.equals("org.springframework.security.web.authentication.WebAuthenticationDetails")) {
                            if (parameterTypes.length == 2 && parameterTypes[0] == String.class && parameterTypes[1] == String.class) {
                                creatorConstructor = (Constructor<?>)constructor;
                                creatorConstructor.setAccessible(true);
                                paramNames = ASMUtils.lookupParameterNames(constructor);
                                break;
                            }
                        }
                        else if (className.equals("org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken")) {
                            if (parameterTypes.length == 3 && parameterTypes[0] == Object.class && parameterTypes[1] == Object.class && parameterTypes[2] == Collection.class) {
                                creatorConstructor = (Constructor<?>)constructor;
                                creatorConstructor.setAccessible(true);
                                paramNames = new String[] { "principal", "credentials", "authorities" };
                                break;
                            }
                        }
                        else if (className.equals("org.springframework.security.core.authority.SimpleGrantedAuthority")) {
                            if (parameterTypes.length == 1 && parameterTypes[0] == String.class) {
                                creatorConstructor = (Constructor<?>)constructor;
                                paramNames = new String[] { "authority" };
                                break;
                            }
                        }
                        else {
                            final boolean is_public = (constructor.getModifiers() & 0x1) != 0x0;
                            if (is_public) {
                                final String[] lookupParameterNames3 = ASMUtils.lookupParameterNames(constructor);
                                if (lookupParameterNames3 != null) {
                                    if (lookupParameterNames3.length != 0) {
                                        if (creatorConstructor == null || paramNames == null || lookupParameterNames3.length > paramNames.length) {
                                            paramNames = lookupParameterNames3;
                                            creatorConstructor = (Constructor<?>)constructor;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Class<?>[] types3 = null;
                if (paramNames != null) {
                    types3 = creatorConstructor.getParameterTypes();
                }
                if (paramNames == null || types3.length != paramNames.length) {
                    throw new JSONException("default constructor not found. " + clazz);
                }
                final Annotation[][] paramAnnotationArrays2 = TypeUtils.getParameterAnnotations(creatorConstructor);
                for (int j = 0; j < types3.length; ++j) {
                    final Annotation[] paramAnnotations2 = paramAnnotationArrays2[j];
                    String paramName = paramNames[j];
                    JSONField fieldAnnotation2 = null;
                    for (final Annotation paramAnnotation2 : paramAnnotations2) {
                        if (paramAnnotation2 instanceof JSONField) {
                            fieldAnnotation2 = (JSONField)paramAnnotation2;
                            break;
                        }
                    }
                    final Class<?> fieldClass3 = types3[j];
                    final Type fieldType3 = creatorConstructor.getGenericParameterTypes()[j];
                    final Field field3 = TypeUtils.getField(clazz, paramName, declaredFields);
                    if (field3 != null && fieldAnnotation2 == null) {
                        fieldAnnotation2 = TypeUtils.getAnnotation(field3, JSONField.class);
                    }
                    int ordinal3;
                    int serialzeFeatures3;
                    int parserFeatures3;
                    if (fieldAnnotation2 == null) {
                        ordinal3 = 0;
                        serialzeFeatures3 = 0;
                        if ("org.springframework.security.core.userdetails.User".equals(className) && "password".equals(paramName)) {
                            parserFeatures3 = Feature.InitStringFieldAsEmpty.mask;
                        }
                        else {
                            parserFeatures3 = 0;
                        }
                    }
                    else {
                        final String nameAnnotated = fieldAnnotation2.name();
                        if (nameAnnotated.length() != 0) {
                            paramName = nameAnnotated;
                        }
                        ordinal3 = fieldAnnotation2.ordinal();
                        serialzeFeatures3 = SerializerFeature.of(fieldAnnotation2.serialzeFeatures());
                        parserFeatures3 = Feature.of(fieldAnnotation2.parseFeatures());
                    }
                    final FieldInfo fieldInfo2 = new FieldInfo(paramName, clazz, fieldClass3, fieldType3, field3, ordinal3, serialzeFeatures3, parserFeatures3);
                    add(fieldList, fieldInfo2);
                }
                if (!kotlin && !clazz.getName().equals("javax.servlet.http.Cookie")) {
                    return new JavaBeanInfo(clazz, builderClass, null, creatorConstructor, null, null, jsonType, fieldList);
                }
            }
        }
        if (defaultConstructor != null) {
            TypeUtils.setAccessible(defaultConstructor);
        }
        if (builderClass != null) {
            String withPrefix = null;
            final JSONPOJOBuilder builderAnno = TypeUtils.getAnnotation(builderClass, JSONPOJOBuilder.class);
            if (builderAnno != null) {
                withPrefix = builderAnno.withPrefix();
            }
            if (withPrefix == null) {
                withPrefix = "with";
            }
            for (final Method method : builderClass.getMethods()) {
                Label_2072: {
                    if (!Modifier.isStatic(method.getModifiers())) {
                        if (method.getReturnType().equals(builderClass)) {
                            int ordinal4 = 0;
                            int serialzeFeatures4 = 0;
                            int parserFeatures4 = 0;
                            JSONField annotation = TypeUtils.getAnnotation(method, JSONField.class);
                            if (annotation == null) {
                                annotation = TypeUtils.getSuperMethodAnnotation(clazz, method);
                            }
                            if (annotation != null) {
                                if (!annotation.deserialize()) {
                                    break Label_2072;
                                }
                                ordinal4 = annotation.ordinal();
                                serialzeFeatures4 = SerializerFeature.of(annotation.serialzeFeatures());
                                parserFeatures4 = Feature.of(annotation.parseFeatures());
                                if (annotation.name().length() != 0) {
                                    final String propertyName = annotation.name();
                                    add(fieldList, new FieldInfo(propertyName, method, null, clazz, type, ordinal4, serialzeFeatures4, parserFeatures4, annotation, null, null, genericInfo));
                                    break Label_2072;
                                }
                            }
                            final String methodName = method.getName();
                            StringBuilder properNameBuilder;
                            if (methodName.startsWith("set") && methodName.length() > 3) {
                                properNameBuilder = new StringBuilder(methodName.substring(3));
                            }
                            else if (withPrefix.length() == 0) {
                                properNameBuilder = new StringBuilder(methodName);
                            }
                            else {
                                if (!methodName.startsWith(withPrefix)) {
                                    break Label_2072;
                                }
                                if (methodName.length() <= withPrefix.length()) {
                                    break Label_2072;
                                }
                                properNameBuilder = new StringBuilder(methodName.substring(withPrefix.length()));
                            }
                            final char c0 = properNameBuilder.charAt(0);
                            if (withPrefix.length() == 0 || Character.isUpperCase(c0)) {
                                properNameBuilder.setCharAt(0, Character.toLowerCase(c0));
                                final String propertyName2 = properNameBuilder.toString();
                                add(fieldList, new FieldInfo(propertyName2, method, null, clazz, type, ordinal4, serialzeFeatures4, parserFeatures4, annotation, null, null, genericInfo));
                            }
                        }
                    }
                }
            }
            if (builderClass != null) {
                final JSONPOJOBuilder builderAnnotation = TypeUtils.getAnnotation(builderClass, JSONPOJOBuilder.class);
                String buildMethodName = null;
                if (builderAnnotation != null) {
                    buildMethodName = builderAnnotation.buildMethod();
                }
                if (buildMethodName == null || buildMethodName.length() == 0) {
                    buildMethodName = "build";
                }
                try {
                    buildMethod = builderClass.getMethod(buildMethodName, (Class<?>[])new Class[0]);
                }
                catch (NoSuchMethodException ex) {}
                catch (SecurityException ex2) {}
                if (buildMethod == null) {
                    try {
                        buildMethod = builderClass.getMethod("create", (Class<?>[])new Class[0]);
                    }
                    catch (NoSuchMethodException ex3) {}
                    catch (SecurityException ex4) {}
                }
                if (buildMethod == null) {
                    throw new JSONException("buildMethod not found.");
                }
                TypeUtils.setAccessible(buildMethod);
            }
        }
        for (final Method method2 : methods) {
            int ordinal5 = 0;
            int serialzeFeatures5 = 0;
            int parserFeatures5 = 0;
            final String methodName2 = method2.getName();
            Label_3054: {
                if (!Modifier.isStatic(method2.getModifiers())) {
                    final Class<?> returnType = method2.getReturnType();
                    if (returnType.equals(Void.TYPE) || returnType.equals(method2.getDeclaringClass())) {
                        if (method2.getDeclaringClass() != Object.class) {
                            final Class<?>[] types4 = method2.getParameterTypes();
                            if (types4.length != 0) {
                                if (types4.length <= 2) {
                                    JSONField annotation2 = TypeUtils.getAnnotation(method2, JSONField.class);
                                    if (annotation2 != null && types4.length == 2 && types4[0] == String.class && types4[1] == Object.class) {
                                        add(fieldList, new FieldInfo("", method2, null, clazz, type, ordinal5, serialzeFeatures5, parserFeatures5, annotation2, null, null, genericInfo));
                                    }
                                    else if (types4.length == 1) {
                                        if (annotation2 == null) {
                                            annotation2 = TypeUtils.getSuperMethodAnnotation(clazz, method2);
                                        }
                                        if (annotation2 != null || methodName2.length() >= 4) {
                                            if (annotation2 != null) {
                                                if (!annotation2.deserialize()) {
                                                    break Label_3054;
                                                }
                                                ordinal5 = annotation2.ordinal();
                                                serialzeFeatures5 = SerializerFeature.of(annotation2.serialzeFeatures());
                                                parserFeatures5 = Feature.of(annotation2.parseFeatures());
                                                if (annotation2.name().length() != 0) {
                                                    final String propertyName3 = annotation2.name();
                                                    add(fieldList, new FieldInfo(propertyName3, method2, null, clazz, type, ordinal5, serialzeFeatures5, parserFeatures5, annotation2, null, null, genericInfo));
                                                    break Label_3054;
                                                }
                                            }
                                            if (annotation2 != null || methodName2.startsWith("set")) {
                                                if (builderClass == null) {
                                                    final char c2 = methodName2.charAt(3);
                                                    Field field4 = null;
                                                    String propertyName4;
                                                    if (Character.isUpperCase(c2) || c2 > '\u0200') {
                                                        if (TypeUtils.compatibleWithJavaBean) {
                                                            propertyName4 = TypeUtils.decapitalize(methodName2.substring(3));
                                                        }
                                                        else {
                                                            propertyName4 = Character.toLowerCase(methodName2.charAt(3)) + methodName2.substring(4);
                                                        }
                                                    }
                                                    else if (c2 == '_') {
                                                        propertyName4 = methodName2.substring(4);
                                                        field4 = TypeUtils.getField(clazz, propertyName4, declaredFields);
                                                        if (field4 == null) {
                                                            final String temp = propertyName4;
                                                            propertyName4 = methodName2.substring(3);
                                                            field4 = TypeUtils.getField(clazz, propertyName4, declaredFields);
                                                            if (field4 == null) {
                                                                propertyName4 = temp;
                                                            }
                                                        }
                                                    }
                                                    else if (c2 == 'f') {
                                                        propertyName4 = methodName2.substring(3);
                                                    }
                                                    else if (methodName2.length() >= 5 && Character.isUpperCase(methodName2.charAt(4))) {
                                                        propertyName4 = TypeUtils.decapitalize(methodName2.substring(3));
                                                    }
                                                    else {
                                                        propertyName4 = methodName2.substring(3);
                                                        field4 = TypeUtils.getField(clazz, propertyName4, declaredFields);
                                                        if (field4 == null) {
                                                            break Label_3054;
                                                        }
                                                    }
                                                    if (field4 == null) {
                                                        field4 = TypeUtils.getField(clazz, propertyName4, declaredFields);
                                                    }
                                                    if (field4 == null && types4[0] == Boolean.TYPE) {
                                                        final String isFieldName = "is" + Character.toUpperCase(propertyName4.charAt(0)) + propertyName4.substring(1);
                                                        field4 = TypeUtils.getField(clazz, isFieldName, declaredFields);
                                                    }
                                                    JSONField fieldAnnotation3 = null;
                                                    if (field4 != null) {
                                                        fieldAnnotation3 = TypeUtils.getAnnotation(field4, JSONField.class);
                                                        if (fieldAnnotation3 != null) {
                                                            if (!fieldAnnotation3.deserialize()) {
                                                                break Label_3054;
                                                            }
                                                            ordinal5 = fieldAnnotation3.ordinal();
                                                            serialzeFeatures5 = SerializerFeature.of(fieldAnnotation3.serialzeFeatures());
                                                            parserFeatures5 = Feature.of(fieldAnnotation3.parseFeatures());
                                                            if (fieldAnnotation3.name().length() != 0) {
                                                                propertyName4 = fieldAnnotation3.name();
                                                                add(fieldList, new FieldInfo(propertyName4, method2, field4, clazz, type, ordinal5, serialzeFeatures5, parserFeatures5, annotation2, fieldAnnotation3, null, genericInfo));
                                                                break Label_3054;
                                                            }
                                                        }
                                                    }
                                                    if (propertyNamingStrategy != null) {
                                                        propertyName4 = propertyNamingStrategy.translate(propertyName4);
                                                    }
                                                    add(fieldList, new FieldInfo(propertyName4, method2, field4, clazz, type, ordinal5, serialzeFeatures5, parserFeatures5, annotation2, fieldAnnotation3, null, genericInfo));
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
        computeFields(clazz, type, propertyNamingStrategy, fieldList, fields);
        for (final Method method3 : clazz.getMethods()) {
            final String methodName3 = method3.getName();
            Label_3428: {
                if (methodName3.length() >= 4) {
                    if (!Modifier.isStatic(method3.getModifiers())) {
                        if (builderClass == null && methodName3.startsWith("get") && Character.isUpperCase(methodName3.charAt(3))) {
                            if (method3.getParameterTypes().length == 0) {
                                if (Collection.class.isAssignableFrom(method3.getReturnType()) || Map.class.isAssignableFrom(method3.getReturnType()) || AtomicBoolean.class == method3.getReturnType() || AtomicInteger.class == method3.getReturnType() || AtomicLong.class == method3.getReturnType()) {
                                    final JSONField annotation3 = TypeUtils.getAnnotation(method3, JSONField.class);
                                    if (annotation3 == null || !annotation3.deserialize()) {
                                        String propertyName5;
                                        if (annotation3 != null && annotation3.name().length() > 0) {
                                            propertyName5 = annotation3.name();
                                        }
                                        else {
                                            propertyName5 = Character.toLowerCase(methodName3.charAt(3)) + methodName3.substring(4);
                                            final Field field5 = TypeUtils.getField(clazz, propertyName5, declaredFields);
                                            if (field5 != null) {
                                                final JSONField fieldAnnotation4 = TypeUtils.getAnnotation(field5, JSONField.class);
                                                if (fieldAnnotation4 != null && !fieldAnnotation4.deserialize()) {
                                                    break Label_3428;
                                                }
                                            }
                                        }
                                        if (propertyNamingStrategy != null) {
                                            propertyName5 = propertyNamingStrategy.translate(propertyName5);
                                        }
                                        final FieldInfo fieldInfo3 = getField(fieldList, propertyName5);
                                        if (fieldInfo3 == null) {
                                            add(fieldList, new FieldInfo(propertyName5, method3, null, clazz, type, 0, 0, 0, annotation3, null, null, genericInfo));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (fieldList.size() == 0) {
            if (TypeUtils.isXmlField(clazz)) {
                fieldBased = true;
            }
            if (fieldBased) {
                for (Class<?> currentClass2 = clazz; currentClass2 != null; currentClass2 = currentClass2.getSuperclass()) {
                    computeFields(clazz, type, propertyNamingStrategy, fieldList, declaredFields);
                }
            }
        }
        return new JavaBeanInfo(clazz, builderClass, defaultConstructor, creatorConstructor, factoryMethod, buildMethod, jsonType, fieldList);
    }
    
    private static void computeFields(final Class<?> clazz, final Type type, final PropertyNamingStrategy propertyNamingStrategy, final List<FieldInfo> fieldList, final Field[] fields) {
        final Map<TypeVariable, Type> genericInfo = buildGenericInfo(clazz);
        for (final Field field : fields) {
            final int modifiers = field.getModifiers();
            Label_0340: {
                if ((modifiers & 0x8) == 0x0) {
                    if ((modifiers & 0x10) != 0x0) {
                        final Class<?> fieldType = field.getType();
                        final boolean supportReadOnly = Map.class.isAssignableFrom(fieldType) || Collection.class.isAssignableFrom(fieldType) || AtomicLong.class.equals(fieldType) || AtomicInteger.class.equals(fieldType) || AtomicBoolean.class.equals(fieldType);
                        if (!supportReadOnly) {
                            break Label_0340;
                        }
                    }
                    boolean contains = false;
                    for (final FieldInfo item : fieldList) {
                        if (item.name.equals(field.getName())) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        int ordinal = 0;
                        int serialzeFeatures = 0;
                        int parserFeatures = 0;
                        String propertyName = field.getName();
                        final JSONField fieldAnnotation = TypeUtils.getAnnotation(field, JSONField.class);
                        if (fieldAnnotation != null) {
                            if (!fieldAnnotation.deserialize()) {
                                break Label_0340;
                            }
                            ordinal = fieldAnnotation.ordinal();
                            serialzeFeatures = SerializerFeature.of(fieldAnnotation.serialzeFeatures());
                            parserFeatures = Feature.of(fieldAnnotation.parseFeatures());
                            if (fieldAnnotation.name().length() != 0) {
                                propertyName = fieldAnnotation.name();
                            }
                        }
                        if (propertyNamingStrategy != null) {
                            propertyName = propertyNamingStrategy.translate(propertyName);
                        }
                        add(fieldList, new FieldInfo(propertyName, null, field, clazz, type, ordinal, serialzeFeatures, parserFeatures, null, fieldAnnotation, null, genericInfo));
                    }
                }
            }
        }
    }
    
    static Constructor<?> getDefaultConstructor(final Class<?> clazz, final Constructor<?>[] constructors) {
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return null;
        }
        Constructor<?> defaultConstructor = null;
        for (final Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                defaultConstructor = constructor;
                break;
            }
        }
        if (defaultConstructor == null && clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
            for (final Constructor<?> constructor2 : constructors) {
                final Class<?>[] types;
                if ((types = constructor2.getParameterTypes()).length == 1 && types[0].equals(clazz.getDeclaringClass())) {
                    defaultConstructor = constructor2;
                    break;
                }
            }
        }
        return defaultConstructor;
    }
    
    public static Constructor<?> getCreatorConstructor(final Constructor[] constructors) {
        Constructor<?> creatorConstructor = null;
        for (final Constructor<?> constructor : constructors) {
            final JSONCreator annotation = constructor.getAnnotation(JSONCreator.class);
            if (annotation != null) {
                if (creatorConstructor != null) {
                    throw new JSONException("multi-JSONCreator");
                }
                creatorConstructor = constructor;
            }
        }
        if (creatorConstructor != null) {
            return creatorConstructor;
        }
        for (final Constructor constructor2 : constructors) {
            final Annotation[][] paramAnnotationArrays = TypeUtils.getParameterAnnotations(constructor2);
            if (paramAnnotationArrays.length != 0) {
                boolean match = true;
                for (final Annotation[] paramAnnotationArray : paramAnnotationArrays) {
                    boolean paramMatch = false;
                    for (final Annotation paramAnnotation : paramAnnotationArray) {
                        if (paramAnnotation instanceof JSONField) {
                            paramMatch = true;
                            break;
                        }
                    }
                    if (!paramMatch) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    if (creatorConstructor != null) {
                        throw new JSONException("multi-JSONCreator");
                    }
                    creatorConstructor = (Constructor<?>)constructor2;
                }
            }
        }
        if (creatorConstructor != null) {
            return creatorConstructor;
        }
        return creatorConstructor;
    }
    
    private static Method getFactoryMethod(final Class<?> clazz, final Method[] methods, final boolean jacksonCompatible) {
        Method factoryMethod = null;
        for (final Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())) {
                if (clazz.isAssignableFrom(method.getReturnType())) {
                    final JSONCreator annotation = TypeUtils.getAnnotation(method, JSONCreator.class);
                    if (annotation != null) {
                        if (factoryMethod != null) {
                            throw new JSONException("multi-JSONCreator");
                        }
                        factoryMethod = method;
                    }
                }
            }
        }
        if (factoryMethod == null && jacksonCompatible) {
            for (final Method method : methods) {
                if (TypeUtils.isJacksonCreator(method)) {
                    factoryMethod = method;
                    break;
                }
            }
        }
        return factoryMethod;
    }
    
    @Deprecated
    public static Class<?> getBuilderClass(final JSONType type) {
        return getBuilderClass(null, type);
    }
    
    public static Class<?> getBuilderClass(final Class<?> clazz, final JSONType type) {
        if (clazz != null && clazz.getName().equals("org.springframework.security.web.savedrequest.DefaultSavedRequest")) {
            return TypeUtils.loadClass("org.springframework.security.web.savedrequest.DefaultSavedRequest$Builder");
        }
        if (type == null) {
            return null;
        }
        final Class<?> builderClass = type.builder();
        if (builderClass == Void.class) {
            return null;
        }
        return builderClass;
    }
}
