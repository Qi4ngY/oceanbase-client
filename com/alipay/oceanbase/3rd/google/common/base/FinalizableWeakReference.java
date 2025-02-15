package com.alipay.oceanbase.3rd.google.common.base;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public abstract class FinalizableWeakReference<T> extends WeakReference<T> implements FinalizableReference
{
    protected FinalizableWeakReference(final T referent, final FinalizableReferenceQueue queue) {
        super(referent, queue.queue);
        queue.cleanUp();
    }
}
