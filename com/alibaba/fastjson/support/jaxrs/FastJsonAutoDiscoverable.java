package com.alibaba.fastjson.support.jaxrs;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;
import javax.annotation.Priority;
import org.glassfish.jersey.internal.spi.AutoDiscoverable;

@Priority(1999)
public class FastJsonAutoDiscoverable implements AutoDiscoverable
{
    public static volatile boolean autoDiscover;
    
    public void configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();
        if (!config.isRegistered((Class)FastJsonFeature.class) && FastJsonAutoDiscoverable.autoDiscover) {
            context.register((Class)FastJsonFeature.class);
        }
    }
    
    static {
        FastJsonAutoDiscoverable.autoDiscover = true;
    }
}
