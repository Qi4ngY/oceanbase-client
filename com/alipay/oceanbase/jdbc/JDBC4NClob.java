package com.alipay.oceanbase.jdbc;

import java.sql.NClob;

public class JDBC4NClob extends Clob implements NClob
{
    JDBC4NClob(final ExceptionInterceptor exceptionInterceptor) {
        super(exceptionInterceptor);
    }
    
    JDBC4NClob(final String charDataInit, final ExceptionInterceptor exceptionInterceptor) {
        super(charDataInit, exceptionInterceptor);
    }
}
