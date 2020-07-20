package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;

public interface LoadBalancedConnection extends MySQLConnection
{
    boolean addHost(final String p0) throws SQLException;
    
    void removeHost(final String p0) throws SQLException;
    
    void removeHostWhenNotInUse(final String p0) throws SQLException;
    
    void ping(final boolean p0) throws SQLException;
}
