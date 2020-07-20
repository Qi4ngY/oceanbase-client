package com.alibaba.fastjson.serializer;

import java.io.Reader;
import java.sql.SQLException;
import java.io.IOException;
import com.alibaba.fastjson.JSONException;
import java.sql.Clob;
import java.lang.reflect.Type;

public class ClobSeriliazer implements ObjectSerializer
{
    public static final ClobSeriliazer instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        try {
            if (object == null) {
                serializer.writeNull();
                return;
            }
            final Clob clob = (Clob)object;
            final Reader reader = clob.getCharacterStream();
            final StringBuilder buf = new StringBuilder();
            try {
                final char[] chars = new char[2048];
                while (true) {
                    final int len = reader.read(chars, 0, chars.length);
                    if (len < 0) {
                        break;
                    }
                    buf.append(chars, 0, len);
                }
            }
            catch (Exception ex) {
                throw new JSONException("read string from reader error", ex);
            }
            final String text = buf.toString();
            reader.close();
            serializer.write(text);
        }
        catch (SQLException e) {
            throw new IOException("write clob error", e);
        }
    }
    
    static {
        instance = new ClobSeriliazer();
    }
}
