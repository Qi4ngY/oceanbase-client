package com.alibaba.fastjson.util;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.io.Closeable;
import java.io.IOException;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.asm.TypeCollector;
import com.alibaba.fastjson.asm.ClassReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.lang.reflect.Method;

public class ASMUtils
{
    public static final String JAVA_VM_NAME;
    public static final boolean IS_ANDROID;
    
    public static boolean isAndroid(final String vmName) {
        if (vmName == null) {
            return false;
        }
        final String lowerVMName = vmName.toLowerCase();
        return lowerVMName.contains("dalvik") || lowerVMName.contains("lemur");
    }
    
    public static String desc(final Method method) {
        final Class<?>[] types = method.getParameterTypes();
        final StringBuilder buf = new StringBuilder(types.length + 1 << 4);
        buf.append('(');
        for (int i = 0; i < types.length; ++i) {
            buf.append(desc(types[i]));
        }
        buf.append(')');
        buf.append(desc(method.getReturnType()));
        return buf.toString();
    }
    
    public static String desc(final Class<?> returnType) {
        if (returnType.isPrimitive()) {
            return getPrimitiveLetter(returnType);
        }
        if (returnType.isArray()) {
            return "[" + desc(returnType.getComponentType());
        }
        return "L" + type(returnType) + ";";
    }
    
    public static String type(final Class<?> parameterType) {
        if (parameterType.isArray()) {
            return "[" + desc(parameterType.getComponentType());
        }
        if (!parameterType.isPrimitive()) {
            final String clsName = parameterType.getName();
            return clsName.replace('.', '/');
        }
        return getPrimitiveLetter(parameterType);
    }
    
    public static String getPrimitiveLetter(final Class<?> type) {
        if (Integer.TYPE == type) {
            return "I";
        }
        if (Void.TYPE == type) {
            return "V";
        }
        if (Boolean.TYPE == type) {
            return "Z";
        }
        if (Character.TYPE == type) {
            return "C";
        }
        if (Byte.TYPE == type) {
            return "B";
        }
        if (Short.TYPE == type) {
            return "S";
        }
        if (Float.TYPE == type) {
            return "F";
        }
        if (Long.TYPE == type) {
            return "J";
        }
        if (Double.TYPE == type) {
            return "D";
        }
        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }
    
    public static Type getMethodType(final Class<?> clazz, final String methodName) {
        try {
            final Method method = clazz.getMethod(methodName, (Class<?>[])new Class[0]);
            return method.getGenericReturnType();
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public static boolean checkName(final String name) {
        for (int i = 0; i < name.length(); ++i) {
            final char c = name.charAt(i);
            if (c < '\u0001' || c > '\u007f' || c == '.') {
                return false;
            }
        }
        return true;
    }
    
    public static String[] lookupParameterNames(final AccessibleObject methodOrCtor) {
        if (ASMUtils.IS_ANDROID) {
            return new String[0];
        }
        Class<?>[] types;
        String name;
        Class<?> declaringClass;
        Annotation[][] parameterAnnotations;
        if (methodOrCtor instanceof Method) {
            final Method method = (Method)methodOrCtor;
            types = method.getParameterTypes();
            name = method.getName();
            declaringClass = method.getDeclaringClass();
            parameterAnnotations = TypeUtils.getParameterAnnotations(method);
        }
        else {
            final Constructor<?> constructor = (Constructor<?>)methodOrCtor;
            types = constructor.getParameterTypes();
            declaringClass = constructor.getDeclaringClass();
            name = "<init>";
            parameterAnnotations = TypeUtils.getParameterAnnotations(constructor);
        }
        if (types.length == 0) {
            return new String[0];
        }
        ClassLoader classLoader = declaringClass.getClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        final String className = declaringClass.getName();
        final String resourceName = className.replace('.', '/') + ".class";
        final InputStream is = classLoader.getResourceAsStream(resourceName);
        if (is == null) {
            return new String[0];
        }
        try {
            final ClassReader reader = new ClassReader(is, false);
            final TypeCollector visitor = new TypeCollector(name, types);
            reader.accept(visitor);
            final String[] parameterNames = visitor.getParameterNamesForMethod();
            for (int i = 0; i < parameterNames.length; ++i) {
                final Annotation[] annotations = parameterAnnotations[i];
                if (annotations != null) {
                    for (int j = 0; j < annotations.length; ++j) {
                        if (annotations[j] instanceof JSONField) {
                            final JSONField jsonField = (JSONField)annotations[j];
                            final String fieldName = jsonField.name();
                            if (fieldName != null && fieldName.length() > 0) {
                                parameterNames[i] = fieldName;
                            }
                        }
                    }
                }
            }
            return parameterNames;
        }
        catch (IOException e) {
            return new String[0];
        }
        finally {
            IOUtils.close(is);
        }
    }
    
    static {
        JAVA_VM_NAME = System.getProperty("java.vm.name");
        IS_ANDROID = isAndroid(ASMUtils.JAVA_VM_NAME);
    }
}
