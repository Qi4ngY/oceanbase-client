package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;

public interface ExceptionInterceptor extends Extension
{
    SQLException interceptException(final SQLException p0, final Connection p1);
}
