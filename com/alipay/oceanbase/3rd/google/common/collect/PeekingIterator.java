package com.alipay.oceanbase.3rd.google.common.collect;

import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;
import java.util.Iterator;

@GwtCompatible
public interface PeekingIterator<E> extends Iterator<E>
{
    E peek();
    
    E next();
    
    void remove();
}
