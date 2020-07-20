package com.alibaba.fastjson.support.geo;

import java.util.ArrayList;
import java.util.List;
import com.alibaba.fastjson.annotation.JSONType;

@JSONType(typeName = "FeatureCollection", orders = { "type", "bbox", "coordinates" })
public class FeatureCollection extends Geometry
{
    private List<Feature> features;
    
    public FeatureCollection() {
        super("FeatureCollection");
        this.features = new ArrayList<Feature>();
    }
    
    public List<Feature> getFeatures() {
        return this.features;
    }
}
