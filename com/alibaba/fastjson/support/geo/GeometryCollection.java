package com.alibaba.fastjson.support.geo;

import java.util.ArrayList;
import java.util.List;
import com.alibaba.fastjson.annotation.JSONType;

@JSONType(typeName = "GeometryCollection", orders = { "type", "bbox", "geometries" })
public class GeometryCollection extends Geometry
{
    private List<Geometry> geometries;
    
    public GeometryCollection() {
        super("GeometryCollection");
        this.geometries = new ArrayList<Geometry>();
    }
    
    public List<Geometry> getGeometries() {
        return this.geometries;
    }
}
