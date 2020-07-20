package com.alibaba.fastjson.util;

public final class RyuFloat
{
    private static final int[][] POW5_SPLIT;
    private static final int[][] POW5_INV_SPLIT;
    
    public static String toString(final float value) {
        final char[] result = new char[15];
        final int len = toString(value, result, 0);
        return new String(result, 0, len);
    }
    
    public static int toString(final float value, final char[] result, final int off) {
        final int FLOAT_MANTISSA_MASK = 8388607;
        final int FLOAT_EXPONENT_MASK = 255;
        final int FLOAT_EXPONENT_BIAS = 127;
        final long LOG10_2_NUMERATOR = 3010299L;
        final long LOG10_5_DENOMINATOR = 10000000L;
        final long LOG10_5_NUMERATOR = 6989700L;
        int index = off;
        if (Float.isNaN(value)) {
            result[index++] = 'N';
            result[index++] = 'a';
            result[index++] = 'N';
            return index - off;
        }
        if (value == Float.POSITIVE_INFINITY) {
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
        if (value == Float.NEGATIVE_INFINITY) {
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
        final int bits = Float.floatToIntBits(value);
        if (bits == 0) {
            result[index++] = '0';
            result[index++] = '.';
            result[index++] = '0';
            return index - off;
        }
        if (bits == Integer.MIN_VALUE) {
            result[index++] = '-';
            result[index++] = '0';
            result[index++] = '.';
            result[index++] = '0';
            return index - off;
        }
        final int ieeeExponent = bits >> 23 & 0xFF;
        final int ieeeMantissa = bits & 0x7FFFFF;
        int e2;
        int m2;
        if (ieeeExponent == 0) {
            e2 = -149;
            m2 = ieeeMantissa;
        }
        else {
            e2 = ieeeExponent - 127 - 23;
            m2 = (ieeeMantissa | 0x800000);
        }
        final boolean sign = bits < 0;
        final boolean even = (m2 & 0x1) == 0x0;
        final int mv = 4 * m2;
        final int mp = 4 * m2 + 2;
        final int mm = 4 * m2 - ((m2 != 8388608L || ieeeExponent <= 1) ? 2 : 1);
        e2 -= 2;
        int lastRemovedDigit = 0;
        int dv;
        int dp;
        int dm;
        int e4;
        boolean dpIsTrailingZeros;
        boolean dvIsTrailingZeros;
        boolean dmIsTrailingZeros;
        if (e2 >= 0) {
            final int q = (int)(e2 * 3010299L / 10000000L);
            final int k = 59 + ((q == 0) ? 1 : ((int)((q * 23219280L + 10000000L - 1L) / 10000000L))) - 1;
            final int i = -e2 + q + k;
            final long pis0 = RyuFloat.POW5_INV_SPLIT[q][0];
            final long pis2 = RyuFloat.POW5_INV_SPLIT[q][1];
            dv = (int)(mv * pis0 + (mv * pis2 >> 31) >> i - 31);
            dp = (int)(mp * pis0 + (mp * pis2 >> 31) >> i - 31);
            dm = (int)(mm * pis0 + (mm * pis2 >> 31) >> i - 31);
            if (q != 0 && (dp - 1) / 10 <= dm / 10) {
                final int e3 = q - 1;
                final int l = 59 + ((e3 == 0) ? 1 : ((int)((e3 * 23219280L + 10000000L - 1L) / 10000000L))) - 1;
                final int qx = q - 1;
                final int ii = -e2 + q - 1 + l;
                final long mulPow5InvDivPow2 = mv * (long)RyuFloat.POW5_INV_SPLIT[qx][0] + (mv * (long)RyuFloat.POW5_INV_SPLIT[qx][1] >> 31) >> ii - 31;
                lastRemovedDigit = (int)(mulPow5InvDivPow2 % 10L);
            }
            e4 = q;
            int pow5Factor_mp = 0;
            for (int v = mp; v > 0 && v % 5 == 0; v /= 5, ++pow5Factor_mp) {}
            int pow5Factor_mv = 0;
            for (int v2 = mv; v2 > 0 && v2 % 5 == 0; v2 /= 5, ++pow5Factor_mv) {}
            int pow5Factor_mm = 0;
            for (int v3 = mm; v3 > 0 && v3 % 5 == 0; v3 /= 5, ++pow5Factor_mm) {}
            dpIsTrailingZeros = (pow5Factor_mp >= q);
            dvIsTrailingZeros = (pow5Factor_mv >= q);
            dmIsTrailingZeros = (pow5Factor_mm >= q);
        }
        else {
            final int q = (int)(-e2 * 6989700L / 10000000L);
            final int j = -e2 - q;
            final int k2 = ((j == 0) ? 1 : ((int)((j * 23219280L + 10000000L - 1L) / 10000000L))) - 61;
            int j2 = q - k2;
            final long ps0 = RyuFloat.POW5_SPLIT[j][0];
            final long ps2 = RyuFloat.POW5_SPLIT[j][1];
            final int j3 = j2 - 31;
            dv = (int)(mv * ps0 + (mv * ps2 >> 31) >> j3);
            dp = (int)(mp * ps0 + (mp * ps2 >> 31) >> j3);
            dm = (int)(mm * ps0 + (mm * ps2 >> 31) >> j3);
            if (q != 0 && (dp - 1) / 10 <= dm / 10) {
                final int e5 = j + 1;
                j2 = q - 1 - (((e5 == 0) ? 1 : ((int)((e5 * 23219280L + 10000000L - 1L) / 10000000L))) - 61);
                final int ix = j + 1;
                final long mulPow5divPow2 = mv * (long)RyuFloat.POW5_SPLIT[ix][0] + (mv * (long)RyuFloat.POW5_SPLIT[ix][1] >> 31) >> j2 - 31;
                lastRemovedDigit = (int)(mulPow5divPow2 % 10L);
            }
            e4 = q + e2;
            dpIsTrailingZeros = (1 >= q);
            dvIsTrailingZeros = (q < 23 && (mv & (1 << q - 1) - 1) == 0x0);
            dmIsTrailingZeros = (((mm % 2 != 1) ? 1 : 0) >= q);
        }
        int dplength = 10;
        for (int factor = 1000000000; dplength > 0 && dp < factor; factor /= 10, --dplength) {}
        int exp = e4 + dplength - 1;
        final boolean scientificNotation = exp < -3 || exp >= 7;
        int removed = 0;
        if (dpIsTrailingZeros && !even) {
            --dp;
        }
        while (dp / 10 > dm / 10 && (dp >= 100 || !scientificNotation)) {
            dmIsTrailingZeros &= (dm % 10 == 0);
            dp /= 10;
            lastRemovedDigit = dv % 10;
            dv /= 10;
            dm /= 10;
            ++removed;
        }
        if (dmIsTrailingZeros && even) {
            while (dm % 10 == 0) {
                if (dp < 100 && scientificNotation) {
                    break;
                }
                dp /= 10;
                lastRemovedDigit = dv % 10;
                dv /= 10;
                dm /= 10;
                ++removed;
            }
        }
        if (dvIsTrailingZeros && lastRemovedDigit == 5 && dv % 2 == 0) {
            lastRemovedDigit = 4;
        }
        final int n;
        int output = n + ((((n = dv) == dm && (!dmIsTrailingZeros || !even)) || lastRemovedDigit >= 5) ? 1 : 0);
        final int olength = dplength - removed;
        if (sign) {
            result[index++] = '-';
        }
        if (scientificNotation) {
            for (int i2 = 0; i2 < olength - 1; ++i2) {
                final int c = output % 10;
                output /= 10;
                result[index + olength - i2] = (char)(48 + c);
            }
            result[index] = (char)(48 + output % 10);
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
            if (exp >= 10) {
                result[index++] = (char)(48 + exp / 10);
            }
            result[index++] = (char)(48 + exp % 10);
        }
        else if (exp < 0) {
            result[index++] = '0';
            result[index++] = '.';
            for (int i2 = -1; i2 > exp; --i2) {
                result[index++] = '0';
            }
            final int current = index;
            for (int i3 = 0; i3 < olength; ++i3) {
                result[current + olength - i3 - 1] = (char)(48 + output % 10);
                output /= 10;
                ++index;
            }
        }
        else if (exp + 1 >= olength) {
            for (int i2 = 0; i2 < olength; ++i2) {
                result[index + olength - i2 - 1] = (char)(48 + output % 10);
                output /= 10;
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
                result[current + olength - i3 - 1] = (char)(48 + output % 10);
                output /= 10;
            }
            index += olength + 1;
        }
        return index - off;
    }
    
    static {
        POW5_SPLIT = new int[][] { { 536870912, 0 }, { 671088640, 0 }, { 838860800, 0 }, { 1048576000, 0 }, { 655360000, 0 }, { 819200000, 0 }, { 1024000000, 0 }, { 640000000, 0 }, { 800000000, 0 }, { 1000000000, 0 }, { 625000000, 0 }, { 781250000, 0 }, { 976562500, 0 }, { 610351562, 1073741824 }, { 762939453, 268435456 }, { 953674316, 872415232 }, { 596046447, 1619001344 }, { 745058059, 1486880768 }, { 931322574, 1321730048 }, { 582076609, 289210368 }, { 727595761, 898383872 }, { 909494701, 1659850752 }, { 568434188, 1305842176 }, { 710542735, 1632302720 }, { 888178419, 1503507488 }, { 555111512, 671256724 }, { 693889390, 839070905 }, { 867361737, 2122580455 }, { 542101086, 521306416 }, { 677626357, 1725374844 }, { 847032947, 546105819 }, { 1058791184, 145761362 }, { 661744490, 91100851 }, { 827180612, 1187617888 }, { 1033975765, 1484522360 }, { 646234853, 1196261931 }, { 807793566, 2032198326 }, { 1009741958, 1466506084 }, { 631088724, 379695390 }, { 788860905, 474619238 }, { 986076131, 1130144959 }, { 616297582, 437905143 }, { 770371977, 1621123253 }, { 962964972, 415791331 }, { 601853107, 1333611405 }, { 752316384, 1130143345 }, { 940395480, 1412679181 } };
        POW5_INV_SPLIT = new int[][] { { 268435456, 1 }, { 214748364, 1717986919 }, { 171798691, 1803886265 }, { 137438953, 1013612282 }, { 219902325, 1192282922 }, { 175921860, 953826338 }, { 140737488, 763061070 }, { 225179981, 791400982 }, { 180143985, 203624056 }, { 144115188, 162899245 }, { 230584300, 1978625710 }, { 184467440, 1582900568 }, { 147573952, 1266320455 }, { 236118324, 308125809 }, { 188894659, 675997377 }, { 151115727, 970294631 }, { 241785163, 1981968139 }, { 193428131, 297084323 }, { 154742504, 1955654377 }, { 247588007, 1840556814 }, { 198070406, 613451992 }, { 158456325, 61264864 }, { 253530120, 98023782 }, { 202824096, 78419026 }, { 162259276, 1780722139 }, { 259614842, 1990161963 }, { 207691874, 733136111 }, { 166153499, 1016005619 }, { 265845599, 337118801 }, { 212676479, 699191770 }, { 170141183, 988850146 } };
    }
}
