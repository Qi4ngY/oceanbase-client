package com.alibaba.fastjson.serializer;

import java.io.IOException;
import com.alibaba.fastjson.JSONObject;
import java.util.Date;
import java.lang.reflect.Type;

public class JSONLibDataFormatSerializer implements ObjectSerializer
{
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        if (object == null) {
            serializer.out.writeNull();
            return;
        }
        final Date date = (Date)object;
        final JSONObject json = new JSONObject();
        json.put("date", date.getDate());
        json.put("day", date.getDay());
        json.put("hours", date.getHours());
        json.put("minutes", date.getMinutes());
        json.put("month", date.getMonth());
        json.put("seconds", date.getSeconds());
        json.put("time", date.getTime());
        json.put("timezoneOffset", date.getTimezoneOffset());
        json.put("year", date.getYear());
        serializer.write(json);
    }
}
