package com.alipay.oceanbase.3rd.google.common.cache;

import com.alipay.oceanbase.3rd.google.common.base.Preconditions;
import java.util.concurrent.Executor;
import com.alipay.oceanbase.3rd.google.common.annotations.Beta;

@Beta
public final class RemovalListeners
{
    private RemovalListeners() {
    }
    
    public static <K, V> RemovalListener<K, V> asynchronous(final RemovalListener<K, V> listener, final Executor executor) {
        Preconditions.checkNotNull(listener);
        Preconditions.checkNotNull(executor);
        return new RemovalListener<K, V>() {
            @Override
            public void onRemoval(final RemovalNotification<K, V> notification) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        listener.onRemoval(notification);
                    }
                });
            }
        };
    }
}
