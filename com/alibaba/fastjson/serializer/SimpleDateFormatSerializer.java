package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.reflect.Type;

public class SimpleDateFormatSerializer implements ObjectSerializer
{
    private final String pattern;
    
    public SimpleDateFormatSerializer(final String pattern) {
        this.pattern = pattern;
    }
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        if (object == null) {
            serializer.out.writeNull();
            return;
        }
        final Date date = (Date)object;
        final SimpleDateFormat format = new SimpleDateFormat(this.pattern, serializer.locale);
        format.setTimeZone(serializer.timeZone);
        final String text = format.format(date);
        serializer.write(text);
    }
}
