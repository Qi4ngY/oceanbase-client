package com.alipay.oceanbase.3rd.google.common.util.concurrent;

import javax.annotation.Nullable;

public class UncheckedTimeoutException extends RuntimeException
{
    private static final long serialVersionUID = 0L;
    
    public UncheckedTimeoutException() {
    }
    
    public UncheckedTimeoutException(@Nullable final String message) {
        super(message);
    }
    
    public UncheckedTimeoutException(@Nullable final Throwable cause) {
        super(cause);
    }
    
    public UncheckedTimeoutException(@Nullable final String message, @Nullable final Throwable cause) {
        super(message, cause);
    }
}
