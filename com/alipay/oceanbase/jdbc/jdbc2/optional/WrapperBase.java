package com.alipay.oceanbase.jdbc.jdbc2.optional;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.ExceptionInterceptor;
import java.util.Map;

abstract class WrapperBase
{
    protected MysqlPooledConnection pooledConnection;
    protected Map<Class<?>, Object> unwrappedInterfaces;
    protected ExceptionInterceptor exceptionInterceptor;
    
    protected void checkAndFireConnectionError(final SQLException sqlEx) throws SQLException {
        if (this.pooledConnection != null && "08S01".equals(sqlEx.getSQLState())) {
            this.pooledConnection.callConnectionEventListeners(1, sqlEx);
        }
        throw sqlEx;
    }
    
    protected WrapperBase(final MysqlPooledConnection pooledConnection) {
        this.unwrappedInterfaces = null;
        this.pooledConnection = pooledConnection;
        this.exceptionInterceptor = this.pooledConnection.getExceptionInterceptor();
    }
    
    protected class ConnectionErrorFiringInvocationHandler implements InvocationHandler
    {
        Object invokeOn;
        
        public ConnectionErrorFiringInvocationHandler(final Object toInvokeOn) {
            this.invokeOn = null;
            this.invokeOn = toInvokeOn;
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            Object result = null;
            try {
                result = method.invoke(this.invokeOn, args);
                if (result != null) {
                    result = this.proxyIfInterfaceIsJdbc(result, result.getClass());
                }
            }
            catch (InvocationTargetException e) {
                if (!(e.getTargetException() instanceof SQLException)) {
                    throw e;
                }
                WrapperBase.this.checkAndFireConnectionError((SQLException)e.getTargetException());
            }
            return result;
        }
        
        private Object proxyIfInterfaceIsJdbc(final Object toProxy, final Class<?> clazz) {
            final Class<?>[] interfaces2;
            final Class<?>[] interfaces = interfaces2 = clazz.getInterfaces();
            final int length = interfaces2.length;
            final int n = 0;
            if (n >= length) {
                return toProxy;
            }
            final Class<?> iclass = interfaces2[n];
            final String packageName = iclass.getPackage().getName();
            if ("java.sql".equals(packageName) || "javax.sql".equals(packageName)) {
                return Proxy.newProxyInstance(toProxy.getClass().getClassLoader(), interfaces, new ConnectionErrorFiringInvocationHandler(toProxy));
            }
            return this.proxyIfInterfaceIsJdbc(toProxy, iclass);
        }
    }
}
