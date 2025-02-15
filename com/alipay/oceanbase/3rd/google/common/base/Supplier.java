package com.alipay.oceanbase.3rd.google.common.base;

import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
public interface Supplier<T>
{
    T get();
}
