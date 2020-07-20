package com.alibaba.fastjson.serializer;

import javax.xml.datatype.DatatypeConfigurationException;
import com.alibaba.fastjson.parser.JSONLexer;
import java.util.GregorianCalendar;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.util.Date;
import com.alibaba.fastjson.util.IOUtils;
import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Type;
import java.io.IOException;
import java.text.DateFormat;
import com.alibaba.fastjson.JSON;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.xml.datatype.DatatypeFactory;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.parser.deserializer.ContextObjectDeserializer;

public class CalendarCodec extends ContextObjectDeserializer implements ObjectSerializer, ObjectDeserializer, ContextObjectSerializer
{
    public static final CalendarCodec instance;
    private DatatypeFactory dateFactory;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final BeanContext context) throws IOException {
        final SerializeWriter out = serializer.out;
        final String format = context.getFormat();
        final Calendar calendar = (Calendar)object;
        if (format.equals("unixtime")) {
            final long seconds = calendar.getTimeInMillis() / 1000L;
            out.writeInt((int)seconds);
            return;
        }
        DateFormat dateFormat = new SimpleDateFormat(format);
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(JSON.DEFFAULT_DATE_FORMAT, serializer.locale);
        }
        dateFormat.setTimeZone(serializer.timeZone);
        final String text = dateFormat.format(calendar.getTime());
        out.writeString(text);
    }
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
            return;
        }
        Calendar calendar;
        if (object instanceof XMLGregorianCalendar) {
            calendar = ((XMLGregorianCalendar)object).toGregorianCalendar();
        }
        else {
            calendar = (Calendar)object;
        }
        if (out.isEnabled(SerializerFeature.UseISO8601DateFormat)) {
            final char quote = out.isEnabled(SerializerFeature.UseSingleQuotes) ? '\'' : '\"';
            out.append(quote);
            final int year = calendar.get(1);
            final int month = calendar.get(2) + 1;
            final int day = calendar.get(5);
            final int hour = calendar.get(11);
            final int minute = calendar.get(12);
            final int second = calendar.get(13);
            final int millis = calendar.get(14);
            char[] buf;
            if (millis != 0) {
                buf = "0000-00-00T00:00:00.000".toCharArray();
                IOUtils.getChars(millis, 23, buf);
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
            final float timeZoneF = calendar.getTimeZone().getOffset(calendar.getTimeInMillis()) / 3600000.0f;
            final int timeZone = (int)timeZoneF;
            if (timeZone == 0.0) {
                out.write(90);
            }
            else {
                if (timeZone > 9) {
                    out.write(43);
                    out.writeInt(timeZone);
                }
                else if (timeZone > 0) {
                    out.write(43);
                    out.write(48);
                    out.writeInt(timeZone);
                }
                else if (timeZone < -9) {
                    out.write(45);
                    out.writeInt(timeZone);
                }
                else if (timeZone < 0) {
                    out.write(45);
                    out.write(48);
                    out.writeInt(-timeZone);
                }
                out.write(58);
                final int offSet = (int)((timeZoneF - timeZone) * 60.0f);
                out.append(String.format("%02d", offSet));
            }
            out.append(quote);
        }
        else {
            final Date date = calendar.getTime();
            serializer.write(date);
        }
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type clazz, final Object fieldName) {
        return this.deserialze(parser, clazz, fieldName, null, 0);
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName, final String format, final int features) {
        final Object value = DateCodec.instance.deserialze(parser, type, fieldName, format, features);
        if (value instanceof Calendar) {
            return (T)value;
        }
        final Date date = (Date)value;
        if (date == null) {
            return null;
        }
        final JSONLexer lexer = parser.lexer;
        final Calendar calendar = Calendar.getInstance(lexer.getTimeZone(), lexer.getLocale());
        calendar.setTime(date);
        if (type == XMLGregorianCalendar.class) {
            return (T)this.createXMLGregorianCalendar(calendar);
        }
        return (T)calendar;
    }
    
    public XMLGregorianCalendar createXMLGregorianCalendar(final Calendar calendar) {
        if (this.dateFactory == null) {
            try {
                this.dateFactory = DatatypeFactory.newInstance();
            }
            catch (DatatypeConfigurationException e) {
                throw new IllegalStateException("Could not obtain an instance of DatatypeFactory.", e);
            }
        }
        return this.dateFactory.newXMLGregorianCalendar((GregorianCalendar)calendar);
    }
    
    @Override
    public int getFastMatchToken() {
        return 2;
    }
    
    static {
        instance = new CalendarCodec();
    }
}
