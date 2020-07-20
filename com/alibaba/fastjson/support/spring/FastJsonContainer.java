package com.alibaba.fastjson.support.spring;

public class FastJsonContainer
{
    private Object value;
    private PropertyPreFilters filters;
    
    FastJsonContainer(final Object body) {
        this.value = body;
    }
    
    public Object getValue() {
        return this.value;
    }
    
    public void setValue(final Object value) {
        this.value = value;
    }
    
    public PropertyPreFilters getFilters() {
        return this.filters;
    }
    
    public void setFilters(final PropertyPreFilters filters) {
        this.filters = filters;
    }
}
