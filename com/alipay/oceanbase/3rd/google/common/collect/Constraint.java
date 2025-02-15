package com.alipay.oceanbase.3rd.google.common.collect;

import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
interface Constraint<E>
{
    E checkElement(final E p0);
    
    String toString();
}
