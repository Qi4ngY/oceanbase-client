package com.alipay.oceanbase.3rd.google.common.base;

import java.lang.ref.WeakReference;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible(emulated = true)
final class Platform
{
    private Platform() {
    }
    
    static long systemNanoTime() {
        return System.nanoTime();
    }
    
    static CharMatcher precomputeCharMatcher(final CharMatcher matcher) {
        return matcher.precomputedInternal();
    }
    
    static <T extends Enum<T>> Optional<T> getEnumIfPresent(final Class<T> enumClass, final String value) {
        final WeakReference<? extends Enum<?>> ref = Enums.getEnumConstants(enumClass).get(value);
        return (ref == null) ? Optional.absent() : Optional.of(enumClass.cast(ref.get()));
    }
}
