package com.alipay.oceanbase.3rd.google.common.cache;

import java.util.concurrent.atomic.AtomicLong;
import com.alipay.oceanbase.3rd.google.common.base.Supplier;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible(emulated = true)
final class LongAddables
{
    private static final Supplier<LongAddable> SUPPLIER;
    
    public static LongAddable create() {
        return LongAddables.SUPPLIER.get();
    }
    
    static {
        Supplier<LongAddable> supplier;
        try {
            new LongAdder();
            supplier = new Supplier<LongAddable>() {
                @Override
                public LongAddable get() {
                    return new LongAdder();
                }
            };
        }
        catch (Throwable t) {
            supplier = new Supplier<LongAddable>() {
                @Override
                public LongAddable get() {
                    return new PureJavaLongAddable();
                }
            };
        }
        SUPPLIER = supplier;
    }
    
    private static final class PureJavaLongAddable extends AtomicLong implements LongAddable
    {
        @Override
        public void increment() {
            this.getAndIncrement();
        }
        
        @Override
        public void add(final long x) {
            this.getAndAdd(x);
        }
        
        @Override
        public long sum() {
            return this.get();
        }
    }
}
