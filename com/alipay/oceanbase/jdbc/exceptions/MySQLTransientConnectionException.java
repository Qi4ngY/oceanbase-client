package com.alipay.oceanbase.jdbc.exceptions;

public class MySQLTransientConnectionException extends MySQLTransientException
{
    static final long serialVersionUID = 8699144578759941201L;
    
    public MySQLTransientConnectionException(final String reason, final String SQLState, final int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public MySQLTransientConnectionException(final String reason, final String SQLState) {
        super(reason, SQLState);
    }
    
    public MySQLTransientConnectionException(final String reason) {
        super(reason);
    }
    
    public MySQLTransientConnectionException() {
    }
}
