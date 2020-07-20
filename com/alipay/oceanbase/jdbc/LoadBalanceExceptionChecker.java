package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;

public interface LoadBalanceExceptionChecker extends Extension
{
    boolean shouldExceptionTriggerFailover(final SQLException p0);
}
