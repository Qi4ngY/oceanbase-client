package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import java.util.Properties;

public interface ConnectionPropertiesTransform
{
    Properties transformProperties(final Properties p0) throws SQLException;
}
