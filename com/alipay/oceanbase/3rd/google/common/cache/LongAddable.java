package com.alipay.oceanbase.3rd.google.common.cache;

import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
interface LongAddable
{
    void increment();
    
    void add(final long p0);
    
    long sum();
}
