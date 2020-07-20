package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class BigIntegerCodec implements ObjectSerializer, ObjectDeserializer
{
    private static final BigInteger LOW;
    private static final BigInteger HIGH;
    public static final BigIntegerCodec instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullNumberAsZero);
            return;
        }
        final BigInteger val = (BigInteger)object;
        final String str = val.toString();
        if (str.length() >= 16 && SerializerFeature.isEnabled(features, out.features, SerializerFeature.BrowserCompatible) && (val.compareTo(BigIntegerCodec.LOW) < 0 || val.compareTo(BigIntegerCodec.HIGH) > 0)) {
            out.writeString(str);
            return;
        }
        out.write(str);
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type clazz, final Object fieldName) {
        return deserialze(parser);
    }
    
    public static <T> T deserialze(final DefaultJSONParser parser) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() == 2) {
            final String val = lexer.numberString();
            lexer.nextToken(16);
            return (T)new BigInteger(val);
        }
        final Object value = parser.parse();
        return (T)((value == null) ? null : TypeUtils.castToBigInteger(value));
    }
    
    @Override
    public int getFastMatchToken() {
        return 2;
    }
    
    static {
        LOW = BigInteger.valueOf(-9007199254740991L);
        HIGH = BigInteger.valueOf(9007199254740991L);
        instance = new BigIntegerCodec();
    }
}
