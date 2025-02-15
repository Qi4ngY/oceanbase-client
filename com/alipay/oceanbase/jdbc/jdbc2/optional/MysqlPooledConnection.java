package com.alipay.oceanbase.jdbc.jdbc2.optional;

import java.util.Iterator;
import javax.sql.ConnectionEvent;
import com.alipay.oceanbase.jdbc.SQLError;
import javax.sql.StatementEventListener;
import java.util.HashMap;
import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.Util;
import com.alipay.oceanbase.jdbc.ExceptionInterceptor;
import java.sql.Connection;
import javax.sql.ConnectionEventListener;
import java.util.Map;
import java.lang.reflect.Constructor;
import javax.sql.PooledConnection;

public class MysqlPooledConnection implements PooledConnection
{
    private static final Constructor<?> JDBC_4_POOLED_CONNECTION_WRAPPER_CTOR;
    public static final int CONNECTION_ERROR_EVENT = 1;
    public static final int CONNECTION_CLOSED_EVENT = 2;
    private Map<ConnectionEventListener, ConnectionEventListener> connectionEventListeners;
    private Connection logicalHandle;
    private com.alipay.oceanbase.jdbc.Connection physicalConn;
    private ExceptionInterceptor exceptionInterceptor;
    
    protected static MysqlPooledConnection getInstance(final com.alipay.oceanbase.jdbc.Connection connection) throws SQLException {
        if (!Util.isJdbc4()) {
            return new MysqlPooledConnection(connection);
        }
        return (MysqlPooledConnection)Util.handleNewInstance(MysqlPooledConnection.JDBC_4_POOLED_CONNECTION_WRAPPER_CTOR, new Object[] { connection }, connection.getExceptionInterceptor());
    }
    
    public MysqlPooledConnection(final com.alipay.oceanbase.jdbc.Connection connection) {
        this.logicalHandle = null;
        this.physicalConn = connection;
        this.connectionEventListeners = new HashMap<ConnectionEventListener, ConnectionEventListener>();
        this.exceptionInterceptor = this.physicalConn.getExceptionInterceptor();
    }
    
    @Override
    public synchronized void addConnectionEventListener(final ConnectionEventListener connectioneventlistener) {
        if (this.connectionEventListeners != null) {
            this.connectionEventListeners.put(connectioneventlistener, connectioneventlistener);
        }
    }
    
    @Override
    public synchronized void removeConnectionEventListener(final ConnectionEventListener connectioneventlistener) {
        if (this.connectionEventListeners != null) {
            this.connectionEventListeners.remove(connectioneventlistener);
        }
    }
    
    @Override
    public void addStatementEventListener(final StatementEventListener listener) {
    }
    
    @Override
    public void removeStatementEventListener(final StatementEventListener listener) {
    }
    
    @Override
    public synchronized Connection getConnection() throws SQLException {
        return this.getConnection(true, false);
    }
    
    protected synchronized Connection getConnection(final boolean resetServerState, final boolean forXa) throws SQLException {
        SQLException sqlException = null;
        if (this.physicalConn == null) {
            sqlException = SQLError.createSQLException("Physical Connection doesn't exist", this.exceptionInterceptor);
            this.callConnectionEventListeners(1, sqlException);
            throw sqlException;
        }
        try {
            if (this.logicalHandle != null) {
                ((ConnectionWrapper)this.logicalHandle).close(false);
            }
            if (resetServerState) {
                this.physicalConn.resetServerState();
            }
            this.logicalHandle = ConnectionWrapper.getInstance(this, this.physicalConn, forXa);
        }
        catch (SQLException sqlException) {
            this.callConnectionEventListeners(1, sqlException);
            throw sqlException;
        }
        return this.logicalHandle;
    }
    
    @Override
    public synchronized void close() throws SQLException {
        if (this.physicalConn != null) {
            this.physicalConn.close();
            this.physicalConn = null;
        }
        if (this.connectionEventListeners != null) {
            this.connectionEventListeners.clear();
            this.connectionEventListeners = null;
        }
    }
    
    protected synchronized void callConnectionEventListeners(final int eventType, final SQLException sqlException) {
        if (this.connectionEventListeners == null) {
            return;
        }
        final Iterator<Map.Entry<ConnectionEventListener, ConnectionEventListener>> iterator = this.connectionEventListeners.entrySet().iterator();
        final ConnectionEvent connectionevent = new ConnectionEvent(this, sqlException);
        while (iterator.hasNext()) {
            final ConnectionEventListener connectioneventlistener = iterator.next().getValue();
            if (eventType == 2) {
                connectioneventlistener.connectionClosed(connectionevent);
            }
            else {
                if (eventType != 1) {
                    continue;
                }
                connectioneventlistener.connectionErrorOccurred(connectionevent);
            }
        }
    }
    
    protected ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }
    
    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_POOLED_CONNECTION_WRAPPER_CTOR = Class.forName("com.alipay.oceanbase.jdbc.jdbc2.optional.JDBC4MysqlPooledConnection").getConstructor(com.alipay.oceanbase.jdbc.Connection.class);
                return;
            }
            catch (SecurityException e) {
                throw new RuntimeException(e);
            }
            catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            }
            catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        JDBC_4_POOLED_CONNECTION_WRAPPER_CTOR = null;
    }
}
