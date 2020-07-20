package com.alibaba.fastjson;

public class JSONPathException extends JSONException
{
    public JSONPathException(final String message) {
        super(message);
    }
    
    public JSONPathException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
