package com.alibaba.fastjson.support.geo;

import com.alibaba.fastjson.annotation.JSONType;

@JSONType(typeName = "MultiLineString", orders = { "type", "bbox", "coordinates" })
public class MultiLineString extends Geometry
{
    private double[][][] coordinates;
    
    public MultiLineString() {
        super("MultiLineString");
    }
    
    public double[][][] getCoordinates() {
        return this.coordinates;
    }
    
    public void setCoordinates(final double[][][] coordinates) {
        this.coordinates = coordinates;
    }
}
