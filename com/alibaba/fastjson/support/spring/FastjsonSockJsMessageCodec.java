package com.alibaba.fastjson.support.spring;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.Feature;
import java.io.InputStream;
import java.io.IOException;
import com.alibaba.fastjson.JSON;
import org.springframework.web.socket.sockjs.frame.AbstractSockJsMessageCodec;

public class FastjsonSockJsMessageCodec extends AbstractSockJsMessageCodec
{
    public String[] decode(final String content) throws IOException {
        return JSON.parseObject(content, String[].class);
    }
    
    public String[] decodeInputStream(final InputStream content) throws IOException {
        return JSON.parseObject(content, String[].class, new Feature[0]);
    }
    
    protected char[] applyJsonQuoting(final String content) {
        final SerializeWriter out = new SerializeWriter();
        try {
            final JSONSerializer serializer = new JSONSerializer(out);
            serializer.write(content);
            return out.toCharArrayForSpringWebSocket();
        }
        finally {
            out.close();
        }
    }
}
