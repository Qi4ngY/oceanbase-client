package com.alibaba.fastjson.parser;

import java.util.Collection;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Calendar;
import java.math.BigDecimal;
import com.alibaba.fastjson.util.ASMUtils;
import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSON;

public final class JSONScanner extends JSONLexerBase
{
    private final String text;
    private final int len;
    
    public JSONScanner(final String input) {
        this(input, JSON.DEFAULT_PARSER_FEATURE);
    }
    
    public JSONScanner(final String input, final int features) {
        super(features);
        this.text = input;
        this.len = this.text.length();
        this.bp = -1;
        this.next();
        if (this.ch == '\ufeff') {
            this.next();
        }
    }
    
    @Override
    public final char charAt(final int index) {
        if (index >= this.len) {
            return '\u001a';
        }
        return this.text.charAt(index);
    }
    
    @Override
    public final char next() {
        final int index = ++this.bp;
        return this.ch = ((index >= this.len) ? '\u001a' : this.text.charAt(index));
    }
    
    public JSONScanner(final char[] input, final int inputLength) {
        this(input, inputLength, JSON.DEFAULT_PARSER_FEATURE);
    }
    
    public JSONScanner(final char[] input, final int inputLength, final int features) {
        this(new String(input, 0, inputLength), features);
    }
    
    @Override
    protected final void copyTo(final int offset, final int count, final char[] dest) {
        this.text.getChars(offset, offset + count, dest, 0);
    }
    
    static boolean charArrayCompare(final String src, final int offset, final char[] dest) {
        final int destLen = dest.length;
        if (destLen + offset > src.length()) {
            return false;
        }
        for (int i = 0; i < destLen; ++i) {
            if (dest[i] != src.charAt(offset + i)) {
                return false;
            }
        }
        return true;
    }
    
    public final boolean charArrayCompare(final char[] chars) {
        return charArrayCompare(this.text, this.bp, chars);
    }
    
    @Override
    public final int indexOf(final char ch, final int startIndex) {
        return this.text.indexOf(ch, startIndex);
    }
    
    @Override
    public final String addSymbol(final int offset, final int len, final int hash, final SymbolTable symbolTable) {
        return symbolTable.addSymbol(this.text, offset, len, hash);
    }
    
    @Override
    public byte[] bytesValue() {
        if (this.token == 26) {
            final int start = this.np + 1;
            final int len = this.sp;
            if (len % 2 != 0) {
                throw new JSONException("illegal state. " + len);
            }
            final byte[] bytes = new byte[len / 2];
            for (int i = 0; i < bytes.length; ++i) {
                final char c0 = this.text.charAt(start + i * 2);
                final char c2 = this.text.charAt(start + i * 2 + 1);
                final int b0 = c0 - ((c0 <= '9') ? '0' : '7');
                final int b2 = c2 - ((c2 <= '9') ? '0' : '7');
                bytes[i] = (byte)(b0 << 4 | b2);
            }
            return bytes;
        }
        else {
            if (!this.hasSpecial) {
                return IOUtils.decodeBase64(this.text, this.np + 1, this.sp);
            }
            final String escapedText = new String(this.sbuf, 0, this.sp);
            return IOUtils.decodeBase64(escapedText);
        }
    }
    
    @Override
    public final String stringVal() {
        if (!this.hasSpecial) {
            return this.subString(this.np + 1, this.sp);
        }
        return new String(this.sbuf, 0, this.sp);
    }
    
    @Override
    public final String subString(final int offset, final int count) {
        if (!ASMUtils.IS_ANDROID) {
            return this.text.substring(offset, offset + count);
        }
        if (count < this.sbuf.length) {
            this.text.getChars(offset, offset + count, this.sbuf, 0);
            return new String(this.sbuf, 0, count);
        }
        final char[] chars = new char[count];
        this.text.getChars(offset, offset + count, chars, 0);
        return new String(chars);
    }
    
    public final char[] sub_chars(final int offset, final int count) {
        if (ASMUtils.IS_ANDROID && count < this.sbuf.length) {
            this.text.getChars(offset, offset + count, this.sbuf, 0);
            return this.sbuf;
        }
        final char[] chars = new char[count];
        this.text.getChars(offset, offset + count, chars, 0);
        return chars;
    }
    
    @Override
    public final String numberString() {
        final char chLocal = this.charAt(this.np + this.sp - 1);
        int sp = this.sp;
        if (chLocal == 'L' || chLocal == 'S' || chLocal == 'B' || chLocal == 'F' || chLocal == 'D') {
            --sp;
        }
        return this.subString(this.np, sp);
    }
    
    @Override
    public final BigDecimal decimalValue() {
        final char chLocal = this.charAt(this.np + this.sp - 1);
        int sp = this.sp;
        if (chLocal == 'L' || chLocal == 'S' || chLocal == 'B' || chLocal == 'F' || chLocal == 'D') {
            --sp;
        }
        final int offset = this.np;
        final int count = sp;
        if (count < this.sbuf.length) {
            this.text.getChars(offset, offset + count, this.sbuf, 0);
            return new BigDecimal(this.sbuf, 0, count);
        }
        final char[] chars = new char[count];
        this.text.getChars(offset, offset + count, chars, 0);
        return new BigDecimal(chars);
    }
    
    public boolean scanISO8601DateIfMatch() {
        return this.scanISO8601DateIfMatch(true);
    }
    
    public boolean scanISO8601DateIfMatch(final boolean strict) {
        final int rest = this.len - this.bp;
        return this.scanISO8601DateIfMatch(strict, rest);
    }
    
