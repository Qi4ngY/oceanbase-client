package com.alipay.oceanbase.jdbc.extend.datatype;

import com.alipay.oceanbase.jdbc.StringUtils;
import com.alipay.oceanbase.jdbc.ExceptionInterceptor;
import com.alipay.oceanbase.jdbc.SQLError;
import java.util.TimeZone;
import java.sql.Timestamp;
import java.sql.Time;
import java.util.Calendar;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Connection;

public class TIMESTAMPTZ extends Datum
{
    static final long serialVersionUID = 6708361144588335769L;
    public static int HOUR_MILLISECOND;
    public static int MINUTE_MILLISECOND;
    public static int SIZE_TIMESTAMPTZ;
    
    public TIMESTAMPTZ() {
        super(initTimestamptz());
    }
    
    public TIMESTAMPTZ(final byte[] bytes) {
        super(bytes);
    }
    
    public TIMESTAMPTZ(final Connection connection, final Date date) throws SQLException {
        super(toBytes(connection, date));
    }
    
    public TIMESTAMPTZ(final Connection connection, final Date date, final Calendar calendar) throws SQLException {
        super(toBytes(connection, date, calendar));
    }
    
    public TIMESTAMPTZ(final Connection connection, final Time time) throws SQLException {
        super(toBytes(connection, time));
    }
    
    public TIMESTAMPTZ(final Connection connection, final Time time, final Calendar calendar) throws SQLException {
        super(toBytes(connection, time, calendar));
    }
    
    public TIMESTAMPTZ(final Connection connection, final Timestamp timestamp) throws SQLException {
        super(toBytes(connection, timestamp));
    }
    
    public TIMESTAMPTZ(final Connection connection, final Timestamp timestamp, final Calendar calendar) throws SQLException {
        super(toBytes(connection, timestamp, calendar));
    }
    
    public TIMESTAMPTZ(final Connection connection, final String time) throws SQLException {
        super(toBytes(connection, time));
    }
    
    public TIMESTAMPTZ(final Connection connection, final String time, final Calendar calendar) throws SQLException {
        super(toBytes(connection, time, calendar));
    }
    
    public static Date toDate(final Connection connection, final byte[] bytes) throws SQLException {
        if (bytes.length < 14) {
            throw new SQLException("invalid bytes length");
        }
        final String tzStr = toTimezoneStr(bytes[12], bytes[13], "GMT");
        final Calendar targetCalendar = Calendar.getInstance(TimeZone.getTimeZone(tzStr));
        final Date date = new Date(getOriginTime(bytes));
        targetCalendar.setTime(date);
        date.setTime(targetCalendar.getTime().getTime());
        return date;
    }
    
    public static Time toTime(final Connection connection, final byte[] bytes) throws SQLException {
        if (bytes.length < 14) {
            throw new SQLException("invalid bytes length");
        }
        final String tzStr = toTimezoneStr(bytes[12], bytes[13], "GMT");
        final Calendar targetCalendar = Calendar.getInstance(TimeZone.getTimeZone(tzStr));
        final Time time = new Time(getOriginTime(bytes));
        targetCalendar.setTime(time);
        time.setTime(targetCalendar.getTime().getTime());
        return time;
    }
    
    public static TIMESTAMP toTIMESTAMP(final Connection connection, final byte[] bytes) throws SQLException {
        return new TIMESTAMP(toTimestamp(connection, bytes));
    }
    
    public static Timestamp toTimestamp(final Connection connection, final byte[] bytes) throws SQLException {
        if (bytes.length < 14) {
            throw new SQLException("invalid bytes length");
        }
        final String tzStr = toTimezoneStr(bytes[12], bytes[13], "GMT");
        final Calendar targetCalendar = Calendar.getInstance(TimeZone.getTimeZone(tzStr));
        final Timestamp timestamp = new Timestamp(getOriginTime(bytes));
        targetCalendar.setTime(timestamp);
        timestamp.setTime(targetCalendar.getTime().getTime());
        timestamp.setNanos(TIMESTAMP.getNanos(bytes, 7));
        return timestamp;
    }
    
