package com.alibaba.fastjson.support.geo;

import com.alibaba.fastjson.annotation.JSONType;

@JSONType(seeAlso = { GeometryCollection.class, LineString.class, MultiLineString.class, Point.class, MultiPoint.class, Polygon.class, MultiPolygon.class, Feature.class, FeatureCollection.class }, typeKey = "type")
public abstract class Geometry
{
    private final String type;
    private double[] bbox;
    
    protected Geometry(final String type) {
        this.type = type;
    }
    
    public String getType() {
        return this.type;
    }
    
    public double[] getBbox() {
        return this.bbox;
    }
    
    public void setBbox(final double[] bbox) {
        this.bbox = bbox;
    }
}
