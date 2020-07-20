package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public abstract class SerializeFilterable
{
    protected List<BeforeFilter> beforeFilters;
    protected List<AfterFilter> afterFilters;
    protected List<PropertyFilter> propertyFilters;
    protected List<ValueFilter> valueFilters;
    protected List<NameFilter> nameFilters;
    protected List<PropertyPreFilter> propertyPreFilters;
    protected List<LabelFilter> labelFilters;
    protected List<ContextValueFilter> contextValueFilters;
    protected boolean writeDirect;
    
    public SerializeFilterable() {
        this.beforeFilters = null;
        this.afterFilters = null;
        this.propertyFilters = null;
        this.valueFilters = null;
        this.nameFilters = null;
        this.propertyPreFilters = null;
        this.labelFilters = null;
        this.contextValueFilters = null;
        this.writeDirect = true;
    }
    
    public List<BeforeFilter> getBeforeFilters() {
        if (this.beforeFilters == null) {
            this.beforeFilters = new ArrayList<BeforeFilter>();
            this.writeDirect = false;
        }
        return this.beforeFilters;
    }
    
    public List<AfterFilter> getAfterFilters() {
        if (this.afterFilters == null) {
            this.afterFilters = new ArrayList<AfterFilter>();
            this.writeDirect = false;
        }
        return this.afterFilters;
    }
    
    public List<NameFilter> getNameFilters() {
        if (this.nameFilters == null) {
            this.nameFilters = new ArrayList<NameFilter>();
            this.writeDirect = false;
        }
        return this.nameFilters;
    }
    
    public List<PropertyPreFilter> getPropertyPreFilters() {
        if (this.propertyPreFilters == null) {
            this.propertyPreFilters = new ArrayList<PropertyPreFilter>();
            this.writeDirect = false;
        }
        return this.propertyPreFilters;
    }
    
    public List<LabelFilter> getLabelFilters() {
        if (this.labelFilters == null) {
            this.labelFilters = new ArrayList<LabelFilter>();
            this.writeDirect = false;
        }
        return this.labelFilters;
    }
    
    public List<PropertyFilter> getPropertyFilters() {
        if (this.propertyFilters == null) {
            this.propertyFilters = new ArrayList<PropertyFilter>();
            this.writeDirect = false;
        }
        return this.propertyFilters;
    }
    
    public List<ContextValueFilter> getContextValueFilters() {
        if (this.contextValueFilters == null) {
            this.contextValueFilters = new ArrayList<ContextValueFilter>();
            this.writeDirect = false;
        }
        return this.contextValueFilters;
    }
    
    public List<ValueFilter> getValueFilters() {
        if (this.valueFilters == null) {
            this.valueFilters = new ArrayList<ValueFilter>();
            this.writeDirect = false;
        }
        return this.valueFilters;
    }
    
    public void addFilter(final SerializeFilter filter) {
        if (filter == null) {
            return;
        }
        if (filter instanceof PropertyPreFilter) {
            this.getPropertyPreFilters().add((PropertyPreFilter)filter);
        }
        if (filter instanceof NameFilter) {
            this.getNameFilters().add((NameFilter)filter);
        }
        if (filter instanceof ValueFilter) {
            this.getValueFilters().add((ValueFilter)filter);
        }
        if (filter instanceof ContextValueFilter) {
            this.getContextValueFilters().add((ContextValueFilter)filter);
        }
        if (filter instanceof PropertyFilter) {
            this.getPropertyFilters().add((PropertyFilter)filter);
        }
        if (filter instanceof BeforeFilter) {
            this.getBeforeFilters().add((BeforeFilter)filter);
        }
        if (filter instanceof AfterFilter) {
            this.getAfterFilters().add((AfterFilter)filter);
        }
        if (filter instanceof LabelFilter) {
            this.getLabelFilters().add((LabelFilter)filter);
        }
    }
    
    public boolean applyName(final JSONSerializer jsonBeanDeser, final Object object, final String key) {
        if (jsonBeanDeser.propertyPreFilters != null) {
            for (final PropertyPreFilter filter : jsonBeanDeser.propertyPreFilters) {
                if (!filter.apply(jsonBeanDeser, object, key)) {
                    return false;
                }
            }
        }
        if (this.propertyPreFilters != null) {
            for (final PropertyPreFilter filter : this.propertyPreFilters) {
                if (!filter.apply(jsonBeanDeser, object, key)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean apply(final JSONSerializer jsonBeanDeser, final Object object, final String key, final Object propertyValue) {
        if (jsonBeanDeser.propertyFilters != null) {
            for (final PropertyFilter propertyFilter : jsonBeanDeser.propertyFilters) {
                if (!propertyFilter.apply(object, key, propertyValue)) {
                    return false;
                }
            }
        }
        if (this.propertyFilters != null) {
            for (final PropertyFilter propertyFilter : this.propertyFilters) {
                if (!propertyFilter.apply(object, key, propertyValue)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    protected String processKey(final JSONSerializer jsonBeanDeser, final Object object, String key, final Object propertyValue) {
        if (jsonBeanDeser.nameFilters != null) {
            for (final NameFilter nameFilter : jsonBeanDeser.nameFilters) {
                key = nameFilter.process(object, key, propertyValue);
            }
        }
        if (this.nameFilters != null) {
            for (final NameFilter nameFilter : this.nameFilters) {
                key = nameFilter.process(object, key, propertyValue);
            }
        }
        return key;
    }
    
    protected Object processValue(final JSONSerializer jsonBeanDeser, final BeanContext beanContext, final Object object, final String key, final Object propertyValue) {
        return this.processValue(jsonBeanDeser, beanContext, object, key, propertyValue, 0);
    }
    
    protected Object processValue(final JSONSerializer jsonBeanDeser, final BeanContext beanContext, final Object object, final String key, Object propertyValue, final int features) {
        if (propertyValue != null) {
            if ((SerializerFeature.isEnabled(jsonBeanDeser.out.features, features, SerializerFeature.WriteNonStringValueAsString) || (beanContext != null && (beanContext.getFeatures() & SerializerFeature.WriteNonStringValueAsString.mask) != 0x0)) && (propertyValue instanceof Number || propertyValue instanceof Boolean)) {
                String format = null;
                if (propertyValue instanceof Number && beanContext != null) {
                    format = beanContext.getFormat();
                }
                if (format != null) {
                    propertyValue = new DecimalFormat(format).format(propertyValue);
                }
                else {
                    propertyValue = propertyValue.toString();
                }
            }
            else if (beanContext != null && beanContext.isJsonDirect()) {
                final String jsonStr = (String)propertyValue;
                propertyValue = JSON.parse(jsonStr);
            }
        }
        if (jsonBeanDeser.valueFilters != null) {
            for (final ValueFilter valueFilter : jsonBeanDeser.valueFilters) {
                propertyValue = valueFilter.process(object, key, propertyValue);
            }
        }
        final List<ValueFilter> valueFilters = this.valueFilters;
        if (valueFilters != null) {
            for (final ValueFilter valueFilter2 : valueFilters) {
                propertyValue = valueFilter2.process(object, key, propertyValue);
            }
        }
        if (jsonBeanDeser.contextValueFilters != null) {
            for (final ContextValueFilter valueFilter3 : jsonBeanDeser.contextValueFilters) {
                propertyValue = valueFilter3.process(beanContext, object, key, propertyValue);
            }
        }
        if (this.contextValueFilters != null) {
            for (final ContextValueFilter valueFilter3 : this.contextValueFilters) {
                propertyValue = valueFilter3.process(beanContext, object, key, propertyValue);
            }
        }
        return propertyValue;
    }
    
    protected boolean writeDirect(final JSONSerializer jsonBeanDeser) {
        return jsonBeanDeser.out.writeDirect && this.writeDirect && jsonBeanDeser.writeDirect;
    }
}
