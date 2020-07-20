package com.alibaba.fastjson.serializer;

public class SerialContext
{
    public final SerialContext parent;
    public final Object object;
    public final Object fieldName;
    public final int features;
    
    public SerialContext(final SerialContext parent, final Object object, final Object fieldName, final int features, final int fieldFeatures) {
        this.parent = parent;
        this.object = object;
        this.fieldName = fieldName;
        this.features = features;
    }
    
    @Override
    public String toString() {
        if (this.parent == null) {
            return "$";
        }
        final StringBuilder buf = new StringBuilder();
        this.toString(buf);
        return buf.toString();
    }
    
    protected void toString(final StringBuilder buf) {
        if (this.parent == null) {
            buf.append('$');
        }
        else {
            this.parent.toString(buf);
            if (this.fieldName == null) {
                buf.append(".null");
            }
            else if (this.fieldName instanceof Integer) {
                buf.append('[');
                buf.append((int)this.fieldName);
                buf.append(']');
            }
            else {
                buf.append('.');
                final String fieldName = this.fieldName.toString();
                boolean special = false;
                for (int i = 0; i < fieldName.length(); ++i) {
                    final char ch = fieldName.charAt(i);
                    if ((ch < '0' || ch > '9') && (ch < 'A' || ch > 'Z') && (ch < 'a' || ch > 'z') && ch <= '\u0080') {
                        special = true;
                        break;
                    }
                }
                if (special) {
                    for (int i = 0; i < fieldName.length(); ++i) {
                        final char ch = fieldName.charAt(i);
                        if (ch == '\\') {
                            buf.append('\\');
                            buf.append('\\');
                            buf.append('\\');
                        }
                        else {
                            if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || ch > '\u0080') {
                                buf.append(ch);
                                continue;
                            }
                            buf.append('\\');
                            buf.append('\\');
                        }
                        buf.append(ch);
                    }
                }
                else {
                    buf.append(fieldName);
                }
            }
        }
    }
    
    @Deprecated
    public SerialContext getParent() {
        return this.parent;
    }
    
    @Deprecated
    public Object getObject() {
        return this.object;
    }
    
    @Deprecated
    public Object getFieldName() {
        return this.fieldName;
    }
    
    @Deprecated
    public String getPath() {
        return this.toString();
    }
}
