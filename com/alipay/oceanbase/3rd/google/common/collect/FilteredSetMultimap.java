package com.alipay.oceanbase.3rd.google.common.collect;

import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
interface FilteredSetMultimap<K, V> extends FilteredMultimap<K, V>, SetMultimap<K, V>
{
    SetMultimap<K, V> unfiltered();
}
