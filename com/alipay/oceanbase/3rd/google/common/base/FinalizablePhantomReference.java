package com.alipay.oceanbase.3rd.google.common.base;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.PhantomReference;

public abstract class FinalizablePhantomReference<T> extends PhantomReference<T> implements FinalizableReference
{
    protected FinalizablePhantomReference(final T referent, final FinalizableReferenceQueue queue) {
        super(referent, queue.queue);
        queue.cleanUp();
    }
}
