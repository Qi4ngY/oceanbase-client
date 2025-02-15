package com.alipay.oceanbase.jdbc.exceptions;

import java.sql.SQLException;

public class MySQLTransientException extends SQLException
{
    static final long serialVersionUID = -1885878228558607563L;
    
    public MySQLTransientException(final String reason, final String SQLState, final int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public MySQLTransientException(final String reason, final String SQLState) {
        super(reason, SQLState);
    }
    
    public MySQLTransientException(final String reason) {
        super(reason);
    }
    
    public MySQLTransientException() {
    }
}
