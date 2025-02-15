package com.alipay.oceanbase.3rd.google.common.base;

import javax.annotation.Nullable;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;
import com.alipay.oceanbase.3rd.google.common.annotations.Beta;

@Beta
@GwtCompatible
public class VerifyException extends RuntimeException
{
    public VerifyException() {
    }
    
    public VerifyException(@Nullable final String message) {
        super(message);
    }
}
