package com.alibaba.fastjson.support.spring;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import java.util.regex.Pattern;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Deprecated
@Order(Integer.MIN_VALUE)
@ControllerAdvice
public class FastJsonpResponseBodyAdvice implements ResponseBodyAdvice<Object>
{
    private static final Pattern CALLBACK_PARAM_PATTERN;
    private final String[] jsonpQueryParamNames;
    public static final String[] DEFAULT_JSONP_QUERY_PARAM_NAMES;
    
    public FastJsonpResponseBodyAdvice() {
        this.jsonpQueryParamNames = FastJsonpResponseBodyAdvice.DEFAULT_JSONP_QUERY_PARAM_NAMES;
    }
    
    public FastJsonpResponseBodyAdvice(final String... queryParamNames) {
        Assert.isTrue(!ObjectUtils.isEmpty((Object[])queryParamNames), "At least one query param name is required");
        this.jsonpQueryParamNames = queryParamNames;
    }
    
    public boolean supports(final MethodParameter returnType, final Class<? extends HttpMessageConverter<?>> converterType) {
        return FastJsonHttpMessageConverter.class.isAssignableFrom(converterType);
    }
    
    public Object beforeBodyWrite(final Object body, final MethodParameter returnType, final MediaType selectedContentType, final Class<? extends HttpMessageConverter<?>> selectedConverterType, final ServerHttpRequest request, final ServerHttpResponse response) {
        final MappingFastJsonValue container = this.getOrCreateContainer(body);
        this.beforeBodyWriteInternal(container, selectedContentType, returnType, request, response);
        return container;
    }
    
    protected MappingFastJsonValue getOrCreateContainer(final Object body) {
        return (MappingFastJsonValue)((body instanceof MappingFastJsonValue) ? body : new MappingFastJsonValue(body));
    }
    
    public void beforeBodyWriteInternal(final MappingFastJsonValue bodyContainer, final MediaType contentType, final MethodParameter returnType, final ServerHttpRequest request, final ServerHttpResponse response) {
        final HttpServletRequest servletRequest = ((ServletServerHttpRequest)request).getServletRequest();
        for (final String name : this.jsonpQueryParamNames) {
            final String value = servletRequest.getParameter(name);
            if (value != null && this.isValidJsonpQueryParam(value)) {
                bodyContainer.setJsonpFunction(value);
                break;
            }
        }
    }
    
    protected boolean isValidJsonpQueryParam(final String value) {
        return FastJsonpResponseBodyAdvice.CALLBACK_PARAM_PATTERN.matcher(value).matches();
    }
    
    protected MediaType getContentType(final MediaType contentType, final ServerHttpRequest request, final ServerHttpResponse response) {
        return new MediaType("application", "javascript");
    }
    
    static {
        CALLBACK_PARAM_PATTERN = Pattern.compile("[0-9A-Za-z_\\.]*");
        DEFAULT_JSONP_QUERY_PARAM_NAMES = new String[] { "callback", "jsonp" };
    }
}
