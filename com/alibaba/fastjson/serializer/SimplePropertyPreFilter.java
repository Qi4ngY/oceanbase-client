package com.alibaba.fastjson.serializer;

import java.util.HashSet;
import java.util.Set;

public class SimplePropertyPreFilter implements PropertyPreFilter
{
    private final Class<?> clazz;
    private final Set<String> includes;
    private final Set<String> excludes;
    private int maxLevel;
    
    public SimplePropertyPreFilter(final String... properties) {
        this((Class<?>)null, properties);
    }
    
    public SimplePropertyPreFilter(final Class<?> clazz, final String... properties) {
        this.includes = new HashSet<String>();
        this.excludes = new HashSet<String>();
        this.maxLevel = 0;
        this.clazz = clazz;
        for (final String item : properties) {
            if (item != null) {
                this.includes.add(item);
            }
        }
    }
    
    public int getMaxLevel() {
        return this.maxLevel;
    }
    
    public void setMaxLevel(final int maxLevel) {
        this.maxLevel = maxLevel;
    }
    
    public Class<?> getClazz() {
        return this.clazz;
    }
    
    public Set<String> getIncludes() {
        return this.includes;
    }
    
    public Set<String> getExcludes() {
        return this.excludes;
    }
    
    @Override
    public boolean apply(final JSONSerializer serializer, final Object source, final String name) {
        if (source == null) {
            return true;
        }
        if (this.clazz != null && !this.clazz.isInstance(source)) {
            return true;
        }
        if (this.excludes.contains(name)) {
            return false;
        }
        if (this.maxLevel > 0) {
            int level = 0;
            for (SerialContext context = serializer.context; context != null; context = context.parent) {
                if (++level > this.maxLevel) {
                    return false;
                }
            }
        }
        return this.includes.size() == 0 || this.includes.contains(name);
    }
}
