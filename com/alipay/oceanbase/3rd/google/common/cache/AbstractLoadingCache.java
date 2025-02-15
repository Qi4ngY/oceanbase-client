package com.alipay.oceanbase.3rd.google.common.cache;

import java.util.Iterator;
import java.util.Map;
import com.alipay.oceanbase.3rd.google.common.collect.Maps;
import com.alipay.oceanbase.3rd.google.common.collect.ImmutableMap;
import java.util.concurrent.ExecutionException;
import com.alipay.oceanbase.3rd.google.common.util.concurrent.UncheckedExecutionException;
import com.alipay.oceanbase.3rd.google.common.annotations.Beta;

@Beta
public abstract class AbstractLoadingCache<K, V> extends AbstractCache<K, V> implements LoadingCache<K, V>
{
    protected AbstractLoadingCache() {
    }
    
    @Override
    public V getUnchecked(final K key) {
        try {
            return this.get(key);
        }
        catch (ExecutionException e) {
            throw new UncheckedExecutionException(e.getCause());
        }
    }
    
    @Override
    public ImmutableMap<K, V> getAll(final Iterable<? extends K> keys) throws ExecutionException {
        final Map<K, V> result = (Map<K, V>)Maps.newLinkedHashMap();
        for (final K key : keys) {
            if (!result.containsKey(key)) {
                result.put(key, this.get(key));
            }
        }
        return ImmutableMap.copyOf((Map<? extends K, ? extends V>)result);
    }
    
    @Override
    public final V apply(final K key) {
        return this.getUnchecked(key);
    }
    
    @Override
    public void refresh(final K key) {
        throw new UnsupportedOperationException();
    }
}
