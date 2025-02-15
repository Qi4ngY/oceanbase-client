package com.alipay.oceanbase.3rd.google.common.io;

import java.io.IOException;
import com.alipay.oceanbase.3rd.google.common.annotations.Beta;

@Beta
public interface ByteProcessor<T>
{
    boolean processBytes(final byte[] p0, final int p1, final int p2) throws IOException;
    
    T getResult();
}
