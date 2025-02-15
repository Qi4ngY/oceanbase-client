package com.alipay.oceanbase.3rd.google.common.util.concurrent;

import javax.annotation.Nullable;

public interface FutureCallback<V>
{
    void onSuccess(@Nullable final V p0);
    
    void onFailure(final Throwable p0);
}
