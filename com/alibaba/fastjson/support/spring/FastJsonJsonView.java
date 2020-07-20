package com.alibaba.fastjson.support.spring;

import java.util.Iterator;
import org.springframework.validation.BindingResult;
import org.springframework.util.CollectionUtils;
import java.util.HashMap;
import javax.servlet.ServletOutputStream;
import java.io.OutputStream;
import com.alibaba.fastjson.JSON;
import java.io.ByteArrayOutputStream;
import com.alibaba.fastjson.JSONPObject;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import com.alibaba.fastjson.util.IOUtils;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import org.springframework.util.Assert;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import java.util.Set;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.nio.charset.Charset;
import java.util.regex.Pattern;
import org.springframework.web.servlet.view.AbstractView;

public class FastJsonJsonView extends AbstractView
{
    public static final String DEFAULT_CONTENT_TYPE = "application/json;charset=UTF-8";
    public static final String DEFAULT_JSONP_CONTENT_TYPE = "application/javascript";
    private static final Pattern CALLBACK_PARAM_PATTERN;
    @Deprecated
    protected Charset charset;
    @Deprecated
    protected SerializerFeature[] features;
    @Deprecated
    protected SerializeFilter[] filters;
    @Deprecated
    protected String dateFormat;
    private Set<String> renderedAttributes;
    private boolean disableCaching;
    private boolean updateContentLength;
    private boolean extractValueFromSingleKeyModel;
    private FastJsonConfig fastJsonConfig;
    private String[] jsonpParameterNames;
    
    public FastJsonJsonView() {
        this.charset = Charset.forName("UTF-8");
        this.features = new SerializerFeature[0];
        this.filters = new SerializeFilter[0];
        this.disableCaching = true;
        this.updateContentLength = true;
        this.extractValueFromSingleKeyModel = false;
        this.fastJsonConfig = new FastJsonConfig();
        this.jsonpParameterNames = new String[] { "jsonp", "callback" };
        this.setContentType("application/json;charset=UTF-8");
        this.setExposePathVariables(false);
    }
    
    public FastJsonConfig getFastJsonConfig() {
        return this.fastJsonConfig;
    }
    
    public void setFastJsonConfig(final FastJsonConfig fastJsonConfig) {
        this.fastJsonConfig = fastJsonConfig;
    }
    
    @Deprecated
    public void setSerializerFeature(final SerializerFeature... features) {
        this.fastJsonConfig.setSerializerFeatures(features);
    }
    
    @Deprecated
    public Charset getCharset() {
        return this.fastJsonConfig.getCharset();
    }
    
    @Deprecated
    public void setCharset(final Charset charset) {
        this.fastJsonConfig.setCharset(charset);
    }
    
    @Deprecated
    public String getDateFormat() {
        return this.fastJsonConfig.getDateFormat();
    }
    
    @Deprecated
    public void setDateFormat(final String dateFormat) {
        this.fastJsonConfig.setDateFormat(dateFormat);
    }
    
    @Deprecated
    public SerializerFeature[] getFeatures() {
        return this.fastJsonConfig.getSerializerFeatures();
    }
    
    @Deprecated
    public void setFeatures(final SerializerFeature... features) {
        this.fastJsonConfig.setSerializerFeatures(features);
    }
    
    @Deprecated
    public SerializeFilter[] getFilters() {
        return this.fastJsonConfig.getSerializeFilters();
    }
    
    @Deprecated
    public void setFilters(final SerializeFilter... filters) {
        this.fastJsonConfig.setSerializeFilters(filters);
    }
    
    public void setRenderedAttributes(final Set<String> renderedAttributes) {
        this.renderedAttributes = renderedAttributes;
    }
    
    public boolean isExtractValueFromSingleKeyModel() {
        return this.extractValueFromSingleKeyModel;
    }
    
    public void setExtractValueFromSingleKeyModel(final boolean extractValueFromSingleKeyModel) {
        this.extractValueFromSingleKeyModel = extractValueFromSingleKeyModel;
    }
    
    public void setJsonpParameterNames(final Set<String> jsonpParameterNames) {
        Assert.notEmpty((Collection)jsonpParameterNames, "jsonpParameterName cannot be empty");
        this.jsonpParameterNames = jsonpParameterNames.toArray(new String[jsonpParameterNames.size()]);
    }
    
    private String getJsonpParameterValue(final HttpServletRequest request) {
        if (this.jsonpParameterNames != null) {
            for (final String name : this.jsonpParameterNames) {
                final String value = request.getParameter(name);
                if (IOUtils.isValidJsonpQueryParam(value)) {
                    return value;
                }
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug((Object)("Ignoring invalid jsonp parameter value: " + value));
                }
            }
        }
        return null;
    }
    
    protected void renderMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        Object value = this.filterModel(model);
        final String jsonpParameterValue = this.getJsonpParameterValue(request);
        if (jsonpParameterValue != null) {
            final JSONPObject jsonpObject = new JSONPObject(jsonpParameterValue);
            jsonpObject.addParameter(value);
            value = jsonpObject;
        }
        final ByteArrayOutputStream outnew = new ByteArrayOutputStream();
        final int len = JSON.writeJSONString(outnew, this.fastJsonConfig.getCharset(), value, this.fastJsonConfig.getSerializeConfig(), this.fastJsonConfig.getSerializeFilters(), this.fastJsonConfig.getDateFormat(), JSON.DEFAULT_GENERATE_FEATURE, this.fastJsonConfig.getSerializerFeatures());
        if (this.updateContentLength) {
            response.setContentLength(len);
        }
        final ServletOutputStream out = response.getOutputStream();
        outnew.writeTo((OutputStream)out);
        outnew.close();
        out.flush();
    }
    
    protected void prepareResponse(final HttpServletRequest request, final HttpServletResponse response) {
        this.setResponseContentType(request, response);
        response.setCharacterEncoding(this.fastJsonConfig.getCharset().name());
        if (this.disableCaching) {
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Cache-Control", "no-cache, no-store, max-age=0");
            response.addDateHeader("Expires", 1L);
        }
    }
    
    public void setDisableCaching(final boolean disableCaching) {
        this.disableCaching = disableCaching;
    }
    
    public void setUpdateContentLength(final boolean updateContentLength) {
        this.updateContentLength = updateContentLength;
    }
    
    protected Object filterModel(final Map<String, Object> model) {
        final Map<String, Object> result = new HashMap<String, Object>(model.size());
        final Set<String> renderedAttributes = CollectionUtils.isEmpty((Collection)this.renderedAttributes) ? model.keySet() : this.renderedAttributes;
        for (final Map.Entry<String, Object> entry : model.entrySet()) {
            if (!(entry.getValue() instanceof BindingResult) && renderedAttributes.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        if (this.extractValueFromSingleKeyModel && result.size() == 1) {
            final Iterator<Map.Entry<String, Object>> iterator2 = result.entrySet().iterator();
            if (iterator2.hasNext()) {
                final Map.Entry<String, Object> entry = iterator2.next();
                return entry.getValue();
            }
        }
        return result;
    }
    
    protected void setResponseContentType(final HttpServletRequest request, final HttpServletResponse response) {
        if (this.getJsonpParameterValue(request) != null) {
            response.setContentType("application/javascript");
        }
        else {
            super.setResponseContentType(request, response);
        }
    }
    
    static {
        CALLBACK_PARAM_PATTERN = Pattern.compile("[0-9A-Za-z_\\.]*");
    }
}
