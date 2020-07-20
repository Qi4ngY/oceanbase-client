package com.alibaba.fastjson.util;

import java.util.Arrays;

public class Base64
{
    public static final char[] CA;
    public static final int[] IA;
    
    public static byte[] decodeFast(final char[] chars, final int offset, final int charsLen) {
        if (charsLen == 0) {
            return new byte[0];
        }
        int sIx;
        int eIx;
        for (sIx = offset, eIx = offset + charsLen - 1; sIx < eIx && Base64.IA[chars[sIx]] < 0; ++sIx) {}
        while (eIx > 0 && Base64.IA[chars[eIx]] < 0) {
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
            final int i = Base64.IA[chars[sIx++]] << 18 | Base64.IA[chars[sIx++]] << 12 | Base64.IA[chars[sIx++]] << 6 | Base64.IA[chars[sIx++]];
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
            for (int k = 0; sIx <= eIx - pad; j |= Base64.IA[chars[sIx++]] << 18 - k * 6, ++k) {}
            for (int r = 16; d < len; bytes[d++] = (byte)(j >> r), r -= 8) {}
        }
        return bytes;
    }
    
    public static byte[] decodeFast(final String chars, final int offset, final int charsLen) {
        if (charsLen == 0) {
            return new byte[0];
        }
        int sIx;
        int eIx;
        for (sIx = offset, eIx = offset + charsLen - 1; sIx < eIx && Base64.IA[chars.charAt(sIx)] < 0; ++sIx) {}
        while (eIx > 0 && Base64.IA[chars.charAt(eIx)] < 0) {
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
            final int i = Base64.IA[chars.charAt(sIx++)] << 18 | Base64.IA[chars.charAt(sIx++)] << 12 | Base64.IA[chars.charAt(sIx++)] << 6 | Base64.IA[chars.charAt(sIx++)];
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
            for (int k = 0; sIx <= eIx - pad; j |= Base64.IA[chars.charAt(sIx++)] << 18 - k * 6, ++k) {}
            for (int r = 16; d < len; bytes[d++] = (byte)(j >> r), r -= 8) {}
        }
        return bytes;
    }
    
    public static byte[] decodeFast(final String s) {
        final int sLen = s.length();
        if (sLen == 0) {
            return new byte[0];
        }
        int sIx;
        int eIx;
        for (sIx = 0, eIx = sLen - 1; sIx < eIx && Base64.IA[s.charAt(sIx) & '\u00ff'] < 0; ++sIx) {}
        while (eIx > 0 && Base64.IA[s.charAt(eIx) & '\u00ff'] < 0) {
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
            final int i = Base64.IA[s.charAt(sIx++)] << 18 | Base64.IA[s.charAt(sIx++)] << 12 | Base64.IA[s.charAt(sIx++)] << 6 | Base64.IA[s.charAt(sIx++)];
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
            for (int k = 0; sIx <= eIx - pad; j |= Base64.IA[s.charAt(sIx++)] << 18 - k * 6, ++k) {}
            for (int r = 16; d < len; dArr[d++] = (byte)(j >> r), r -= 8) {}
        }
        return dArr;
    }
    
    static {
        CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
        Arrays.fill(IA = new int[256], -1);
        for (int i = 0, iS = Base64.CA.length; i < iS; ++i) {
            Base64.IA[Base64.CA[i]] = i;
        }
        Base64.IA[61] = 0;
    }
}
