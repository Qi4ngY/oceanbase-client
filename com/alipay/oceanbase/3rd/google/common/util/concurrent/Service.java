package com.alipay.oceanbase.3rd.google.common.util.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import com.alipay.oceanbase.3rd.google.common.annotations.Beta;

@Beta
public interface Service
{
    Service startAsync();
    
    boolean isRunning();
    
    State state();
    
    Service stopAsync();
    
    void awaitRunning();
    
    void awaitRunning(final long p0, final TimeUnit p1) throws TimeoutException;
    
    void awaitTerminated();
    
    void awaitTerminated(final long p0, final TimeUnit p1) throws TimeoutException;
    
    Throwable failureCause();
    
    void addListener(final Listener p0, final Executor p1);
    
    @Beta
    public enum State
    {
        NEW {
            @Override
            boolean isTerminal() {
                return false;
            }
        }, 
        STARTING {
            @Override
            boolean isTerminal() {
                return false;
            }
        }, 
        RUNNING {
            @Override
            boolean isTerminal() {
                return false;
            }
        }, 
        STOPPING {
            @Override
            boolean isTerminal() {
                return false;
            }
        }, 
        TERMINATED {
            @Override
            boolean isTerminal() {
                return true;
            }
        }, 
        FAILED {
            @Override
            boolean isTerminal() {
                return true;
            }
        };
        
        abstract boolean isTerminal();
    }
    
    @Beta
    public abstract static class Listener
    {
        public void starting() {
        }
        
        public void running() {
        }
        
        public void stopping(final State from) {
        }
        
        public void terminated(final State from) {
        }
        
        public void failed(final State from, final Throwable failure) {
        }
    }
}
