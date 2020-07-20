package com.alibaba.fastjson.support.spring;

import com.alibaba.fastjson.support.spring.annotation.FastJsonFilter;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.MediaType;
import com.alibaba.fastjson.support.spring.annotation.FastJsonView;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Order
@ControllerAdvice
public class FastJsonViewResponseBodyAdvice implements ResponseBodyAdvice<Object>
{
    public boolean supports(final MethodParameter returnType, final Class<? extends HttpMessageConverter<?>> converterType) {
        return FastJsonHttpMessageConverter.class.isAssignableFrom(converterType) && returnType.hasMethodAnnotation((Class)FastJsonView.class);
    }
    
    public FastJsonContainer beforeBodyWrite(final Object body, final MethodParameter returnType, final MediaType selectedContentType, final Class<? extends HttpMessageConverter<?>> selectedConverterType, final ServerHttpRequest request, final ServerHttpResponse response) {
        final FastJsonContainer container = this.getOrCreateContainer(body);
        this.beforeBodyWriteInternal(container, selectedContentType, returnType, request, response);
        return container;
    }
    
    private FastJsonContainer getOrCreateContainer(final Object body) {
        return (FastJsonContainer)((body instanceof FastJsonContainer) ? body : new FastJsonContainer(body));
    }
    
    protected void beforeBodyWriteInternal(final FastJsonContainer container, final MediaType contentType, final MethodParameter returnType, final ServerHttpRequest request, final ServerHttpResponse response) {
        final FastJsonView annotation = (FastJsonView)returnType.getMethodAnnotation((Class)FastJsonView.class);
        final FastJsonFilter[] include = annotation.include();
        final FastJsonFilter[] exclude = annotation.exclude();
        final PropertyPreFilters filters = new PropertyPreFilters();
        for (final FastJsonFilter item : include) {
            filters.addFilter(item.clazz(), item.props());
        }
        for (final FastJsonFilter item : exclude) {
            filters.addFilter(item.clazz(), new String[0]).addExcludes(item.props());
        }
        container.setFilters(filters);
    }
}
