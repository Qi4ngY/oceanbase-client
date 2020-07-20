package com.alibaba.fastjson.support.geo;

import com.alibaba.fastjson.annotation.JSONType;

@JSONType(typeName = "MultiPolygon", orders = { "type", "bbox", "coordinates" })
public class MultiPolygon extends Geometry
{
    private double[][][][] coordinates;
    
    public MultiPolygon() {
        super("MultiPolygon");
    }
    
    public double[][][][] getCoordinates() {
        return this.coordinates;
    }
    
    public void setCoordinates(final double[][][][] coordinates) {
        this.coordinates = coordinates;
    }
}
