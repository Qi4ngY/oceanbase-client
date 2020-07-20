package com.alibaba.fastjson.support.spring;

import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import java.util.ArrayList;
import java.util.List;

public class PropertyPreFilters
{
    private List<MySimplePropertyPreFilter> filters;
    
    public PropertyPreFilters() {
        this.filters = new ArrayList<MySimplePropertyPreFilter>();
    }
    
    public MySimplePropertyPreFilter addFilter() {
        final MySimplePropertyPreFilter filter = new MySimplePropertyPreFilter();
        this.filters.add(filter);
        return filter;
    }
    
    public MySimplePropertyPreFilter addFilter(final String... properties) {
        final MySimplePropertyPreFilter filter = new MySimplePropertyPreFilter(properties);
        this.filters.add(filter);
        return filter;
    }
    
    public MySimplePropertyPreFilter addFilter(final Class<?> clazz, final String... properties) {
        final MySimplePropertyPreFilter filter = new MySimplePropertyPreFilter(clazz, properties);
        this.filters.add(filter);
        return filter;
    }
    
    public List<MySimplePropertyPreFilter> getFilters() {
        return this.filters;
    }
    
    public void setFilters(final List<MySimplePropertyPreFilter> filters) {
        this.filters = filters;
    }
    
    public MySimplePropertyPreFilter[] toFilters() {
        return this.filters.toArray(new MySimplePropertyPreFilter[0]);
    }
    
    public class MySimplePropertyPreFilter extends SimplePropertyPreFilter
    {
        public MySimplePropertyPreFilter() {
            super(new String[0]);
        }
        
        public MySimplePropertyPreFilter(final String... properties) {
            super(properties);
        }
        
        public MySimplePropertyPreFilter(final Class<?> clazz, final String[] properties) {
            super(clazz, properties);
        }
        
        public MySimplePropertyPreFilter addExcludes(final String... filters) {
            for (int i = 0; i < filters.length; ++i) {
                this.getExcludes().add(filters[i]);
            }
            return this;
        }
        
        public MySimplePropertyPreFilter addIncludes(final String... filters) {
            for (int i = 0; i < filters.length; ++i) {
                this.getIncludes().add(filters[i]);
            }
            return this;
        }
    }
}
