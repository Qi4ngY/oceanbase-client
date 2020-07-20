package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class BigDecimalCodec implements ObjectSerializer, ObjectDeserializer
{
    static final BigDecimal LOW;
    static final BigDecimal HIGH;
    public static final BigDecimalCodec instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullNumberAsZero);
        }
        else {
            final BigDecimal val = (BigDecimal)object;
            final int scale = val.scale();
            String outText;
            if (SerializerFeature.isEnabled(features, out.features, SerializerFeature.WriteBigDecimalAsPlain) && scale >= -100 && scale < 100) {
                outText = val.toPlainString();
            }
            else {
                outText = val.toString();
            }
            if (scale == 0 && outText.length() >= 16 && SerializerFeature.isEnabled(features, out.features, SerializerFeature.BrowserCompatible) && (val.compareTo(BigDecimalCodec.LOW) < 0 || val.compareTo(BigDecimalCodec.HIGH) > 0)) {
                out.writeString(outText);
                return;
            }
            out.write(outText);
            if (out.isEnabled(SerializerFeature.WriteClassName) && fieldType != BigDecimal.class && val.scale() == 0) {
                out.write(46);
            }
        }
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type clazz, final Object fieldName) {
        try {
            return deserialze(parser);
        }
        catch (Exception ex) {
            throw new JSONException("parseDecimal error, field : " + fieldName, ex);
        }
    }
    
    public static <T> T deserialze(final DefaultJSONParser parser) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() == 2) {
            final BigDecimal decimalValue = lexer.decimalValue();
            lexer.nextToken(16);
            return (T)decimalValue;
        }
        if (lexer.token() == 3) {
            final BigDecimal val = lexer.decimalValue();
            lexer.nextToken(16);
            return (T)val;
        }
        final Object value = parser.parse();
        return (T)((value == null) ? null : TypeUtils.castToBigDecimal(value));
    }
    
    @Override
    public int getFastMatchToken() {
        return 2;
    }
    
    static {
        LOW = BigDecimal.valueOf(-9007199254740991L);
        HIGH = BigDecimal.valueOf(9007199254740991L);
        instance = new BigDecimalCodec();
    }
}
