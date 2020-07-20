package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSON;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import com.alibaba.fastjson.parser.JSONScanner;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.util.TypeUtils;
import java.math.BigDecimal;
import java.util.Date;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;

public class SqlDateDeserializer extends AbstractDateDeserializer implements ObjectDeserializer
{
    public static final SqlDateDeserializer instance;
    public static final SqlDateDeserializer instance_timestamp;
    private boolean timestamp;
    
    public SqlDateDeserializer() {
        this.timestamp = false;
    }
    
    public SqlDateDeserializer(final boolean timestmap) {
        this.timestamp = false;
        this.timestamp = true;
    }
    
    @Override
    protected <T> T cast(final DefaultJSONParser parser, final Type clazz, final Object fieldName, Object val) {
        if (this.timestamp) {
            return (T)this.castTimestamp(parser, clazz, fieldName, val);
        }
        if (val == null) {
            return null;
        }
        if (val instanceof Date) {
            val = new java.sql.Date(((Date)val).getTime());
        }
        else if (val instanceof BigDecimal) {
            val = new java.sql.Date(TypeUtils.longValue((BigDecimal)val));
        }
        else if (val instanceof Number) {
            val = new java.sql.Date(((Number)val).longValue());
        }
        else {
            if (!(val instanceof String)) {
                throw new JSONException("parse error : " + val);
            }
            final String strVal = (String)val;
            if (strVal.length() == 0) {
                return null;
            }
            final JSONScanner dateLexer = new JSONScanner(strVal);
            long longVal;
            try {
                if (dateLexer.scanISO8601DateIfMatch()) {
                    longVal = dateLexer.getCalendar().getTimeInMillis();
                }
                else {
                    final DateFormat dateFormat = parser.getDateFormat();
                    try {
                        final Date date = dateFormat.parse(strVal);
                        final java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                        return (T)sqlDate;
                    }
                    catch (ParseException ex) {
                        longVal = Long.parseLong(strVal);
                    }
                }
            }
            finally {
                dateLexer.close();
            }
            return (T)new java.sql.Date(longVal);
        }
        return (T)val;
    }
    
    protected <T> T castTimestamp(final DefaultJSONParser parser, final Type clazz, final Object fieldName, final Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Date) {
            return (T)new Timestamp(((Date)val).getTime());
        }
        if (val instanceof BigDecimal) {
            return (T)new Timestamp(TypeUtils.longValue((BigDecimal)val));
        }
        if (val instanceof Number) {
            return (T)new Timestamp(((Number)val).longValue());
        }
        if (!(val instanceof String)) {
            throw new JSONException("parse error");
        }
        final String strVal = (String)val;
        if (strVal.length() == 0) {
            return null;
        }
        final JSONScanner dateLexer = new JSONScanner(strVal);
        long longVal;
        try {
            if (dateLexer.scanISO8601DateIfMatch(false)) {
                longVal = dateLexer.getCalendar().getTimeInMillis();
            }
            else {
                if (strVal.length() == 29) {
                    final String dateFomartPattern = parser.getDateFomartPattern();
                    if (dateFomartPattern.length() != 29 && dateFomartPattern == JSON.DEFFAULT_DATE_FORMAT) {
                        return (T)Timestamp.valueOf(strVal);
                    }
                }
                final DateFormat dateFormat = parser.getDateFormat();
                try {
                    final Date date = dateFormat.parse(strVal);
                    final Timestamp sqlDate = new Timestamp(date.getTime());
                    return (T)sqlDate;
                }
                catch (ParseException ex) {
                    longVal = Long.parseLong(strVal);
                }
            }
        }
        finally {
            dateLexer.close();
        }
        return (T)new Timestamp(longVal);
    }
    
    @Override
    public int getFastMatchToken() {
        return 2;
    }
    
    static {
        instance = new SqlDateDeserializer();
        instance_timestamp = new SqlDateDeserializer(true);
    }
}
