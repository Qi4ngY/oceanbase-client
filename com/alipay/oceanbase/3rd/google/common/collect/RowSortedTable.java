package com.alipay.oceanbase.3rd.google.common.collect;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import com.alipay.oceanbase.3rd.google.common.annotations.Beta;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
@Beta
public interface RowSortedTable<R, C, V> extends Table<R, C, V>
{
    SortedSet<R> rowKeySet();
    
    SortedMap<R, Map<C, V>> rowMap();
}
