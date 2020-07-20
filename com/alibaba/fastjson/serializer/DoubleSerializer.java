package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;

public class DoubleSerializer implements ObjectSerializer
{
    public static final DoubleSerializer instance;
    private DecimalFormat decimalFormat;
    
    public DoubleSerializer() {
        this.decimalFormat = null;
    }
    
    public DoubleSerializer(final DecimalFormat decimalFormat) {
        this.decimalFormat = null;
        this.decimalFormat = decimalFormat;
    }
    
    public DoubleSerializer(final String decimalFormat) {
        this(new DecimalFormat(decimalFormat));
    }
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullNumberAsZero);
            return;
        }
        final double doubleValue = (double)object;
        if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
            out.writeNull();
        }
        else if (this.decimalFormat == null) {
            out.writeDouble(doubleValue, true);
        }
        else {
            final String doubleText = this.decimalFormat.format(doubleValue);
            out.write(doubleText);
        }
    }
    
    static {
        instance = new DoubleSerializer();
    }
}
