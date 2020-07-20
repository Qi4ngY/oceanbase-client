package com.alibaba.fastjson.parser;

import java.lang.reflect.Type;

public class ParseContext
{
    public Object object;
    public final ParseContext parent;
    public final Object fieldName;
    public final int level;
    public Type type;
    private transient String path;
    
    public ParseContext(final ParseContext parent, final Object object, final Object fieldName) {
        this.parent = parent;
        this.object = object;
        this.fieldName = fieldName;
        this.level = ((parent == null) ? 0 : (parent.level + 1));
    }
    
    @Override
    public String toString() {
        if (this.path == null) {
            if (this.parent == null) {
                this.path = "$";
            }
            else if (this.fieldName instanceof Integer) {
                this.path = this.parent.toString() + "[" + this.fieldName + "]";
            }
            else {
                this.path = this.parent.toString() + "." + this.fieldName;
            }
        }
        return this.path;
    }
}
