package com.alipay.oceanbase.3rd.google.common.collect;

import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;
import java.util.Iterator;

@GwtCompatible
public abstract class ForwardingIterator<T> extends ForwardingObject implements Iterator<T>
{
    protected ForwardingIterator() {
    }
    
    @Override
    protected abstract Iterator<T> delegate();
    
    @Override
    public boolean hasNext() {
        return this.delegate().hasNext();
    }
    
    @Override
    public T next() {
        return this.delegate().next();
    }
    
    @Override
    public void remove() {
        this.delegate().remove();
    }
}
