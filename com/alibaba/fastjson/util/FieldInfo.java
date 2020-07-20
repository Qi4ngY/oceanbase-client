package com.alibaba.fastjson.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Modifier;
import com.alibaba.fastjson.annotation.JSONField;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FieldInfo implements Comparable<FieldInfo>
{
    public final String name;
    public final Method method;
    public final Field field;
    private int ordinal;
    public final Class<?> fieldClass;
    public final Type fieldType;
    public final Class<?> declaringClass;
    public final boolean getOnly;
    public final int serialzeFeatures;
    public final int parserFeatures;
    public final String label;
    private final JSONField fieldAnnotation;
    private final JSONField methodAnnotation;
    public final boolean fieldAccess;
    public final boolean fieldTransient;
    public final char[] name_chars;
    public final boolean isEnum;
    public final boolean jsonDirect;
    public final boolean unwrapped;
    public final String format;
    public final String[] alternateNames;
    
    public FieldInfo(final String name, final Class<?> declaringClass, final Class<?> fieldClass, final Type fieldType, final Field field, int ordinal, final int serialzeFeatures, final int parserFeatures) {
        this.ordinal = 0;
        if (ordinal < 0) {
            ordinal = 0;
        }
        this.name = name;
        this.declaringClass = declaringClass;
        this.fieldClass = fieldClass;
        this.fieldType = fieldType;
        this.method = null;
        this.field = field;
        this.ordinal = ordinal;
        this.serialzeFeatures = serialzeFeatures;
        this.parserFeatures = parserFeatures;
        this.isEnum = fieldClass.isEnum();
        if (field != null) {
            final int modifiers = field.getModifiers();
            this.fieldAccess = ((modifiers & 0x1) != 0x0 || this.method == null);
            this.fieldTransient = Modifier.isTransient(modifiers);
        }
        else {
            this.fieldTransient = false;
            this.fieldAccess = false;
        }
        this.name_chars = this.genFieldNameChars();
        if (field != null) {
            TypeUtils.setAccessible(field);
        }
        this.label = "";
        this.fieldAnnotation = ((field == null) ? null : TypeUtils.getAnnotation(field, JSONField.class));
        this.methodAnnotation = null;
        this.getOnly = false;
        this.jsonDirect = false;
        this.unwrapped = false;
        this.format = null;
        this.alternateNames = new String[0];
    }
    
    public FieldInfo(final String name, final Method method, final Field field, final Class<?> clazz, final Type type, final int ordinal, final int serialzeFeatures, final int parserFeatures, final JSONField fieldAnnotation, final JSONField methodAnnotation, final String label) {
        this(name, method, field, clazz, type, ordinal, serialzeFeatures, parserFeatures, fieldAnnotation, methodAnnotation, label, null);
    }
    
    public FieldInfo(String name, final Method method, final Field field, final Class<?> clazz, final Type type, int ordinal, final int serialzeFeatures, final int parserFeatures, final JSONField fieldAnnotation, final JSONField methodAnnotation, final String label, final Map<TypeVariable, Type> genericInfo) {
        this.ordinal = 0;
        if (field != null) {
            final String fieldName = field.getName();
            if (fieldName.equals(name)) {
                name = fieldName;
            }
        }
        if (ordinal < 0) {
            ordinal = 0;
        }
        this.name = name;
        this.method = method;
        this.field = field;
        this.ordinal = ordinal;
        this.serialzeFeatures = serialzeFeatures;
        this.parserFeatures = parserFeatures;
        this.fieldAnnotation = fieldAnnotation;
        this.methodAnnotation = methodAnnotation;
        if (field != null) {
            final int modifiers = field.getModifiers();
            this.fieldAccess = ((modifiers & 0x1) != 0x0 || method == null);
            this.fieldTransient = (Modifier.isTransient(modifiers) || TypeUtils.isTransient(method));
        }
        else {
            this.fieldAccess = false;
            this.fieldTransient = TypeUtils.isTransient(method);
        }
        if (label != null && label.length() > 0) {
            this.label = label;
        }
        else {
            this.label = "";
        }
        String format = null;
        final JSONField annotation = this.getAnnotation();
        boolean jsonDirect = false;
        if (annotation != null) {
            format = annotation.format();
            if (format.trim().length() == 0) {
                format = null;
            }
            jsonDirect = annotation.jsonDirect();
            this.unwrapped = annotation.unwrapped();
            this.alternateNames = annotation.alternateNames();
        }
        else {
            jsonDirect = false;
            this.unwrapped = false;
            this.alternateNames = new String[0];
        }
        this.format = format;
        this.name_chars = this.genFieldNameChars();
        if (method != null) {
            TypeUtils.setAccessible(method);
        }
        if (field != null) {
            TypeUtils.setAccessible(field);
        }
        boolean getOnly = false;
        Class<?> fieldClass;
        Type fieldType;
        if (method != null) {
            final Class<?>[] types;
            if ((types = method.getParameterTypes()).length == 1) {
                fieldClass = types[0];
                fieldType = method.getGenericParameterTypes()[0];
            }
            else if (types.length == 2 && types[0] == String.class && types[1] == Object.class) {
                fieldClass = (Class<?>)(fieldType = types[0]);
            }
            else {
                fieldClass = method.getReturnType();
                fieldType = method.getGenericReturnType();
                getOnly = true;
            }
            this.declaringClass = method.getDeclaringClass();
        }
        else {
            fieldClass = field.getType();
            fieldType = field.getGenericType();
            this.declaringClass = field.getDeclaringClass();
            getOnly = Modifier.isFinal(field.getModifiers());
        }
        this.getOnly = getOnly;
        this.jsonDirect = (jsonDirect && fieldClass == String.class);
        if (clazz != null && fieldClass == Object.class && fieldType instanceof TypeVariable) {
            final TypeVariable<?> tv = (TypeVariable<?>)fieldType;
            final Type genericFieldType = getInheritGenericType(clazz, type, tv);
            if (genericFieldType != null) {
                this.fieldClass = TypeUtils.getClass(genericFieldType);
                this.fieldType = genericFieldType;
                this.isEnum = fieldClass.isEnum();
                return;
            }
        }
        Type genericFieldType2 = fieldType;
        if (!(fieldType instanceof Class)) {
            genericFieldType2 = getFieldType(clazz, (type != null) ? type : clazz, fieldType, genericInfo);
            if (genericFieldType2 != fieldType) {
                if (genericFieldType2 instanceof ParameterizedType) {
                    fieldClass = TypeUtils.getClass(genericFieldType2);
                }
                else if (genericFieldType2 instanceof Class) {
                    fieldClass = TypeUtils.getClass(genericFieldType2);
                }
            }
        }
        this.fieldType = genericFieldType2;
        this.fieldClass = fieldClass;
        this.isEnum = fieldClass.isEnum();
    }
    
    protected char[] genFieldNameChars() {
        final int nameLen = this.name.length();
        final char[] name_chars = new char[nameLen + 3];
        this.name.getChars(0, this.name.length(), name_chars, 1);
        name_chars[nameLen + 1] = (name_chars[0] = '\"');
        name_chars[nameLen + 2] = ':';
        return name_chars;
    }
    
    public <T extends Annotation> T getAnnation(final Class<T> annotationClass) {
        if (annotationClass == JSONField.class) {
            return (T)this.getAnnotation();
        }
        T annotatition = null;
        if (this.method != null) {
            annotatition = TypeUtils.getAnnotation(this.method, annotationClass);
        }
        if (annotatition == null && this.field != null) {
            annotatition = TypeUtils.getAnnotation(this.field, annotationClass);
        }
        return annotatition;
    }
    
    public static Type getFieldType(final Class<?> clazz, final Type type, final Type fieldType) {
        return getFieldType(clazz, type, fieldType, null);
    }
    
    public static Type getFieldType(final Class<?> clazz, final Type type, Type fieldType, final Map<TypeVariable, Type> genericInfo) {
        if (clazz == null || type == null) {
            return fieldType;
        }
        if (fieldType instanceof GenericArrayType) {
            final GenericArrayType genericArrayType = (GenericArrayType)fieldType;
            final Type componentType = genericArrayType.getGenericComponentType();
            final Type componentTypeX = getFieldType(clazz, type, componentType, genericInfo);
            if (componentType != componentTypeX) {
                final Type fieldTypeX = Array.newInstance(TypeUtils.getClass(componentTypeX), 0).getClass();
                return fieldTypeX;
            }
            return fieldType;
        }
        else {
            if (!TypeUtils.isGenericParamType(type)) {
                return fieldType;
            }
            if (fieldType instanceof TypeVariable) {
                final ParameterizedType paramType = (ParameterizedType)TypeUtils.getGenericParamType(type);
                final Class<?> parameterizedClass = TypeUtils.getClass(paramType);
                final TypeVariable<?> typeVar = (TypeVariable<?>)fieldType;
                final TypeVariable<?>[] typeVariables = parameterizedClass.getTypeParameters();
                for (int i = 0; i < typeVariables.length; ++i) {
                    if (typeVariables[i].getName().equals(typeVar.getName())) {
                        fieldType = paramType.getActualTypeArguments()[i];
                        return fieldType;
                    }
                }
            }
            if (fieldType instanceof ParameterizedType) {
                final ParameterizedType parameterizedFieldType = (ParameterizedType)fieldType;
                final Type[] arguments = parameterizedFieldType.getActualTypeArguments();
                boolean changed = getArgument(arguments, genericInfo);
                if (!changed) {
                    ParameterizedType paramType2;
                    TypeVariable<?>[] typeVariables2;
                    if (type instanceof ParameterizedType) {
                        paramType2 = (ParameterizedType)type;
                        typeVariables2 = clazz.getTypeParameters();
                    }
                    else if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
                        paramType2 = (ParameterizedType)clazz.getGenericSuperclass();
                        typeVariables2 = clazz.getSuperclass().getTypeParameters();
                    }
                    else {
                        paramType2 = parameterizedFieldType;
                        typeVariables2 = type.getClass().getTypeParameters();
                    }
                    changed = getArgument(arguments, typeVariables2, paramType2.getActualTypeArguments());
                }
                if (changed) {
                    fieldType = new ParameterizedTypeImpl(arguments, parameterizedFieldType.getOwnerType(), parameterizedFieldType.getRawType());
                    return fieldType;
                }
            }
            return fieldType;
        }
    }
    
    private static boolean getArgument(final Type[] typeArgs, final Map<TypeVariable, Type> genericInfo) {
        if (genericInfo == null || genericInfo.size() == 0) {
            return false;
        }
        boolean changed = false;
        for (int i = 0; i < typeArgs.length; ++i) {
            final Type typeArg = typeArgs[i];
            if (typeArg instanceof ParameterizedType) {
                final ParameterizedType p_typeArg = (ParameterizedType)typeArg;
                final Type[] p_typeArg_args = p_typeArg.getActualTypeArguments();
                final boolean p_changed = getArgument(p_typeArg_args, genericInfo);
                if (p_changed) {
                    typeArgs[i] = new ParameterizedTypeImpl(p_typeArg_args, p_typeArg.getOwnerType(), p_typeArg.getRawType());
                    changed = true;
                }
            }
            else if (typeArg instanceof TypeVariable && genericInfo.containsKey(typeArg)) {
                typeArgs[i] = genericInfo.get(typeArg);
                changed = true;
            }
        }
        return changed;
    }
    
    private static boolean getArgument(final Type[] typeArgs, final TypeVariable[] typeVariables, final Type[] arguments) {
        if (arguments == null || typeVariables.length == 0) {
            return false;
        }
        boolean changed = false;
        for (int i = 0; i < typeArgs.length; ++i) {
            final Type typeArg = typeArgs[i];
            if (typeArg instanceof ParameterizedType) {
                final ParameterizedType p_typeArg = (ParameterizedType)typeArg;
                final Type[] p_typeArg_args = p_typeArg.getActualTypeArguments();
                final boolean p_changed = getArgument(p_typeArg_args, typeVariables, arguments);
                if (p_changed) {
                    typeArgs[i] = new ParameterizedTypeImpl(p_typeArg_args, p_typeArg.getOwnerType(), p_typeArg.getRawType());
                    changed = true;
                }
            }
            else if (typeArg instanceof TypeVariable) {
                for (int j = 0; j < typeVariables.length; ++j) {
                    if (typeArg.equals(typeVariables[j])) {
                        typeArgs[i] = arguments[j];
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }
    
    private static Type getInheritGenericType(final Class<?> clazz, final Type type, final TypeVariable<?> tv) {
        final GenericDeclaration gd = (GenericDeclaration)tv.getGenericDeclaration();
        Class<?> class_gd = null;
        if (gd instanceof Class) {
            class_gd = (Class<?>)tv.getGenericDeclaration();
        }
        Type[] arguments = null;
        if (class_gd == clazz) {
            if (type instanceof ParameterizedType) {
                final ParameterizedType ptype = (ParameterizedType)type;
                arguments = ptype.getActualTypeArguments();
            }
        }
        else {
            for (Class<?> c = clazz; c != null && c != Object.class && c != class_gd; c = c.getSuperclass()) {
                final Type superType = c.getGenericSuperclass();
                if (superType instanceof ParameterizedType) {
                    final ParameterizedType p_superType = (ParameterizedType)superType;
                    final Type[] p_superType_args = p_superType.getActualTypeArguments();
                    getArgument(p_superType_args, c.getTypeParameters(), arguments);
                    arguments = p_superType_args;
                }
            }
        }
        if (arguments == null || class_gd == null) {
            return null;
        }
        Type actualType = null;
        final TypeVariable<?>[] typeVariables = class_gd.getTypeParameters();
        for (int j = 0; j < typeVariables.length; ++j) {
            if (tv.equals(typeVariables[j])) {
                actualType = arguments[j];
                break;
            }
        }
        return actualType;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    public Member getMember() {
        if (this.method != null) {
            return this.method;
        }
        return this.field;
    }
    
    protected Class<?> getDeclaredClass() {
        if (this.method != null) {
            return this.method.getDeclaringClass();
        }
        if (this.field != null) {
            return this.field.getDeclaringClass();
        }
        return null;
    }
    
    @Override
    public int compareTo(final FieldInfo o) {
        if (this.ordinal < o.ordinal) {
            return -1;
        }
        if (this.ordinal > o.ordinal) {
            return 1;
        }
        final int result = this.name.compareTo(o.name);
        if (result != 0) {
            return result;
        }
        final Class<?> thisDeclaringClass = this.getDeclaredClass();
        final Class<?> otherDeclaringClass = o.getDeclaredClass();
        if (thisDeclaringClass != null && otherDeclaringClass != null && thisDeclaringClass != otherDeclaringClass) {
            if (thisDeclaringClass.isAssignableFrom(otherDeclaringClass)) {
                return -1;
            }
            if (otherDeclaringClass.isAssignableFrom(thisDeclaringClass)) {
                return 1;
            }
        }
        final boolean isSampeType = this.field != null && this.field.getType() == this.fieldClass;
        final boolean oSameType = o.field != null && o.field.getType() == o.fieldClass;
        if (isSampeType && !oSameType) {
            return 1;
        }
        if (oSameType && !isSampeType) {
            return -1;
        }
        if (o.fieldClass.isPrimitive() && !this.fieldClass.isPrimitive()) {
            return 1;
        }
        if (this.fieldClass.isPrimitive() && !o.fieldClass.isPrimitive()) {
            return -1;
        }
        if (o.fieldClass.getName().startsWith("java.") && !this.fieldClass.getName().startsWith("java.")) {
            return 1;
        }
        if (this.fieldClass.getName().startsWith("java.") && !o.fieldClass.getName().startsWith("java.")) {
            return -1;
        }
        return this.fieldClass.getName().compareTo(o.fieldClass.getName());
    }
    
    public JSONField getAnnotation() {
        if (this.fieldAnnotation != null) {
            return this.fieldAnnotation;
        }
        return this.methodAnnotation;
    }
    
    public String getFormat() {
        return this.format;
    }
    
    public Object get(final Object javaObject) throws IllegalAccessException, InvocationTargetException {
        return (this.method != null) ? this.method.invoke(javaObject, new Object[0]) : this.field.get(javaObject);
    }
    
    public void set(final Object javaObject, final Object value) throws IllegalAccessException, InvocationTargetException {
        if (this.method != null) {
            this.method.invoke(javaObject, value);
            return;
        }
        this.field.set(javaObject, value);
    }
    
    public void setAccessible() throws SecurityException {
        if (this.method != null) {
            TypeUtils.setAccessible(this.method);
            return;
        }
        TypeUtils.setAccessible(this.field);
    }
}
