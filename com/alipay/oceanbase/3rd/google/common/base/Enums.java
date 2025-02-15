package com.alipay.oceanbase.3rd.google.common.base;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.WeakHashMap;
import java.util.Iterator;
import java.util.EnumSet;
import java.util.HashMap;
import java.lang.reflect.Field;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtIncompatible;
import java.lang.ref.WeakReference;
import java.util.Map;
import com.alipay.oceanbase.3rd.google.common.annotations.Beta;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible(emulated = true)
@Beta
public final class Enums
{
    @GwtIncompatible("java.lang.ref.WeakReference")
    private static final Map<Class<? extends Enum<?>>, Map<String, WeakReference<? extends Enum<?>>>> enumConstantCache;
    
    private Enums() {
    }
    
    @GwtIncompatible("reflection")
    public static Field getField(final Enum<?> enumValue) {
        final Class<?> clazz = enumValue.getDeclaringClass();
        try {
            return clazz.getDeclaredField(enumValue.name());
        }
        catch (NoSuchFieldException impossible) {
            throw new AssertionError((Object)impossible);
        }
    }
    
    public static <T extends Enum<T>> Optional<T> getIfPresent(final Class<T> enumClass, final String value) {
        Preconditions.checkNotNull(enumClass);
        Preconditions.checkNotNull(value);
        return Platform.getEnumIfPresent(enumClass, value);
    }
    
    @GwtIncompatible("java.lang.ref.WeakReference")
    private static <T extends Enum<T>> Map<String, WeakReference<? extends Enum<?>>> populateCache(final Class<T> enumClass) {
        final Map<String, WeakReference<? extends Enum<?>>> result = new HashMap<String, WeakReference<? extends Enum<?>>>();
        for (final T enumInstance : EnumSet.allOf(enumClass)) {
            result.put(enumInstance.name(), new WeakReference<Enum<?>>(enumInstance));
        }
        Enums.enumConstantCache.put(enumClass, result);
        return result;
    }
    
    @GwtIncompatible("java.lang.ref.WeakReference")
    static <T extends Enum<T>> Map<String, WeakReference<? extends Enum<?>>> getEnumConstants(final Class<T> enumClass) {
        synchronized (Enums.enumConstantCache) {
            Map<String, WeakReference<? extends Enum<?>>> constants = Enums.enumConstantCache.get(enumClass);
            if (constants == null) {
                constants = populateCache((Class<Enum>)enumClass);
            }
            return constants;
        }
    }
    
    public static <T extends Enum<T>> Converter<String, T> stringConverter(final Class<T> enumClass) {
        return (Converter<String, T>)new StringConverter((Class<Enum>)enumClass);
    }
    
    static {
        enumConstantCache = new WeakHashMap<Class<? extends Enum<?>>, Map<String, WeakReference<? extends Enum<?>>>>();
    }
    
    private static final class StringConverter<T extends Enum<T>> extends Converter<String, T> implements Serializable
    {
        private final Class<T> enumClass;
        private static final long serialVersionUID = 0L;
        
        StringConverter(final Class<T> enumClass) {
            this.enumClass = Preconditions.checkNotNull(enumClass);
        }
        
        @Override
        protected T doForward(final String value) {
            return Enum.valueOf(this.enumClass, value);
        }
        
        @Override
        protected String doBackward(final T enumValue) {
            return enumValue.name();
        }
        
        @Override
        public boolean equals(@Nullable final Object object) {
            if (object instanceof StringConverter) {
                final StringConverter<?> that = (StringConverter<?>)object;
                return this.enumClass.equals(that.enumClass);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return this.enumClass.hashCode();
        }
        
        @Override
        public String toString() {
            final String value = String.valueOf(String.valueOf(this.enumClass.getName()));
            return new StringBuilder(29 + value.length()).append("Enums.stringConverter(").append(value).append(".class)").toString();
        }
    }
}
