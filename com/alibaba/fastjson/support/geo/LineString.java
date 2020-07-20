package com.alibaba.fastjson.support.geo;

import com.alibaba.fastjson.annotation.JSONType;

@JSONType(typeName = "LineString", orders = { "type", "bbox", "coordinates" })
public class LineString extends Geometry
{
    private double[][] coordinates;
    
    public LineString() {
        super("LineString");
    }
    
    public double[][] getCoordinates() {
        return this.coordinates;
    }
    
    public void setCoordinates(final double[][] coordinates) {
        this.coordinates = coordinates;
    }
}
