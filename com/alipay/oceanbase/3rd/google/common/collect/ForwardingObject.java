package com.alipay.oceanbase.3rd.google.common.collect;

import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
public abstract class ForwardingObject
{
    protected ForwardingObject() {
    }
    
    protected abstract Object delegate();
    
    @Override
    public String toString() {
        return this.delegate().toString();
    }
}
