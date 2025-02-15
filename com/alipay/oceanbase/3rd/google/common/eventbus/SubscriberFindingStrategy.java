package com.alipay.oceanbase.3rd.google.common.eventbus;

import com.alipay.oceanbase.3rd.google.common.collect.Multimap;

interface SubscriberFindingStrategy
{
    Multimap<Class<?>, EventSubscriber> findAllSubscribers(final Object p0);
}
