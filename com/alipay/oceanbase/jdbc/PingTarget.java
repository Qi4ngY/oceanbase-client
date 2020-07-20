package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;

public interface PingTarget
{
    void doPing() throws SQLException;
}
