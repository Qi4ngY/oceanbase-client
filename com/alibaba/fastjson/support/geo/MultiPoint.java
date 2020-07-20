package com.alibaba.fastjson.support.geo;

import com.alibaba.fastjson.annotation.JSONType;

@JSONType(typeName = "MultiPoint", orders = { "type", "bbox", "coordinates" })
public class MultiPoint extends Geometry
{
    private double[][] coordinates;
    
    public MultiPoint() {
        super("MultiPoint");
    }
    
    public double[][] getCoordinates() {
        return this.coordinates;
    }
    
    public void setCoordinates(final double[][] coordinates) {
        this.coordinates = coordinates;
    }
}
