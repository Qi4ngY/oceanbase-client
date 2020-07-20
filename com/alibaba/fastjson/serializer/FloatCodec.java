package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class FloatCodec implements ObjectSerializer, ObjectDeserializer
{
    private NumberFormat decimalFormat;
    public static FloatCodec instance;
    
    public FloatCodec() {
    }
    
    public FloatCodec(final DecimalFormat decimalFormat) {
        this.decimalFormat = decimalFormat;
    }
    
    public FloatCodec(final String decimalFormat) {
        this(new DecimalFormat(decimalFormat));
    }
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullNumberAsZero);
            return;
        }
        final float floatValue = (float)object;
        if (this.decimalFormat != null) {
            final String floatText = this.decimalFormat.format(floatValue);
            out.write(floatText);
        }
        else {
            out.writeFloat(floatValue, true);
        }
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type clazz, final Object fieldName) {
        try {
            return deserialze(parser);
        }
        catch (Exception ex) {
            throw new JSONException("parseLong error, field : " + fieldName, ex);
        }
    }
    
    public static <T> T deserialze(final DefaultJSONParser parser) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() == 2) {
            final String val = lexer.numberString();
            lexer.nextToken(16);
            return (T)Float.valueOf(Float.parseFloat(val));
        }
        if (lexer.token() == 3) {
            final float val2 = lexer.floatValue();
            lexer.nextToken(16);
            return (T)Float.valueOf(val2);
        }
        final Object value = parser.parse();
        if (value == null) {
            return null;
        }
        return (T)TypeUtils.castToFloat(value);
    }
    
    @Override
    public int getFastMatchToken() {
        return 2;
    }
    
    static {
        FloatCodec.instance = new FloatCodec();
    }
}
