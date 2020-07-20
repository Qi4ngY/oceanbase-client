package com.alibaba.fastjson.support.spring;

import javax.servlet.http.HttpServletRequest;
import com.alibaba.fastjson.JSONPObject;
import com.alibaba.fastjson.util.IOUtils;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.MediaType;
import com.alibaba.fastjson.support.spring.annotation.ResponseJSONP;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.core.MethodParameter;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Order(Integer.MIN_VALUE)
@ControllerAdvice
public class JSONPResponseBodyAdvice implements ResponseBodyAdvice<Object>
{
    public final Log logger;
    
    public JSONPResponseBodyAdvice() {
        this.logger = LogFactory.getLog((Class)this.getClass());
    }
    
    public boolean supports(final MethodParameter returnType, final Class<? extends HttpMessageConverter<?>> converterType) {
        return FastJsonHttpMessageConverter.class.isAssignableFrom(converterType) && (returnType.getContainingClass().isAnnotationPresent(ResponseJSONP.class) || returnType.hasMethodAnnotation((Class)ResponseJSONP.class));
    }
    
    public Object beforeBodyWrite(final Object body, final MethodParameter returnType, final MediaType selectedContentType, final Class<? extends HttpMessageConverter<?>> selectedConverterType, final ServerHttpRequest request, final ServerHttpResponse response) {
        ResponseJSONP responseJsonp = (ResponseJSONP)returnType.getMethodAnnotation((Class)ResponseJSONP.class);
        if (responseJsonp == null) {
            responseJsonp = returnType.getContainingClass().getAnnotation(ResponseJSONP.class);
        }
        final HttpServletRequest servletRequest = ((ServletServerHttpRequest)request).getServletRequest();
        String callbackMethodName = servletRequest.getParameter(responseJsonp.callback());
        if (!IOUtils.isValidJsonpQueryParam(callbackMethodName)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug((Object)("Invalid jsonp parameter value:" + callbackMethodName));
            }
            callbackMethodName = null;
        }
        final JSONPObject jsonpObject = new JSONPObject(callbackMethodName);
        jsonpObject.addParameter(body);
        this.beforeBodyWriteInternal(jsonpObject, selectedContentType, returnType, request, response);
        return jsonpObject;
    }
    
    public void beforeBodyWriteInternal(final JSONPObject jsonpObject, final MediaType contentType, final MethodParameter returnType, final ServerHttpRequest request, final ServerHttpResponse response) {
    }
    
    protected MediaType getContentType(final MediaType contentType, final ServerHttpRequest request, final ServerHttpResponse response) {
        return FastJsonHttpMessageConverter.APPLICATION_JAVASCRIPT;
    }
}
