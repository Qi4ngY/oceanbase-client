package com.alibaba.fastjson.support.jaxrs;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.MessageBodyReader;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.CommonProperties;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Feature;

public class FastJsonFeature implements Feature
{
    private static final String JSON_FEATURE;
    
    public boolean configure(final FeatureContext context) {
        try {
            final Configuration config = context.getConfiguration();
            final String jsonFeature = (String)CommonProperties.getValue(config.getProperties(), config.getRuntimeType(), "jersey.config.jsonFeature", (Object)FastJsonFeature.JSON_FEATURE, (Class)String.class);
            if (!FastJsonFeature.JSON_FEATURE.equalsIgnoreCase(jsonFeature)) {
                return false;
            }
            context.property(PropertiesHelper.getPropertyNameForRuntime("jersey.config.jsonFeature", config.getRuntimeType()), (Object)FastJsonFeature.JSON_FEATURE);
            if (!config.isRegistered((Class)FastJsonProvider.class)) {
                context.register((Class)FastJsonProvider.class, new Class[] { MessageBodyReader.class, MessageBodyWriter.class });
            }
        }
        catch (NoSuchMethodError noSuchMethodError) {}
        return true;
    }
    
    static {
        JSON_FEATURE = FastJsonFeature.class.getSimpleName();
    }
}
