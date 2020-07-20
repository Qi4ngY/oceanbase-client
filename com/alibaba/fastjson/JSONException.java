package com.alibaba.fastjson;

public class JSONException extends RuntimeException
{
    public JSONException() {
    }
    
    public JSONException(final String message) {
        super(message);
    }
    
    public JSONException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
