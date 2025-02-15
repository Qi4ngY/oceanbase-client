package com.alipay.oceanbase.3rd.google.common.base;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public abstract class FinalizableSoftReference<T> extends SoftReference<T> implements FinalizableReference
{
    protected FinalizableSoftReference(final T referent, final FinalizableReferenceQueue queue) {
        super(referent, queue.queue);
        queue.cleanUp();
    }
}
