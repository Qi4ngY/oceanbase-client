package com.alipay.oceanbase.3rd.google.common.util.concurrent;

import com.alipay.oceanbase.3rd.google.common.annotations.Beta;

@Beta
public interface FutureFallback<V>
{
    ListenableFuture<V> create(final Throwable p0) throws Exception;
}
