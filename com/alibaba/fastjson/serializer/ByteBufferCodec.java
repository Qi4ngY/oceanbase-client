package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class ByteBufferCodec implements ObjectSerializer, ObjectDeserializer
{
    public static final ByteBufferCodec instance;
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        final ByteBufferBean bean = parser.parseObject(ByteBufferBean.class);
        return (T)bean.byteBuffer();
    }
    
    @Override
    public int getFastMatchToken() {
        return 14;
    }
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final ByteBuffer byteBuf = (ByteBuffer)object;
        final byte[] array = byteBuf.array();
        final SerializeWriter out = serializer.out;
        out.write(123);
        out.writeFieldName("array");
        out.writeByteArray(array);
        out.writeFieldValue(',', "limit", byteBuf.limit());
        out.writeFieldValue(',', "position", byteBuf.position());
        out.write(125);
    }
    
    static {
        instance = new ByteBufferCodec();
    }
    
    public static class ByteBufferBean
    {
        public byte[] array;
        public int limit;
        public int position;
        
        public ByteBuffer byteBuffer() {
            final ByteBuffer buf = ByteBuffer.wrap(this.array);
            buf.limit(this.limit);
            buf.position(this.position);
            return buf;
        }
    }
}
