package com.alibaba.fastjson.support.geo;

import com.alibaba.fastjson.annotation.JSONType;

@JSONType(typeName = "Point", orders = { "type", "bbox", "coordinates" })
public class Point extends Geometry
{
    private double[] coordinates;
    
    public Point() {
        super("Point");
    }
    
    public double[] getCoordinates() {
        return this.coordinates;
    }
    
    public void setCoordinates(final double[] coordinates) {
        this.coordinates = coordinates;
    }
}
