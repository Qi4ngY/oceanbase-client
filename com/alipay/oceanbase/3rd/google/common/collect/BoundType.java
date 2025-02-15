package com.alipay.oceanbase.3rd.google.common.collect;

import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
public enum BoundType
{
    OPEN {
        @Override
        BoundType flip() {
            return BoundType$1.CLOSED;
        }
    }, 
    CLOSED {
        @Override
        BoundType flip() {
            return BoundType$2.OPEN;
        }
    };
    
    static BoundType forBoolean(final boolean inclusive) {
        return inclusive ? BoundType.CLOSED : BoundType.OPEN;
    }
    
    abstract BoundType flip();
}