    private boolean scanISO8601DateIfMatch(final boolean strict, final int rest) {
        if (rest < 8) {
            return false;
        }
        final char c0 = this.charAt(this.bp);
        final char c2 = this.charAt(this.bp + 1);
        final char c3 = this.charAt(this.bp + 2);
        final char c4 = this.charAt(this.bp + 3);
        final char c5 = this.charAt(this.bp + 4);
        final char c6 = this.charAt(this.bp + 5);
        final char c7 = this.charAt(this.bp + 6);
        final char c8 = this.charAt(this.bp + 7);
        if (!strict && rest > 13) {
            final char c_r0 = this.charAt(this.bp + rest - 1);
            final char c_r2 = this.charAt(this.bp + rest - 2);
            if (c0 == '/' && c2 == 'D' && c3 == 'a' && c4 == 't' && c5 == 'e' && c6 == '(' && c_r0 == '/' && c_r2 == ')') {
                int plusIndex = -1;
                for (int i = 6; i < rest; ++i) {
                    final char c9 = this.charAt(this.bp + i);
                    if (c9 == '+') {
                        plusIndex = i;
                    }
                    else {
                        if (c9 < '0') {
                            break;
                        }
                        if (c9 > '9') {
                            break;
                        }
                    }
                }
                if (plusIndex == -1) {
                    return false;
                }
                final int offset = this.bp + 6;
                final String numberText = this.subString(offset, this.bp + plusIndex - offset);
                final long millis = Long.parseLong(numberText);
                (this.calendar = Calendar.getInstance(this.timeZone, this.locale)).setTimeInMillis(millis);
                this.token = 5;
                return true;
            }
        }
        char c10;
        if (rest == 8 || rest == 14 || (rest == 16 && ((c10 = this.charAt(this.bp + 10)) == 'T' || c10 == ' ')) || (rest == 17 && this.charAt(this.bp + 6) != '-')) {
            if (strict) {
                return false;
            }
            final char c11 = this.charAt(this.bp + 8);
            final boolean c_47 = c5 == '-' && c8 == '-';
            final boolean sperate16 = c_47 && rest == 16;
            final boolean sperate17 = c_47 && rest == 17;
            char y0;
            char y2;
            char y3;
            char y4;
            char M0;
            char M2;
            char d0;
            char d2;
            if (sperate17 || sperate16) {
                y0 = c0;
                y2 = c2;
                y3 = c3;
                y4 = c4;
                M0 = c6;
                M2 = c7;
                d0 = c11;
                d2 = this.charAt(this.bp + 9);
            }
            else if (c5 == '-' && c7 == '-') {
                y0 = c0;
                y2 = c2;
                y3 = c3;
                y4 = c4;
                M0 = '0';
                M2 = c6;
                d0 = '0';
                d2 = c8;
            }
            else {
                y0 = c0;
                y2 = c2;
                y3 = c3;
                y4 = c4;
                M0 = c5;
                M2 = c6;
                d0 = c7;
                d2 = c8;
            }
            if (!checkDate(y0, y2, y3, y4, M0, M2, d0, d2)) {
                return false;
            }
            this.setCalendar(y0, y2, y3, y4, M0, M2, d0, d2);
            int millis2;
            int hour;
            int minute;
            int seconds;
            if (rest != 8) {
                final char c12 = this.charAt(this.bp + 9);
                c10 = this.charAt(this.bp + 10);
                final char c13 = this.charAt(this.bp + 11);
                final char c14 = this.charAt(this.bp + 12);
                final char c15 = this.charAt(this.bp + 13);
                char h0;
                char h2;
                char m0;
                char m2;
                char s0;
                char s2;
                if ((sperate17 && c10 == 'T' && c15 == ':' && this.charAt(this.bp + 16) == 'Z') || (sperate16 && (c10 == ' ' || c10 == 'T') && c15 == ':')) {
                    h0 = c13;
                    h2 = c14;
                    m0 = this.charAt(this.bp + 14);
                    m2 = this.charAt(this.bp + 15);
                    s0 = '0';
                    s2 = '0';
                }
                else {
                    h0 = c11;
                    h2 = c12;
                    m0 = c10;
                    m2 = c13;
                    s0 = c14;
                    s2 = c15;
                }
                if (!this.checkTime(h0, h2, m0, m2, s0, s2)) {
                    return false;
                }
                if (rest == 17 && !sperate17) {
                    final char S0 = this.charAt(this.bp + 14);
                    final char S2 = this.charAt(this.bp + 15);
                    final char S3 = this.charAt(this.bp + 16);
                    if (S0 < '0' || S0 > '9') {
                        return false;
                    }
                    if (S2 < '0' || S2 > '9') {
                        return false;
                    }
                    if (S3 < '0' || S3 > '9') {
                        return false;
                    }
                    millis2 = (S0 - '0') * 100 + (S2 - '0') * 10 + (S3 - '0');
                }
                else {
                    millis2 = 0;
                }
                hour = (h0 - '0') * 10 + (h2 - '0');
                minute = (m0 - '0') * 10 + (m2 - '0');
                seconds = (s0 - '0') * 10 + (s2 - '0');
            }
            else {
                hour = 0;
                minute = 0;
                seconds = 0;
                millis2 = 0;
            }
            this.calendar.set(11, hour);
            this.calendar.set(12, minute);
            this.calendar.set(13, seconds);
            this.calendar.set(14, millis2);
            this.token = 5;
            return true;
        }
        else {
            if (rest < 9) {
                return false;
            }
            final char c16 = this.charAt(this.bp + 8);
            final char c17 = this.charAt(this.bp + 9);
            int date_len = 10;
            char y5;
            char y6;
            char y7;
            char y8;
            char M3;
            char M4;
            char d3;
            char d4;
            if ((c5 == '-' && c8 == '-') || (c5 == '/' && c8 == '/')) {
                y5 = c0;
                y6 = c2;
                y7 = c3;
                y8 = c4;
                M3 = c6;
                M4 = c7;
                if (c17 == ' ') {
                    d3 = '0';
                    d4 = c16;
                    date_len = 9;
                }
                else {
                    d3 = c16;
                    d4 = c17;
                }
            }
            else if (c5 == '-' && c7 == '-') {
                y5 = c0;
                y6 = c2;
                y7 = c3;
                y8 = c4;
                M3 = '0';
                M4 = c6;
                if (c16 == ' ') {
                    d3 = '0';
                    d4 = c8;
                    date_len = 8;
                }
                else {
                    d3 = c8;
                    d4 = c16;
                    date_len = 9;
                }
            }
            else if ((c3 == '.' && c6 == '.') || (c3 == '-' && c6 == '-')) {
                d3 = c0;
                d4 = c2;
                M3 = c4;
                M4 = c5;
                y5 = c7;
                y6 = c8;
                y7 = c16;
                y8 = c17;
            }
            else if (c16 == 'T') {
                y5 = c0;
                y6 = c2;
                y7 = c3;
                y8 = c4;
                M3 = c5;
                M4 = c6;
                d3 = c7;
                d4 = c8;
                date_len = 8;
            }
            else {
                if (c5 != '\u5e74' && c5 != '\ub144') {
                    return false;
                }
                y5 = c0;
                y6 = c2;
                y7 = c3;
                y8 = c4;
                if (c8 == '\u6708' || c8 == '\uc6d4') {
                    M3 = c6;
                    M4 = c7;
                    if (c17 == '\u65e5' || c17 == '\uc77c') {
                        d3 = '0';
                        d4 = c16;
                    }
                    else {
                        if (this.charAt(this.bp + 10) != '\u65e5' && this.charAt(this.bp + 10) != '\uc77c') {
                            return false;
                        }
                        d3 = c16;
                        d4 = c17;
                        date_len = 11;
                    }
                }
                else {
                    if (c7 != '\u6708' && c7 != '\uc6d4') {
                        return false;
                    }
                    M3 = '0';
                    M4 = c6;
                    if (c16 == '\u65e5' || c16 == '\uc77c') {
                        d3 = '0';
                        d4 = c8;
                    }
                    else {
                        if (c17 != '\u65e5' && c17 != '\uc77c') {
                            return false;
                        }
                        d3 = c8;
                        d4 = c16;
                    }
                }
            }
            if (!checkDate(y5, y6, y7, y8, M3, M4, d3, d4)) {
                return false;
            }
            this.setCalendar(y5, y6, y7, y8, M3, M4, d3, d4);
            final char t = this.charAt(this.bp + date_len);
            if (t == 'T' && rest == 16 && date_len == 8 && this.charAt(this.bp + 15) == 'Z') {
                final char h3 = this.charAt(this.bp + date_len + 1);
                final char h4 = this.charAt(this.bp + date_len + 2);
                final char m3 = this.charAt(this.bp + date_len + 3);
                final char m4 = this.charAt(this.bp + date_len + 4);
                final char s3 = this.charAt(this.bp + date_len + 5);
                final char s4 = this.charAt(this.bp + date_len + 6);
                if (!this.checkTime(h3, h4, m3, m4, s3, s4)) {
                    return false;
                }
                this.setTime(h3, h4, m3, m4, s3, s4);
                this.calendar.set(14, 0);
                if (this.calendar.getTimeZone().getRawOffset() != 0) {
                    final String[] timeZoneIDs = TimeZone.getAvailableIDs(0);
                    if (timeZoneIDs.length > 0) {
                        final TimeZone timeZone = TimeZone.getTimeZone(timeZoneIDs[0]);
                        this.calendar.setTimeZone(timeZone);
                    }
                }
                this.token = 5;
                return true;
            }
            else if (t == 'T' || (t == ' ' && !strict)) {
                if (rest < date_len + 9) {
                    return false;
                }
                if (this.charAt(this.bp + date_len + 3) != ':') {
                    return false;
                }
                if (this.charAt(this.bp + date_len + 6) != ':') {
                    return false;
                }
                final char h3 = this.charAt(this.bp + date_len + 1);
                final char h4 = this.charAt(this.bp + date_len + 2);
                final char m3 = this.charAt(this.bp + date_len + 4);
                final char m4 = this.charAt(this.bp + date_len + 5);
                final char s3 = this.charAt(this.bp + date_len + 7);
                final char s4 = this.charAt(this.bp + date_len + 8);
                if (!this.checkTime(h3, h4, m3, m4, s3, s4)) {
                    return false;
                }
                this.setTime(h3, h4, m3, m4, s3, s4);
                final char dot = this.charAt(this.bp + date_len + 9);
                int millisLen = -1;
                int millis3 = 0;
                if (dot == '.') {
                    if (rest < date_len + 11) {
                        return false;
                    }
                    final char S4 = this.charAt(this.bp + date_len + 10);
                    if (S4 < '0' || S4 > '9') {
                        return false;
                    }
                    millis3 = S4 - '0';
                    millisLen = 1;
                    if (rest > date_len + 11) {
                        final char S5 = this.charAt(this.bp + date_len + 11);
                        if (S5 >= '0' && S5 <= '9') {
                            millis3 = millis3 * 10 + (S5 - '0');
                            millisLen = 2;
                        }
                    }
                    if (millisLen == 2) {
                        final char S6 = this.charAt(this.bp + date_len + 12);
                        if (S6 >= '0' && S6 <= '9') {
                            millis3 = millis3 * 10 + (S6 - '0');
                            millisLen = 3;
                        }
                    }
                }
                this.calendar.set(14, millis3);
                int timzeZoneLength = 0;
                char timeZoneFlag = this.charAt(this.bp + date_len + 10 + millisLen);
                if (timeZoneFlag == ' ') {
                    ++millisLen;
                    timeZoneFlag = this.charAt(this.bp + date_len + 10 + millisLen);
                }
                if (timeZoneFlag == '+' || timeZoneFlag == '-') {
                    final char t2 = this.charAt(this.bp + date_len + 10 + millisLen + 1);
                    if (t2 < '0' || t2 > '1') {
                        return false;
                    }
                    final char t3 = this.charAt(this.bp + date_len + 10 + millisLen + 2);
                    if (t3 < '0' || t3 > '9') {
                        return false;
                    }
                    final char t4 = this.charAt(this.bp + date_len + 10 + millisLen + 3);
                    char t5 = '0';
                    char t6 = '0';
                    if (t4 == ':') {
                        t5 = this.charAt(this.bp + date_len + 10 + millisLen + 4);
                        t6 = this.charAt(this.bp + date_len + 10 + millisLen + 5);
                        Label_2856: {
                            if (t5 == '4' && t6 == '5') {
                                if (t2 == '1') {
                                    if (t3 == '2') {
                                        break Label_2856;
                                    }
                                    if (t3 == '3') {
                                        break Label_2856;
                                    }
                                }
                                if (t2 == '0') {
                                    if (t3 == '5') {
                                        break Label_2856;
                                    }
                                    if (t3 == '8') {
                                        break Label_2856;
                                    }
                                }
                                return false;
                            }
                            if (t5 != '0' && t5 != '3') {
                                return false;
                            }
                            if (t6 != '0') {
                                return false;
                            }
                        }
                        timzeZoneLength = 6;
                    }
                    else if (t4 == '0') {
                        t5 = this.charAt(this.bp + date_len + 10 + millisLen + 4);
                        if (t5 != '0' && t5 != '3') {
                            return false;
                        }
                        timzeZoneLength = 5;
                    }
                    else if (t4 == '3' && this.charAt(this.bp + date_len + 10 + millisLen + 4) == '0') {
                        t5 = '3';
                        t6 = '0';
                        timzeZoneLength = 5;
                    }
                    else if (t4 == '4' && this.charAt(this.bp + date_len + 10 + millisLen + 4) == '5') {
                        t5 = '4';
                        t6 = '5';
                        timzeZoneLength = 5;
                    }
                    else {
                        timzeZoneLength = 3;
                    }
                    this.setTimeZone(timeZoneFlag, t2, t3, t5, t6);
                }
                else if (timeZoneFlag == 'Z') {
                    timzeZoneLength = 1;
                    if (this.calendar.getTimeZone().getRawOffset() != 0) {
                        final String[] timeZoneIDs2 = TimeZone.getAvailableIDs(0);
                        if (timeZoneIDs2.length > 0) {
                            final TimeZone timeZone2 = TimeZone.getTimeZone(timeZoneIDs2[0]);
                            this.calendar.setTimeZone(timeZone2);
                        }
                    }
                }
                final char end = this.charAt(this.bp + (date_len + 10 + millisLen + timzeZoneLength));
                if (end != '\u001a' && end != '\"') {
                    return false;
                }
                final int n = this.bp + (date_len + 10 + millisLen + timzeZoneLength);
                this.bp = n;
                this.ch = this.charAt(n);
                this.token = 5;
                return true;
            }
            else {
                if (t == '\"' || t == '\u001a' || t == '\u65e5' || t == '\uc77c') {
                    this.calendar.set(11, 0);
                    this.calendar.set(12, 0);
                    this.calendar.set(13, 0);
                    this.calendar.set(14, 0);
                    final int n2 = this.bp + date_len;
                    this.bp = n2;
                    this.ch = this.charAt(n2);
                    this.token = 5;
                    return true;
                }
                if (t != '+' && t != '-') {
                    return false;
                }
                if (this.len != date_len + 6) {
                    return false;
                }
                if (this.charAt(this.bp + date_len + 3) != ':' || this.charAt(this.bp + date_len + 4) != '0' || this.charAt(this.bp + date_len + 5) != '0') {
                    return false;
                }
                this.setTime('0', '0', '0', '0', '0', '0');
                this.calendar.set(14, 0);
                this.setTimeZone(t, this.charAt(this.bp + date_len + 1), this.charAt(this.bp + date_len + 2));
                return true;
            }
        }
    }
    
