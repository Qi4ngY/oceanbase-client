package com.alipay.oceanbase.jdbc.feedback;

import java.sql.SQLException;

public class ObFBOutOfBoundException extends SQLException
{
    ObFBOutOfBoundException(final String message) {
        super(message);
    }
}
