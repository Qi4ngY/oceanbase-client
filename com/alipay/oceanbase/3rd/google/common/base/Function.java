package com.alipay.oceanbase.3rd.google.common.base;

import javax.annotation.Nullable;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
public interface Function<F, T>
{
    @Nullable
    T apply(@Nullable final F p0);
    
    boolean equals(@Nullable final Object p0);
}
