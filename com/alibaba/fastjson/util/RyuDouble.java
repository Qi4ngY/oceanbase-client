package com.alibaba.fastjson.util;

import java.math.BigInteger;

public final class RyuDouble
{
    private static final int[][] POW5_SPLIT;
    private static final int[][] POW5_INV_SPLIT;
    
    public static String toString(final double value) {
        final char[] result = new char[24];
        final int len = toString(value, result, 0);
        return new String(result, 0, len);
    }
    
    public static int toString(final double value, final char[] result, final int off) {
        final long DOUBLE_MANTISSA_MASK = 4503599627370495L;
        final int DOUBLE_EXPONENT_MASK = 2047;
        final int DOUBLE_EXPONENT_BIAS = 1023;
        final long LOG10_5_NUMERATOR = 6989700L;
        final long LOG10_2_NUMERATOR = 3010299L;
        int index = off;
        if (Double.isNaN(value)) {
            result[index++] = 'N';
            result[index++] = 'a';
            result[index++] = 'N';
            return index - off;
        }
        if (value == Double.POSITIVE_INFINITY) {
            result[index++] = 'I';
            result[index++] = 'n';
            result[index++] = 'f';
            result[index++] = 'i';
            result[index++] = 'n';
            result[index++] = 'i';
            result[index++] = 't';
            result[index++] = 'y';
            return index - off;
        }
        if (value == Double.NEGATIVE_INFINITY) {
            result[index++] = '-';
            result[index++] = 'I';
            result[index++] = 'n';
            result[index++] = 'f';
            result[index++] = 'i';
            result[index++] = 'n';
            result[index++] = 'i';
            result[index++] = 't';
            result[index++] = 'y';
            return index - off;
        }
        final long bits = Double.doubleToLongBits(value);
        if (bits == 0L) {
            result[index++] = '0';
            result[index++] = '.';
            result[index++] = '0';
            return index - off;
        }
        if (bits == Long.MIN_VALUE) {
            result[index++] = '-';
            result[index++] = '0';
            result[index++] = '.';
            result[index++] = '0';
            return index - off;
        }
        final int DOUBLE_MANTISSA_BITS = 52;
        final int ieeeExponent = (int)(bits >>> 52 & 0x7FFL);
        final long ieeeMantissa = bits & 0xFFFFFFFFFFFFFL;
        int e2;
        long m2;
        if (ieeeExponent == 0) {
            e2 = -1074;
            m2 = ieeeMantissa;
        }
        else {
            e2 = ieeeExponent - 1023 - 52;
            m2 = (ieeeMantissa | 0x10000000000000L);
        }
        final boolean sign = bits < 0L;
        final boolean even = (m2 & 0x1L) == 0x0L;
        final long mv = 4L * m2;
        final long mp = 4L * m2 + 2L;
        final int mmShift = (m2 != 4503599627370496L || ieeeExponent <= 1) ? 1 : 0;
        final long mm = 4L * m2 - 1L - mmShift;
        e2 -= 2;
        boolean dmIsTrailingZeros = false;
        boolean dvIsTrailingZeros = false;
        long dv;
        long dp;
        long dm;
        int e3;
        if (e2 >= 0) {
            final int q = Math.max(0, (int)(e2 * 3010299L / 10000000L) - 1);
            final int k = 122 + ((q == 0) ? 1 : ((int)((q * 23219280L + 10000000L - 1L) / 10000000L))) - 1;
            final int i = -e2 + q + k;
            final int actualShift = i - 93 - 21;
            if (actualShift < 0) {
                throw new IllegalArgumentException("" + actualShift);
            }
            final int[] ints = RyuDouble.POW5_INV_SPLIT[q];
            long mHigh = mv >>> 31;
            long mLow = mv & 0x7FFFFFFFL;
            long bits2 = mHigh * ints[0];
            long bits3 = mLow * ints[0];
            long bits4 = mHigh * ints[1];
            long bits5 = mLow * ints[1];
            long bits6 = mHigh * ints[2];
            long bits7 = mLow * ints[2];
            long bits8 = mHigh * ints[3];
            long bits9 = mLow * ints[3];
            dv = ((((bits9 >>> 31) + bits7 + bits8 >>> 31) + bits5 + bits6 >>> 31) + bits3 + bits4 >>> 21) + (bits2 << 10) >>> actualShift;
            mHigh = mp >>> 31;
            mLow = (mp & 0x7FFFFFFFL);
            bits2 = mHigh * ints[0];
            bits3 = mLow * ints[0];
            bits4 = mHigh * ints[1];
            bits5 = mLow * ints[1];
            bits6 = mHigh * ints[2];
            bits7 = mLow * ints[2];
            bits8 = mHigh * ints[3];
            bits9 = mLow * ints[3];
            dp = ((((bits9 >>> 31) + bits7 + bits8 >>> 31) + bits5 + bits6 >>> 31) + bits3 + bits4 >>> 21) + (bits2 << 10) >>> actualShift;
            mHigh = mm >>> 31;
            mLow = (mm & 0x7FFFFFFFL);
            bits2 = mHigh * ints[0];
            bits3 = mLow * ints[0];
            bits4 = mHigh * ints[1];
            bits5 = mLow * ints[1];
            bits6 = mHigh * ints[2];
            bits7 = mLow * ints[2];
            bits8 = mHigh * ints[3];
            bits9 = mLow * ints[3];
            dm = ((((bits9 >>> 31) + bits7 + bits8 >>> 31) + bits5 + bits6 >>> 31) + bits3 + bits4 >>> 21) + (bits2 << 10) >>> actualShift;
            if ((e3 = q) <= 21) {
                if (mv % 5L == 0L) {
                    long v = mv;
                    int pow5Factor_mv;
                    if (v % 5L != 0L) {
                        pow5Factor_mv = 0;
                    }
                    else if (v % 25L != 0L) {
                        pow5Factor_mv = 1;
                    }
                    else if (v % 125L != 0L) {
                        pow5Factor_mv = 2;
                    }
                    else if (v % 625L != 0L) {
                        pow5Factor_mv = 3;
                    }
                    else {
                        for (pow5Factor_mv = 4, v /= 625L; v > 0L; v /= 5L, ++pow5Factor_mv) {
                            if (v % 5L != 0L) {
                                break;
                            }
                        }
                    }
                    dvIsTrailingZeros = (pow5Factor_mv >= q);
                }
                else if (even) {
                    long v = mm;
                    int pow5Factor_mm;
                    if (v % 5L != 0L) {
                        pow5Factor_mm = 0;
                    }
                    else if (v % 25L != 0L) {
                        pow5Factor_mm = 1;
                    }
                    else if (v % 125L != 0L) {
                        pow5Factor_mm = 2;
                    }
                    else if (v % 625L != 0L) {
                        pow5Factor_mm = 3;
                    }
                    else {
                        for (pow5Factor_mm = 4, v /= 625L; v > 0L; v /= 5L, ++pow5Factor_mm) {
                            if (v % 5L != 0L) {
                                break;
                            }
                        }
                    }
                    dmIsTrailingZeros = (pow5Factor_mm >= q);
                }
                else {
                    long v = mp;
                    int pow5Factor_mp;
                    if (v % 5L != 0L) {
                        pow5Factor_mp = 0;
                    }
                    else if (v % 25L != 0L) {
                        pow5Factor_mp = 1;
                    }
                    else if (v % 125L != 0L) {
                        pow5Factor_mp = 2;
                    }
                    else if (v % 625L != 0L) {
                        pow5Factor_mp = 3;
                    }
                    else {
                        for (pow5Factor_mp = 4, v /= 625L; v > 0L; v /= 5L, ++pow5Factor_mp) {
                            if (v % 5L != 0L) {
                                break;
                            }
                        }
                    }
                    if (pow5Factor_mp >= q) {
                        --dp;
                    }
                }
            }
        }
        else {
            final int q = Math.max(0, (int)(-e2 * 6989700L / 10000000L) - 1);
            final int j = -e2 - q;
            final int l = ((j == 0) ? 1 : ((int)((j * 23219280L + 10000000L - 1L) / 10000000L))) - 121;
            final int j2 = q - l;
            final int actualShift2 = j2 - 93 - 21;
            if (actualShift2 < 0) {
                throw new IllegalArgumentException("" + actualShift2);
            }
            final int[] ints2 = RyuDouble.POW5_SPLIT[j];
            long mHigh2 = mv >>> 31;
            long mLow2 = mv & 0x7FFFFFFFL;
            long bits10 = mHigh2 * ints2[0];
            long bits11 = mLow2 * ints2[0];
            long bits12 = mHigh2 * ints2[1];
            long bits13 = mLow2 * ints2[1];
            long bits14 = mHigh2 * ints2[2];
            long bits15 = mLow2 * ints2[2];
            long bits16 = mHigh2 * ints2[3];
            long bits17 = mLow2 * ints2[3];
            dv = ((((bits17 >>> 31) + bits15 + bits16 >>> 31) + bits13 + bits14 >>> 31) + bits11 + bits12 >>> 21) + (bits10 << 10) >>> actualShift2;
            mHigh2 = mp >>> 31;
            mLow2 = (mp & 0x7FFFFFFFL);
            bits10 = mHigh2 * ints2[0];
            bits11 = mLow2 * ints2[0];
            bits12 = mHigh2 * ints2[1];
            bits13 = mLow2 * ints2[1];
            bits14 = mHigh2 * ints2[2];
            bits15 = mLow2 * ints2[2];
            bits16 = mHigh2 * ints2[3];
            bits17 = mLow2 * ints2[3];
            dp = ((((bits17 >>> 31) + bits15 + bits16 >>> 31) + bits13 + bits14 >>> 31) + bits11 + bits12 >>> 21) + (bits10 << 10) >>> actualShift2;
            mHigh2 = mm >>> 31;
            mLow2 = (mm & 0x7FFFFFFFL);
            bits10 = mHigh2 * ints2[0];
            bits11 = mLow2 * ints2[0];
            bits12 = mHigh2 * ints2[1];
            bits13 = mLow2 * ints2[1];
            bits14 = mHigh2 * ints2[2];
            bits15 = mLow2 * ints2[2];
            bits16 = mHigh2 * ints2[3];
            bits17 = mLow2 * ints2[3];
            dm = ((((bits17 >>> 31) + bits15 + bits16 >>> 31) + bits13 + bits14 >>> 31) + bits11 + bits12 >>> 21) + (bits10 << 10) >>> actualShift2;
            e3 = q + e2;
            if (q <= 1) {
                dvIsTrailingZeros = true;
                if (even) {
                    dmIsTrailingZeros = (mmShift == 1);
                }
                else {
                    --dp;
                }
            }
            else if (q < 63) {
                dvIsTrailingZeros = ((mv & (1L << q - 1) - 1L) == 0x0L);
            }
        }
        int vplength;
        if (dp >= 1000000000000000000L) {
            vplength = 19;
        }
        else if (dp >= 100000000000000000L) {
            vplength = 18;
        }
        else if (dp >= 10000000000000000L) {
            vplength = 17;
        }
        else if (dp >= 1000000000000000L) {
            vplength = 16;
        }
        else if (dp >= 100000000000000L) {
            vplength = 15;
        }
        else if (dp >= 10000000000000L) {
            vplength = 14;
        }
        else if (dp >= 1000000000000L) {
            vplength = 13;
        }
        else if (dp >= 100000000000L) {
            vplength = 12;
        }
        else if (dp >= 10000000000L) {
            vplength = 11;
        }
        else if (dp >= 1000000000L) {
            vplength = 10;
        }
        else if (dp >= 100000000L) {
            vplength = 9;
        }
        else if (dp >= 10000000L) {
            vplength = 8;
        }
        else if (dp >= 1000000L) {
            vplength = 7;
        }
        else if (dp >= 100000L) {
            vplength = 6;
        }
        else if (dp >= 10000L) {
            vplength = 5;
        }
        else if (dp >= 1000L) {
            vplength = 4;
        }
        else if (dp >= 100L) {
            vplength = 3;
        }
        else if (dp >= 10L) {
            vplength = 2;
        }
        else {
            vplength = 1;
        }
        int exp = e3 + vplength - 1;
        final boolean scientificNotation = exp < -3 || exp >= 7;
        int removed = 0;
        int lastRemovedDigit = 0;
        long output;
        if (dmIsTrailingZeros || dvIsTrailingZeros) {
            while (dp / 10L > dm / 10L && (dp >= 100L || !scientificNotation)) {
                dmIsTrailingZeros &= (dm % 10L == 0L);
                dvIsTrailingZeros &= (lastRemovedDigit == 0);
                lastRemovedDigit = (int)(dv % 10L);
                dp /= 10L;
                dv /= 10L;
                dm /= 10L;
                ++removed;
            }
            if (dmIsTrailingZeros && even) {
                while (dm % 10L == 0L) {
                    if (dp < 100L && scientificNotation) {
                        break;
                    }
                    dvIsTrailingZeros &= (lastRemovedDigit == 0);
                    lastRemovedDigit = (int)(dv % 10L);
                    dp /= 10L;
                    dv /= 10L;
                    dm /= 10L;
                    ++removed;
                }
            }
            if (dvIsTrailingZeros && lastRemovedDigit == 5 && dv % 2L == 0L) {
                lastRemovedDigit = 4;
            }
            output = dv + (((dv == dm && (!dmIsTrailingZeros || !even)) || lastRemovedDigit >= 5) ? 1 : 0);
        }
        else {
            while (dp / 10L > dm / 10L && (dp >= 100L || !scientificNotation)) {
                lastRemovedDigit = (int)(dv % 10L);
                dp /= 10L;
                dv /= 10L;
                dm /= 10L;
                ++removed;
            }
            output = dv + ((dv == dm || lastRemovedDigit >= 5) ? 1 : 0);
        }
        final int olength = vplength - removed;
        if (sign) {
            result[index++] = '-';
        }
        if (scientificNotation) {
            for (int i2 = 0; i2 < olength - 1; ++i2) {
                final int c = (int)(output % 10L);
                output /= 10L;
                result[index + olength - i2] = (char)(48 + c);
            }
            result[index] = (char)(48L + output % 10L);
            result[index + 1] = '.';
            index += olength + 1;
            if (olength == 1) {
                result[index++] = '0';
            }
            result[index++] = 'E';
            if (exp < 0) {
                result[index++] = '-';
                exp = -exp;
            }
            if (exp >= 100) {
                result[index++] = (char)(48 + exp / 100);
                exp %= 100;
                result[index++] = (char)(48 + exp / 10);
            }
            else if (exp >= 10) {
                result[index++] = (char)(48 + exp / 10);
            }
            result[index++] = (char)(48 + exp % 10);
            return index - off;
        }
        if (exp < 0) {
            result[index++] = '0';
            result[index++] = '.';
            for (int i2 = -1; i2 > exp; --i2) {
                result[index++] = '0';
            }
            final int current = index;
            for (int i3 = 0; i3 < olength; ++i3) {
                result[current + olength - i3 - 1] = (char)(48L + output % 10L);
                output /= 10L;
                ++index;
            }
        }
        else if (exp + 1 >= olength) {
            for (int i2 = 0; i2 < olength; ++i2) {
                result[index + olength - i2 - 1] = (char)(48L + output % 10L);
                output /= 10L;
            }
            index += olength;
            for (int i2 = olength; i2 < exp + 1; ++i2) {
                result[index++] = '0';
            }
            result[index++] = '.';
            result[index++] = '0';
        }
        else {
            int current = index + 1;
            for (int i3 = 0; i3 < olength; ++i3) {
                if (olength - i3 - 1 == exp) {
                    result[current + olength - i3 - 1] = '.';
                    --current;
                }
                result[current + olength - i3 - 1] = (char)(48L + output % 10L);
                output /= 10L;
            }
            index += olength + 1;
        }
        return index - off;
    }
    
