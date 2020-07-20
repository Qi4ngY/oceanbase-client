package com.alibaba.fastjson.util;

import java.nio.charset.CharsetDecoder;
import java.lang.ref.SoftReference;

public class ThreadLocalCache
{
    public static final int CHARS_CACH_INIT_SIZE = 1024;
    public static final int CHARS_CACH_INIT_SIZE_EXP = 10;
    public static final int CHARS_CACH_MAX_SIZE = 131072;
    public static final int CHARS_CACH_MAX_SIZE_EXP = 17;
    private static final ThreadLocal<SoftReference<char[]>> charsBufLocal;
    private static final ThreadLocal<CharsetDecoder> decoderLocal;
    public static final int BYTES_CACH_INIT_SIZE = 1024;
    public static final int BYTES_CACH_INIT_SIZE_EXP = 10;
    public static final int BYTES_CACH_MAX_SIZE = 131072;
    public static final int BYTES_CACH_MAX_SIZE_EXP = 17;
    private static final ThreadLocal<SoftReference<byte[]>> bytesBufLocal;
    
    public static CharsetDecoder getUTF8Decoder() {
        CharsetDecoder decoder = ThreadLocalCache.decoderLocal.get();
        if (decoder == null) {
            decoder = new UTF8Decoder();
            ThreadLocalCache.decoderLocal.set(decoder);
        }
        return decoder;
    }
    
    public static void clearChars() {
        ThreadLocalCache.charsBufLocal.set(null);
    }
    
    public static char[] getChars(final int length) {
        final SoftReference<char[]> ref = ThreadLocalCache.charsBufLocal.get();
        if (ref == null) {
            return allocate(length);
        }
        char[] chars = ref.get();
        if (chars == null) {
            return allocate(length);
        }
        if (chars.length < length) {
            chars = allocate(length);
        }
        return chars;
    }
    
    private static char[] allocate(final int length) {
        if (length > 131072) {
            return new char[length];
        }
        final int allocateLength = getAllocateLengthExp(10, 17, length);
        final char[] chars = new char[allocateLength];
        ThreadLocalCache.charsBufLocal.set(new SoftReference<char[]>(chars));
        return chars;
    }
    
    private static int getAllocateLengthExp(final int minExp, final int maxExp, final int length) {
        assert 1 << maxExp >= length;
        final int part = length >>> minExp;
        if (part <= 0) {
            return 1 << minExp;
        }
        return 1 << 32 - Integer.numberOfLeadingZeros(length - 1);
    }
    
    public static void clearBytes() {
        ThreadLocalCache.bytesBufLocal.set(null);
    }
    
    public static byte[] getBytes(final int length) {
        final SoftReference<byte[]> ref = ThreadLocalCache.bytesBufLocal.get();
        if (ref == null) {
            return allocateBytes(length);
        }
        byte[] bytes = ref.get();
        if (bytes == null) {
            return allocateBytes(length);
        }
        if (bytes.length < length) {
            bytes = allocateBytes(length);
        }
        return bytes;
    }
    
    private static byte[] allocateBytes(final int length) {
        if (length > 131072) {
            return new byte[length];
        }
        final int allocateLength = getAllocateLengthExp(10, 17, length);
        final byte[] chars = new byte[allocateLength];
        ThreadLocalCache.bytesBufLocal.set(new SoftReference<byte[]>(chars));
        return chars;
    }
    
    static {
        charsBufLocal = new ThreadLocal<SoftReference<char[]>>();
        decoderLocal = new ThreadLocal<CharsetDecoder>();
        bytesBufLocal = new ThreadLocal<SoftReference<byte[]>>();
    }
}