    protected void setTime(final char h0, final char h1, final char m0, final char m1, final char s0, final char s1) {
        final int hour = (h0 - '0') * 10 + (h1 - '0');
        final int minute = (m0 - '0') * 10 + (m1 - '0');
        final int seconds = (s0 - '0') * 10 + (s1 - '0');
        this.calendar.set(11, hour);
        this.calendar.set(12, minute);
        this.calendar.set(13, seconds);
    }
    
    protected void setTimeZone(final char timeZoneFlag, final char t0, final char t1) {
        this.setTimeZone(timeZoneFlag, t0, t1, '0', '0');
    }
    
    protected void setTimeZone(final char timeZoneFlag, final char t0, final char t1, final char t3, final char t4) {
        int timeZoneOffset = ((t0 - '0') * 10 + (t1 - '0')) * 3600 * 1000;
        timeZoneOffset += ((t3 - '0') * 10 + (t4 - '0')) * 60 * 1000;
        if (timeZoneFlag == '-') {
            timeZoneOffset = -timeZoneOffset;
        }
        if (this.calendar.getTimeZone().getRawOffset() != timeZoneOffset) {
            this.calendar.setTimeZone(new SimpleTimeZone(timeZoneOffset, Integer.toString(timeZoneOffset)));
        }
    }
    
    private boolean checkTime(final char h0, final char h1, final char m0, final char m1, final char s0, final char s1) {
        if (h0 == '0') {
            if (h1 < '0' || h1 > '9') {
                return false;
            }
        }
        else if (h0 == '1') {
            if (h1 < '0' || h1 > '9') {
                return false;
            }
        }
        else {
            if (h0 != '2') {
                return false;
            }
            if (h1 < '0' || h1 > '4') {
                return false;
            }
        }
        if (m0 >= '0' && m0 <= '5') {
            if (m1 < '0' || m1 > '9') {
                return false;
            }
        }
        else {
            if (m0 != '6') {
                return false;
            }
            if (m1 != '0') {
                return false;
            }
        }
        if (s0 >= '0' && s0 <= '5') {
            if (s1 < '0' || s1 > '9') {
                return false;
            }
        }
        else {
            if (s0 != '6') {
                return false;
            }
            if (s1 != '0') {
                return false;
            }
        }
        return true;
    }
    
    private void setCalendar(final char y0, final char y1, final char y2, final char y3, final char M0, final char M1, final char d0, final char d1) {
        this.calendar = Calendar.getInstance(this.timeZone, this.locale);
        final int year = (y0 - '0') * 1000 + (y1 - '0') * 100 + (y2 - '0') * 10 + (y3 - '0');
        final int month = (M0 - '0') * 10 + (M1 - '0') - 1;
        final int day = (d0 - '0') * 10 + (d1 - '0');
        this.calendar.set(1, year);
        this.calendar.set(2, month);
        this.calendar.set(5, day);
    }
    
