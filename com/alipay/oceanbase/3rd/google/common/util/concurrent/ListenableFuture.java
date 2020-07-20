package com.alipay.oceanbase.3rd.google.common.util.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public interface ListenableFuture<V> extends Future<V>
{
    void addListener(final Runnable p0, final Executor p1);
}
