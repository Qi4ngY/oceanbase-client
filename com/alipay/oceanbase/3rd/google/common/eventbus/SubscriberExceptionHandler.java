package com.alipay.oceanbase.3rd.google.common.eventbus;

public interface SubscriberExceptionHandler
{
    void handleException(final Throwable p0, final SubscriberExceptionContext p1);
}