    static {
        POW5_SPLIT = new int[326][4];
        POW5_INV_SPLIT = new int[291][4];
        final BigInteger mask = BigInteger.ONE.shiftLeft(31).subtract(BigInteger.ONE);
        final BigInteger invMask = BigInteger.ONE.shiftLeft(31).subtract(BigInteger.ONE);
        for (int i = 0; i < 326; ++i) {
            final BigInteger pow = BigInteger.valueOf(5L).pow(i);
            final int pow5len = pow.bitLength();
            final int expectedPow5Bits = (i == 0) ? 1 : ((int)((i * 23219280L + 10000000L - 1L) / 10000000L));
            if (expectedPow5Bits != pow5len) {
                throw new IllegalStateException(pow5len + " != " + expectedPow5Bits);
            }
            if (i < RyuDouble.POW5_SPLIT.length) {
                for (int j = 0; j < 4; ++j) {
                    RyuDouble.POW5_SPLIT[i][j] = pow.shiftRight(pow5len - 121 + (3 - j) * 31).and(mask).intValue();
                }
            }
            if (i < RyuDouble.POW5_INV_SPLIT.length) {
                final int j = pow5len + 121;
                final BigInteger inv = BigInteger.ONE.shiftLeft(j).divide(pow).add(BigInteger.ONE);
                for (int k = 0; k < 4; ++k) {
                    if (k == 0) {
                        RyuDouble.POW5_INV_SPLIT[i][k] = inv.shiftRight((3 - k) * 31).intValue();
                    }
                    else {
                        RyuDouble.POW5_INV_SPLIT[i][k] = inv.shiftRight((3 - k) * 31).and(invMask).intValue();
                    }
                }
            }
        }
    }
}
