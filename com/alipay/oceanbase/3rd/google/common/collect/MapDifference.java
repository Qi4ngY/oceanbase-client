package com.alipay.oceanbase.3rd.google.common.collect;

import javax.annotation.Nullable;
import java.util.Map;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
public interface MapDifference<K, V>
{
    boolean areEqual();
    
    Map<K, V> entriesOnlyOnLeft();
    
    Map<K, V> entriesOnlyOnRight();
    
    Map<K, V> entriesInCommon();
    
    Map<K, ValueDifference<V>> entriesDiffering();
    
    boolean equals(@Nullable final Object p0);
    
    int hashCode();
    
    public interface ValueDifference<V>
    {
        V leftValue();
        
        V rightValue();
        
        boolean equals(@Nullable final Object p0);
        
        int hashCode();
    }
}
