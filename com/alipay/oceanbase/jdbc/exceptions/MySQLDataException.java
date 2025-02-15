package com.alipay.oceanbase.jdbc.exceptions;

public class MySQLDataException extends MySQLNonTransientException
{
    static final long serialVersionUID = 4317904269797988676L;
    
    public MySQLDataException() {
    }
    
    public MySQLDataException(final String reason, final String SQLState, final int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public MySQLDataException(final String reason, final String SQLState) {
        super(reason, SQLState);
    }
    
    public MySQLDataException(final String reason) {
        super(reason);
    }
}