    public static long getOriginTime(final byte[] bytes) throws SQLException {
        if (bytes.length < 7) {
            throw new SQLException("invalid bytes length");
        }
        final int[] result = new int[7];
        for (int i = 0; i < 7; ++i) {
            result[i] = (bytes[i] & 0xFF);
        }
        int i = result[0] * 100 + result[1];
        final Calendar localCalendar = Calendar.getInstance();
        localCalendar.set(1, i);
        localCalendar.set(2, result[2] - 1);
        localCalendar.set(5, result[3]);
        localCalendar.set(11, result[4]);
        localCalendar.set(12, result[5]);
        localCalendar.set(13, result[6]);
        localCalendar.set(14, 0);
        return localCalendar.getTime().getTime();
    }
    
    public static String toString(final Connection connection, final byte[] bytes) throws SQLException {
        if (bytes.length < 14) {
            throw new SQLException("invalid bytes length");
        }
        final String tzStr = toTimezoneStr(bytes[12], bytes[13], "GMT");
        final Timestamp timestamp = new Timestamp(getOriginTime(bytes));
        timestamp.setNanos(TIMESTAMP.getNanos(bytes, 7));
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        final int year = calendar.get(1);
        final int month = calendar.get(2) + 1;
        final int day = calendar.get(5);
        final int hour = calendar.get(11);
        final int minute = calendar.get(12);
        final int second = calendar.get(13);
        final int nanos = TIMESTAMP.getNanos(bytes, 7);
        return toString(year, month, day, hour, minute, second, nanos, bytes[11], tzStr);
    }
    
    public String toResultSetString(final Connection connection) throws SQLException {
        return toString(connection, this.getBytes());
    }
    
