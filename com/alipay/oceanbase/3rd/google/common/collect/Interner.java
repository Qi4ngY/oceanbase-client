package com.alipay.oceanbase.3rd.google.common.collect;

import com.alipay.oceanbase.3rd.google.common.annotations.Beta;

@Beta
public interface Interner<E>
{
    E intern(final E p0);
}
