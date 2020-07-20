package com.alibaba.fastjson.serializer;

import java.io.IOException;
import org.joda.time.ReadablePartial;
import java.util.Locale;
import com.alibaba.fastjson.parser.JSONLexer;
import java.util.TimeZone;
import com.alibaba.fastjson.JSON;
import org.joda.time.Instant;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.DateTimeZone;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import org.joda.time.format.DateTimeFormatter;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class JodaCodec implements ObjectSerializer, ContextObjectSerializer, ObjectDeserializer
{
    public static final JodaCodec instance;
    private static final String defaultPatttern = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter defaultFormatter;
    private static final DateTimeFormatter defaultFormatter_23;
    private static final DateTimeFormatter formatter_dt19_tw;
    private static final DateTimeFormatter formatter_dt19_cn;
    private static final DateTimeFormatter formatter_dt19_cn_1;
    private static final DateTimeFormatter formatter_dt19_kr;
    private static final DateTimeFormatter formatter_dt19_us;
    private static final DateTimeFormatter formatter_dt19_eur;
    private static final DateTimeFormatter formatter_dt19_de;
    private static final DateTimeFormatter formatter_dt19_in;
    private static final DateTimeFormatter formatter_d8;
    private static final DateTimeFormatter formatter_d10_tw;
    private static final DateTimeFormatter formatter_d10_cn;
    private static final DateTimeFormatter formatter_d10_kr;
    private static final DateTimeFormatter formatter_d10_us;
    private static final DateTimeFormatter formatter_d10_eur;
    private static final DateTimeFormatter formatter_d10_de;
    private static final DateTimeFormatter formatter_d10_in;
    private static final DateTimeFormatter ISO_FIXED_FORMAT;
    private static final String formatter_iso8601_pattern = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String formatter_iso8601_pattern_23 = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final String formatter_iso8601_pattern_29 = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS";
    private static final DateTimeFormatter formatter_iso8601;
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        return this.deserialze(parser, type, fieldName, null, 0);
    }
    
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName, final String format, final int feature) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() == 8) {
            lexer.nextToken();
            return null;
        }
        if (lexer.token() == 4) {
            final String text = lexer.stringVal();
            lexer.nextToken();
            DateTimeFormatter formatter = null;
            if (format != null) {
                if ("yyyy-MM-dd HH:mm:ss".equals(format)) {
                    formatter = JodaCodec.defaultFormatter;
                }
                else {
                    formatter = DateTimeFormat.forPattern(format);
                }
            }
            if ("".equals(text)) {
                return null;
            }
            if (type == LocalDateTime.class) {
                LocalDateTime localDateTime;
                if (text.length() == 10 || text.length() == 8) {
                    final LocalDate localDate = this.parseLocalDate(text, format, formatter);
                    localDateTime = localDate.toLocalDateTime(LocalTime.MIDNIGHT);
                }
                else {
                    localDateTime = this.parseDateTime(text, formatter);
                }
                return (T)localDateTime;
            }
            if (type == LocalDate.class) {
                LocalDate localDate2;
                if (text.length() == 23) {
                    final LocalDateTime localDateTime2 = LocalDateTime.parse(text);
                    localDate2 = localDateTime2.toLocalDate();
                }
                else {
                    localDate2 = this.parseLocalDate(text, format, formatter);
                }
                return (T)localDate2;
            }
            if (type == LocalTime.class) {
                LocalTime localDate3;
                if (text.length() == 23) {
                    final LocalDateTime localDateTime2 = LocalDateTime.parse(text);
                    localDate3 = localDateTime2.toLocalTime();
                }
                else {
                    localDate3 = LocalTime.parse(text);
                }
                return (T)localDate3;
            }
            if (type == DateTime.class) {
                if (formatter == JodaCodec.defaultFormatter) {
                    formatter = JodaCodec.ISO_FIXED_FORMAT;
                }
                final DateTime zonedDateTime = this.parseZonedDateTime(text, formatter);
                return (T)zonedDateTime;
            }
            if (type == DateTimeZone.class) {
                final DateTimeZone offsetTime = DateTimeZone.forID(text);
                return (T)offsetTime;
            }
            if (type == Period.class) {
                final Period period = Period.parse(text);
                return (T)period;
            }
            if (type == Duration.class) {
                final Duration duration = Duration.parse(text);
                return (T)duration;
            }
            if (type == Instant.class) {
                boolean digit = true;
                for (int i = 0; i < text.length(); ++i) {
                    final char ch = text.charAt(i);
                    if (ch < '0' || ch > '9') {
                        digit = false;
                        break;
                    }
                }
                if (digit && text.length() > 8 && text.length() < 19) {
                    final long epochMillis = Long.parseLong(text);
                    return (T)new Instant(epochMillis);
                }
                final Instant instant = Instant.parse(text);
                return (T)instant;
            }
            else {
                if (type == DateTimeFormatter.class) {
                    return (T)DateTimeFormat.forPattern(text);
                }
                return null;
            }
        }
        else {
            if (lexer.token() != 2) {
                throw new UnsupportedOperationException();
            }
            final long millis = lexer.longValue();
            lexer.nextToken();
            TimeZone timeZone = JSON.defaultTimeZone;
            if (timeZone == null) {
                timeZone = TimeZone.getDefault();
            }
            if (type == DateTime.class) {
                return (T)new DateTime(millis, DateTimeZone.forTimeZone(timeZone));
            }
            final LocalDateTime localDateTime2 = new LocalDateTime(millis, DateTimeZone.forTimeZone(timeZone));
            if (type == LocalDateTime.class) {
                return (T)localDateTime2;
            }
            if (type == LocalDate.class) {
                return (T)localDateTime2.toLocalDate();
            }
            if (type == LocalTime.class) {
                return (T)localDateTime2.toLocalTime();
            }
            if (type == Instant.class) {
                final Instant instant2 = new Instant(millis);
                return (T)instant2;
            }
            throw new UnsupportedOperationException();
        }
    }
    
    protected LocalDateTime parseDateTime(final String text, DateTimeFormatter formatter) {
        if (formatter == null) {
            if (text.length() == 19) {
                final char c4 = text.charAt(4);
                final char c5 = text.charAt(7);
                final char c6 = text.charAt(10);
                final char c7 = text.charAt(13);
                final char c8 = text.charAt(16);
                if (c7 == ':' && c8 == ':') {
                    if (c4 == '-' && c5 == '-') {
                        if (c6 == 'T') {
                            formatter = JodaCodec.formatter_iso8601;
                        }
                        else if (c6 == ' ') {
                            formatter = JodaCodec.defaultFormatter;
                        }
                    }
                    else if (c4 == '/' && c5 == '/') {
                        formatter = JodaCodec.formatter_dt19_tw;
                    }
                    else {
                        final char c9 = text.charAt(0);
                        final char c10 = text.charAt(1);
                        final char c11 = text.charAt(2);
                        final char c12 = text.charAt(3);
                        final char c13 = text.charAt(5);
                        if (c11 == '/' && c13 == '/') {
                            final int v0 = (c9 - '0') * 10 + (c10 - '0');
                            final int v2 = (c12 - '0') * 10 + (c4 - '0');
                            if (v0 > 12) {
                                formatter = JodaCodec.formatter_dt19_eur;
                            }
                            else if (v2 > 12) {
                                formatter = JodaCodec.formatter_dt19_us;
                            }
                            else {
                                final String country = Locale.getDefault().getCountry();
                                if (country.equals("US")) {
                                    formatter = JodaCodec.formatter_dt19_us;
                                }
                                else if (country.equals("BR") || country.equals("AU")) {
                                    formatter = JodaCodec.formatter_dt19_eur;
                                }
                            }
                        }
                        else if (c11 == '.' && c13 == '.') {
                            formatter = JodaCodec.formatter_dt19_de;
                        }
                        else if (c11 == '-' && c13 == '-') {
                            formatter = JodaCodec.formatter_dt19_in;
                        }
                    }
                }
            }
            else if (text.length() == 23) {
                final char c4 = text.charAt(4);
                final char c5 = text.charAt(7);
                final char c6 = text.charAt(10);
                final char c7 = text.charAt(13);
                final char c8 = text.charAt(16);
                final char c14 = text.charAt(19);
                if (c7 == ':' && c8 == ':' && c4 == '-' && c5 == '-' && c6 == ' ' && c14 == '.') {
                    formatter = JodaCodec.defaultFormatter_23;
                }
            }
            if (text.length() >= 17) {
                final char c4 = text.charAt(4);
                if (c4 == '\u5e74') {
                    if (text.charAt(text.length() - 1) == '\u79d2') {
                        formatter = JodaCodec.formatter_dt19_cn_1;
                    }
                    else {
                        formatter = JodaCodec.formatter_dt19_cn;
                    }
                }
                else if (c4 == '\ub144') {
                    formatter = JodaCodec.formatter_dt19_kr;
                }
            }
            boolean digit = true;
            for (int i = 0; i < text.length(); ++i) {
                final char ch = text.charAt(i);
                if (ch < '0' || ch > '9') {
                    digit = false;
                    break;
                }
            }
            if (digit && text.length() > 8 && text.length() < 19) {
                final long epochMillis = Long.parseLong(text);
                return new LocalDateTime(epochMillis, DateTimeZone.forTimeZone(JSON.defaultTimeZone));
            }
        }
        return (formatter == null) ? LocalDateTime.parse(text) : LocalDateTime.parse(text, formatter);
    }
    
    protected LocalDate parseLocalDate(final String text, final String format, DateTimeFormatter formatter) {
        if (formatter == null) {
            if (text.length() == 8) {
                formatter = JodaCodec.formatter_d8;
            }
            if (text.length() == 10) {
                final char c4 = text.charAt(4);
                final char c5 = text.charAt(7);
                if (c4 == '/' && c5 == '/') {
                    formatter = JodaCodec.formatter_d10_tw;
                }
                final char c6 = text.charAt(0);
                final char c7 = text.charAt(1);
                final char c8 = text.charAt(2);
                final char c9 = text.charAt(3);
                final char c10 = text.charAt(5);
                if (c8 == '/' && c10 == '/') {
                    final int v0 = (c6 - '0') * 10 + (c7 - '0');
                    final int v2 = (c9 - '0') * 10 + (c4 - '0');
                    if (v0 > 12) {
                        formatter = JodaCodec.formatter_d10_eur;
                    }
                    else if (v2 > 12) {
                        formatter = JodaCodec.formatter_d10_us;
                    }
                    else {
                        final String country = Locale.getDefault().getCountry();
                        if (country.equals("US")) {
                            formatter = JodaCodec.formatter_d10_us;
                        }
                        else if (country.equals("BR") || country.equals("AU")) {
                            formatter = JodaCodec.formatter_d10_eur;
                        }
                    }
                }
                else if (c8 == '.' && c10 == '.') {
                    formatter = JodaCodec.formatter_d10_de;
                }
                else if (c8 == '-' && c10 == '-') {
                    formatter = JodaCodec.formatter_d10_in;
                }
            }
            if (text.length() >= 9) {
                final char c4 = text.charAt(4);
                if (c4 == '\u5e74') {
                    formatter = JodaCodec.formatter_d10_cn;
                }
                else if (c4 == '\ub144') {
                    formatter = JodaCodec.formatter_d10_kr;
                }
            }
            boolean digit = true;
            for (int i = 0; i < text.length(); ++i) {
                final char ch = text.charAt(i);
                if (ch < '0' || ch > '9') {
                    digit = false;
                    break;
                }
            }
            if (digit && text.length() > 8 && text.length() < 19) {
                final long epochMillis = Long.parseLong(text);
                return new LocalDateTime(epochMillis, DateTimeZone.forTimeZone(JSON.defaultTimeZone)).toLocalDate();
            }
        }
        return (formatter == null) ? LocalDate.parse(text) : LocalDate.parse(text, formatter);
    }
    
    protected DateTime parseZonedDateTime(final String text, DateTimeFormatter formatter) {
        if (formatter == null) {
            if (text.length() == 19) {
                final char c4 = text.charAt(4);
                final char c5 = text.charAt(7);
                final char c6 = text.charAt(10);
                final char c7 = text.charAt(13);
                final char c8 = text.charAt(16);
                if (c7 == ':' && c8 == ':') {
                    if (c4 == '-' && c5 == '-') {
                        if (c6 == 'T') {
                            formatter = JodaCodec.formatter_iso8601;
                        }
                        else if (c6 == ' ') {
                            formatter = JodaCodec.defaultFormatter;
                        }
                    }
                    else if (c4 == '/' && c5 == '/') {
                        formatter = JodaCodec.formatter_dt19_tw;
                    }
                    else {
                        final char c9 = text.charAt(0);
                        final char c10 = text.charAt(1);
                        final char c11 = text.charAt(2);
                        final char c12 = text.charAt(3);
                        final char c13 = text.charAt(5);
                        if (c11 == '/' && c13 == '/') {
                            final int v0 = (c9 - '0') * 10 + (c10 - '0');
                            final int v2 = (c12 - '0') * 10 + (c4 - '0');
                            if (v0 > 12) {
                                formatter = JodaCodec.formatter_dt19_eur;
                            }
                            else if (v2 > 12) {
                                formatter = JodaCodec.formatter_dt19_us;
                            }
                            else {
                                final String country = Locale.getDefault().getCountry();
                                if (country.equals("US")) {
                                    formatter = JodaCodec.formatter_dt19_us;
                                }
                                else if (country.equals("BR") || country.equals("AU")) {
                                    formatter = JodaCodec.formatter_dt19_eur;
                                }
                            }
                        }
                        else if (c11 == '.' && c13 == '.') {
                            formatter = JodaCodec.formatter_dt19_de;
                        }
                        else if (c11 == '-' && c13 == '-') {
                            formatter = JodaCodec.formatter_dt19_in;
                        }
                    }
                }
            }
            if (text.length() >= 17) {
                final char c4 = text.charAt(4);
                if (c4 == '\u5e74') {
                    if (text.charAt(text.length() - 1) == '\u79d2') {
                        formatter = JodaCodec.formatter_dt19_cn_1;
                    }
                    else {
                        formatter = JodaCodec.formatter_dt19_cn;
                    }
                }
                else if (c4 == '\ub144') {
                    formatter = JodaCodec.formatter_dt19_kr;
                }
            }
        }
        return (formatter == null) ? DateTime.parse(text) : DateTime.parse(text, formatter);
    }
    
    @Override
    public int getFastMatchToken() {
        return 4;
    }
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
        }
        else {
            if (fieldType == null) {
                fieldType = object.getClass();
            }
            if (fieldType == LocalDateTime.class) {
                final int mask = SerializerFeature.UseISO8601DateFormat.getMask();
                final LocalDateTime dateTime = (LocalDateTime)object;
                String format = serializer.getDateFormatPattern();
                if (format == null) {
                    if ((features & mask) != 0x0 || serializer.isEnabled(SerializerFeature.UseISO8601DateFormat)) {
                        format = "yyyy-MM-dd'T'HH:mm:ss";
                    }
                    else {
                        final int millis = dateTime.getMillisOfSecond();
                        if (millis == 0) {
                            format = "yyyy-MM-dd'T'HH:mm:ss.SSS";
                        }
                        else {
                            format = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS";
                        }
                    }
                }
                if (format != null) {
                    this.write(out, (ReadablePartial)dateTime, format);
                }
                else if (out.isEnabled(SerializerFeature.WriteDateUseDateFormat)) {
                    this.write(out, (ReadablePartial)dateTime, JSON.DEFFAULT_DATE_FORMAT);
                }
                else {
                    out.writeLong(dateTime.toDateTime(DateTimeZone.forTimeZone(JSON.defaultTimeZone)).toInstant().getMillis());
                }
            }
            else {
                out.writeString(object.toString());
            }
        }
    }
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final BeanContext context) throws IOException {
        final SerializeWriter out = serializer.out;
        final String format = context.getFormat();
        this.write(out, (ReadablePartial)object, format);
    }
    
    private void write(final SerializeWriter out, final ReadablePartial object, final String format) {
        DateTimeFormatter formatter;
        if (format.equals("yyyy-MM-dd'T'HH:mm:ss")) {
            formatter = JodaCodec.formatter_iso8601;
        }
        else {
            formatter = DateTimeFormat.forPattern(format);
        }
        final String text = formatter.print(object);
        out.writeString(text);
    }
    
    static {
        instance = new JodaCodec();
        defaultFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        defaultFormatter_23 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
        formatter_dt19_tw = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
        formatter_dt19_cn = DateTimeFormat.forPattern("yyyy\u5e74M\u6708d\u65e5 HH:mm:ss");
        formatter_dt19_cn_1 = DateTimeFormat.forPattern("yyyy\u5e74M\u6708d\u65e5 H\u65f6m\u5206s\u79d2");
        formatter_dt19_kr = DateTimeFormat.forPattern("yyyy\ub144M\uc6d4d\uc77c HH:mm:ss");
        formatter_dt19_us = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
        formatter_dt19_eur = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        formatter_dt19_de = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");
        formatter_dt19_in = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss");
        formatter_d8 = DateTimeFormat.forPattern("yyyyMMdd");
        formatter_d10_tw = DateTimeFormat.forPattern("yyyy/MM/dd");
        formatter_d10_cn = DateTimeFormat.forPattern("yyyy\u5e74M\u6708d\u65e5");
        formatter_d10_kr = DateTimeFormat.forPattern("yyyy\ub144M\uc6d4d\uc77c");
        formatter_d10_us = DateTimeFormat.forPattern("MM/dd/yyyy");
        formatter_d10_eur = DateTimeFormat.forPattern("dd/MM/yyyy");
        formatter_d10_de = DateTimeFormat.forPattern("dd.MM.yyyy");
        formatter_d10_in = DateTimeFormat.forPattern("dd-MM-yyyy");
        ISO_FIXED_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.getDefault());
        formatter_iso8601 = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    }
}
