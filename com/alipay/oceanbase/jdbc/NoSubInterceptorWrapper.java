package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import java.util.Properties;

public class NoSubInterceptorWrapper implements StatementInterceptorV2
{
    private final StatementInterceptorV2 underlyingInterceptor;
    
    public NoSubInterceptorWrapper(final StatementInterceptorV2 underlyingInterceptor) {
        if (underlyingInterceptor == null) {
            throw new RuntimeException("Interceptor to be wrapped can not be NULL");
        }
        this.underlyingInterceptor = underlyingInterceptor;
    }
    
    @Override
    public void destroy() {
        this.underlyingInterceptor.destroy();
    }
    
    @Override
    public boolean executeTopLevelOnly() {
        return this.underlyingInterceptor.executeTopLevelOnly();
    }
    
    @Override
    public void init(final Connection conn, final Properties props) throws SQLException {
        this.underlyingInterceptor.init(conn, props);
    }
    
    @Override
    public ResultSetInternalMethods postProcess(final String sql, final Statement interceptedStatement, final ResultSetInternalMethods originalResultSet, final Connection connection, final int warningCount, final boolean noIndexUsed, final boolean noGoodIndexUsed, final SQLException statementException) throws SQLException {
        this.underlyingInterceptor.postProcess(sql, interceptedStatement, originalResultSet, connection, warningCount, noIndexUsed, noGoodIndexUsed, statementException);
        return null;
    }
    
    @Override
    public ResultSetInternalMethods preProcess(final String sql, final Statement interceptedStatement, final Connection connection) throws SQLException {
        this.underlyingInterceptor.preProcess(sql, interceptedStatement, connection);
        return null;
    }
    
    public StatementInterceptorV2 getUnderlyingInterceptor() {
        return this.underlyingInterceptor;
    }
}
