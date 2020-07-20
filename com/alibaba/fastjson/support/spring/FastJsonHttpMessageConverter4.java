package com.alibaba.fastjson.support.spring;

import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.io.IOException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import java.lang.reflect.Type;

@Deprecated
public class FastJsonHttpMessageConverter4 extends FastJsonHttpMessageConverter
{
    @Override
    protected boolean supports(final Class<?> clazz) {
        return super.supports(clazz);
    }
    
    @Override
    public boolean canRead(final Type type, final Class<?> contextClass, final MediaType mediaType) {
        return super.canRead(type, contextClass, mediaType);
    }
    
    @Override
    public boolean canWrite(final Type type, final Class<?> clazz, final MediaType mediaType) {
        return super.canWrite(type, clazz, mediaType);
    }
    
    @Override
    public Object read(final Type type, final Class<?> contextClass, final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return super.read(type, contextClass, inputMessage);
    }
    
    @Override
    public void write(final Object o, final Type type, final MediaType contentType, final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        super.write(o, type, contentType, outputMessage);
    }
    
    @Override
    protected Object readInternal(final Class<?> clazz, final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return super.readInternal(clazz, inputMessage);
    }
    
    @Override
    protected void writeInternal(final Object object, final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        super.writeInternal(object, outputMessage);
    }
}
