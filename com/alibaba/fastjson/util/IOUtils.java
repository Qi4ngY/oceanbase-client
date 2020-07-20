package com.alibaba.fastjson.util;

import java.util.Arrays;
import java.io.Reader;
import java.nio.charset.CoderResult;
import java.nio.charset.CharacterCodingException;
import com.alibaba.fastjson.JSONException;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.io.Closeable;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;

public class IOUtils
{
    public static final String FASTJSON_PROPERTIES = "fastjson.properties";
    public static final String FASTJSON_COMPATIBLEWITHJAVABEAN = "fastjson.compatibleWithJavaBean";
    public static final String FASTJSON_COMPATIBLEWITHFIELDNAME = "fastjson.compatibleWithFieldName";
    public static final Properties DEFAULT_PROPERTIES;
    public static final Charset UTF8;
    public static final char[] DIGITS;
    public static final boolean[] firstIdentifierFlags;
    public static final boolean[] identifierFlags;
    public static final byte[] specicalFlags_doubleQuotes;
    public static final byte[] specicalFlags_singleQuotes;
    public static final boolean[] specicalFlags_doubleQuotesFlags;
    public static final boolean[] specicalFlags_singleQuotesFlags;
    public static final char[] replaceChars;
    public static final char[] ASCII_CHARS;
    static final char[] digits;
    static final char[] DigitTens;
    static final char[] DigitOnes;
    static final int[] sizeTable;
    public static final char[] CA;
    public static final int[] IA;
    