    public static final String toString(final int year, final int month, final int day, final int hour, final int minute, final int second, final int nanos, final int scale, final String timezone) {
        String time = "" + year + "-" + toStr(month) + "-" + toStr(day) + " " + toStr(hour) + ":" + toStr(minute) + ":" + toStr(second);
        if (nanos >= 0) {
            String temp = String.format("%09d", nanos);
            char[] chars;
            int index;
            for (chars = temp.toCharArray(), index = chars.length; index > 1 && chars[index - 1] == '0'; --index) {}
            temp = temp.substring(0, index);
            if (scale > temp.length()) {
                final int x = scale - temp.length();
                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < x; ++i) {
                    sb.append("0");
                }
                temp += sb.toString();
            }
            time = time + "." + temp;
        }
        if (timezone != null) {
            time = time + " " + timezone;
        }
        return time;
    }
    
    private static final String toStr(final int temp) {
        return (temp < 10) ? ("0" + temp) : Integer.toString(temp);
    }
    
    public Timestamp timestampValue(final Connection connection) throws SQLException {
        return toTimestamp(connection, this.getBytes());
    }
    
    public byte[] toBytes() {
        return this.getBytes();
    }
    
    public static byte[] toBytes(final Connection connection, final Date date) throws SQLException {
        return toBytes(connection, date, null);
    }
    
    public static byte[] toBytes(final Connection connection, final Date date, Calendar calendar) throws SQLException {
        if (date == null) {
            return null;
        }
        if (null == calendar) {
            calendar = Calendar.getInstance();
        }
        final Calendar localCalendar = Calendar.getInstance();
        final byte[] resultBytes = new byte[TIMESTAMPTZ.SIZE_TIMESTAMPTZ];
        final int offset = calendar.getTimeZone().getRawOffset();
        localCalendar.setTime(date);
        final int year = calendar.get(1);
        if (year >= -4712 && year <= 9999) {
            resultBytes[0] = (byte)(localCalendar.get(1) / 100);
            resultBytes[1] = (byte)(localCalendar.get(1) % 100);
            resultBytes[2] = (byte)(localCalendar.get(2) + 1);
            resultBytes[3] = (byte)localCalendar.get(5);
            resultBytes[4] = 0;
            resultBytes[6] = (resultBytes[5] = 0);
            TIMESTAMP.setNanos(resultBytes, 7, 0);
            resultBytes[12] = (byte)(offset / TIMESTAMPTZ.HOUR_MILLISECOND);
            resultBytes[13] = (byte)(offset % TIMESTAMPTZ.HOUR_MILLISECOND / TIMESTAMPTZ.MINUTE_MILLISECOND);
            return resultBytes;
        }
        final SQLException exception = SQLError.createSQLException(String.format("error format, timestamp = %s", date.toString()), "268", null);
        exception.fillInStackTrace();
        throw exception;
    }
    
    public static byte[] toBytes(final Connection connection, final Time time) throws SQLException {
        return toBytes(connection, time, null);
    }
    
    public static byte[] toBytes(final Connection connection, final Time time, Calendar calendar) throws SQLException {
        if (time == null) {
            return null;
        }
        if (null == calendar) {
            calendar = Calendar.getInstance();
        }
        final Calendar localCalendar = Calendar.getInstance();
        final byte[] resultBytes = new byte[TIMESTAMPTZ.SIZE_TIMESTAMPTZ];
        final int offset = calendar.getTimeZone().getRawOffset();
        localCalendar.setTime(time);
        final short base = 1970;
        localCalendar.set(1, base);
        localCalendar.set(2, 0);
        localCalendar.set(5, 1);
        final int year = calendar.get(1);
        if (year >= -4712 && year <= 9999) {
            resultBytes[0] = (byte)(localCalendar.get(1) / 100);
            resultBytes[1] = (byte)(localCalendar.get(1) % 100);
            resultBytes[2] = (byte)(localCalendar.get(2) + 1);
            resultBytes[3] = (byte)localCalendar.get(5);
            resultBytes[4] = (byte)localCalendar.get(11);
            resultBytes[5] = (byte)localCalendar.get(12);
            resultBytes[6] = (byte)localCalendar.get(13);
            TIMESTAMP.setNanos(resultBytes, 7, 0);
            resultBytes[12] = (byte)(offset / TIMESTAMPTZ.HOUR_MILLISECOND);
            resultBytes[13] = (byte)(offset % TIMESTAMPTZ.HOUR_MILLISECOND / TIMESTAMPTZ.MINUTE_MILLISECOND);
            return resultBytes;
        }
        final SQLException exception = SQLError.createSQLException(String.format("error format, timestamp = %s", time.toString()), "268", null);
        exception.fillInStackTrace();
        throw exception;
    }
    
    public static byte[] toBytes(final Connection connection, final Timestamp timeStamp) throws SQLException {
        return toBytes(connection, timeStamp, null);
    }
    
    public static TIMESTAMPTZ toTIMESTAMPTZ(final Timestamp timestamp, final String timeZoneStr) throws SQLException {
        if (timestamp == null) {
            return null;
        }
        if (StringUtils.isBlank(timeZoneStr)) {
            throw new SQLException("illegal time zone");
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZoneStr));
        final byte[] resultBytes = new byte[TIMESTAMPTZ.SIZE_TIMESTAMPTZ];
        final int offset = calendar.getTimeZone().getRawOffset();
        calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        final int year = calendar.get(1);
        if (year >= -4712 && year <= 9999) {
            resultBytes[0] = (byte)(calendar.get(1) / 100);
            resultBytes[1] = (byte)(calendar.get(1) % 100);
            resultBytes[2] = (byte)(calendar.get(2) + 1);
            resultBytes[3] = (byte)calendar.get(5);
            resultBytes[4] = (byte)calendar.get(11);
            resultBytes[5] = (byte)calendar.get(12);
            resultBytes[6] = (byte)calendar.get(13);
            TIMESTAMP.setNanos(resultBytes, 7, timestamp.getNanos());
            resultBytes[12] = (byte)(offset / TIMESTAMPTZ.HOUR_MILLISECOND);
            resultBytes[13] = (byte)(offset % TIMESTAMPTZ.HOUR_MILLISECOND / TIMESTAMPTZ.MINUTE_MILLISECOND);
            return new TIMESTAMPTZ(resultBytes);
        }
        final SQLException exception = SQLError.createSQLException(String.format("error format, timestamp = %s", timestamp.toString()), "268", null);
        exception.fillInStackTrace();
        throw exception;
    }
    
    public static byte[] toBytes(final Connection connection, final Timestamp timestamp, Calendar calendar) throws SQLException {
        if (timestamp == null) {
            return null;
        }
        if (null == calendar) {
            calendar = Calendar.getInstance();
        }
        final Calendar localCalendar = Calendar.getInstance();
        final byte[] resultBytes = new byte[TIMESTAMPTZ.SIZE_TIMESTAMPTZ];
        final int offset = calendar.getTimeZone().getRawOffset();
        localCalendar.setTime(timestamp);
        final int year = localCalendar.get(1);
        if (year >= -4712 && year <= 9999) {
            resultBytes[0] = (byte)(localCalendar.get(1) / 100);
            resultBytes[1] = (byte)(localCalendar.get(1) % 100);
            resultBytes[2] = (byte)(localCalendar.get(2) + 1);
            resultBytes[3] = (byte)localCalendar.get(5);
            resultBytes[4] = (byte)localCalendar.get(11);
            resultBytes[5] = (byte)localCalendar.get(12);
            resultBytes[6] = (byte)localCalendar.get(13);
            TIMESTAMP.setNanos(resultBytes, 7, timestamp.getNanos());
            resultBytes[12] = (byte)(offset / TIMESTAMPTZ.HOUR_MILLISECOND);
            resultBytes[13] = (byte)(offset % TIMESTAMPTZ.HOUR_MILLISECOND / TIMESTAMPTZ.MINUTE_MILLISECOND);
            return resultBytes;
        }
        final SQLException exception = SQLError.createSQLException(String.format("error format, timestamp = %s", timestamp.toString()), "268", null);
        exception.fillInStackTrace();
        throw exception;
    }
    
    public static byte[] toBytes(final Connection connection, final String time) throws SQLException {
        return toBytes(connection, Timestamp.valueOf(time));
    }
    
    public static byte[] toBytes(final Connection connection, final String time, final Calendar calendar) throws SQLException {
        return toBytes(connection, Timestamp.valueOf(time), calendar);
    }
    
    @Override
    public String stringValue(final Connection connection) throws SQLException {
        return toString(connection, this.getBytes());
    }
    
    public Time timeValue(final Connection connection) throws SQLException {
        return toTime(connection, this.getBytes());
    }
    
    private static byte[] initTimestamptz() {
        final byte[] result = { 19, 70, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0 };
        return result;
    }
    
    @Override
    public Object toJdbc() throws SQLException {
        return null;
    }
    
    @Override
    public Object makeJdbcArray(final int temp) {
        final Timestamp[] timestamps = new Timestamp[temp];
        return timestamps;
    }
    
    @Override
    public boolean isConvertibleTo(final Class claz) {
        return claz.getName().compareTo("java.sql.Date") == 0 || claz.getName().compareTo("java.sql.Time") == 0 || claz.getName().compareTo("java.sql.Timestamp") == 0 || claz.getName().compareTo("java.lang.String") == 0;
    }
    
    private static int getHighOrderbits(final int bits) {
        return (bits & 0x7F) << 6;
    }
    
    private static int getLowOrderbits(final int bits) {
        return (bits & 0xFC) >> 2;
    }
    
    public static String toTimezoneStr(final byte hour, final byte minute, final String pre) {
        final StringBuilder offsetTimeZone = new StringBuilder();
        boolean isPostive = true;
        if (hour <= -10) {
            isPostive = false;
            offsetTimeZone.append(-hour);
        }
        else if (hour < 0) {
            isPostive = false;
            offsetTimeZone.append("0");
            offsetTimeZone.append(-hour);
        }
        else if (hour < 10) {
            offsetTimeZone.append("0");
            offsetTimeZone.append(hour);
        }
        else {
            offsetTimeZone.append(hour);
        }
        offsetTimeZone.append(":");
        if (minute <= -10) {
            isPostive = false;
            offsetTimeZone.append(-minute);
        }
        else if (minute < 0) {
            isPostive = false;
            offsetTimeZone.append("0");
            offsetTimeZone.append(-minute);
        }
        else if (minute < 10) {
            offsetTimeZone.append("0");
            offsetTimeZone.append(minute);
        }
        else {
            offsetTimeZone.append(minute);
        }
        return ((pre == null) ? "" : pre) + (isPostive ? ("+" + (Object)offsetTimeZone) : ("-" + (Object)offsetTimeZone));
    }
    
    static {
        TIMESTAMPTZ.SIZE_TIMESTAMPTZ = 14;
        TIMESTAMPTZ.HOUR_MILLISECOND = 3600000;
        TIMESTAMPTZ.MINUTE_MILLISECOND = 60000;
    }
}
