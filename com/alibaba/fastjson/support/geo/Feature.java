package com.alibaba.fastjson.support.geo;

import java.util.LinkedHashMap;
import java.util.Map;
import com.alibaba.fastjson.annotation.JSONType;

@JSONType(typeName = "Feature", orders = { "type", "id", "bbox", "coordinates", "properties" })
public class Feature extends Geometry
{
    private String id;
    private Geometry geometry;
    private Map<String, String> properties;
    
    public Feature() {
        super("Feature");
        this.properties = new LinkedHashMap<String, String>();
    }
    
    public Geometry getGeometry() {
        return this.geometry;
    }
    
    public void setGeometry(final Geometry geometry) {
        this.geometry = geometry;
    }
    
    public Map<String, String> getProperties() {
        return this.properties;
    }
    
    public void setProperties(final Map<String, String> properties) {
        this.properties = properties;
    }
    
    public String getId() {
        return this.id;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
}
