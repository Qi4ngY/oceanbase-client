package com.alipay.oceanbase.3rd.google.common.base;

import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;
import com.alipay.oceanbase.3rd.google.common.annotations.Beta;

@Beta
@GwtCompatible
public abstract class Ticker
{
    private static final Ticker SYSTEM_TICKER;
    
    protected Ticker() {
    }
    
    public abstract long read();
    
    public static Ticker systemTicker() {
        return Ticker.SYSTEM_TICKER;
    }
    
    static {
        SYSTEM_TICKER = new Ticker() {
            @Override
            public long read() {
                return Platform.systemNanoTime();
            }
        };
    }
}
