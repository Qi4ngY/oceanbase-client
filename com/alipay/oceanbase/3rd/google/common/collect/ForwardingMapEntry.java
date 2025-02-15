package com.alipay.oceanbase.3rd.google.common.collect;

import com.alipay.oceanbase.3rd.google.common.annotations.Beta;
import com.alipay.oceanbase.3rd.google.common.base.Objects;
import javax.annotation.Nullable;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;
import java.util.Map;

@GwtCompatible
public abstract class ForwardingMapEntry<K, V> extends ForwardingObject implements Map.Entry<K, V>
{
    protected ForwardingMapEntry() {
    }
    
    @Override
    protected abstract Map.Entry<K, V> delegate();
    
    @Override
    public K getKey() {
        return this.delegate().getKey();
    }
    
    @Override
    public V getValue() {
        return this.delegate().getValue();
    }
    
    @Override
    public V setValue(final V value) {
        return this.delegate().setValue(value);
    }
    
    @Override
    public boolean equals(@Nullable final Object object) {
        return this.delegate().equals(object);
    }
    
    @Override
    public int hashCode() {
        return this.delegate().hashCode();
    }
    
    protected boolean standardEquals(@Nullable final Object object) {
        if (object instanceof Map.Entry) {
            final Map.Entry<?, ?> that = (Map.Entry<?, ?>)object;
            return Objects.equal(this.getKey(), that.getKey()) && Objects.equal(this.getValue(), that.getValue());
        }
        return false;
    }
    
    protected int standardHashCode() {
        final K k = this.getKey();
        final V v = this.getValue();
        return ((k == null) ? 0 : k.hashCode()) ^ ((v == null) ? 0 : v.hashCode());
    }
    
    @Beta
    protected String standardToString() {
        final String value = String.valueOf(String.valueOf(this.getKey()));
        final String value2 = String.valueOf(String.valueOf(this.getValue()));
        return new StringBuilder(1 + value.length() + value2.length()).append(value).append("=").append(value2).toString();
    }
}