    static boolean checkDate(final char y0, final char y1, final char y2, final char y3, final char M0, final char M1, final int d0, final int d1) {
        if (y0 < '0' || y0 > '9') {
            return false;
        }
        if (y1 < '0' || y1 > '9') {
            return false;
        }
        if (y2 < '0' || y2 > '9') {
            return false;
        }
        if (y3 < '0' || y3 > '9') {
            return false;
        }
        if (M0 == '0') {
            if (M1 < '1' || M1 > '9') {
                return false;
            }
        }
        else {
            if (M0 != '1') {
                return false;
            }
            if (M1 != '0' && M1 != '1' && M1 != '2') {
                return false;
            }
        }
        if (d0 == 48) {
            if (d1 < 49 || d1 > 57) {
                return false;
            }
        }
        else if (d0 == 49 || d0 == 50) {
            if (d1 < 48 || d1 > 57) {
                return false;
            }
        }
        else {
            if (d0 != 51) {
                return false;
            }
            if (d1 != 48 && d1 != 49) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean isEOF() {
        return this.bp == this.len || (this.ch == '\u001a' && this.bp + 1 >= this.len);
    }
    
    @Override
    public int scanFieldInt(final char[] fieldName) {
        this.matchStat = 0;
        final int startPos = this.bp;
        final char startChar = this.ch;
        if (!charArrayCompare(this.text, this.bp, fieldName)) {
            this.matchStat = -2;
            return 0;
        }
        int index = this.bp + fieldName.length;
        char ch = this.charAt(index++);
        final boolean quote = ch == '\"';
        if (quote) {
            ch = this.charAt(index++);
        }
        final boolean negative = ch == '-';
        if (negative) {
            ch = this.charAt(index++);
        }
        if (ch < '0' || ch > '9') {
            this.matchStat = -1;
            return 0;
        }
        int value = ch - '0';
    Label_0564_Outer:
        while (true) {
            ch = this.charAt(index++);
            if (ch >= '0' && ch <= '9') {
                final int value_10 = value * 10;
                if (value_10 < value) {
                    this.matchStat = -1;
                    return 0;
                }
                value = value_10 + (ch - '0');
            }
            else {
                if (ch == '.') {
                    this.matchStat = -1;
                    return 0;
                }
                if (value < 0) {
                    this.matchStat = -1;
                    return 0;
                }
                if (quote) {
                    if (ch != '\"') {
                        this.matchStat = -1;
                        return 0;
                    }
                    ch = this.charAt(index++);
                }
                while (ch != ',' && ch != '}') {
                    if (!JSONLexerBase.isWhitespace(ch)) {
                        this.matchStat = -1;
                        return 0;
                    }
                    ch = this.charAt(index++);
                }
                this.bp = index - 1;
                if (ch == ',') {
                    this.ch = this.charAt(++this.bp);
                    this.matchStat = 3;
                    this.token = 16;
                    return negative ? (-value) : value;
                }
                if (ch == '}') {
                    this.bp = index - 1;
                    ch = this.charAt(++this.bp);
                    while (true) {
                        while (ch != ',') {
                            if (ch == ']') {
                                this.token = 15;
                                this.ch = this.charAt(++this.bp);
                            }
                            else if (ch == '}') {
                                this.token = 13;
                                this.ch = this.charAt(++this.bp);
                            }
                            else if (ch == '\u001a') {
                                this.token = 20;
                            }
                            else {
                                if (JSONLexerBase.isWhitespace(ch)) {
                                    ch = this.charAt(++this.bp);
                                    continue Label_0564_Outer;
                                }
                                this.bp = startPos;
                                this.ch = startChar;
                                this.matchStat = -1;
                                return 0;
                            }
                            this.matchStat = 4;
                            return negative ? (-value) : value;
                        }
                        this.token = 16;
                        this.ch = this.charAt(++this.bp);
                        continue;
                    }
                }
                return negative ? (-value) : value;
            }
        }
    }
    
    @Override
    public String scanFieldString(final char[] fieldName) {
        this.matchStat = 0;
        final int startPos = this.bp;
        final char startChar = this.ch;
        while (!charArrayCompare(this.text, this.bp, fieldName)) {
            if (!JSONLexerBase.isWhitespace(this.ch)) {
                this.matchStat = -2;
                return this.stringDefaultValue();
            }
            this.next();
        }
        int index = this.bp + fieldName.length;
        char ch = this.charAt(index++);
        if (ch != '\"') {
            this.matchStat = -1;
            return this.stringDefaultValue();
        }
        final int startIndex = index;
        int endIndex = this.indexOf('\"', startIndex);
        if (endIndex == -1) {
            throw new JSONException("unclosed str");
        }
        String stringVal = this.subString(startIndex, endIndex - startIndex);
        if (stringVal.indexOf(92) != -1) {
            while (true) {
                int slashCount = 0;
                for (int i = endIndex - 1; i >= 0 && this.charAt(i) == '\\'; --i) {
                    ++slashCount;
                }
                if (slashCount % 2 == 0) {
                    break;
                }
                endIndex = this.indexOf('\"', endIndex + 1);
            }
            final int chars_len = endIndex - (this.bp + fieldName.length + 1);
            final char[] chars = this.sub_chars(this.bp + fieldName.length + 1, chars_len);
            stringVal = JSONLexerBase.readString(chars, chars_len);
        }
        for (ch = this.charAt(endIndex + 1); ch != ',' && ch != '}'; ch = this.charAt(endIndex + 1)) {
            if (!JSONLexerBase.isWhitespace(ch)) {
                this.matchStat = -1;
                return this.stringDefaultValue();
            }
            ++endIndex;
        }
        this.bp = endIndex + 1;
        this.ch = ch;
        final String strVal = stringVal;
        if (ch == ',') {
            this.ch = this.charAt(++this.bp);
            this.matchStat = 3;
            return strVal;
        }
        ch = this.charAt(++this.bp);
        if (ch == ',') {
            this.token = 16;
            this.ch = this.charAt(++this.bp);
        }
        else if (ch == ']') {
            this.token = 15;
            this.ch = this.charAt(++this.bp);
        }
        else if (ch == '}') {
            this.token = 13;
            this.ch = this.charAt(++this.bp);
        }
        else {
            if (ch != '\u001a') {
                this.bp = startPos;
                this.ch = startChar;
                this.matchStat = -1;
                return this.stringDefaultValue();
            }
            this.token = 20;
        }
        this.matchStat = 4;
        return strVal;
    }
    
    @Override
    public Date scanFieldDate(final char[] fieldName) {
        this.matchStat = 0;
        final int startPos = this.bp;
        final char startChar = this.ch;
        if (!charArrayCompare(this.text, this.bp, fieldName)) {
            this.matchStat = -2;
            return null;
        }
        int index = this.bp + fieldName.length;
        char ch = this.charAt(index++);
        Date dateVal;
        if (ch == '\"') {
            final int startIndex = index;
            int endIndex = this.indexOf('\"', startIndex);
            if (endIndex == -1) {
                throw new JSONException("unclosed str");
            }
            final int rest = endIndex - startIndex;
            this.bp = index;
            if (!this.scanISO8601DateIfMatch(false, rest)) {
                this.bp = startPos;
                this.matchStat = -1;
                return null;
            }
            dateVal = this.calendar.getTime();
            ch = this.charAt(endIndex + 1);
            this.bp = startPos;
            while (ch != ',' && ch != '}') {
                if (!JSONLexerBase.isWhitespace(ch)) {
                    this.matchStat = -1;
                    return null;
                }
                ++endIndex;
                ch = this.charAt(endIndex + 1);
            }
            this.bp = endIndex + 1;
            this.ch = ch;
        }
        else {
            if (ch != '-' && (ch < '0' || ch > '9')) {
                this.matchStat = -1;
                return null;
            }
            long millis = 0L;
            boolean negative = false;
            if (ch == '-') {
                ch = this.charAt(index++);
                negative = true;
            }
            if (ch >= '0' && ch <= '9') {
                millis = ch - '0';
                while (true) {
                    ch = this.charAt(index++);
                    if (ch < '0' || ch > '9') {
                        break;
                    }
                    millis = millis * 10L + (ch - '0');
                }
                if (ch == ',' || ch == '}') {
                    this.bp = index - 1;
                }
            }
            if (millis < 0L) {
                this.matchStat = -1;
                return null;
            }
            if (negative) {
                millis = -millis;
            }
            dateVal = new Date(millis);
        }
        if (ch == ',') {
            this.ch = this.charAt(++this.bp);
            this.matchStat = 3;
            this.token = 16;
            return dateVal;
        }
        ch = this.charAt(++this.bp);
        if (ch == ',') {
            this.token = 16;
            this.ch = this.charAt(++this.bp);
        }
        else if (ch == ']') {
            this.token = 15;
            this.ch = this.charAt(++this.bp);
        }
        else if (ch == '}') {
            this.token = 13;
            this.ch = this.charAt(++this.bp);
        }
        else {
            if (ch != '\u001a') {
                this.bp = startPos;
                this.ch = startChar;
                this.matchStat = -1;
                return null;
            }
            this.token = 20;
        }
        this.matchStat = 4;
        return dateVal;
    }
    
    @Override
    public long scanFieldSymbol(final char[] fieldName) {
        this.matchStat = 0;
        if (!charArrayCompare(this.text, this.bp, fieldName)) {
            this.matchStat = -2;
            return 0L;
        }
        int index = this.bp + fieldName.length;
        char ch = this.charAt(index++);
        if (ch != '\"') {
            this.matchStat = -1;
            return 0L;
        }
        long hash = -3750763034362895579L;
        while (true) {
            ch = this.charAt(index++);
            if (ch == '\"') {
                this.bp = index;
                for (ch = (this.ch = this.charAt(this.bp)); ch != ','; ch = this.charAt(++this.bp)) {
                    if (ch == '}') {
                        this.next();
                        this.skipWhitespace();
                        ch = this.getCurrent();
                        if (ch == ',') {
                            this.token = 16;
                            this.ch = this.charAt(++this.bp);
                        }
                        else if (ch == ']') {
                            this.token = 15;
                            this.ch = this.charAt(++this.bp);
                        }
                        else if (ch == '}') {
                            this.token = 13;
                            this.ch = this.charAt(++this.bp);
                        }
                        else {
                            if (ch != '\u001a') {
                                this.matchStat = -1;
                                return 0L;
                            }
                            this.token = 20;
                        }
                        this.matchStat = 4;
                        return hash;
                    }
                    if (!JSONLexerBase.isWhitespace(ch)) {
                        this.matchStat = -1;
                        return 0L;
                    }
                }
                this.ch = this.charAt(++this.bp);
                this.matchStat = 3;
                return hash;
            }
            if (index > this.len) {
                this.matchStat = -1;
                return 0L;
            }
            hash ^= ch;
            hash *= 1099511628211L;
        }
    }
    
    @Override
    public Collection<String> scanFieldStringArray(final char[] fieldName, final Class<?> type) {
        this.matchStat = 0;
        while (this.ch == '\n' || this.ch == ' ') {
            final int index = ++this.bp;
            this.ch = ((index >= this.len) ? '\u001a' : this.text.charAt(index));
        }
        if (!charArrayCompare(this.text, this.bp, fieldName)) {
            this.matchStat = -2;
            return null;
        }
        Collection<String> list = this.newCollectionByType(type);
        final int startPos = this.bp;
        final char startChar = this.ch;
        int index2 = this.bp + fieldName.length;
        char ch = this.charAt(index2++);
        if (ch == '[') {
            ch = this.charAt(index2++);
            while (true) {
                if (ch == '\"') {
                    final int startIndex = index2;
                    int endIndex = this.indexOf('\"', startIndex);
                    if (endIndex == -1) {
                        throw new JSONException("unclosed str");
                    }
                    String stringVal = this.subString(startIndex, endIndex - startIndex);
                    if (stringVal.indexOf(92) != -1) {
                        while (true) {
                            int slashCount = 0;
                            for (int i = endIndex - 1; i >= 0 && this.charAt(i) == '\\'; --i) {
                                ++slashCount;
                            }
                            if (slashCount % 2 == 0) {
                                break;
                            }
                            endIndex = this.indexOf('\"', endIndex + 1);
                        }
                        final int chars_len = endIndex - startIndex;
                        final char[] chars = this.sub_chars(startIndex, chars_len);
                        stringVal = JSONLexerBase.readString(chars, chars_len);
                    }
                    index2 = endIndex + 1;
                    ch = this.charAt(index2++);
                    list.add(stringVal);
                }
                else if (ch == 'n' && this.text.startsWith("ull", index2)) {
                    index2 += 3;
                    ch = this.charAt(index2++);
                    list.add(null);
                }
                else {
                    if (ch == ']' && list.size() == 0) {
                        ch = this.charAt(index2++);
                        break;
                    }
                    this.matchStat = -1;
                    return null;
                }
                if (ch == ',') {
                    ch = this.charAt(index2++);
                }
                else {
                    if (ch == ']') {
                        for (ch = this.charAt(index2++); JSONLexerBase.isWhitespace(ch); ch = this.charAt(index2++)) {}
                        break;
                    }
                    this.matchStat = -1;
                    return null;
                }
            }
        }
        else {
            if (!this.text.startsWith("ull", index2)) {
                this.matchStat = -1;
                return null;
            }
            index2 += 3;
            ch = this.charAt(index2++);
            list = null;
        }
        this.bp = index2;
        if (ch == ',') {
            this.ch = this.charAt(this.bp);
            this.matchStat = 3;
            return list;
        }
        if (ch == '}') {
            ch = this.charAt(this.bp);
            while (true) {
                while (ch != ',') {
                    if (ch == ']') {
                        this.token = 15;
                        this.ch = this.charAt(++this.bp);
                    }
                    else if (ch == '}') {
                        this.token = 13;
                        this.ch = this.charAt(++this.bp);
                    }
                    else if (ch == '\u001a') {
                        this.token = 20;
                        this.ch = ch;
                    }
                    else {
                        boolean space = false;
                        while (JSONLexerBase.isWhitespace(ch)) {
                            ch = this.charAt(index2++);
                            this.bp = index2;
                            space = true;
                        }
                        if (space) {
                            continue;
                        }
                        this.matchStat = -1;
                        return null;
                    }
                    this.matchStat = 4;
                    return list;
                }
                this.token = 16;
                this.ch = this.charAt(++this.bp);
                continue;
            }
        }
        this.ch = startChar;
        this.bp = startPos;
        this.matchStat = -1;
        return null;
    }
    
    @Override
    public long scanFieldLong(final char[] fieldName) {
        this.matchStat = 0;
        final int startPos = this.bp;
        final char startChar = this.ch;
        if (!charArrayCompare(this.text, this.bp, fieldName)) {
            this.matchStat = -2;
            return 0L;
        }
        int index = this.bp + fieldName.length;
        char ch = this.charAt(index++);
        final boolean quote = ch == '\"';
        if (quote) {
            ch = this.charAt(index++);
        }
        boolean negative = false;
        if (ch == '-') {
            ch = this.charAt(index++);
            negative = true;
        }
        if (ch < '0' || ch > '9') {
            this.bp = startPos;
            this.ch = startChar;
            this.matchStat = -1;
            return 0L;
        }
        long value = ch - '0';
        while (true) {
            ch = this.charAt(index++);
            if (ch < '0' || ch > '9') {
                break;
            }
            value = value * 10L + (ch - '0');
        }
        if (ch == '.') {
            this.matchStat = -1;
            return 0L;
        }
        if (quote) {
            if (ch != '\"') {
                this.matchStat = -1;
                return 0L;
            }
            ch = this.charAt(index++);
        }
        if (ch == ',' || ch == '}') {
            this.bp = index - 1;
        }
        final boolean valid = value >= 0L || (value == Long.MIN_VALUE && negative);
        if (!valid) {
            this.bp = startPos;
            this.ch = startChar;
            this.matchStat = -1;
            return 0L;
        }
    Label_0557_Outer:
        while (ch != ',') {
            if (ch == '}') {
                ch = this.charAt(++this.bp);
                while (true) {
                    while (ch != ',') {
                        if (ch == ']') {
                            this.token = 15;
                            this.ch = this.charAt(++this.bp);
                        }
                        else if (ch == '}') {
                            this.token = 13;
                            this.ch = this.charAt(++this.bp);
                        }
                        else if (ch == '\u001a') {
                            this.token = 20;
                        }
                        else {
                            if (JSONLexerBase.isWhitespace(ch)) {
                                ch = this.charAt(++this.bp);
                                continue Label_0557_Outer;
                            }
                            this.bp = startPos;
                            this.ch = startChar;
                            this.matchStat = -1;
                            return 0L;
                        }
                        this.matchStat = 4;
                        return negative ? (-value) : value;
                    }
                    this.token = 16;
                    this.ch = this.charAt(++this.bp);
                    continue;
                }
            }
            if (!JSONLexerBase.isWhitespace(ch)) {
                this.matchStat = -1;
                return 0L;
            }
            this.bp = index;
            ch = this.charAt(index++);
        }
        this.ch = this.charAt(++this.bp);
        this.matchStat = 3;
        this.token = 16;
        return negative ? (-value) : value;
    }
    
    @Override
    public boolean scanFieldBoolean(final char[] fieldName) {
        this.matchStat = 0;
        if (!charArrayCompare(this.text, this.bp, fieldName)) {
            this.matchStat = -2;
            return false;
        }
        final int startPos = this.bp;
        int index = this.bp + fieldName.length;
        char ch = this.charAt(index++);
        final boolean quote = ch == '\"';
        if (quote) {
            ch = this.charAt(index++);
        }
        boolean value;
        if (ch == 't') {
            if (this.charAt(index++) != 'r') {
                this.matchStat = -1;
                return false;
            }
            if (this.charAt(index++) != 'u') {
                this.matchStat = -1;
                return false;
            }
            if (this.charAt(index++) != 'e') {
                this.matchStat = -1;
                return false;
            }
            if (quote && this.charAt(index++) != '\"') {
                this.matchStat = -1;
                return false;
            }
            this.bp = index;
            ch = this.charAt(this.bp);
            value = true;
        }
        else if (ch == 'f') {
            if (this.charAt(index++) != 'a') {
                this.matchStat = -1;
                return false;
            }
            if (this.charAt(index++) != 'l') {
                this.matchStat = -1;
                return false;
            }
            if (this.charAt(index++) != 's') {
                this.matchStat = -1;
                return false;
            }
            if (this.charAt(index++) != 'e') {
                this.matchStat = -1;
                return false;
            }
            if (quote && this.charAt(index++) != '\"') {
                this.matchStat = -1;
                return false;
            }
            this.bp = index;
            ch = this.charAt(this.bp);
            value = false;
        }
        else if (ch == '1') {
            if (quote && this.charAt(index++) != '\"') {
                this.matchStat = -1;
                return false;
            }
            this.bp = index;
            ch = this.charAt(this.bp);
            value = true;
        }
        else {
            if (ch != '0') {
                this.matchStat = -1;
                return false;
            }
            if (quote && this.charAt(index++) != '\"') {
                this.matchStat = -1;
                return false;
            }
            this.bp = index;
            ch = this.charAt(this.bp);
            value = false;
        }
    Label_0659_Outer:
        while (ch != ',') {
            if (ch == '}') {
                ch = this.charAt(++this.bp);
                while (true) {
                    while (ch != ',') {
                        if (ch == ']') {
                            this.token = 15;
                            this.ch = this.charAt(++this.bp);
                        }
                        else if (ch == '}') {
                            this.token = 13;
                            this.ch = this.charAt(++this.bp);
                        }
                        else if (ch == '\u001a') {
                            this.token = 20;
                        }
                        else {
                            if (JSONLexerBase.isWhitespace(ch)) {
                                ch = this.charAt(++this.bp);
                                continue Label_0659_Outer;
                            }
                            this.matchStat = -1;
                            return false;
                        }
                        this.matchStat = 4;
                        return value;
                    }
                    this.token = 16;
                    this.ch = this.charAt(++this.bp);
                    continue;
                }
            }
            if (!JSONLexerBase.isWhitespace(ch)) {
                this.bp = startPos;
                ch = this.charAt(this.bp);
                this.matchStat = -1;
                return false;
            }
            ch = this.charAt(++this.bp);
        }
        this.ch = this.charAt(++this.bp);
        this.matchStat = 3;
        this.token = 16;
        return value;
    }
    
    @Override
    public final int scanInt(final char expectNext) {
        this.matchStat = 0;
        final int mark = this.bp;
        int offset;
        char chLocal;
        for (offset = this.bp, chLocal = this.charAt(offset++); JSONLexerBase.isWhitespace(chLocal); chLocal = this.charAt(offset++)) {}
        final boolean quote = chLocal == '\"';
        if (quote) {
            chLocal = this.charAt(offset++);
        }
        final boolean negative = chLocal == '-';
        if (negative) {
            chLocal = this.charAt(offset++);
        }
        if (chLocal >= '0' && chLocal <= '9') {
            int value = chLocal - '0';
            while (true) {
                chLocal = this.charAt(offset++);
                if (chLocal >= '0' && chLocal <= '9') {
                    final int value_10 = value * 10;
                    if (value_10 < value) {
                        throw new JSONException("parseInt error : " + this.subString(mark, offset - 1));
                    }
                    value = value_10 + (chLocal - '0');
                }
                else {
                    if (chLocal == '.') {
                        this.matchStat = -1;
                        return 0;
                    }
                    if (quote) {
                        if (chLocal != '\"') {
                            this.matchStat = -1;
                            return 0;
                        }
                        chLocal = this.charAt(offset++);
                    }
                    if (value < 0) {
                        this.matchStat = -1;
                        return 0;
                    }
                    while (chLocal != expectNext) {
                        if (!JSONLexerBase.isWhitespace(chLocal)) {
                            this.matchStat = -1;
                            return negative ? (-value) : value;
                        }
                        chLocal = this.charAt(offset++);
                    }
                    this.bp = offset;
                    this.ch = this.charAt(this.bp);
                    this.matchStat = 3;
                    this.token = 16;
                    return negative ? (-value) : value;
                }
            }
        }
        else {
            if (chLocal == 'n' && this.charAt(offset++) == 'u' && this.charAt(offset++) == 'l' && this.charAt(offset++) == 'l') {
                this.matchStat = 5;
                final int value = 0;
                chLocal = this.charAt(offset++);
                if (quote && chLocal == '\"') {
                    chLocal = this.charAt(offset++);
                }
                while (chLocal != ',') {
                    if (chLocal == ']') {
                        this.bp = offset;
                        this.ch = this.charAt(this.bp);
                        this.matchStat = 5;
                        this.token = 15;
                        return value;
                    }
                    if (!JSONLexerBase.isWhitespace(chLocal)) {
                        this.matchStat = -1;
                        return 0;
                    }
                    chLocal = this.charAt(offset++);
                }
                this.bp = offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 5;
                this.token = 16;
                return value;
            }
            this.matchStat = -1;
            return 0;
        }
    }
    
    @Override
    public double scanDouble(final char seperator) {
        this.matchStat = 0;
        int offset = this.bp;
        char chLocal = this.charAt(offset++);
        final boolean quote = chLocal == '\"';
        if (quote) {
            chLocal = this.charAt(offset++);
        }
        final boolean negative = chLocal == '-';
        if (negative) {
            chLocal = this.charAt(offset++);
        }
        if (chLocal >= '0' && chLocal <= '9') {
            long intVal = chLocal - '0';
            while (true) {
                chLocal = this.charAt(offset++);
                if (chLocal < '0' || chLocal > '9') {
                    break;
                }
                intVal = intVal * 10L + (chLocal - '0');
            }
            long power = 1L;
            final boolean small = chLocal == '.';
            if (small) {
                chLocal = this.charAt(offset++);
                if (chLocal < '0' || chLocal > '9') {
                    this.matchStat = -1;
                    return 0.0;
                }
                intVal = intVal * 10L + (chLocal - '0');
                power = 10L;
                while (true) {
                    chLocal = this.charAt(offset++);
                    if (chLocal < '0' || chLocal > '9') {
                        break;
                    }
                    intVal = intVal * 10L + (chLocal - '0');
                    power *= 10L;
                }
            }
            final boolean exp = chLocal == 'e' || chLocal == 'E';
            if (exp) {
                chLocal = this.charAt(offset++);
                if (chLocal == '+' || chLocal == '-') {
                    chLocal = this.charAt(offset++);
                }
                while (chLocal >= '0' && chLocal <= '9') {
                    chLocal = this.charAt(offset++);
                }
            }
            int start;
            int count;
            if (quote) {
                if (chLocal != '\"') {
                    this.matchStat = -1;
                    return 0.0;
                }
                chLocal = this.charAt(offset++);
                start = this.bp + 1;
                count = offset - start - 2;
            }
            else {
                start = this.bp;
                count = offset - start - 1;
            }
            double value;
            if (!exp && count < 18) {
                value = intVal / (double)power;
                if (negative) {
                    value = -value;
                }
            }
            else {
                final String text = this.subString(start, count);
                value = Double.parseDouble(text);
            }
            if (chLocal == seperator) {
                this.bp = offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 3;
                this.token = 16;
                return value;
            }
            this.matchStat = -1;
            return value;
        }
        else {
            if (chLocal == 'n' && this.charAt(offset++) == 'u' && this.charAt(offset++) == 'l' && this.charAt(offset++) == 'l') {
                this.matchStat = 5;
                final double value = 0.0;
                chLocal = this.charAt(offset++);
                if (quote && chLocal == '\"') {
                    chLocal = this.charAt(offset++);
                }
                while (chLocal != ',') {
                    if (chLocal == ']') {
                        this.bp = offset;
                        this.ch = this.charAt(this.bp);
                        this.matchStat = 5;
                        this.token = 15;
                        return value;
                    }
                    if (!JSONLexerBase.isWhitespace(chLocal)) {
                        this.matchStat = -1;
                        return 0.0;
                    }
                    chLocal = this.charAt(offset++);
                }
                this.bp = offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 5;
                this.token = 16;
                return value;
            }
            this.matchStat = -1;
            return 0.0;
        }
    }
    
    @Override
    public long scanLong(final char seperator) {
        this.matchStat = 0;
        int offset = this.bp;
        char chLocal = this.charAt(offset++);
        final boolean quote = chLocal == '\"';
        if (quote) {
            chLocal = this.charAt(offset++);
        }
        final boolean negative = chLocal == '-';
        if (negative) {
            chLocal = this.charAt(offset++);
        }
        if (chLocal >= '0' && chLocal <= '9') {
            long value = chLocal - '0';
            while (true) {
                chLocal = this.charAt(offset++);
                if (chLocal < '0' || chLocal > '9') {
                    break;
                }
                value = value * 10L + (chLocal - '0');
            }
            if (chLocal == '.') {
                this.matchStat = -1;
                return 0L;
            }
            if (quote) {
                if (chLocal != '\"') {
                    this.matchStat = -1;
                    return 0L;
                }
                chLocal = this.charAt(offset++);
            }
            final boolean valid = value >= 0L || (value == Long.MIN_VALUE && negative);
            if (!valid) {
                this.matchStat = -1;
                return 0L;
            }
            while (chLocal != seperator) {
                if (!JSONLexerBase.isWhitespace(chLocal)) {
                    this.matchStat = -1;
                    return value;
                }
                chLocal = this.charAt(offset++);
            }
            this.bp = offset;
            this.ch = this.charAt(this.bp);
            this.matchStat = 3;
            this.token = 16;
            return negative ? (-value) : value;
        }
        else {
            if (chLocal == 'n' && this.charAt(offset++) == 'u' && this.charAt(offset++) == 'l' && this.charAt(offset++) == 'l') {
                this.matchStat = 5;
                final long value = 0L;
                chLocal = this.charAt(offset++);
                if (quote && chLocal == '\"') {
                    chLocal = this.charAt(offset++);
                }
                while (chLocal != ',') {
                    if (chLocal == ']') {
                        this.bp = offset;
                        this.ch = this.charAt(this.bp);
                        this.matchStat = 5;
                        this.token = 15;
                        return value;
                    }
                    if (!JSONLexerBase.isWhitespace(chLocal)) {
                        this.matchStat = -1;
                        return 0L;
                    }
                    chLocal = this.charAt(offset++);
                }
                this.bp = offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 5;
                this.token = 16;
                return value;
            }
            this.matchStat = -1;
            return 0L;
        }
    }
    
    @Override
    public Date scanDate(final char seperator) {
        this.matchStat = 0;
        final int startPos = this.bp;
        final char startChar = this.ch;
        int index = this.bp;
        char ch = this.charAt(index++);
        Date dateVal;
        if (ch == '\"') {
            final int startIndex = index;
            int endIndex = this.indexOf('\"', startIndex);
            if (endIndex == -1) {
                throw new JSONException("unclosed str");
            }
            final int rest = endIndex - startIndex;
            this.bp = index;
            if (!this.scanISO8601DateIfMatch(false, rest)) {
                this.bp = startPos;
                this.ch = startChar;
                this.matchStat = -1;
                return null;
            }
            dateVal = this.calendar.getTime();
            ch = this.charAt(endIndex + 1);
            this.bp = startPos;
            while (ch != ',' && ch != ']') {
                if (!JSONLexerBase.isWhitespace(ch)) {
                    this.bp = startPos;
                    this.ch = startChar;
                    this.matchStat = -1;
                    return null;
                }
                ++endIndex;
                ch = this.charAt(endIndex + 1);
            }
            this.bp = endIndex + 1;
            this.ch = ch;
        }
        else if (ch == '-' || (ch >= '0' && ch <= '9')) {
            long millis = 0L;
            boolean negative = false;
            if (ch == '-') {
                ch = this.charAt(index++);
                negative = true;
            }
            if (ch >= '0' && ch <= '9') {
                millis = ch - '0';
                while (true) {
                    ch = this.charAt(index++);
                    if (ch < '0' || ch > '9') {
                        break;
                    }
                    millis = millis * 10L + (ch - '0');
                }
                if (ch == ',' || ch == ']') {
                    this.bp = index - 1;
                }
            }
            if (millis < 0L) {
                this.bp = startPos;
                this.ch = startChar;
                this.matchStat = -1;
                return null;
            }
            if (negative) {
                millis = -millis;
            }
            dateVal = new Date(millis);
        }
        else {
            if (ch != 'n' || this.charAt(index++) != 'u' || this.charAt(index++) != 'l' || this.charAt(index++) != 'l') {
                this.bp = startPos;
                this.ch = startChar;
                this.matchStat = -1;
                return null;
            }
            dateVal = null;
            ch = this.charAt(index);
            this.bp = index;
        }
        if (ch == ',') {
            this.ch = this.charAt(++this.bp);
            this.matchStat = 3;
            return dateVal;
        }
        ch = this.charAt(++this.bp);
        if (ch == ',') {
            this.token = 16;
            this.ch = this.charAt(++this.bp);
        }
        else if (ch == ']') {
            this.token = 15;
            this.ch = this.charAt(++this.bp);
        }
        else if (ch == '}') {
            this.token = 13;
            this.ch = this.charAt(++this.bp);
        }
        else {
            if (ch != '\u001a') {
                this.bp = startPos;
                this.ch = startChar;
                this.matchStat = -1;
                return null;
            }
            this.ch = '\u001a';
            this.token = 20;
        }
        this.matchStat = 4;
        return dateVal;
    }
    
    @Override
    protected final void arrayCopy(final int srcPos, final char[] dest, final int destPos, final int length) {
        this.text.getChars(srcPos, srcPos + length, dest, destPos);
    }
    
    @Override
    public String info() {
        final StringBuilder buf = new StringBuilder();
        int line = 1;
        int column = 1;
        for (int i = 0; i < this.bp; ++i, ++column) {
            final char ch = this.text.charAt(i);
            if (ch == '\n') {
                column = 1;
                ++line;
            }
        }
        buf.append("pos ").append(this.bp).append(", line ").append(line).append(", column ").append(column);
        if (this.text.length() < 65535) {
            buf.append(this.text);
        }
        else {
            buf.append(this.text.substring(0, 65535));
        }
        return buf.toString();
    }
    
    @Override
    public String[] scanFieldStringArray(final char[] fieldName, final int argTypesCount, final SymbolTable typeSymbolTable) {
        final int startPos = this.bp;
        final char starChar = this.ch;
        while (JSONLexerBase.isWhitespace(this.ch)) {
            this.next();
        }
        int offset;
        char ch;
        if (fieldName != null) {
            this.matchStat = 0;
            if (!this.charArrayCompare(fieldName)) {
                this.matchStat = -2;
                return null;
            }
            for (offset = this.bp + fieldName.length, ch = this.text.charAt(offset++); JSONLexerBase.isWhitespace(ch); ch = this.text.charAt(offset++)) {}
            if (ch != ':') {
                this.matchStat = -1;
                return null;
            }
            for (ch = this.text.charAt(offset++); JSONLexerBase.isWhitespace(ch); ch = this.text.charAt(offset++)) {}
        }
        else {
            offset = this.bp + 1;
            ch = this.ch;
        }
        if (ch == '[') {
            this.bp = offset;
            this.ch = this.text.charAt(this.bp);
            String[] types = (argTypesCount >= 0) ? new String[argTypesCount] : new String[4];
            int typeIndex = 0;
            while (true) {
                if (JSONLexerBase.isWhitespace(this.ch)) {
                    this.next();
                }
                else {
                    if (this.ch != '\"') {
                        this.bp = startPos;
                        this.ch = starChar;
                        this.matchStat = -1;
                        return null;
                    }
                    final String type = this.scanSymbol(typeSymbolTable, '\"');
                    if (typeIndex == types.length) {
                        final int newCapacity = types.length + (types.length >> 1) + 1;
                        final String[] array = new String[newCapacity];
                        System.arraycopy(types, 0, array, 0, types.length);
                        types = array;
                    }
                    types[typeIndex++] = type;
                    while (JSONLexerBase.isWhitespace(this.ch)) {
                        this.next();
                    }
                    if (this.ch == ',') {
                        this.next();
                    }
                    else {
                        if (types.length != typeIndex) {
                            final String[] array2 = new String[typeIndex];
                            System.arraycopy(types, 0, array2, 0, typeIndex);
                            types = array2;
                        }
                        while (JSONLexerBase.isWhitespace(this.ch)) {
                            this.next();
                        }
                        if (this.ch == ']') {
                            this.next();
                            return types;
                        }
                        this.bp = startPos;
                        this.ch = starChar;
                        this.matchStat = -1;
                        return null;
                    }
                }
            }
        }
        else {
            if (ch == 'n' && this.text.startsWith("ull", this.bp + 1)) {
                this.bp += 4;
                this.ch = this.text.charAt(this.bp);
                return null;
            }
            this.matchStat = -1;
            return null;
        }
    }
    
    @Override
    public boolean matchField2(final char[] fieldName) {
        while (JSONLexerBase.isWhitespace(this.ch)) {
            this.next();
        }
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return false;
        }
        int offset;
        char ch;
        for (offset = this.bp + fieldName.length, ch = this.text.charAt(offset++); JSONLexerBase.isWhitespace(ch); ch = this.text.charAt(offset++)) {}
        if (ch == ':') {
            this.bp = offset;
            this.ch = this.charAt(this.bp);
            return true;
        }
        this.matchStat = -2;
        return false;
    }
    
    @Override
    public final void skipObject() {
        this.skipObject(false);
    }
    
    @Override
    public final void skipObject(final boolean valid) {
        boolean quote = false;
        int braceCnt = 0;
        int i;
        for (i = this.bp; i < this.text.length(); ++i) {
            final char ch = this.text.charAt(i);
            if (ch == '\\') {
                if (i >= this.len - 1) {
                    this.ch = ch;
                    this.bp = i;
                    throw new JSONException("illegal str, " + this.info());
                }
                ++i;
            }
            else if (ch == '\"') {
                quote = !quote;
            }
            else if (ch == '{') {
                if (!quote) {
                    ++braceCnt;
                }
            }
            else if (ch == '}') {
                if (!quote) {
                    if (--braceCnt == -1) {
                        this.bp = i + 1;
                        if (this.bp == this.text.length()) {
                            this.ch = '\u001a';
                            this.token = 20;
                            return;
                        }
                        this.ch = this.text.charAt(this.bp);
                        if (this.ch == ',') {
                            this.token = 16;
                            final int index = ++this.bp;
                            this.ch = ((index >= this.text.length()) ? '\u001a' : this.text.charAt(index));
                            return;
                        }
                        if (this.ch == '}') {
                            this.token = 13;
                            this.next();
                            return;
                        }
                        if (this.ch == ']') {
                            this.token = 15;
                            this.next();
                            return;
                        }
                        this.nextToken(16);
                        return;
                    }
                }
            }
        }
        for (int j = 0; j < this.bp; ++j) {
            if (j < this.text.length() && this.text.charAt(j) == ' ') {
                ++i;
            }
        }
        if (i == this.text.length()) {
            throw new JSONException("illegal str, " + this.info());
        }
    }
    
    @Override
    public final void skipArray() {
        this.skipArray(false);
    }
    
    public final void skipArray(final boolean valid) {
        boolean quote = false;
        int bracketCnt = 0;
        int i;
        for (i = this.bp; i < this.text.length(); ++i) {
            final char ch = this.text.charAt(i);
            if (ch == '\\') {
                if (i >= this.len - 1) {
                    this.ch = ch;
                    this.bp = i;
                    throw new JSONException("illegal str, " + this.info());
                }
                ++i;
            }
            else if (ch == '\"') {
                quote = !quote;
            }
            else if (ch == '[') {
                if (!quote) {
                    ++bracketCnt;
                }
            }
            else if (ch == '{' && valid) {
                final int index = ++this.bp;
                this.ch = ((index >= this.text.length()) ? '\u001a' : this.text.charAt(index));
                this.skipObject(valid);
            }
            else if (ch == ']') {
                if (!quote) {
                    if (--bracketCnt == -1) {
                        this.bp = i + 1;
                        if (this.bp == this.text.length()) {
                            this.ch = '\u001a';
                            this.token = 20;
                            return;
                        }
                        this.ch = this.text.charAt(this.bp);
                        this.nextToken(16);
                        return;
                    }
                }
            }
        }
        if (i == this.text.length()) {
            throw new JSONException("illegal str, " + this.info());
        }
    }
    
    public final void skipString() {
        if (this.ch == '\"') {
            for (int i = this.bp + 1; i < this.text.length(); ++i) {
                final char c = this.text.charAt(i);
                if (c == '\\') {
                    if (i < this.len - 1) {
                        ++i;
                    }
                }
                else if (c == '\"') {
                    final String text = this.text;
                    final int bp = i + 1;
                    this.bp = bp;
                    this.ch = text.charAt(bp);
                    return;
                }
            }
            throw new JSONException("unclosed str");
        }
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean seekArrayToItem(final int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must > 0, but " + index);
        }
        if (this.token == 20) {
            return false;
        }
        if (this.token != 14) {
            throw new UnsupportedOperationException();
        }
        for (int i = 0; i < index; ++i) {
            this.skipWhitespace();
            if (this.ch == '\"' || this.ch == '\'') {
                this.skipString();
                if (this.ch == ',') {
                    this.next();
                }
                else {
                    if (this.ch == ']') {
                        this.next();
                        this.nextToken(16);
                        return false;
                    }
                    throw new JSONException("illegal json.");
                }
            }
            else {
                if (this.ch == '{') {
                    this.next();
                    this.token = 12;
                    this.skipObject(false);
                }
                else if (this.ch == '[') {
                    this.next();
                    this.token = 14;
                    this.skipArray(false);
                }
                else {
                    boolean match = false;
                    for (int j = this.bp + 1; j < this.text.length(); ++j) {
                        final char c = this.text.charAt(j);
                        if (c == ',') {
                            match = true;
                            this.bp = j + 1;
                            this.ch = this.charAt(this.bp);
                            break;
                        }
                        if (c == ']') {
                            this.bp = j + 1;
                            this.ch = this.charAt(this.bp);
                            this.nextToken();
                            return false;
                        }
                    }
                    if (!match) {
                        throw new JSONException("illegal json.");
                    }
                    continue;
                }
                if (this.token != 16) {
                    if (this.token == 15) {
                        return false;
                    }
                    throw new UnsupportedOperationException();
                }
            }
        }
        this.nextToken();
        return true;
    }
    
    @Override
    public int seekObjectToField(final long fieldNameHash, final boolean deepScan) {
        if (this.token == 20) {
            return -1;
        }
        if (this.token == 13 || this.token == 15) {
            this.nextToken();
            return -1;
        }
        if (this.token != 12 && this.token != 16) {
            throw new UnsupportedOperationException(JSONToken.name(this.token));
        }
        while (this.ch != '}') {
            if (this.ch == '\u001a') {
                return -1;
            }
            if (this.ch != '\"') {
                this.skipWhitespace();
            }
            if (this.ch != '\"') {
                throw new UnsupportedOperationException();
            }
            long hash = -3750763034362895579L;
            for (int i = this.bp + 1; i < this.text.length(); ++i) {
                char c = this.text.charAt(i);
                if (c == '\\') {
                    if (++i == this.text.length()) {
                        throw new JSONException("unclosed str, " + this.info());
                    }
                    c = this.text.charAt(i);
                }
                if (c == '\"') {
                    this.bp = i + 1;
                    this.ch = ((this.bp >= this.text.length()) ? '\u001a' : this.text.charAt(this.bp));
                    break;
                }
                hash ^= c;
                hash *= 1099511628211L;
            }
            if (hash == fieldNameHash) {
                if (this.ch != ':') {
                    this.skipWhitespace();
                }
                if (this.ch == ':') {
                    int index = ++this.bp;
                    this.ch = ((index >= this.text.length()) ? '\u001a' : this.text.charAt(index));
                    if (this.ch == ',') {
                        index = ++this.bp;
                        this.ch = ((index >= this.text.length()) ? '\u001a' : this.text.charAt(index));
                        this.token = 16;
                    }
                    else if (this.ch == ']') {
                        index = ++this.bp;
                        this.ch = ((index >= this.text.length()) ? '\u001a' : this.text.charAt(index));
                        this.token = 15;
                    }
                    else if (this.ch == '}') {
                        index = ++this.bp;
                        this.ch = ((index >= this.text.length()) ? '\u001a' : this.text.charAt(index));
                        this.token = 13;
                    }
                    else if (this.ch >= '0' && this.ch <= '9') {
                        this.sp = 0;
                        this.pos = this.bp;
                        this.scanNumber();
                    }
                    else {
                        this.nextToken(2);
                    }
                }
                return 3;
            }
            if (this.ch != ':') {
                this.skipWhitespace();
            }
            if (this.ch != ':') {
                throw new JSONException("illegal json, " + this.info());
            }
            int index = ++this.bp;
            this.ch = ((index >= this.text.length()) ? '\u001a' : this.text.charAt(index));
            if (this.ch != '\"' && this.ch != '\'' && this.ch != '{' && this.ch != '[' && this.ch != '0' && this.ch != '1' && this.ch != '2' && this.ch != '3' && this.ch != '4' && this.ch != '5' && this.ch != '6' && this.ch != '7' && this.ch != '8' && this.ch != '9' && this.ch != '+' && this.ch != '-') {
                this.skipWhitespace();
            }
            if (this.ch == '-' || this.ch == '+' || (this.ch >= '0' && this.ch <= '9')) {
                this.next();
                while (this.ch >= '0' && this.ch <= '9') {
                    this.next();
                }
                if (this.ch == '.') {
                    this.next();
                    while (this.ch >= '0' && this.ch <= '9') {
                        this.next();
                    }
                }
                if (this.ch == 'E' || this.ch == 'e') {
                    this.next();
                    if (this.ch == '-' || this.ch == '+') {
                        this.next();
                    }
                    while (this.ch >= '0' && this.ch <= '9') {
                        this.next();
                    }
                }
                if (this.ch != ',') {
                    this.skipWhitespace();
                }
                if (this.ch != ',') {
                    continue;
                }
                this.next();
            }
            else if (this.ch == '\"') {
                this.skipString();
                if (this.ch != ',' && this.ch != '}') {
                    this.skipWhitespace();
                }
                if (this.ch != ',') {
                    continue;
                }
                this.next();
            }
            else if (this.ch == 't') {
                this.next();
                if (this.ch == 'r') {
                    this.next();
                    if (this.ch == 'u') {
                        this.next();
                        if (this.ch == 'e') {
                            this.next();
                        }
                    }
                }
                if (this.ch != ',' && this.ch != '}') {
                    this.skipWhitespace();
                }
                if (this.ch != ',') {
                    continue;
                }
                this.next();
            }
            else if (this.ch == 'n') {
                this.next();
                if (this.ch == 'u') {
                    this.next();
                    if (this.ch == 'l') {
                        this.next();
                        if (this.ch == 'l') {
                            this.next();
                        }
                    }
                }
                if (this.ch != ',' && this.ch != '}') {
                    this.skipWhitespace();
                }
                if (this.ch != ',') {
                    continue;
                }
                this.next();
            }
            else if (this.ch == 'f') {
                this.next();
                if (this.ch == 'a') {
                    this.next();
                    if (this.ch == 'l') {
                        this.next();
                        if (this.ch == 's') {
                            this.next();
                            if (this.ch == 'e') {
                                this.next();
                            }
                        }
                    }
                }
                if (this.ch != ',' && this.ch != '}') {
                    this.skipWhitespace();
                }
                if (this.ch != ',') {
                    continue;
                }
                this.next();
            }
            else if (this.ch == '{') {
                index = ++this.bp;
                this.ch = ((index >= this.text.length()) ? '\u001a' : this.text.charAt(index));
                if (deepScan) {
                    this.token = 12;
                    return 1;
                }
                this.skipObject(false);
                if (this.token == 13) {
                    return -1;
                }
                continue;
            }
            else {
                if (this.ch != '[') {
                    throw new UnsupportedOperationException();
                }
                this.next();
                if (deepScan) {
                    this.token = 14;
                    return 2;
                }
                this.skipArray(false);
                if (this.token == 13) {
                    return -1;
                }
                continue;
            }
        }
        this.next();
        this.nextToken();
        return -1;
    }
    
    @Override
    public int seekObjectToField(final long[] fieldNameHash) {
        if (this.token != 12 && this.token != 16) {
            throw new UnsupportedOperationException();
        }
        while (this.ch != '}') {
            if (this.ch == '\u001a') {
                return this.matchStat = -1;
            }
            if (this.ch != '\"') {
                this.skipWhitespace();
            }
            if (this.ch != '\"') {
                throw new UnsupportedOperationException();
            }
            long hash = -3750763034362895579L;
            for (int i = this.bp + 1; i < this.text.length(); ++i) {
                char c = this.text.charAt(i);
                if (c == '\\') {
                    if (++i == this.text.length()) {
                        throw new JSONException("unclosed str, " + this.info());
                    }
                    c = this.text.charAt(i);
                }
                if (c == '\"') {
                    this.bp = i + 1;
                    this.ch = ((this.bp >= this.text.length()) ? '\u001a' : this.text.charAt(this.bp));
                    break;
                }
                hash ^= c;
                hash *= 1099511628211L;
            }
            int matchIndex = -1;
            for (int j = 0; j < fieldNameHash.length; ++j) {
                if (hash == fieldNameHash[j]) {
                    matchIndex = j;
                    break;
                }
            }
            if (matchIndex != -1) {
                if (this.ch != ':') {
                    this.skipWhitespace();
                }
                if (this.ch == ':') {
                    int index = ++this.bp;
                    this.ch = ((index >= this.text.length()) ? '\u001a' : this.text.charAt(index));
                    if (this.ch == ',') {
                        index = ++this.bp;
                        this.ch = ((index >= this.text.length()) ? '\u001a' : this.text.charAt(index));
                        this.token = 16;
                    }
                    else if (this.ch == ']') {
                        index = ++this.bp;
                        this.ch = ((index >= this.text.length()) ? '\u001a' : this.text.charAt(index));
                        this.token = 15;
                    }
                    else if (this.ch == '}') {
                        index = ++this.bp;
                        this.ch = ((index >= this.text.length()) ? '\u001a' : this.text.charAt(index));
                        this.token = 13;
                    }
                    else if (this.ch >= '0' && this.ch <= '9') {
                        this.sp = 0;
                        this.pos = this.bp;
                        this.scanNumber();
                    }
                    else {
                        this.nextToken(2);
                    }
                }
                this.matchStat = 3;
                return matchIndex;
            }
            if (this.ch != ':') {
                this.skipWhitespace();
            }
            if (this.ch != ':') {
                throw new JSONException("illegal json, " + this.info());
            }
            int index = ++this.bp;
            this.ch = ((index >= this.text.length()) ? '\u001a' : this.text.charAt(index));
            if (this.ch != '\"' && this.ch != '\'' && this.ch != '{' && this.ch != '[' && this.ch != '0' && this.ch != '1' && this.ch != '2' && this.ch != '3' && this.ch != '4' && this.ch != '5' && this.ch != '6' && this.ch != '7' && this.ch != '8' && this.ch != '9' && this.ch != '+' && this.ch != '-') {
                this.skipWhitespace();
            }
            if (this.ch == '-' || this.ch == '+' || (this.ch >= '0' && this.ch <= '9')) {
                this.next();
                while (this.ch >= '0' && this.ch <= '9') {
                    this.next();
                }
                if (this.ch == '.') {
                    this.next();
                    while (this.ch >= '0' && this.ch <= '9') {
                        this.next();
                    }
                }
                if (this.ch == 'E' || this.ch == 'e') {
                    this.next();
                    if (this.ch == '-' || this.ch == '+') {
                        this.next();
                    }
                    while (this.ch >= '0' && this.ch <= '9') {
                        this.next();
                    }
                }
                if (this.ch != ',') {
                    this.skipWhitespace();
                }
                if (this.ch != ',') {
                    continue;
                }
                this.next();
            }
            else if (this.ch == '\"') {
                this.skipString();
                if (this.ch != ',' && this.ch != '}') {
                    this.skipWhitespace();
                }
                if (this.ch != ',') {
                    continue;
                }
                this.next();
            }
            else if (this.ch == '{') {
                index = ++this.bp;
                this.ch = ((index >= this.text.length()) ? '\u001a' : this.text.charAt(index));
                this.skipObject(false);
            }
            else {
                if (this.ch != '[') {
                    throw new UnsupportedOperationException();
                }
                this.next();
                this.skipArray(false);
            }
        }
        this.next();
        this.nextToken();
        return this.matchStat = -1;
    }
    
    @Override
    public String scanTypeName(final SymbolTable symbolTable) {
        if (this.text.startsWith("\"@type\":\"", this.bp)) {
            final int p = this.text.indexOf(34, this.bp + 9);
            if (p != -1) {
                this.bp += 9;
                int h = 0;
                for (int i = this.bp; i < p; ++i) {
                    h = 31 * h + this.text.charAt(i);
                }
                final String typeName = this.addSymbol(this.bp, p - this.bp, h, symbolTable);
                final char separator = this.text.charAt(p + 1);
                if (separator != ',' && separator != ']') {
                    return null;
                }
                this.bp = p + 2;
                this.ch = this.text.charAt(this.bp);
                return typeName;
            }
        }
        return null;
    }
}
