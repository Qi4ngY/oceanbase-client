package com.alipay.oceanbase.jdbc;

import java.io.Writer;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Reader;
import java.sql.SQLException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Clob implements java.sql.Clob, OutputStreamWatcher, WriterWatcher
{
    private String charData;
    private ExceptionInterceptor exceptionInterceptor;
    
    Clob(final ExceptionInterceptor exceptionInterceptor) {
        this.charData = "";
        this.exceptionInterceptor = exceptionInterceptor;
    }
    
    Clob(final String charDataInit, final ExceptionInterceptor exceptionInterceptor) {
        this.charData = charDataInit;
        this.exceptionInterceptor = exceptionInterceptor;
    }
    
    @Override
    public InputStream getAsciiStream() throws SQLException {
        if (this.charData != null) {
            return new ByteArrayInputStream(StringUtils.getBytes(this.charData));
        }
        return null;
    }
    
    @Override
    public Reader getCharacterStream() throws SQLException {
        if (this.charData != null) {
            return new StringReader(this.charData);
        }
        return null;
    }
    
    @Override
    public String getSubString(final long startPos, final int length) throws SQLException {
        if (startPos < 1L) {
            throw SQLError.createSQLException(Messages.getString("Clob.6"), "S1009", this.exceptionInterceptor);
        }
        final int adjustedStartPos = (int)startPos - 1;
        final int adjustedEndIndex = adjustedStartPos + length;
        if (this.charData == null) {
            return null;
        }
        if (adjustedEndIndex > this.charData.length()) {
            throw SQLError.createSQLException(Messages.getString("Clob.7"), "S1009", this.exceptionInterceptor);
        }
        return this.charData.substring(adjustedStartPos, adjustedEndIndex);
    }
    
    @Override
    public long length() throws SQLException {
        if (this.charData != null) {
            return this.charData.length();
        }
        return 0L;
    }
    
    @Override
    public long position(final java.sql.Clob arg0, final long arg1) throws SQLException {
        return this.position(arg0.getSubString(1L, (int)arg0.length()), arg1);
    }
    
    @Override
    public long position(final String stringToFind, final long startPos) throws SQLException {
        if (startPos < 1L) {
            throw SQLError.createSQLException(Messages.getString("Clob.8") + startPos + Messages.getString("Clob.9"), "S1009", this.exceptionInterceptor);
        }
        if (this.charData == null) {
            return -1L;
        }
        if (startPos - 1L > this.charData.length()) {
            throw SQLError.createSQLException(Messages.getString("Clob.10"), "S1009", this.exceptionInterceptor);
        }
        final int pos = this.charData.indexOf(stringToFind, (int)(startPos - 1L));
        return (pos == -1) ? -1L : (pos + 1);
    }
    
    @Override
    public OutputStream setAsciiStream(final long indexToWriteAt) throws SQLException {
        if (indexToWriteAt < 1L) {
            throw SQLError.createSQLException(Messages.getString("Clob.0"), "S1009", this.exceptionInterceptor);
        }
        final WatchableOutputStream bytesOut = new WatchableOutputStream();
        bytesOut.setWatcher(this);
        if (indexToWriteAt > 0L) {
            bytesOut.write(StringUtils.getBytes(this.charData), 0, (int)(indexToWriteAt - 1L));
        }
        return bytesOut;
    }
    
    @Override
    public Writer setCharacterStream(final long indexToWriteAt) throws SQLException {
        if (indexToWriteAt < 1L) {
            throw SQLError.createSQLException(Messages.getString("Clob.1"), "S1009", this.exceptionInterceptor);
        }
        final WatchableWriter writer = new WatchableWriter();
        writer.setWatcher(this);
        if (indexToWriteAt > 1L) {
            writer.write(this.charData, 0, (int)(indexToWriteAt - 1L));
        }
        return writer;
    }
    
    @Override
    public int setString(long pos, final String str) throws SQLException {
        if (pos < 1L) {
            throw SQLError.createSQLException(Messages.getString("Clob.2"), "S1009", this.exceptionInterceptor);
        }
        if (str == null) {
            throw SQLError.createSQLException(Messages.getString("Clob.3"), "S1009", this.exceptionInterceptor);
        }
        final StringBuilder charBuf = new StringBuilder(this.charData);
        --pos;
        final int strLength = str.length();
        charBuf.replace((int)pos, (int)(pos + strLength), str);
        this.charData = charBuf.toString();
        return strLength;
    }
    
    @Override
    public int setString(long pos, final String str, final int offset, final int len) throws SQLException {
        if (pos < 1L) {
            throw SQLError.createSQLException(Messages.getString("Clob.4"), "S1009", this.exceptionInterceptor);
        }
        if (str == null) {
            throw SQLError.createSQLException(Messages.getString("Clob.5"), "S1009", this.exceptionInterceptor);
        }
        final StringBuilder charBuf = new StringBuilder(this.charData);
        --pos;
        try {
            final String replaceString = str.substring(offset, offset + len);
            charBuf.replace((int)pos, (int)(pos + replaceString.length()), replaceString);
        }
        catch (StringIndexOutOfBoundsException e) {
            throw SQLError.createSQLException(e.getMessage(), "S1009", e, this.exceptionInterceptor);
        }
        this.charData = charBuf.toString();
        return len;
    }
    
    @Override
    public void streamClosed(final WatchableOutputStream out) {
        final int streamSize = out.size();
        if (streamSize < this.charData.length()) {
            try {
                out.write(StringUtils.getBytes(this.charData, null, null, false, null, this.exceptionInterceptor), streamSize, this.charData.length() - streamSize);
            }
            catch (SQLException ex) {}
        }
        this.charData = StringUtils.toAsciiString(out.toByteArray());
    }
    
    @Override
    public void truncate(final long length) throws SQLException {
        if (length > this.charData.length()) {
            throw SQLError.createSQLException(Messages.getString("Clob.11") + this.charData.length() + Messages.getString("Clob.12") + length + Messages.getString("Clob.13"), this.exceptionInterceptor);
        }
        this.charData = this.charData.substring(0, (int)length);
    }
    
    public void writerClosed(final char[] charDataBeingWritten) {
        this.charData = new String(charDataBeingWritten);
    }
    
    @Override
    public void writerClosed(final WatchableWriter out) {
        final int dataLength = out.size();
        if (dataLength < this.charData.length()) {
            out.write(this.charData, dataLength, this.charData.length() - dataLength);
        }
        this.charData = out.toString();
    }
    
    @Override
    public void free() throws SQLException {
        this.charData = null;
    }
    
    @Override
    public Reader getCharacterStream(final long pos, final long length) throws SQLException {
        return new StringReader(this.getSubString(pos, (int)length));
    }
}
