package com.alipay.oceanbase.jdbc;

import java.sql.SQLXML;
import java.io.Reader;
import java.sql.NClob;
import java.sql.SQLException;
import java.sql.RowId;

public class JDBC4PreparedStatementHelper
{
    private JDBC4PreparedStatementHelper() {
    }
    
    static void setRowId(final PreparedStatement pstmt, final int parameterIndex, final RowId x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    static void setNClob(final PreparedStatement pstmt, final int parameterIndex, final NClob value) throws SQLException {
        if (value == null) {
            pstmt.setNull(parameterIndex, 2011);
        }
        else {
            pstmt.setNCharacterStream(parameterIndex, value.getCharacterStream(), value.length());
        }
    }
    
    static void setNClob(final PreparedStatement pstmt, final int parameterIndex, final Reader reader) throws SQLException {
        pstmt.setNCharacterStream(parameterIndex, reader);
    }
    
    static void setNClob(final PreparedStatement pstmt, final int parameterIndex, final Reader reader, final long length) throws SQLException {
        if (reader == null) {
            pstmt.setNull(parameterIndex, 2011);
        }
        else {
            pstmt.setNCharacterStream(parameterIndex, reader, length);
        }
    }
    
    static void setSQLXML(final PreparedStatement pstmt, final int parameterIndex, final SQLXML xmlObject) throws SQLException {
        if (xmlObject == null) {
            pstmt.setNull(parameterIndex, 2009);
        }
        else {
            pstmt.setCharacterStream(parameterIndex, ((JDBC4MysqlSQLXML)xmlObject).serializeAsCharacterStream());
        }
    }
}