    public static String getStringProperty(final String name) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        }
        catch (SecurityException ex) {}
        return (prop == null) ? IOUtils.DEFAULT_PROPERTIES.getProperty(name) : prop;
    }
    
    public static void loadPropertiesFromFile() {
        final InputStream imputStream = AccessController.doPrivileged((PrivilegedAction<InputStream>)new PrivilegedAction<InputStream>() {
            @Override
            public InputStream run() {
                final ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl != null) {
                    return cl.getResourceAsStream("fastjson.properties");
                }
                return ClassLoader.getSystemResourceAsStream("fastjson.properties");
            }
        });
        if (null != imputStream) {
            try {
                IOUtils.DEFAULT_PROPERTIES.load(imputStream);
                imputStream.close();
            }
            catch (IOException ex) {}
        }
    }
    
    public static void close(final Closeable x) {
        if (x != null) {
            try {
                x.close();
            }
            catch (Exception ex) {}
        }
    }
    
    public static int stringSize(final long x) {
        long p = 10L;
        for (int i = 1; i < 19; ++i) {
            if (x < p) {
                return i;
            }
            p *= 10L;
        }
        return 19;
    }
    
    public static void getChars(long i, final int index, final char[] buf) {
        int charPos = index;
        char sign = '\0';
        if (i < 0L) {
            sign = '-';
            i = -i;
        }
        while (i > 2147483647L) {
            final long q = i / 100L;
            final int r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = IOUtils.DigitOnes[r];
            buf[--charPos] = IOUtils.DigitTens[r];
        }
        int r;
        int i2;
        int q2;
        for (i2 = (int)i; i2 >= 65536; i2 = q2, buf[--charPos] = IOUtils.DigitOnes[r], buf[--charPos] = IOUtils.DigitTens[r]) {
            q2 = i2 / 100;
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
        }
        do {
            q2 = i2 * 52429 >>> 19;
            r = i2 - ((q2 << 3) + (q2 << 1));
            buf[--charPos] = IOUtils.digits[r];
            i2 = q2;
        } while (i2 != 0);
        if (sign != '\0') {
            buf[--charPos] = sign;
        }
    }
    
    public static void getChars(int i, final int index, final char[] buf) {
        int p = index;
        char sign = '\0';
        if (i < 0) {
            sign = '-';
            i = -i;
        }
        while (i >= 65536) {
            final int q = i / 100;
            final int r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf[--p] = IOUtils.DigitOnes[r];
            buf[--p] = IOUtils.DigitTens[r];
        }
        do {
            final int q = i * 52429 >>> 19;
            final int r = i - ((q << 3) + (q << 1));
            buf[--p] = IOUtils.digits[r];
            i = q;
        } while (i != 0);
        if (sign != '\0') {
            buf[--p] = sign;
        }
    }
    
    public static void getChars(final byte b, final int index, final char[] buf) {
        int i = b;
        int charPos = index;
        char sign = '\0';
        if (i < 0) {
            sign = '-';
            i = -i;
        }
        do {
            final int q = i * 52429 >>> 19;
            final int r = i - ((q << 3) + (q << 1));
            buf[--charPos] = IOUtils.digits[r];
            i = q;
        } while (i != 0);
        if (sign != '\0') {
            buf[--charPos] = sign;
        }
    }
    
    public static int stringSize(final int x) {
        int i;
        for (i = 0; x > IOUtils.sizeTable[i]; ++i) {}
        return i + 1;
    }
    
    public static void decode(final CharsetDecoder charsetDecoder, final ByteBuffer byteBuf, final CharBuffer charByte) {
        try {
            CoderResult cr = charsetDecoder.decode(byteBuf, charByte, true);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
            cr = charsetDecoder.flush(charByte);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
        }
        catch (CharacterCodingException x) {
            throw new JSONException("utf8 decode error, " + x.getMessage(), x);
        }
    }
    
    public static boolean firstIdentifier(final char ch) {
        return ch < IOUtils.firstIdentifierFlags.length && IOUtils.firstIdentifierFlags[ch];
    }
    
    public static boolean isIdent(final char ch) {
        return ch < IOUtils.identifierFlags.length && IOUtils.identifierFlags[ch];
    }
    
    public static byte[] decodeBase64(final char[] chars, final int offset, final int charsLen) {
        if (charsLen == 0) {
            return new byte[0];
        }
        int sIx;
        int eIx;
        for (sIx = offset, eIx = offset + charsLen - 1; sIx < eIx && IOUtils.IA[chars[sIx]] < 0; ++sIx) {}
        while (eIx > 0 && IOUtils.IA[chars[eIx]] < 0) {
            --eIx;
        }
        final int pad = (chars[eIx] == '=') ? ((chars[eIx - 1] == '=') ? 2 : 1) : 0;
        final int cCnt = eIx - sIx + 1;
        final int sepCnt = (charsLen > 76) ? (((chars[76] == '\r') ? (cCnt / 78) : 0) << 1) : 0;
        final int len = ((cCnt - sepCnt) * 6 >> 3) - pad;
        final byte[] bytes = new byte[len];
        int d = 0;
        int cc = 0;
        final int eLen = len / 3 * 3;
        while (d < eLen) {
            final int i = IOUtils.IA[chars[sIx++]] << 18 | IOUtils.IA[chars[sIx++]] << 12 | IOUtils.IA[chars[sIx++]] << 6 | IOUtils.IA[chars[sIx++]];
            bytes[d++] = (byte)(i >> 16);
            bytes[d++] = (byte)(i >> 8);
            bytes[d++] = (byte)i;
            if (sepCnt > 0 && ++cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }
        if (d < len) {
            int j = 0;
            for (int k = 0; sIx <= eIx - pad; j |= IOUtils.IA[chars[sIx++]] << 18 - k * 6, ++k) {}
            for (int r = 16; d < len; bytes[d++] = (byte)(j >> r), r -= 8) {}
        }
        return bytes;
    }
    
    public static byte[] decodeBase64(final String chars, final int offset, final int charsLen) {
        if (charsLen == 0) {
            return new byte[0];
        }
        int sIx;
        int eIx;
        for (sIx = offset, eIx = offset + charsLen - 1; sIx < eIx && IOUtils.IA[chars.charAt(sIx)] < 0; ++sIx) {}
        while (eIx > 0 && IOUtils.IA[chars.charAt(eIx)] < 0) {
            --eIx;
        }
        final int pad = (chars.charAt(eIx) == '=') ? ((chars.charAt(eIx - 1) == '=') ? 2 : 1) : 0;
        final int cCnt = eIx - sIx + 1;
        final int sepCnt = (charsLen > 76) ? (((chars.charAt(76) == '\r') ? (cCnt / 78) : 0) << 1) : 0;
        final int len = ((cCnt - sepCnt) * 6 >> 3) - pad;
        final byte[] bytes = new byte[len];
        int d = 0;
        int cc = 0;
        final int eLen = len / 3 * 3;
        while (d < eLen) {
            final int i = IOUtils.IA[chars.charAt(sIx++)] << 18 | IOUtils.IA[chars.charAt(sIx++)] << 12 | IOUtils.IA[chars.charAt(sIx++)] << 6 | IOUtils.IA[chars.charAt(sIx++)];
            bytes[d++] = (byte)(i >> 16);
            bytes[d++] = (byte)(i >> 8);
            bytes[d++] = (byte)i;
            if (sepCnt > 0 && ++cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }
        if (d < len) {
            int j = 0;
            for (int k = 0; sIx <= eIx - pad; j |= IOUtils.IA[chars.charAt(sIx++)] << 18 - k * 6, ++k) {}
            for (int r = 16; d < len; bytes[d++] = (byte)(j >> r), r -= 8) {}
        }
        return bytes;
    }
    
    public static byte[] decodeBase64(final String s) {
        final int sLen = s.length();
        if (sLen == 0) {
            return new byte[0];
        }
        int sIx;
        int eIx;
        for (sIx = 0, eIx = sLen - 1; sIx < eIx && IOUtils.IA[s.charAt(sIx) & '\u00ff'] < 0; ++sIx) {}
        while (eIx > 0 && IOUtils.IA[s.charAt(eIx) & '\u00ff'] < 0) {
            --eIx;
        }
        final int pad = (s.charAt(eIx) == '=') ? ((s.charAt(eIx - 1) == '=') ? 2 : 1) : 0;
        final int cCnt = eIx - sIx + 1;
        final int sepCnt = (sLen > 76) ? (((s.charAt(76) == '\r') ? (cCnt / 78) : 0) << 1) : 0;
        final int len = ((cCnt - sepCnt) * 6 >> 3) - pad;
        final byte[] dArr = new byte[len];
        int d = 0;
        int cc = 0;
        final int eLen = len / 3 * 3;
        while (d < eLen) {
            final int i = IOUtils.IA[s.charAt(sIx++)] << 18 | IOUtils.IA[s.charAt(sIx++)] << 12 | IOUtils.IA[s.charAt(sIx++)] << 6 | IOUtils.IA[s.charAt(sIx++)];
            dArr[d++] = (byte)(i >> 16);
            dArr[d++] = (byte)(i >> 8);
            dArr[d++] = (byte)i;
            if (sepCnt > 0 && ++cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }
        if (d < len) {
            int j = 0;
            for (int k = 0; sIx <= eIx - pad; j |= IOUtils.IA[s.charAt(sIx++)] << 18 - k * 6, ++k) {}
            for (int r = 16; d < len; dArr[d++] = (byte)(j >> r), r -= 8) {}
        }
        return dArr;
    }
    
    public static int encodeUTF8(final char[] chars, int offset, final int len, final byte[] bytes) {
        final int sl = offset + len;
        int dp = 0;
        for (int dlASCII = dp + Math.min(len, bytes.length); dp < dlASCII && chars[offset] < '\u0080'; bytes[dp++] = (byte)chars[offset++]) {}
        while (offset < sl) {
            final char c = chars[offset++];
            if (c < '\u0080') {
                bytes[dp++] = (byte)c;
            }
            else if (c < '\u0800') {
                bytes[dp++] = (byte)(0xC0 | c >> 6);
                bytes[dp++] = (byte)(0x80 | (c & '?'));
            }
            else if (c >= '\ud800' && c < '\ue000') {
                final int ip = offset - 1;
                int uc;
                if (c >= '\ud800' && c < '\udc00') {
                    if (sl - ip < 2) {
                        uc = -1;
                    }
                    else {
                        final char d = chars[ip + 1];
                        if (d < '\udc00' || d >= '\ue000') {
                            bytes[dp++] = 63;
                            continue;
                        }
                        uc = (c << 10) + d - 56613888;
                    }
                }
                else {
                    if (c >= '\udc00' && c < '\ue000') {
                        bytes[dp++] = 63;
                        continue;
                    }
                    uc = c;
                }
                if (uc < 0) {
                    bytes[dp++] = 63;
                }
                else {
                    bytes[dp++] = (byte)(0xF0 | uc >> 18);
                    bytes[dp++] = (byte)(0x80 | (uc >> 12 & 0x3F));
                    bytes[dp++] = (byte)(0x80 | (uc >> 6 & 0x3F));
                    bytes[dp++] = (byte)(0x80 | (uc & 0x3F));
                    ++offset;
                }
            }
            else {
                bytes[dp++] = (byte)(0xE0 | c >> 12);
                bytes[dp++] = (byte)(0x80 | (c >> 6 & 0x3F));
                bytes[dp++] = (byte)(0x80 | (c & '?'));
            }
        }
        return dp;
    }
    
    @Deprecated
    public static int decodeUTF8(final byte[] sa, int sp, final int len, final char[] da) {
        final int sl = sp + len;
        int dp = 0;
        for (int dlASCII = Math.min(len, da.length); dp < dlASCII && sa[sp] >= 0; da[dp++] = (char)sa[sp++]) {}
        while (sp < sl) {
            final int b1 = sa[sp++];
            if (b1 >= 0) {
                da[dp++] = (char)b1;
            }
            else if (b1 >> 5 == -2 && (b1 & 0x1E) != 0x0) {
                if (sp >= sl) {
                    return -1;
                }
                final int b2 = sa[sp++];
                if ((b2 & 0xC0) != 0x80) {
                    return -1;
                }
                da[dp++] = (char)(b1 << 6 ^ b2 ^ 0xF80);
            }
            else if (b1 >> 4 == -2) {
                if (sp + 1 >= sl) {
                    return -1;
                }
                final int b2 = sa[sp++];
                final int b3 = sa[sp++];
                if ((b1 == -32 && (b2 & 0xE0) == 0x80) || (b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80) {
                    return -1;
                }
                final char c = (char)(b1 << 12 ^ b2 << 6 ^ (b3 ^ 0xFFFE1F80));
                final boolean isSurrogate = c >= '\ud800' && c < '\ue000';
                if (isSurrogate) {
                    return -1;
                }
                da[dp++] = c;
            }
            else {
                if (b1 >> 3 != -2) {
                    return -1;
                }
                if (sp + 2 >= sl) {
                    return -1;
                }
                final int b2 = sa[sp++];
                final int b3 = sa[sp++];
                final int b4 = sa[sp++];
                final int uc = b1 << 18 ^ b2 << 12 ^ b3 << 6 ^ (b4 ^ 0x381F80);
                if ((b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80 || (b4 & 0xC0) != 0x80 || uc < 65536 || uc >= 1114112) {
                    return -1;
                }
                da[dp++] = (char)((uc >>> 10) + 55232);
                da[dp++] = (char)((uc & 0x3FF) + 56320);
            }
        }
        return dp;
    }
    
    @Deprecated
    public static String readAll(final Reader reader) {
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
        return buf.toString();
    }
    
    public static boolean isValidJsonpQueryParam(final String value) {
        if (value == null || value.length() == 0) {
            return false;
        }
        for (int i = 0, len = value.length(); i < len; ++i) {
            final char ch = value.charAt(i);
            if (ch != '.' && !isIdent(ch)) {
                return false;
            }
        }
        return true;
    }
    
    static {
        DEFAULT_PROPERTIES = new Properties();
        UTF8 = Charset.forName("UTF-8");
        DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        firstIdentifierFlags = new boolean[256];
        identifierFlags = new boolean[256];
        for (char c = '\0'; c < IOUtils.firstIdentifierFlags.length; ++c) {
            if (c >= 'A' && c <= 'Z') {
                IOUtils.firstIdentifierFlags[c] = true;
            }
            else if (c >= 'a' && c <= 'z') {
                IOUtils.firstIdentifierFlags[c] = true;
            }
            else if (c == '_' || c == '$') {
                IOUtils.firstIdentifierFlags[c] = true;
            }
        }
        for (char c = '\0'; c < IOUtils.identifierFlags.length; ++c) {
            if (c >= 'A' && c <= 'Z') {
                IOUtils.identifierFlags[c] = true;
            }
            else if (c >= 'a' && c <= 'z') {
                IOUtils.identifierFlags[c] = true;
            }
            else if (c == '_') {
                IOUtils.identifierFlags[c] = true;
            }
            else if (c >= '0' && c <= '9') {
                IOUtils.identifierFlags[c] = true;
            }
        }
        try {
            loadPropertiesFromFile();
        }
        catch (Throwable t) {}
        specicalFlags_doubleQuotes = new byte[161];
        specicalFlags_singleQuotes = new byte[161];
        specicalFlags_doubleQuotesFlags = new boolean[161];
        specicalFlags_singleQuotesFlags = new boolean[161];
        replaceChars = new char[93];
        IOUtils.specicalFlags_doubleQuotes[0] = 4;
        IOUtils.specicalFlags_doubleQuotes[1] = 4;
        IOUtils.specicalFlags_doubleQuotes[2] = 4;
        IOUtils.specicalFlags_doubleQuotes[3] = 4;
        IOUtils.specicalFlags_doubleQuotes[4] = 4;
        IOUtils.specicalFlags_doubleQuotes[5] = 4;
        IOUtils.specicalFlags_doubleQuotes[6] = 4;
        IOUtils.specicalFlags_doubleQuotes[7] = 4;
        IOUtils.specicalFlags_doubleQuotes[8] = 1;
        IOUtils.specicalFlags_doubleQuotes[9] = 1;
        IOUtils.specicalFlags_doubleQuotes[10] = 1;
        IOUtils.specicalFlags_doubleQuotes[11] = 4;
        IOUtils.specicalFlags_doubleQuotes[12] = 1;
        IOUtils.specicalFlags_doubleQuotes[13] = 1;
        IOUtils.specicalFlags_doubleQuotes[34] = 1;
        IOUtils.specicalFlags_doubleQuotes[92] = 1;
        IOUtils.specicalFlags_singleQuotes[0] = 4;
        IOUtils.specicalFlags_singleQuotes[1] = 4;
        IOUtils.specicalFlags_singleQuotes[2] = 4;
        IOUtils.specicalFlags_singleQuotes[3] = 4;
        IOUtils.specicalFlags_singleQuotes[4] = 4;
        IOUtils.specicalFlags_singleQuotes[5] = 4;
        IOUtils.specicalFlags_singleQuotes[6] = 4;
        IOUtils.specicalFlags_singleQuotes[7] = 4;
        IOUtils.specicalFlags_singleQuotes[8] = 1;
        IOUtils.specicalFlags_singleQuotes[9] = 1;
        IOUtils.specicalFlags_singleQuotes[10] = 1;
        IOUtils.specicalFlags_singleQuotes[11] = 4;
        IOUtils.specicalFlags_singleQuotes[12] = 1;
        IOUtils.specicalFlags_singleQuotes[13] = 1;
        IOUtils.specicalFlags_singleQuotes[92] = 1;
        IOUtils.specicalFlags_singleQuotes[39] = 1;
        for (int i = 14; i <= 31; ++i) {
            IOUtils.specicalFlags_doubleQuotes[i] = 4;
            IOUtils.specicalFlags_singleQuotes[i] = 4;
        }
        for (int i = 127; i < 160; ++i) {
            IOUtils.specicalFlags_doubleQuotes[i] = 4;
            IOUtils.specicalFlags_singleQuotes[i] = 4;
        }
        for (int i = 0; i < 161; ++i) {
            IOUtils.specicalFlags_doubleQuotesFlags[i] = (IOUtils.specicalFlags_doubleQuotes[i] != 0);
            IOUtils.specicalFlags_singleQuotesFlags[i] = (IOUtils.specicalFlags_singleQuotes[i] != 0);
        }
        IOUtils.replaceChars[0] = '0';
        IOUtils.replaceChars[1] = '1';
        IOUtils.replaceChars[2] = '2';
        IOUtils.replaceChars[3] = '3';
        IOUtils.replaceChars[4] = '4';
        IOUtils.replaceChars[5] = '5';
        IOUtils.replaceChars[6] = '6';
        IOUtils.replaceChars[7] = '7';
        IOUtils.replaceChars[8] = 'b';
        IOUtils.replaceChars[9] = 't';
        IOUtils.replaceChars[10] = 'n';
        IOUtils.replaceChars[11] = 'v';
        IOUtils.replaceChars[12] = 'f';
        IOUtils.replaceChars[13] = 'r';
        IOUtils.replaceChars[34] = '\"';
        IOUtils.replaceChars[39] = '\'';
        IOUtils.replaceChars[47] = '/';
        IOUtils.replaceChars[92] = '\\';
        ASCII_CHARS = new char[] { '0', '0', '0', '1', '0', '2', '0', '3', '0', '4', '0', '5', '0', '6', '0', '7', '0', '8', '0', '9', '0', 'A', '0', 'B', '0', 'C', '0', 'D', '0', 'E', '0', 'F', '1', '0', '1', '1', '1', '2', '1', '3', '1', '4', '1', '5', '1', '6', '1', '7', '1', '8', '1', '9', '1', 'A', '1', 'B', '1', 'C', '1', 'D', '1', 'E', '1', 'F', '2', '0', '2', '1', '2', '2', '2', '3', '2', '4', '2', '5', '2', '6', '2', '7', '2', '8', '2', '9', '2', 'A', '2', 'B', '2', 'C', '2', 'D', '2', 'E', '2', 'F' };
        digits = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
        DigitTens = new char[] { '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9' };
        DigitOnes = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
        sizeTable = new int[] { 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };
        CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
        Arrays.fill(IA = new int[256], -1);
        for (int i = 0, iS = IOUtils.CA.length; i < iS; ++i) {
            IOUtils.IA[IOUtils.CA[i]] = i;
        }
        IOUtils.IA[61] = 0;
    }
}
