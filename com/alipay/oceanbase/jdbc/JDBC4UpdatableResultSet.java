package com.alipay.oceanbase.jdbc;

import java.io.UnsupportedEncodingException;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLXML;
import java.io.Reader;
import java.io.InputStream;
import java.sql.SQLException;

public class JDBC4UpdatableResultSet extends UpdatableResultSet
{
    public JDBC4UpdatableResultSet(final String catalog, final Field[] fields, final RowData tuples, final MySQLConnection conn, final StatementImpl creatorStmt) throws SQLException {
        super(catalog, fields, tuples, conn, creatorStmt);
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x, final long length) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x, final long length) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateClob(final int columnIndex, final Reader reader) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {
        this.updateNCharacterStream(columnIndex, x, (int)length);
    }
    
    @Override
    public void updateNClob(final int columnIndex, final Reader reader) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateNClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateSQLXML(final int columnIndex, final SQLXML xmlObject) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateRowId(final int columnIndex, final RowId x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream x) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnLabel), x);
    }
    
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream x, final long length) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnLabel), x, length);
    }
    
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream x) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnLabel), x);
    }
    
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream x, final long length) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnLabel), x, length);
    }
    
    @Override
    public void updateBlob(final String columnLabel, final InputStream inputStream) throws SQLException {
        this.updateBlob(this.findColumn(columnLabel), inputStream);
    }
    
    @Override
    public void updateBlob(final String columnLabel, final InputStream inputStream, final long length) throws SQLException {
        this.updateBlob(this.findColumn(columnLabel), inputStream, length);
    }
    
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnLabel), reader);
    }
    
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnLabel), reader, length);
    }
    
    @Override
    public void updateClob(final String columnLabel, final Reader reader) throws SQLException {
        this.updateClob(this.findColumn(columnLabel), reader);
    }
    
    @Override
    public void updateClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
        this.updateClob(this.findColumn(columnLabel), reader, length);
    }
    
    @Override
    public void updateNCharacterStream(final String columnLabel, final Reader reader) throws SQLException {
        this.updateNCharacterStream(this.findColumn(columnLabel), reader);
    }
    
    @Override
    public void updateNCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {
        this.updateNCharacterStream(this.findColumn(columnLabel), reader, length);
    }
    
    @Override
    public void updateNClob(final String columnLabel, final Reader reader) throws SQLException {
        this.updateNClob(this.findColumn(columnLabel), reader);
    }
    
    @Override
    public void updateNClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
        this.updateNClob(this.findColumn(columnLabel), reader, length);
    }
    
    @Override
    public void updateSQLXML(final String columnLabel, final SQLXML xmlObject) throws SQLException {
        this.updateSQLXML(this.findColumn(columnLabel), xmlObject);
    }
    
    public synchronized void updateNCharacterStream(final int columnIndex, final Reader x, final int length) throws SQLException {
        final String fieldEncoding = this.fields[columnIndex - 1].getEncoding();
        if (fieldEncoding == null || !fieldEncoding.equals("UTF-8")) {
            throw new SQLException("Can not call updateNCharacterStream() when field's character set isn't UTF-8");
        }
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            ((JDBC4PreparedStatement)this.updater).setNCharacterStream(columnIndex, x, length);
        }
        else {
            ((JDBC4PreparedStatement)this.inserter).setNCharacterStream(columnIndex, x, length);
            if (x == null) {
                this.thisRow.setColumnValue(columnIndex - 1, null);
            }
            else {
                this.thisRow.setColumnValue(columnIndex - 1, JDBC4UpdatableResultSet.STREAM_DATA_MARKER);
            }
        }
    }
    
    public synchronized void updateNCharacterStream(final String columnName, final Reader reader, final int length) throws SQLException {
        this.updateNCharacterStream(this.findColumn(columnName), reader, length);
    }
    
    @Override
    public void updateNClob(final int columnIndex, final NClob nClob) throws SQLException {
        final String fieldEncoding = this.fields[columnIndex - 1].getEncoding();
        if (fieldEncoding == null || !fieldEncoding.equals("UTF-8")) {
            throw new SQLException("Can not call updateNClob() when field's character set isn't UTF-8");
        }
        if (nClob == null) {
            this.updateNull(columnIndex);
        }
        else {
            this.updateNCharacterStream(columnIndex, nClob.getCharacterStream(), (int)nClob.length());
        }
    }
    
    @Override
    public void updateNClob(final String columnName, final NClob nClob) throws SQLException {
        this.updateNClob(this.findColumn(columnName), nClob);
    }
    
    @Override
    public synchronized void updateNString(final int columnIndex, final String x) throws SQLException {
        final String fieldEncoding = this.fields[columnIndex - 1].getEncoding();
        if (fieldEncoding == null || !fieldEncoding.equals("UTF-8")) {
            throw new SQLException("Can not call updateNString() when field's character set isn't UTF-8");
        }
        if (!this.onInsertRow) {
            if (!this.doingUpdates) {
                this.doingUpdates = true;
                this.syncUpdate();
            }
            ((JDBC4PreparedStatement)this.updater).setNString(columnIndex, x);
        }
        else {
            ((JDBC4PreparedStatement)this.inserter).setNString(columnIndex, x);
            if (x == null) {
                this.thisRow.setColumnValue(columnIndex - 1, null);
            }
            else {
                this.thisRow.setColumnValue(columnIndex - 1, StringUtils.getBytes(x, this.charConverter, fieldEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor()));
            }
        }
    }
    
    @Override
    public synchronized void updateNString(final String columnName, final String x) throws SQLException {
        this.updateNString(this.findColumn(columnName), x);
    }
    
    @Override
    public int getHoldability() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    protected NClob getNativeNClob(final int columnIndex) throws SQLException {
        final String stringVal = this.getStringForNClob(columnIndex);
        if (stringVal == null) {
            return null;
        }
        return this.getNClobFromString(stringVal, columnIndex);
    }
    
    @Override
    public Reader getNCharacterStream(final int columnIndex) throws SQLException {
        final String fieldEncoding = this.fields[columnIndex - 1].getEncoding();
        if (fieldEncoding == null || !fieldEncoding.equals("UTF-8")) {
            throw new SQLException("Can not call getNCharacterStream() when field's charset isn't UTF-8");
        }
        return this.getCharacterStream(columnIndex);
    }
    
    @Override
    public Reader getNCharacterStream(final String columnName) throws SQLException {
        return this.getNCharacterStream(this.findColumn(columnName));
    }
    
    @Override
    public NClob getNClob(final int columnIndex) throws SQLException {
        final String fieldEncoding = this.fields[columnIndex - 1].getEncoding();
        if (fieldEncoding == null || !fieldEncoding.equals("UTF-8")) {
            throw new SQLException("Can not call getNClob() when field's charset isn't UTF-8");
        }
        if (this.isBinaryEncoded) {
            return this.getNativeNClob(columnIndex);
        }
        final String asString = this.getStringForNClob(columnIndex);
        if (asString == null) {
            return null;
        }
        return new JDBC4NClob(asString, this.getExceptionInterceptor());
    }
    
    @Override
    public NClob getNClob(final String columnName) throws SQLException {
        return this.getNClob(this.findColumn(columnName));
    }
    
    private final NClob getNClobFromString(final String stringVal, final int columnIndex) throws SQLException {
        return new JDBC4NClob(stringVal, this.getExceptionInterceptor());
    }
    
    @Override
    public String getNString(final int columnIndex) throws SQLException {
        final String fieldEncoding = this.fields[columnIndex - 1].getEncoding();
        if (fieldEncoding == null || !fieldEncoding.equals("UTF-8")) {
            throw new SQLException("Can not call getNString() when field's charset isn't UTF-8");
        }
        return this.getString(columnIndex);
    }
    
    @Override
    public String getNString(final String columnName) throws SQLException {
        return this.getNString(this.findColumn(columnName));
    }
    
    @Override
    public RowId getRowId(final int columnIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public RowId getRowId(final String columnLabel) throws SQLException {
        return this.getRowId(this.findColumn(columnLabel));
    }
    
    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        return new JDBC4MysqlSQLXML(this, columnIndex, this.getExceptionInterceptor());
    }
    
    @Override
    public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        return this.getSQLXML(this.findColumn(columnLabel));
    }
    
    private String getStringForNClob(final int columnIndex) throws SQLException {
        String asString = null;
        final String forcedEncoding = "UTF-8";
        try {
            byte[] asBytes = null;
            if (!this.isBinaryEncoded) {
                asBytes = this.getBytes(columnIndex);
            }
            else {
                asBytes = this.getNativeBytes(columnIndex, true);
            }
            if (asBytes != null) {
                asString = new String(asBytes, forcedEncoding);
            }
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009", this.getExceptionInterceptor());
        }
        return asString;
    }
    
    @Override
    public synchronized boolean isClosed() throws SQLException {
        return this.isClosed;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        this.checkClosed();
        return iface.isInstance(this);
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        try {
            return iface.cast(this);
        }
        catch (ClassCastException cce) {
            throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.getExceptionInterceptor());
        }
    }
}
