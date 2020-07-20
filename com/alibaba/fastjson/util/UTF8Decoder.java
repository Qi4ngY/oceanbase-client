package com.alibaba.fastjson.util;

import java.nio.Buffer;
import java.nio.CharBuffer;
import java.nio.charset.CoderResult;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class UTF8Decoder extends CharsetDecoder
{
    private static final Charset charset;
    
    public UTF8Decoder() {
        super(UTF8Decoder.charset, 1.0f, 1.0f);
    }
    
    private static boolean isNotContinuation(final int b) {
        return (b & 0xC0) != 0x80;
    }
    
    private static boolean isMalformed2(final int b1, final int b2) {
        return (b1 & 0x1E) == 0x0 || (b2 & 0xC0) != 0x80;
    }
    
    private static boolean isMalformed3(final int b1, final int b2, final int b3) {
        return (b1 == -32 && (b2 & 0xE0) == 0x80) || (b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80;
    }
    
    private static boolean isMalformed4(final int b2, final int b3, final int b4) {
        return (b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80 || (b4 & 0xC0) != 0x80;
    }
    
    private static CoderResult lookupN(final ByteBuffer src, final int n) {
        for (int i = 1; i < n; ++i) {
            if (isNotContinuation(src.get())) {
                return CoderResult.malformedForLength(i);
            }
        }
        return CoderResult.malformedForLength(n);
    }
    
    public static CoderResult malformedN(final ByteBuffer src, final int nb) {
        switch (nb) {
            case 1: {
                final int b1 = src.get();
                if (b1 >> 2 == -2) {
                    if (src.remaining() < 4) {
                        return CoderResult.UNDERFLOW;
                    }
                    return lookupN(src, 5);
                }
                else {
                    if (b1 >> 1 != -2) {
                        return CoderResult.malformedForLength(1);
                    }
                    if (src.remaining() < 5) {
                        return CoderResult.UNDERFLOW;
                    }
                    return lookupN(src, 6);
                }
                break;
            }
            case 2: {
                return CoderResult.malformedForLength(1);
            }
            case 3: {
                final int b1 = src.get();
                final int b2 = src.get();
                return CoderResult.malformedForLength(((b1 == -32 && (b2 & 0xE0) == 0x80) || isNotContinuation(b2)) ? 1 : 2);
            }
            case 4: {
                final int b1 = src.get() & 0xFF;
                final int b2 = src.get() & 0xFF;
                if (b1 > 244 || (b1 == 240 && (b2 < 144 || b2 > 191)) || (b1 == 244 && (b2 & 0xF0) != 0x80) || isNotContinuation(b2)) {
                    return CoderResult.malformedForLength(1);
                }
                if (isNotContinuation(src.get())) {
                    return CoderResult.malformedForLength(2);
                }
                return CoderResult.malformedForLength(3);
            }
            default: {
                throw new IllegalStateException();
            }
        }
    }
    
    private static CoderResult malformed(final ByteBuffer src, final int sp, final CharBuffer dst, final int dp, final int nb) {
        src.position(sp - src.arrayOffset());
        final CoderResult cr = malformedN(src, nb);
        src.position(sp);
        dst.position(dp);
        return cr;
    }
    
    private static CoderResult xflow(final Buffer src, final int sp, final int sl, final Buffer dst, final int dp, final int nb) {
        src.position(sp);
        dst.position(dp);
        return (nb == 0 || sl - sp < nb) ? CoderResult.UNDERFLOW : CoderResult.OVERFLOW;
    }
    
    private CoderResult decodeArrayLoop(final ByteBuffer src, final CharBuffer dst) {
        final byte[] srcArray = src.array();
        int srcPosition = src.arrayOffset() + src.position();
        final int srcLength = src.arrayOffset() + src.limit();
        final char[] destArray = dst.array();
        int destPosition = dst.arrayOffset() + dst.position();
        final int destLength = dst.arrayOffset() + dst.limit();
        for (int destLengthASCII = destPosition + Math.min(srcLength - srcPosition, destLength - destPosition); destPosition < destLengthASCII && srcArray[srcPosition] >= 0; destArray[destPosition++] = (char)srcArray[srcPosition++]) {}
        while (srcPosition < srcLength) {
            final int b1 = srcArray[srcPosition];
            if (b1 >= 0) {
                if (destPosition >= destLength) {
                    return xflow(src, srcPosition, srcLength, dst, destPosition, 1);
                }
                destArray[destPosition++] = (char)b1;
                ++srcPosition;
            }
            else if (b1 >> 5 == -2) {
                if (srcLength - srcPosition < 2 || destPosition >= destLength) {
                    return xflow(src, srcPosition, srcLength, dst, destPosition, 2);
                }
                final int b2 = srcArray[srcPosition + 1];
                if (isMalformed2(b1, b2)) {
                    return malformed(src, srcPosition, dst, destPosition, 2);
                }
                destArray[destPosition++] = (char)(b1 << 6 ^ b2 ^ 0xF80);
                srcPosition += 2;
            }
            else if (b1 >> 4 == -2) {
                if (srcLength - srcPosition < 3 || destPosition >= destLength) {
                    return xflow(src, srcPosition, srcLength, dst, destPosition, 3);
                }
                final int b2 = srcArray[srcPosition + 1];
                final int b3 = srcArray[srcPosition + 2];
                if (isMalformed3(b1, b2, b3)) {
                    return malformed(src, srcPosition, dst, destPosition, 3);
                }
                destArray[destPosition++] = (char)(b1 << 12 ^ b2 << 6 ^ b3 ^ 0x1F80);
                srcPosition += 3;
            }
            else {
                if (b1 >> 3 != -2) {
                    return malformed(src, srcPosition, dst, destPosition, 1);
                }
                if (srcLength - srcPosition < 4 || destLength - destPosition < 2) {
                    return xflow(src, srcPosition, srcLength, dst, destPosition, 4);
                }
                final int b2 = srcArray[srcPosition + 1];
                final int b3 = srcArray[srcPosition + 2];
                final int b4 = srcArray[srcPosition + 3];
                final int uc = (b1 & 0x7) << 18 | (b2 & 0x3F) << 12 | (b3 & 0x3F) << 6 | (b4 & 0x3F);
                if (isMalformed4(b2, b3, b4) || uc < 65536 || uc > 1114111) {
                    return malformed(src, srcPosition, dst, destPosition, 4);
                }
                destArray[destPosition++] = (char)(0xD800 | (uc - 65536 >> 10 & 0x3FF));
                destArray[destPosition++] = (char)(0xDC00 | (uc - 65536 & 0x3FF));
                srcPosition += 4;
            }
        }
        return xflow(src, srcPosition, srcLength, dst, destPosition, 0);
    }
    
    @Override
    protected CoderResult decodeLoop(final ByteBuffer src, final CharBuffer dst) {
        return this.decodeArrayLoop(src, dst);
    }
    
    static {
        charset = Charset.forName("UTF-8");
    }
}
