package com.alipay.oceanbase.3rd.google.common.io;

import java.io.IOException;

@Deprecated
public interface OutputSupplier<T>
{
    T getOutput() throws IOException;
}
