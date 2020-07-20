package com.alibaba.fastjson.serializer;

import java.text.ParseException;
import com.alibaba.fastjson.parser.JSONScanner;
import com.alibaba.fastjson.JSONException;
import java.math.BigDecimal;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.io.IOException;
import java.text.DateFormat;
import java.util.TimeZone;
import com.alibaba.fastjson.util.IOUtils;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.TypeUtils;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Date;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.parser.deserializer.AbstractDateDeserializer;

public class DateCodec extends AbstractDateDeserializer implements ObjectSerializer, ObjectDeserializer
{
    public static final DateCodec instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
            return;
        }
        final Class<?> clazz = object.getClass();
        if (clazz == Date.class) {
            final long millis = ((Date)object).getTime();
            final TimeZone timeZone = serializer.timeZone;
            final int offset = timeZone.getOffset(millis);
            if ((millis + offset) % 86400000L == 0L && !SerializerFeature.isEnabled(out.features, features, SerializerFeature.WriteClassName)) {
                out.writeString(object.toString());
                return;
            }
        }
        if (clazz == Time.class) {
            final long millis = ((Time)object).getTime();
            if ("unixtime".equals(serializer.getDateFormatPattern())) {
                final long seconds = millis / 1000L;
                out.writeLong(seconds);
                return;
            }
            if ("millis".equals(serializer.getDateFormatPattern())) {
                final long seconds = millis;
                out.writeLong(millis);
                return;
            }
            if (millis < 86400000L) {
                out.writeString(object.toString());
                return;
            }
        }
        int nanos = 0;
        if (clazz == Timestamp.class) {
            final Timestamp ts = (Timestamp)object;
            nanos = ts.getNanos();
        }
        java.util.Date date;
        if (object instanceof java.util.Date) {
            date = (java.util.Date)object;
        }
        else {
            date = TypeUtils.castToDate(object);
        }
        if ("unixtime".equals(serializer.getDateFormatPattern())) {
            final long seconds = date.getTime() / 1000L;
            out.writeLong(seconds);
            return;
        }
        if ("millis".equals(serializer.getDateFormatPattern())) {
            final long millis2 = date.getTime();
            out.writeLong(millis2);
            return;
        }
        if (out.isEnabled(SerializerFeature.WriteDateUseDateFormat)) {
            DateFormat format = serializer.getDateFormat();
            if (format == null) {
                format = new SimpleDateFormat(JSON.DEFFAULT_DATE_FORMAT, serializer.locale);
                format.setTimeZone(serializer.timeZone);
            }
            final String text = format.format(date);
            out.writeString(text);
            return;
        }
        if (out.isEnabled(SerializerFeature.WriteClassName) && clazz != fieldType) {
            if (clazz == java.util.Date.class) {
                out.write("new Date(");
                out.writeLong(((java.util.Date)object).getTime());
                out.write(41);
            }
            else {
                out.write(123);
                out.writeFieldName(JSON.DEFAULT_TYPE_KEY);
                serializer.write(clazz.getName());
                out.writeFieldValue(',', "val", ((java.util.Date)object).getTime());
                out.write(125);
            }
            return;
        }
        final long time = date.getTime();
        if (out.isEnabled(SerializerFeature.UseISO8601DateFormat)) {
            final char quote = out.isEnabled(SerializerFeature.UseSingleQuotes) ? '\'' : '\"';
            out.write(quote);
            final Calendar calendar = Calendar.getInstance(serializer.timeZone, serializer.locale);
            calendar.setTimeInMillis(time);
            final int year = calendar.get(1);
            final int month = calendar.get(2) + 1;
            final int day = calendar.get(5);
            final int hour = calendar.get(11);
            final int minute = calendar.get(12);
            final int second = calendar.get(13);
            final int millis3 = calendar.get(14);
            char[] buf;
            if (nanos > 0) {
                buf = "0000-00-00 00:00:00.000000000".toCharArray();
                final int nanoSize = IOUtils.stringSize(nanos);
                IOUtils.getChars(nanos, 29 - (9 - nanoSize), buf);
                IOUtils.getChars(second, 19, buf);
                IOUtils.getChars(minute, 16, buf);
                IOUtils.getChars(hour, 13, buf);
                IOUtils.getChars(day, 10, buf);
                IOUtils.getChars(month, 7, buf);
                IOUtils.getChars(year, 4, buf);
            }
            else if (millis3 != 0) {
                buf = "0000-00-00T00:00:00.000".toCharArray();
                IOUtils.getChars(millis3, 23, buf);
                IOUtils.getChars(second, 19, buf);
                IOUtils.getChars(minute, 16, buf);
                IOUtils.getChars(hour, 13, buf);
                IOUtils.getChars(day, 10, buf);
                IOUtils.getChars(month, 7, buf);
                IOUtils.getChars(year, 4, buf);
            }
            else if (second == 0 && minute == 0 && hour == 0) {
                buf = "0000-00-00".toCharArray();
                IOUtils.getChars(day, 10, buf);
                IOUtils.getChars(month, 7, buf);
                IOUtils.getChars(year, 4, buf);
            }
            else {
                buf = "0000-00-00T00:00:00".toCharArray();
                IOUtils.getChars(second, 19, buf);
                IOUtils.getChars(minute, 16, buf);
                IOUtils.getChars(hour, 13, buf);
                IOUtils.getChars(day, 10, buf);
                IOUtils.getChars(month, 7, buf);
                IOUtils.getChars(year, 4, buf);
            }
            out.write(buf);
            if (nanos > 0) {
                out.write(quote);
                return;
            }
            final float timeZoneF = calendar.getTimeZone().getOffset(calendar.getTimeInMillis()) / 3600000.0f;
            final int timeZone2 = (int)timeZoneF;
            if (timeZone2 == 0.0) {
                out.write(90);
            }
            else {
                if (timeZone2 > 9) {
                    out.write(43);
                    out.writeInt(timeZone2);
                }
                else if (timeZone2 > 0) {
                    out.write(43);
                    out.write(48);
                    out.writeInt(timeZone2);
                }
                else if (timeZone2 < -9) {
                    out.write(45);
                    out.writeInt(-timeZone2);
                }
                else if (timeZone2 < 0) {
                    out.write(45);
                    out.write(48);
                    out.writeInt(-timeZone2);
                }
                out.write(58);
                final int offSet = (int)(Math.abs(timeZoneF - timeZone2) * 60.0f);
                out.append(String.format("%02d", offSet));
            }
            out.write(quote);
        }
        else {
            out.writeLong(time);
        }
    }
    
    public <T> T cast(final DefaultJSONParser parser, final Type clazz, final Object fieldName, final Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof java.util.Date) {
            return (T)val;
        }
        if (val instanceof BigDecimal) {
            return (T)new java.util.Date(TypeUtils.longValue((BigDecimal)val));
        }
        if (val instanceof Number) {
            return (T)new java.util.Date(((Number)val).longValue());
        }
        if (!(val instanceof String)) {
            throw new JSONException("parse error");
        }
        String strVal = (String)val;
        if (strVal.length() == 0) {
            return null;
        }
        final JSONScanner dateLexer = new JSONScanner(strVal);
        try {
            if (dateLexer.scanISO8601DateIfMatch(false)) {
                final Calendar calendar = dateLexer.getCalendar();
                if (clazz == Calendar.class) {
                    return (T)calendar;
                }
                return (T)calendar.getTime();
            }
        }
        finally {
            dateLexer.close();
        }
        if (strVal.length() == parser.getDateFomartPattern().length() || (strVal.length() == 22 && parser.getDateFomartPattern().equals("yyyyMMddHHmmssSSSZ"))) {
            final DateFormat dateFormat = parser.getDateFormat();
            try {
                return (T)dateFormat.parse(strVal);
            }
            catch (ParseException ex) {}
        }
        if (strVal.startsWith("/Date(") && strVal.endsWith(")/")) {
            final String dotnetDateStr = strVal = strVal.substring(6, strVal.length() - 2);
        }
        if ("0000-00-00".equals(strVal) || "0000-00-00T00:00:00".equalsIgnoreCase(strVal) || "0001-01-01T00:00:00+08:00".equalsIgnoreCase(strVal)) {
            return null;
        }
        final int index = strVal.lastIndexOf(124);
        if (index > 20) {
            final String tzStr = strVal.substring(index + 1);
            final TimeZone timeZone = TimeZone.getTimeZone(tzStr);
            if (!"GMT".equals(timeZone.getID())) {
                final String subStr = strVal.substring(0, index);
                final JSONScanner dateLexer2 = new JSONScanner(subStr);
                try {
                    if (dateLexer2.scanISO8601DateIfMatch(false)) {
                        final Calendar calendar2 = dateLexer2.getCalendar();
                        calendar2.setTimeZone(timeZone);
                        if (clazz == Calendar.class) {
                            return (T)calendar2;
                        }
                        return (T)calendar2.getTime();
                    }
                }
                finally {
                    dateLexer2.close();
                }
            }
        }
        final long longVal = Long.parseLong(strVal);
        return (T)new java.util.Date(longVal);
    }
    
    @Override
    public int getFastMatchToken() {
        return 2;
    }
    
    static {
        instance = new DateCodec();
    }
}
