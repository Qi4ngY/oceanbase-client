package com.alibaba.fastjson.serializer;

import java.math.BigDecimal;
import java.util.List;
import com.alibaba.fastjson.util.RyuDouble;
import com.alibaba.fastjson.util.RyuFloat;
import com.alibaba.fastjson.util.IOUtils;
import java.nio.charset.Charset;
import java.io.OutputStream;
import java.io.IOException;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSON;
import java.io.Writer;

public final class SerializeWriter extends Writer
{
    private static final ThreadLocal<char[]> bufLocal;
    private static final ThreadLocal<byte[]> bytesBufLocal;
    private static int BUFFER_THRESHOLD;
    protected char[] buf;
    protected int count;
    protected int features;
    private final Writer writer;
    protected boolean useSingleQuotes;
    protected boolean quoteFieldNames;
    protected boolean sortField;
    protected boolean disableCircularReferenceDetect;
    protected boolean beanToArray;
    protected boolean writeNonStringValueAsString;
    protected boolean notWriteDefaultValue;
    protected boolean writeEnumUsingName;
    protected boolean writeEnumUsingToString;
    protected boolean writeDirect;
    protected char keySeperator;
    protected int maxBufSize;
    protected boolean browserSecure;
    protected long sepcialBits;
    static final int nonDirectFeatures;
    
    public SerializeWriter() {
        this((Writer)null);
    }
    
    public SerializeWriter(final Writer writer) {
        this(writer, JSON.DEFAULT_GENERATE_FEATURE, SerializerFeature.EMPTY);
    }
    
    public SerializeWriter(final SerializerFeature... features) {
        this((Writer)null, features);
    }
    
    public SerializeWriter(final Writer writer, final SerializerFeature... features) {
        this(writer, 0, features);
    }
    
    public SerializeWriter(final Writer writer, final int defaultFeatures, final SerializerFeature... features) {
        this.maxBufSize = -1;
        this.writer = writer;
        this.buf = SerializeWriter.bufLocal.get();
        if (this.buf != null) {
            SerializeWriter.bufLocal.set(null);
        }
        else {
            this.buf = new char[2048];
        }
        int featuresValue = defaultFeatures;
        for (final SerializerFeature feature : features) {
            featuresValue |= feature.getMask();
        }
        this.features = featuresValue;
        this.computeFeatures();
    }
    
    public int getMaxBufSize() {
        return this.maxBufSize;
    }
    
    public void setMaxBufSize(final int maxBufSize) {
        if (maxBufSize < this.buf.length) {
            throw new JSONException("must > " + this.buf.length);
        }
        this.maxBufSize = maxBufSize;
    }
    
    public int getBufferLength() {
        return this.buf.length;
    }
    
    public SerializeWriter(final int initialSize) {
        this(null, initialSize);
    }
    
    public SerializeWriter(final Writer writer, final int initialSize) {
        this.maxBufSize = -1;
        this.writer = writer;
        if (initialSize <= 0) {
            throw new IllegalArgumentException("Negative initial size: " + initialSize);
        }
        this.buf = new char[initialSize];
        this.computeFeatures();
    }
    
    public void config(final SerializerFeature feature, final boolean state) {
        if (state) {
            this.features |= feature.getMask();
            if (feature == SerializerFeature.WriteEnumUsingToString) {
                this.features &= ~SerializerFeature.WriteEnumUsingName.getMask();
            }
            else if (feature == SerializerFeature.WriteEnumUsingName) {
                this.features &= ~SerializerFeature.WriteEnumUsingToString.getMask();
            }
        }
        else {
            this.features &= ~feature.getMask();
        }
        this.computeFeatures();
    }
    
    protected void computeFeatures() {
        this.quoteFieldNames = ((this.features & SerializerFeature.QuoteFieldNames.mask) != 0x0);
        this.useSingleQuotes = ((this.features & SerializerFeature.UseSingleQuotes.mask) != 0x0);
        this.sortField = ((this.features & SerializerFeature.SortField.mask) != 0x0);
        this.disableCircularReferenceDetect = ((this.features & SerializerFeature.DisableCircularReferenceDetect.mask) != 0x0);
        this.beanToArray = ((this.features & SerializerFeature.BeanToArray.mask) != 0x0);
        this.writeNonStringValueAsString = ((this.features & SerializerFeature.WriteNonStringValueAsString.mask) != 0x0);
        this.notWriteDefaultValue = ((this.features & SerializerFeature.NotWriteDefaultValue.mask) != 0x0);
        this.writeEnumUsingName = ((this.features & SerializerFeature.WriteEnumUsingName.mask) != 0x0);
        this.writeEnumUsingToString = ((this.features & SerializerFeature.WriteEnumUsingToString.mask) != 0x0);
        this.writeDirect = (this.quoteFieldNames && (this.features & SerializeWriter.nonDirectFeatures) == 0x0 && (this.beanToArray || this.writeEnumUsingName));
        this.keySeperator = (this.useSingleQuotes ? '\'' : '\"');
        this.browserSecure = ((this.features & SerializerFeature.BrowserSecure.mask) != 0x0);
        final long S0 = 21474836479L;
        final long S2 = 140758963191807L;
        final long S3 = 5764610843043954687L;
        this.sepcialBits = (this.browserSecure ? 5764610843043954687L : (((this.features & SerializerFeature.WriteSlashAsSpecial.mask) != 0x0) ? 140758963191807L : 21474836479L));
    }
    
    public boolean isSortField() {
        return this.sortField;
    }
    
    public boolean isNotWriteDefaultValue() {
        return this.notWriteDefaultValue;
    }
    
    public boolean isEnabled(final SerializerFeature feature) {
        return (this.features & feature.mask) != 0x0;
    }
    
    public boolean isEnabled(final int feature) {
        return (this.features & feature) != 0x0;
    }
    
    @Override
    public void write(final int c) {
        int newcount = this.count + 1;
        if (newcount > this.buf.length) {
            if (this.writer == null) {
                this.expandCapacity(newcount);
            }
            else {
                this.flush();
                newcount = 1;
            }
        }
        this.buf[this.count] = (char)c;
        this.count = newcount;
    }
    
    @Override
    public void write(final char[] c, int off, int len) {
        if (off < 0 || off > c.length || len < 0 || off + len > c.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        int newcount = this.count + len;
        if (newcount > this.buf.length) {
            if (this.writer == null) {
                this.expandCapacity(newcount);
            }
            else {
                do {
                    final int rest = this.buf.length - this.count;
                    System.arraycopy(c, off, this.buf, this.count, rest);
                    this.count = this.buf.length;
                    this.flush();
                    len -= rest;
                    off += rest;
                } while (len > this.buf.length);
                newcount = len;
            }
        }
        System.arraycopy(c, off, this.buf, this.count, len);
        this.count = newcount;
    }
    
    public void expandCapacity(final int minimumCapacity) {
        if (this.maxBufSize != -1 && minimumCapacity >= this.maxBufSize) {
            throw new JSONException("serialize exceeded MAX_OUTPUT_LENGTH=" + this.maxBufSize + ", minimumCapacity=" + minimumCapacity);
        }
        int newCapacity = this.buf.length + (this.buf.length >> 1) + 1;
        if (newCapacity < minimumCapacity) {
            newCapacity = minimumCapacity;
        }
        final char[] newValue = new char[newCapacity];
        System.arraycopy(this.buf, 0, newValue, 0, this.count);
        if (this.buf.length < SerializeWriter.BUFFER_THRESHOLD) {
            final char[] charsLocal = SerializeWriter.bufLocal.get();
            if (charsLocal == null || charsLocal.length < this.buf.length) {
                SerializeWriter.bufLocal.set(this.buf);
            }
        }
        this.buf = newValue;
    }
    
    @Override
    public SerializeWriter append(final CharSequence csq) {
        final String s = (csq == null) ? "null" : csq.toString();
        this.write(s, 0, s.length());
        return this;
    }
    
    @Override
    public SerializeWriter append(final CharSequence csq, final int start, final int end) {
        final String s = ((csq == null) ? "null" : csq).subSequence(start, end).toString();
        this.write(s, 0, s.length());
        return this;
    }
    
    @Override
    public SerializeWriter append(final char c) {
        this.write(c);
        return this;
    }
    
    @Override
    public void write(final String str, int off, int len) {
        int newcount = this.count + len;
        if (newcount > this.buf.length) {
            if (this.writer == null) {
                this.expandCapacity(newcount);
            }
            else {
                do {
                    final int rest = this.buf.length - this.count;
                    str.getChars(off, off + rest, this.buf, this.count);
                    this.count = this.buf.length;
                    this.flush();
                    len -= rest;
                    off += rest;
                } while (len > this.buf.length);
                newcount = len;
            }
        }
        str.getChars(off, off + len, this.buf, this.count);
        this.count = newcount;
    }
    
    public void writeTo(final Writer out) throws IOException {
        if (this.writer != null) {
            throw new UnsupportedOperationException("writer not null");
        }
        out.write(this.buf, 0, this.count);
    }
    
    public void writeTo(final OutputStream out, final String charsetName) throws IOException {
        this.writeTo(out, Charset.forName(charsetName));
    }
    
    public void writeTo(final OutputStream out, final Charset charset) throws IOException {
        this.writeToEx(out, charset);
    }
    
    public int writeToEx(final OutputStream out, final Charset charset) throws IOException {
        if (this.writer != null) {
            throw new UnsupportedOperationException("writer not null");
        }
        if (charset == IOUtils.UTF8) {
            return this.encodeToUTF8(out);
        }
        final byte[] bytes = new String(this.buf, 0, this.count).getBytes(charset);
        out.write(bytes);
        return bytes.length;
    }
    
    public char[] toCharArray() {
        if (this.writer != null) {
            throw new UnsupportedOperationException("writer not null");
        }
        final char[] newValue = new char[this.count];
        System.arraycopy(this.buf, 0, newValue, 0, this.count);
        return newValue;
    }
    
    public char[] toCharArrayForSpringWebSocket() {
        if (this.writer != null) {
            throw new UnsupportedOperationException("writer not null");
        }
        final char[] newValue = new char[this.count - 2];
        System.arraycopy(this.buf, 1, newValue, 0, this.count - 2);
        return newValue;
    }
    
    public byte[] toBytes(final String charsetName) {
        return this.toBytes((charsetName == null || "UTF-8".equals(charsetName)) ? IOUtils.UTF8 : Charset.forName(charsetName));
    }
    
    public byte[] toBytes(final Charset charset) {
        if (this.writer != null) {
            throw new UnsupportedOperationException("writer not null");
        }
        if (charset == IOUtils.UTF8) {
            return this.encodeToUTF8Bytes();
        }
        return new String(this.buf, 0, this.count).getBytes(charset);
    }
    
    private int encodeToUTF8(final OutputStream out) throws IOException {
        final int bytesLength = (int)(this.count * 3.0);
        byte[] bytes = SerializeWriter.bytesBufLocal.get();
        if (bytes == null) {
            bytes = new byte[8192];
            SerializeWriter.bytesBufLocal.set(bytes);
        }
        if (bytes.length < bytesLength) {
            bytes = new byte[bytesLength];
        }
        final int position = IOUtils.encodeUTF8(this.buf, 0, this.count, bytes);
        out.write(bytes, 0, position);
        return position;
    }
    
    private byte[] encodeToUTF8Bytes() {
        final int bytesLength = (int)(this.count * 3.0);
        byte[] bytes = SerializeWriter.bytesBufLocal.get();
        if (bytes == null) {
            bytes = new byte[8192];
            SerializeWriter.bytesBufLocal.set(bytes);
        }
        if (bytes.length < bytesLength) {
            bytes = new byte[bytesLength];
        }
        final int position = IOUtils.encodeUTF8(this.buf, 0, this.count, bytes);
        final byte[] copy = new byte[position];
        System.arraycopy(bytes, 0, copy, 0, position);
        return copy;
    }
    
    public int size() {
        return this.count;
    }
    
    @Override
    public String toString() {
        return new String(this.buf, 0, this.count);
    }
    
    @Override
    public void close() {
        if (this.writer != null && this.count > 0) {
            this.flush();
        }
        if (this.buf.length <= SerializeWriter.BUFFER_THRESHOLD) {
            SerializeWriter.bufLocal.set(this.buf);
        }
        this.buf = null;
    }
    
    @Override
    public void write(final String text) {
        if (text == null) {
            this.writeNull();
            return;
        }
        this.write(text, 0, text.length());
    }
    
    public void writeInt(final int i) {
        if (i == Integer.MIN_VALUE) {
            this.write("-2147483648");
            return;
        }
        final int size = (i < 0) ? (IOUtils.stringSize(-i) + 1) : IOUtils.stringSize(i);
        final int newcount = this.count + size;
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                final char[] chars = new char[size];
                IOUtils.getChars(i, size, chars);
                this.write(chars, 0, chars.length);
                return;
            }
            this.expandCapacity(newcount);
        }
        IOUtils.getChars(i, newcount, this.buf);
        this.count = newcount;
    }
    
    public void writeByteArray(final byte[] bytes) {
        if (this.isEnabled(SerializerFeature.WriteClassName.mask)) {
            this.writeHex(bytes);
            return;
        }
        final int bytesLen = bytes.length;
        final char quote = this.useSingleQuotes ? '\'' : '\"';
        if (bytesLen == 0) {
            final String emptyString = this.useSingleQuotes ? "''" : "\"\"";
            this.write(emptyString);
            return;
        }
        final char[] CA = IOUtils.CA;
        final int eLen = bytesLen / 3 * 3;
        final int charsLen = (bytesLen - 1) / 3 + 1 << 2;
        int offset = this.count;
        final int newcount = this.count + charsLen + 2;
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                this.write(quote);
                int s = 0;
                while (s < eLen) {
                    final int i = (bytes[s++] & 0xFF) << 16 | (bytes[s++] & 0xFF) << 8 | (bytes[s++] & 0xFF);
                    this.write(CA[i >>> 18 & 0x3F]);
                    this.write(CA[i >>> 12 & 0x3F]);
                    this.write(CA[i >>> 6 & 0x3F]);
                    this.write(CA[i & 0x3F]);
                }
                final int left = bytesLen - eLen;
                if (left > 0) {
                    final int i = (bytes[eLen] & 0xFF) << 10 | ((left == 2) ? ((bytes[bytesLen - 1] & 0xFF) << 2) : 0);
                    this.write(CA[i >> 12]);
                    this.write(CA[i >>> 6 & 0x3F]);
                    this.write((left == 2) ? CA[i & 0x3F] : '=');
                    this.write(61);
                }
                this.write(quote);
                return;
            }
            this.expandCapacity(newcount);
        }
        this.count = newcount;
        this.buf[offset++] = quote;
        int s = 0;
        int j;
        for (int d = offset; s < eLen; j = ((bytes[s++] & 0xFF) << 16 | (bytes[s++] & 0xFF) << 8 | (bytes[s++] & 0xFF)), this.buf[d++] = CA[j >>> 18 & 0x3F], this.buf[d++] = CA[j >>> 12 & 0x3F], this.buf[d++] = CA[j >>> 6 & 0x3F], this.buf[d++] = CA[j & 0x3F]) {}
        final int left = bytesLen - eLen;
        if (left > 0) {
            final int i = (bytes[eLen] & 0xFF) << 10 | ((left == 2) ? ((bytes[bytesLen - 1] & 0xFF) << 2) : 0);
            this.buf[newcount - 5] = CA[i >> 12];
            this.buf[newcount - 4] = CA[i >>> 6 & 0x3F];
            this.buf[newcount - 3] = ((left == 2) ? CA[i & 0x3F] : '=');
            this.buf[newcount - 2] = '=';
        }
        this.buf[newcount - 1] = quote;
    }
    
    public void writeHex(final byte[] bytes) {
        final int newcount = this.count + bytes.length * 2 + 3;
        if (newcount > this.buf.length) {
            this.expandCapacity(newcount);
        }
        this.buf[this.count++] = 'x';
        this.buf[this.count++] = '\'';
        for (int i = 0; i < bytes.length; ++i) {
            final byte b = bytes[i];
            final int a = b & 0xFF;
            final int b2 = a >> 4;
            final int b3 = a & 0xF;
            this.buf[this.count++] = (char)(b2 + ((b2 < 10) ? 48 : 55));
            this.buf[this.count++] = (char)(b3 + ((b3 < 10) ? 48 : 55));
        }
        this.buf[this.count++] = '\'';
    }
    
    public void writeFloat(final float value, final boolean checkWriteClassName) {
        if (value != value || value == Float.POSITIVE_INFINITY || value == Float.NEGATIVE_INFINITY) {
            this.writeNull();
        }
        else {
            final int newcount = this.count + 15;
            if (newcount > this.buf.length) {
                if (this.writer != null) {
                    final String str = RyuFloat.toString(value);
                    this.write(str, 0, str.length());
                    if (checkWriteClassName && this.isEnabled(SerializerFeature.WriteClassName)) {
                        this.write(70);
                    }
                    return;
                }
                this.expandCapacity(newcount);
            }
            final int len = RyuFloat.toString(value, this.buf, this.count);
            this.count += len;
            if (checkWriteClassName && this.isEnabled(SerializerFeature.WriteClassName)) {
                this.write(70);
            }
        }
    }
    
    public void writeDouble(final double value, final boolean checkWriteClassName) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            this.writeNull();
            return;
        }
        final int newcount = this.count + 24;
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                final String str = RyuDouble.toString(value);
                this.write(str, 0, str.length());
                if (checkWriteClassName && this.isEnabled(SerializerFeature.WriteClassName)) {
                    this.write(68);
                }
                return;
            }
            this.expandCapacity(newcount);
        }
        final int len = RyuDouble.toString(value, this.buf, this.count);
        this.count += len;
        if (checkWriteClassName && this.isEnabled(SerializerFeature.WriteClassName)) {
            this.write(68);
        }
    }
    
    public void writeEnum(final Enum<?> value) {
        if (value == null) {
            this.writeNull();
            return;
        }
        String strVal = null;
        if (this.writeEnumUsingName && !this.writeEnumUsingToString) {
            strVal = value.name();
        }
        else if (this.writeEnumUsingToString) {
            strVal = value.toString();
        }
        if (strVal != null) {
            final char quote = this.isEnabled(SerializerFeature.UseSingleQuotes) ? '\'' : '\"';
            this.write(quote);
            this.write(strVal);
            this.write(quote);
        }
        else {
            this.writeInt(value.ordinal());
        }
    }
    
    @Deprecated
    public void writeLongAndChar(final long i, final char c) throws IOException {
        this.writeLong(i);
        this.write(c);
    }
    
    public void writeLong(final long i) {
        final boolean needQuotationMark = this.isEnabled(SerializerFeature.BrowserCompatible) && !this.isEnabled(SerializerFeature.WriteClassName) && (i > 9007199254740991L || i < -9007199254740991L);
        if (i == Long.MIN_VALUE) {
            if (needQuotationMark) {
                this.write("\"-9223372036854775808\"");
            }
            else {
                this.write("-9223372036854775808");
            }
            return;
        }
        final int size = (i < 0L) ? (IOUtils.stringSize(-i) + 1) : IOUtils.stringSize(i);
        int newcount = this.count + size;
        if (needQuotationMark) {
            newcount += 2;
        }
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                final char[] chars = new char[size];
                IOUtils.getChars(i, size, chars);
                if (needQuotationMark) {
                    this.write(34);
                    this.write(chars, 0, chars.length);
                    this.write(34);
                }
                else {
                    this.write(chars, 0, chars.length);
                }
                return;
            }
            this.expandCapacity(newcount);
        }
        if (needQuotationMark) {
            this.buf[this.count] = '\"';
            IOUtils.getChars(i, newcount - 1, this.buf);
            this.buf[newcount - 1] = '\"';
        }
        else {
            IOUtils.getChars(i, newcount, this.buf);
        }
        this.count = newcount;
    }
    
    public void writeNull() {
        this.write("null");
    }
    
    public void writeNull(final SerializerFeature feature) {
        this.writeNull(0, feature.mask);
    }
    
    public void writeNull(final int beanFeatures, final int feature) {
        if ((beanFeatures & feature) == 0x0 && (this.features & feature) == 0x0) {
            this.writeNull();
            return;
        }
        if ((beanFeatures & SerializerFeature.WriteMapNullValue.mask) != 0x0 && (beanFeatures & ~SerializerFeature.WriteMapNullValue.mask & SerializerFeature.WRITE_MAP_NULL_FEATURES) == 0x0) {
            this.writeNull();
            return;
        }
        if (feature == SerializerFeature.WriteNullListAsEmpty.mask) {
            this.write("[]");
        }
        else if (feature == SerializerFeature.WriteNullStringAsEmpty.mask) {
            this.writeString("");
        }
        else if (feature == SerializerFeature.WriteNullBooleanAsFalse.mask) {
            this.write("false");
        }
        else if (feature == SerializerFeature.WriteNullNumberAsZero.mask) {
            this.write(48);
        }
        else {
            this.writeNull();
        }
    }
    
    public void writeStringWithDoubleQuote(final String text, final char seperator) {
        if (text == null) {
            this.writeNull();
            if (seperator != '\0') {
                this.write(seperator);
            }
            return;
        }
        final int len = text.length();
        int newcount = this.count + len + 2;
        if (seperator != '\0') {
            ++newcount;
        }
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                this.write(34);
                for (int i = 0; i < text.length(); ++i) {
                    final char ch = text.charAt(i);
                    if (this.isEnabled(SerializerFeature.BrowserSecure) && (ch == '(' || ch == ')' || ch == '<' || ch == '>')) {
                        this.write(92);
                        this.write(117);
                        this.write(IOUtils.DIGITS[ch >>> 12 & 0xF]);
                        this.write(IOUtils.DIGITS[ch >>> 8 & 0xF]);
                        this.write(IOUtils.DIGITS[ch >>> 4 & 0xF]);
                        this.write(IOUtils.DIGITS[ch & '\u000f']);
                    }
                    else {
                        if (this.isEnabled(SerializerFeature.BrowserCompatible)) {
                            if (ch == '\b' || ch == '\f' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '\"' || ch == '/' || ch == '\\') {
                                this.write(92);
                                this.write(IOUtils.replaceChars[ch]);
                                continue;
                            }
                            if (ch < ' ') {
                                this.write(92);
                                this.write(117);
                                this.write(48);
                                this.write(48);
                                this.write(IOUtils.ASCII_CHARS[ch * '\u0002']);
                                this.write(IOUtils.ASCII_CHARS[ch * '\u0002' + 1]);
                                continue;
                            }
                            if (ch >= '\u007f') {
                                this.write(92);
                                this.write(117);
                                this.write(IOUtils.DIGITS[ch >>> 12 & 0xF]);
                                this.write(IOUtils.DIGITS[ch >>> 8 & 0xF]);
                                this.write(IOUtils.DIGITS[ch >>> 4 & 0xF]);
                                this.write(IOUtils.DIGITS[ch & '\u000f']);
                                continue;
                            }
                        }
                        else if ((ch < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch] != 0) || (ch == '/' && this.isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                            this.write(92);
                            if (IOUtils.specicalFlags_doubleQuotes[ch] == 4) {
                                this.write(117);
                                this.write(IOUtils.DIGITS[ch >>> 12 & 0xF]);
                                this.write(IOUtils.DIGITS[ch >>> 8 & 0xF]);
                                this.write(IOUtils.DIGITS[ch >>> 4 & 0xF]);
                                this.write(IOUtils.DIGITS[ch & '\u000f']);
                                continue;
                            }
                            this.write(IOUtils.replaceChars[ch]);
                            continue;
                        }
                        this.write(ch);
                    }
                }
                this.write(34);
                if (seperator != '\0') {
                    this.write(seperator);
                }
                return;
            }
            this.expandCapacity(newcount);
        }
        final int start = this.count + 1;
        int end = start + len;
        this.buf[this.count] = '\"';
        text.getChars(0, len, this.buf, start);
        this.count = newcount;
        if (this.isEnabled(SerializerFeature.BrowserCompatible)) {
            int lastSpecialIndex = -1;
            for (int j = start; j < end; ++j) {
                final char ch2 = this.buf[j];
                if (ch2 == '\"' || ch2 == '/' || ch2 == '\\') {
                    lastSpecialIndex = j;
                    ++newcount;
                }
                else if (ch2 == '\b' || ch2 == '\f' || ch2 == '\n' || ch2 == '\r' || ch2 == '\t') {
                    lastSpecialIndex = j;
                    ++newcount;
                }
                else if (ch2 < ' ') {
                    lastSpecialIndex = j;
                    newcount += 5;
                }
                else if (ch2 >= '\u007f') {
                    lastSpecialIndex = j;
                    newcount += 5;
                }
            }
            if (newcount > this.buf.length) {
                this.expandCapacity(newcount);
            }
            this.count = newcount;
            for (int j = lastSpecialIndex; j >= start; --j) {
                final char ch2 = this.buf[j];
                if (ch2 == '\b' || ch2 == '\f' || ch2 == '\n' || ch2 == '\r' || ch2 == '\t') {
                    System.arraycopy(this.buf, j + 1, this.buf, j + 2, end - j - 1);
                    this.buf[j] = '\\';
                    this.buf[j + 1] = IOUtils.replaceChars[ch2];
                    ++end;
                }
                else if (ch2 == '\"' || ch2 == '/' || ch2 == '\\') {
                    System.arraycopy(this.buf, j + 1, this.buf, j + 2, end - j - 1);
                    this.buf[j] = '\\';
                    this.buf[j + 1] = ch2;
                    ++end;
                }
                else if (ch2 < ' ') {
                    System.arraycopy(this.buf, j + 1, this.buf, j + 6, end - j - 1);
                    this.buf[j] = '\\';
                    this.buf[j + 1] = 'u';
                    this.buf[j + 2] = '0';
                    this.buf[j + 3] = '0';
                    this.buf[j + 4] = IOUtils.ASCII_CHARS[ch2 * '\u0002'];
                    this.buf[j + 5] = IOUtils.ASCII_CHARS[ch2 * '\u0002' + 1];
                    end += 5;
                }
                else if (ch2 >= '\u007f') {
                    System.arraycopy(this.buf, j + 1, this.buf, j + 6, end - j - 1);
                    this.buf[j] = '\\';
                    this.buf[j + 1] = 'u';
                    this.buf[j + 2] = IOUtils.DIGITS[ch2 >>> 12 & 0xF];
                    this.buf[j + 3] = IOUtils.DIGITS[ch2 >>> 8 & 0xF];
                    this.buf[j + 4] = IOUtils.DIGITS[ch2 >>> 4 & 0xF];
                    this.buf[j + 5] = IOUtils.DIGITS[ch2 & '\u000f'];
                    end += 5;
                }
            }
            if (seperator != '\0') {
                this.buf[this.count - 2] = '\"';
                this.buf[this.count - 1] = seperator;
            }
            else {
                this.buf[this.count - 1] = '\"';
            }
            return;
        }
        int specialCount = 0;
        int lastSpecialIndex2 = -1;
        int firstSpecialIndex = -1;
        char lastSpecial = '\0';
        for (int k = start; k < end; ++k) {
            final char ch3 = this.buf[k];
            if (ch3 >= ']') {
                if (ch3 >= '\u007f' && (ch3 == '\u2028' || ch3 == '\u2029' || ch3 < ' ')) {
                    if (firstSpecialIndex == -1) {
                        firstSpecialIndex = k;
                    }
                    ++specialCount;
                    lastSpecialIndex2 = k;
                    lastSpecial = ch3;
                    newcount += 4;
                }
            }
            else {
                final boolean special = (ch3 < '@' && (this.sepcialBits & 1L << ch3) != 0x0L) || ch3 == '\\';
                if (special) {
                    ++specialCount;
                    lastSpecialIndex2 = k;
                    lastSpecial = ch3;
                    if (ch3 == '(' || ch3 == ')' || ch3 == '<' || ch3 == '>' || (ch3 < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch3] == 4)) {
                        newcount += 4;
                    }
                    if (firstSpecialIndex == -1) {
                        firstSpecialIndex = k;
                    }
                }
            }
        }
        if (specialCount > 0) {
            newcount += specialCount;
            if (newcount > this.buf.length) {
                this.expandCapacity(newcount);
            }
            this.count = newcount;
            if (specialCount == 1) {
                if (lastSpecial == '\u2028') {
                    final int srcPos = lastSpecialIndex2 + 1;
                    final int destPos = lastSpecialIndex2 + 6;
                    final int LengthOfCopy = end - lastSpecialIndex2 - 1;
                    System.arraycopy(this.buf, srcPos, this.buf, destPos, LengthOfCopy);
                    this.buf[lastSpecialIndex2] = '\\';
                    this.buf[++lastSpecialIndex2] = 'u';
                    this.buf[++lastSpecialIndex2] = '2';
                    this.buf[++lastSpecialIndex2] = '0';
                    this.buf[++lastSpecialIndex2] = '2';
                    this.buf[++lastSpecialIndex2] = '8';
                }
                else if (lastSpecial == '\u2029') {
                    final int srcPos = lastSpecialIndex2 + 1;
                    final int destPos = lastSpecialIndex2 + 6;
                    final int LengthOfCopy = end - lastSpecialIndex2 - 1;
                    System.arraycopy(this.buf, srcPos, this.buf, destPos, LengthOfCopy);
                    this.buf[lastSpecialIndex2] = '\\';
                    this.buf[++lastSpecialIndex2] = 'u';
                    this.buf[++lastSpecialIndex2] = '2';
                    this.buf[++lastSpecialIndex2] = '0';
                    this.buf[++lastSpecialIndex2] = '2';
                    this.buf[++lastSpecialIndex2] = '9';
                }
                else if (lastSpecial == '(' || lastSpecial == ')' || lastSpecial == '<' || lastSpecial == '>') {
                    final int srcPos = lastSpecialIndex2 + 1;
                    final int destPos = lastSpecialIndex2 + 6;
                    final int LengthOfCopy = end - lastSpecialIndex2 - 1;
                    System.arraycopy(this.buf, srcPos, this.buf, destPos, LengthOfCopy);
                    this.buf[lastSpecialIndex2] = '\\';
                    this.buf[++lastSpecialIndex2] = 'u';
                    final char ch4 = lastSpecial;
                    this.buf[++lastSpecialIndex2] = IOUtils.DIGITS[ch4 >>> 12 & 0xF];
                    this.buf[++lastSpecialIndex2] = IOUtils.DIGITS[ch4 >>> 8 & 0xF];
                    this.buf[++lastSpecialIndex2] = IOUtils.DIGITS[ch4 >>> 4 & 0xF];
                    this.buf[++lastSpecialIndex2] = IOUtils.DIGITS[ch4 & '\u000f'];
                }
                else {
                    final char ch5 = lastSpecial;
                    if (ch5 < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch5] == 4) {
                        final int srcPos2 = lastSpecialIndex2 + 1;
                        final int destPos2 = lastSpecialIndex2 + 6;
                        final int LengthOfCopy2 = end - lastSpecialIndex2 - 1;
                        System.arraycopy(this.buf, srcPos2, this.buf, destPos2, LengthOfCopy2);
                        int bufIndex = lastSpecialIndex2;
                        this.buf[bufIndex++] = '\\';
                        this.buf[bufIndex++] = 'u';
                        this.buf[bufIndex++] = IOUtils.DIGITS[ch5 >>> 12 & 0xF];
                        this.buf[bufIndex++] = IOUtils.DIGITS[ch5 >>> 8 & 0xF];
                        this.buf[bufIndex++] = IOUtils.DIGITS[ch5 >>> 4 & 0xF];
                        this.buf[bufIndex++] = IOUtils.DIGITS[ch5 & '\u000f'];
                    }
                    else {
                        final int srcPos2 = lastSpecialIndex2 + 1;
                        final int destPos2 = lastSpecialIndex2 + 2;
                        final int LengthOfCopy2 = end - lastSpecialIndex2 - 1;
                        System.arraycopy(this.buf, srcPos2, this.buf, destPos2, LengthOfCopy2);
                        this.buf[lastSpecialIndex2] = '\\';
                        this.buf[++lastSpecialIndex2] = IOUtils.replaceChars[ch5];
                    }
                }
            }
            else if (specialCount > 1) {
                final int textIndex = firstSpecialIndex - start;
                int bufIndex2 = firstSpecialIndex;
                for (int l = textIndex; l < text.length(); ++l) {
                    final char ch4 = text.charAt(l);
                    if (this.browserSecure && (ch4 == '(' || ch4 == ')' || ch4 == '<' || ch4 == '>')) {
                        this.buf[bufIndex2++] = '\\';
                        this.buf[bufIndex2++] = 'u';
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 12 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 8 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 4 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 & '\u000f'];
                        end += 5;
                    }
                    else if ((ch4 < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch4] != 0) || (ch4 == '/' && this.isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                        this.buf[bufIndex2++] = '\\';
                        if (IOUtils.specicalFlags_doubleQuotes[ch4] == 4) {
                            this.buf[bufIndex2++] = 'u';
                            this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 12 & 0xF];
                            this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 8 & 0xF];
                            this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 4 & 0xF];
                            this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 & '\u000f'];
                            end += 5;
                        }
                        else {
                            this.buf[bufIndex2++] = IOUtils.replaceChars[ch4];
                            ++end;
                        }
                    }
                    else if (ch4 == '\u2028' || ch4 == '\u2029') {
                        this.buf[bufIndex2++] = '\\';
                        this.buf[bufIndex2++] = 'u';
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 12 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 8 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 4 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 & '\u000f'];
                        end += 5;
                    }
                    else {
                        this.buf[bufIndex2++] = ch4;
                    }
                }
            }
        }
        if (seperator != '\0') {
            this.buf[this.count - 2] = '\"';
            this.buf[this.count - 1] = seperator;
        }
        else {
            this.buf[this.count - 1] = '\"';
        }
    }
    
    public void writeStringWithDoubleQuote(final char[] text, final char seperator) {
        if (text == null) {
            this.writeNull();
            if (seperator != '\0') {
                this.write(seperator);
            }
            return;
        }
        final int len = text.length;
        int newcount = this.count + len + 2;
        if (seperator != '\0') {
            ++newcount;
        }
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                this.write(34);
                for (int i = 0; i < text.length; ++i) {
                    final char ch = text[i];
                    if (this.isEnabled(SerializerFeature.BrowserSecure) && (ch == '(' || ch == ')' || ch == '<' || ch == '>')) {
                        this.write(92);
                        this.write(117);
                        this.write(IOUtils.DIGITS[ch >>> 12 & 0xF]);
                        this.write(IOUtils.DIGITS[ch >>> 8 & 0xF]);
                        this.write(IOUtils.DIGITS[ch >>> 4 & 0xF]);
                        this.write(IOUtils.DIGITS[ch & '\u000f']);
                    }
                    else {
                        if (this.isEnabled(SerializerFeature.BrowserCompatible)) {
                            if (ch == '\b' || ch == '\f' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '\"' || ch == '/' || ch == '\\') {
                                this.write(92);
                                this.write(IOUtils.replaceChars[ch]);
                                continue;
                            }
                            if (ch < ' ') {
                                this.write(92);
                                this.write(117);
                                this.write(48);
                                this.write(48);
                                this.write(IOUtils.ASCII_CHARS[ch * '\u0002']);
                                this.write(IOUtils.ASCII_CHARS[ch * '\u0002' + 1]);
                                continue;
                            }
                            if (ch >= '\u007f') {
                                this.write(92);
                                this.write(117);
                                this.write(IOUtils.DIGITS[ch >>> 12 & 0xF]);
                                this.write(IOUtils.DIGITS[ch >>> 8 & 0xF]);
                                this.write(IOUtils.DIGITS[ch >>> 4 & 0xF]);
                                this.write(IOUtils.DIGITS[ch & '\u000f']);
                                continue;
                            }
                        }
                        else if ((ch < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch] != 0) || (ch == '/' && this.isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                            this.write(92);
                            if (IOUtils.specicalFlags_doubleQuotes[ch] == 4) {
                                this.write(117);
                                this.write(IOUtils.DIGITS[ch >>> 12 & 0xF]);
                                this.write(IOUtils.DIGITS[ch >>> 8 & 0xF]);
                                this.write(IOUtils.DIGITS[ch >>> 4 & 0xF]);
                                this.write(IOUtils.DIGITS[ch & '\u000f']);
                                continue;
                            }
                            this.write(IOUtils.replaceChars[ch]);
                            continue;
                        }
                        this.write(ch);
                    }
                }
                this.write(34);
                if (seperator != '\0') {
                    this.write(seperator);
                }
                return;
            }
            this.expandCapacity(newcount);
        }
        final int start = this.count + 1;
        int end = start + len;
        this.buf[this.count] = '\"';
        System.arraycopy(text, 0, this.buf, start, text.length);
        this.count = newcount;
        if (this.isEnabled(SerializerFeature.BrowserCompatible)) {
            int lastSpecialIndex = -1;
            for (int j = start; j < end; ++j) {
                final char ch2 = this.buf[j];
                if (ch2 == '\"' || ch2 == '/' || ch2 == '\\') {
                    lastSpecialIndex = j;
                    ++newcount;
                }
                else if (ch2 == '\b' || ch2 == '\f' || ch2 == '\n' || ch2 == '\r' || ch2 == '\t') {
                    lastSpecialIndex = j;
                    ++newcount;
                }
                else if (ch2 < ' ') {
                    lastSpecialIndex = j;
                    newcount += 5;
                }
                else if (ch2 >= '\u007f') {
                    lastSpecialIndex = j;
                    newcount += 5;
                }
            }
            if (newcount > this.buf.length) {
                this.expandCapacity(newcount);
            }
            this.count = newcount;
            for (int j = lastSpecialIndex; j >= start; --j) {
                final char ch2 = this.buf[j];
                if (ch2 == '\b' || ch2 == '\f' || ch2 == '\n' || ch2 == '\r' || ch2 == '\t') {
                    System.arraycopy(this.buf, j + 1, this.buf, j + 2, end - j - 1);
                    this.buf[j] = '\\';
                    this.buf[j + 1] = IOUtils.replaceChars[ch2];
                    ++end;
                }
                else if (ch2 == '\"' || ch2 == '/' || ch2 == '\\') {
                    System.arraycopy(this.buf, j + 1, this.buf, j + 2, end - j - 1);
                    this.buf[j] = '\\';
                    this.buf[j + 1] = ch2;
                    ++end;
                }
                else if (ch2 < ' ') {
                    System.arraycopy(this.buf, j + 1, this.buf, j + 6, end - j - 1);
                    this.buf[j] = '\\';
                    this.buf[j + 1] = 'u';
                    this.buf[j + 2] = '0';
                    this.buf[j + 3] = '0';
                    this.buf[j + 4] = IOUtils.ASCII_CHARS[ch2 * '\u0002'];
                    this.buf[j + 5] = IOUtils.ASCII_CHARS[ch2 * '\u0002' + 1];
                    end += 5;
                }
                else if (ch2 >= '\u007f') {
                    System.arraycopy(this.buf, j + 1, this.buf, j + 6, end - j - 1);
                    this.buf[j] = '\\';
                    this.buf[j + 1] = 'u';
                    this.buf[j + 2] = IOUtils.DIGITS[ch2 >>> 12 & 0xF];
                    this.buf[j + 3] = IOUtils.DIGITS[ch2 >>> 8 & 0xF];
                    this.buf[j + 4] = IOUtils.DIGITS[ch2 >>> 4 & 0xF];
                    this.buf[j + 5] = IOUtils.DIGITS[ch2 & '\u000f'];
                    end += 5;
                }
            }
            if (seperator != '\0') {
                this.buf[this.count - 2] = '\"';
                this.buf[this.count - 1] = seperator;
            }
            else {
                this.buf[this.count - 1] = '\"';
            }
            return;
        }
        int specialCount = 0;
        int lastSpecialIndex2 = -1;
        int firstSpecialIndex = -1;
        char lastSpecial = '\0';
        for (int k = start; k < end; ++k) {
            final char ch3 = this.buf[k];
            if (ch3 >= ']') {
                if (ch3 >= '\u007f' && (ch3 == '\u2028' || ch3 == '\u2029' || ch3 < ' ')) {
                    if (firstSpecialIndex == -1) {
                        firstSpecialIndex = k;
                    }
                    ++specialCount;
                    lastSpecialIndex2 = k;
                    lastSpecial = ch3;
                    newcount += 4;
                }
            }
            else {
                final boolean special = (ch3 < '@' && (this.sepcialBits & 1L << ch3) != 0x0L) || ch3 == '\\';
                if (special) {
                    ++specialCount;
                    lastSpecialIndex2 = k;
                    lastSpecial = ch3;
                    if (ch3 == '(' || ch3 == ')' || ch3 == '<' || ch3 == '>' || (ch3 < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch3] == 4)) {
                        newcount += 4;
                    }
                    if (firstSpecialIndex == -1) {
                        firstSpecialIndex = k;
                    }
                }
            }
        }
        if (specialCount > 0) {
            newcount += specialCount;
            if (newcount > this.buf.length) {
                this.expandCapacity(newcount);
            }
            this.count = newcount;
            if (specialCount == 1) {
                if (lastSpecial == '\u2028') {
                    final int srcPos = lastSpecialIndex2 + 1;
                    final int destPos = lastSpecialIndex2 + 6;
                    final int LengthOfCopy = end - lastSpecialIndex2 - 1;
                    System.arraycopy(this.buf, srcPos, this.buf, destPos, LengthOfCopy);
                    this.buf[lastSpecialIndex2] = '\\';
                    this.buf[++lastSpecialIndex2] = 'u';
                    this.buf[++lastSpecialIndex2] = '2';
                    this.buf[++lastSpecialIndex2] = '0';
                    this.buf[++lastSpecialIndex2] = '2';
                    this.buf[++lastSpecialIndex2] = '8';
                }
                else if (lastSpecial == '\u2029') {
                    final int srcPos = lastSpecialIndex2 + 1;
                    final int destPos = lastSpecialIndex2 + 6;
                    final int LengthOfCopy = end - lastSpecialIndex2 - 1;
                    System.arraycopy(this.buf, srcPos, this.buf, destPos, LengthOfCopy);
                    this.buf[lastSpecialIndex2] = '\\';
                    this.buf[++lastSpecialIndex2] = 'u';
                    this.buf[++lastSpecialIndex2] = '2';
                    this.buf[++lastSpecialIndex2] = '0';
                    this.buf[++lastSpecialIndex2] = '2';
                    this.buf[++lastSpecialIndex2] = '9';
                }
                else if (lastSpecial == '(' || lastSpecial == ')' || lastSpecial == '<' || lastSpecial == '>') {
                    final int srcPos = lastSpecialIndex2 + 1;
                    final int destPos = lastSpecialIndex2 + 6;
                    final int LengthOfCopy = end - lastSpecialIndex2 - 1;
                    System.arraycopy(this.buf, srcPos, this.buf, destPos, LengthOfCopy);
                    this.buf[lastSpecialIndex2] = '\\';
                    this.buf[++lastSpecialIndex2] = 'u';
                    final char ch4 = lastSpecial;
                    this.buf[++lastSpecialIndex2] = IOUtils.DIGITS[ch4 >>> 12 & 0xF];
                    this.buf[++lastSpecialIndex2] = IOUtils.DIGITS[ch4 >>> 8 & 0xF];
                    this.buf[++lastSpecialIndex2] = IOUtils.DIGITS[ch4 >>> 4 & 0xF];
                    this.buf[++lastSpecialIndex2] = IOUtils.DIGITS[ch4 & '\u000f'];
                }
                else {
                    final char ch5 = lastSpecial;
                    if (ch5 < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch5] == 4) {
                        final int srcPos2 = lastSpecialIndex2 + 1;
                        final int destPos2 = lastSpecialIndex2 + 6;
                        final int LengthOfCopy2 = end - lastSpecialIndex2 - 1;
                        System.arraycopy(this.buf, srcPos2, this.buf, destPos2, LengthOfCopy2);
                        int bufIndex = lastSpecialIndex2;
                        this.buf[bufIndex++] = '\\';
                        this.buf[bufIndex++] = 'u';
                        this.buf[bufIndex++] = IOUtils.DIGITS[ch5 >>> 12 & 0xF];
                        this.buf[bufIndex++] = IOUtils.DIGITS[ch5 >>> 8 & 0xF];
                        this.buf[bufIndex++] = IOUtils.DIGITS[ch5 >>> 4 & 0xF];
                        this.buf[bufIndex++] = IOUtils.DIGITS[ch5 & '\u000f'];
                    }
                    else {
                        final int srcPos2 = lastSpecialIndex2 + 1;
                        final int destPos2 = lastSpecialIndex2 + 2;
                        final int LengthOfCopy2 = end - lastSpecialIndex2 - 1;
                        System.arraycopy(this.buf, srcPos2, this.buf, destPos2, LengthOfCopy2);
                        this.buf[lastSpecialIndex2] = '\\';
                        this.buf[++lastSpecialIndex2] = IOUtils.replaceChars[ch5];
                    }
                }
            }
            else if (specialCount > 1) {
                final int textIndex = firstSpecialIndex - start;
                int bufIndex2 = firstSpecialIndex;
                for (int l = textIndex; l < text.length; ++l) {
                    final char ch4 = text[l];
                    if (this.browserSecure && (ch4 == '(' || ch4 == ')' || ch4 == '<' || ch4 == '>')) {
                        this.buf[bufIndex2++] = '\\';
                        this.buf[bufIndex2++] = 'u';
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 12 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 8 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 4 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 & '\u000f'];
                        end += 5;
                    }
                    else if ((ch4 < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch4] != 0) || (ch4 == '/' && this.isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                        this.buf[bufIndex2++] = '\\';
                        if (IOUtils.specicalFlags_doubleQuotes[ch4] == 4) {
                            this.buf[bufIndex2++] = 'u';
                            this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 12 & 0xF];
                            this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 8 & 0xF];
                            this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 4 & 0xF];
                            this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 & '\u000f'];
                            end += 5;
                        }
                        else {
                            this.buf[bufIndex2++] = IOUtils.replaceChars[ch4];
                            ++end;
                        }
                    }
                    else if (ch4 == '\u2028' || ch4 == '\u2029') {
                        this.buf[bufIndex2++] = '\\';
                        this.buf[bufIndex2++] = 'u';
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 12 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 8 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 >>> 4 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch4 & '\u000f'];
                        end += 5;
                    }
                    else {
                        this.buf[bufIndex2++] = ch4;
                    }
                }
            }
        }
        if (seperator != '\0') {
            this.buf[this.count - 2] = '\"';
            this.buf[this.count - 1] = seperator;
        }
        else {
            this.buf[this.count - 1] = '\"';
        }
    }
    
    public void writeFieldNameDirect(final String text) {
        final int len = text.length();
        final int newcount = this.count + len + 3;
        if (newcount > this.buf.length) {
            this.expandCapacity(newcount);
        }
        final int start = this.count + 1;
        this.buf[this.count] = '\"';
        text.getChars(0, len, this.buf, start);
        this.count = newcount;
        this.buf[this.count - 2] = '\"';
        this.buf[this.count - 1] = ':';
    }
    
    public void write(final List<String> list) {
        if (list.isEmpty()) {
            this.write("[]");
            return;
        }
        final int initOffset;
        int offset = initOffset = this.count;
        for (int i = 0, list_size = list.size(); i < list_size; ++i) {
            String text = list.get(i);
            boolean hasSpecial = false;
            if (text == null) {
                hasSpecial = true;
            }
            else {
                for (int j = 0, len = text.length(); j < len; ++j) {
                    final char ch = text.charAt(j);
                    if (hasSpecial = (ch < ' ' || ch > '~' || ch == '\"' || ch == '\\')) {
                        break;
                    }
                }
            }
            if (hasSpecial) {
                this.count = initOffset;
                this.write(91);
                for (int j = 0; j < list.size(); ++j) {
                    text = list.get(j);
                    if (j != 0) {
                        this.write(44);
                    }
                    if (text == null) {
                        this.write("null");
                    }
                    else {
                        this.writeStringWithDoubleQuote(text, '\0');
                    }
                }
                this.write(93);
                return;
            }
            int newcount = offset + text.length() + 3;
            if (i == list.size() - 1) {
                ++newcount;
            }
            if (newcount > this.buf.length) {
                this.count = offset;
                this.expandCapacity(newcount);
            }
            if (i == 0) {
                this.buf[offset++] = '[';
            }
            else {
                this.buf[offset++] = ',';
            }
            this.buf[offset++] = '\"';
            text.getChars(0, text.length(), this.buf, offset);
            offset += text.length();
            this.buf[offset++] = '\"';
        }
        this.buf[offset++] = ']';
        this.count = offset;
    }
    
    public void writeFieldValue(final char seperator, final String name, final char value) {
        this.write(seperator);
        this.writeFieldName(name);
        if (value == '\0') {
            this.writeString("\u0000");
        }
        else {
            this.writeString(Character.toString(value));
        }
    }
    
    public void writeFieldValue(final char seperator, final String name, final boolean value) {
        if (!this.quoteFieldNames) {
            this.write(seperator);
            this.writeFieldName(name);
            this.write(value);
            return;
        }
        final int intSize = value ? 4 : 5;
        final int nameLen = name.length();
        final int newcount = this.count + nameLen + 4 + intSize;
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                this.write(seperator);
                this.writeString(name);
                this.write(58);
                this.write(value);
                return;
            }
            this.expandCapacity(newcount);
        }
        final int start = this.count;
        this.count = newcount;
        this.buf[start] = seperator;
        final int nameEnd = start + nameLen + 1;
        this.buf[start + 1] = this.keySeperator;
        name.getChars(0, nameLen, this.buf, start + 2);
        this.buf[nameEnd + 1] = this.keySeperator;
        if (value) {
            System.arraycopy(":true".toCharArray(), 0, this.buf, nameEnd + 2, 5);
        }
        else {
            System.arraycopy(":false".toCharArray(), 0, this.buf, nameEnd + 2, 6);
        }
    }
    
    public void write(final boolean value) {
        if (value) {
            this.write("true");
        }
        else {
            this.write("false");
        }
    }
    
    public void writeFieldValue(final char seperator, final String name, final int value) {
        if (value == Integer.MIN_VALUE || !this.quoteFieldNames) {
            this.write(seperator);
            this.writeFieldName(name);
            this.writeInt(value);
            return;
        }
        final int intSize = (value < 0) ? (IOUtils.stringSize(-value) + 1) : IOUtils.stringSize(value);
        final int nameLen = name.length();
        final int newcount = this.count + nameLen + 4 + intSize;
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                this.write(seperator);
                this.writeFieldName(name);
                this.writeInt(value);
                return;
            }
            this.expandCapacity(newcount);
        }
        final int start = this.count;
        this.count = newcount;
        this.buf[start] = seperator;
        final int nameEnd = start + nameLen + 1;
        this.buf[start + 1] = this.keySeperator;
        name.getChars(0, nameLen, this.buf, start + 2);
        this.buf[nameEnd + 1] = this.keySeperator;
        this.buf[nameEnd + 2] = ':';
        IOUtils.getChars(value, this.count, this.buf);
    }
    
    public void writeFieldValue(final char seperator, final String name, final long value) {
        if (value == Long.MIN_VALUE || !this.quoteFieldNames || this.isEnabled(SerializerFeature.BrowserCompatible.mask)) {
            this.write(seperator);
            this.writeFieldName(name);
            this.writeLong(value);
            return;
        }
        final int intSize = (value < 0L) ? (IOUtils.stringSize(-value) + 1) : IOUtils.stringSize(value);
        final int nameLen = name.length();
        final int newcount = this.count + nameLen + 4 + intSize;
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                this.write(seperator);
                this.writeFieldName(name);
                this.writeLong(value);
                return;
            }
            this.expandCapacity(newcount);
        }
        final int start = this.count;
        this.count = newcount;
        this.buf[start] = seperator;
        final int nameEnd = start + nameLen + 1;
        this.buf[start + 1] = this.keySeperator;
        name.getChars(0, nameLen, this.buf, start + 2);
        this.buf[nameEnd + 1] = this.keySeperator;
        this.buf[nameEnd + 2] = ':';
        IOUtils.getChars(value, this.count, this.buf);
    }
    
    public void writeFieldValue(final char seperator, final String name, final float value) {
        this.write(seperator);
        this.writeFieldName(name);
        this.writeFloat(value, false);
    }
    
    public void writeFieldValue(final char seperator, final String name, final double value) {
        this.write(seperator);
        this.writeFieldName(name);
        this.writeDouble(value, false);
    }
    
    public void writeFieldValue(final char seperator, final String name, final String value) {
        if (this.quoteFieldNames) {
            if (this.useSingleQuotes) {
                this.write(seperator);
                this.writeFieldName(name);
                if (value == null) {
                    this.writeNull();
                }
                else {
                    this.writeString(value);
                }
            }
            else if (this.isEnabled(SerializerFeature.BrowserCompatible)) {
                this.write(seperator);
                this.writeStringWithDoubleQuote(name, ':');
                this.writeStringWithDoubleQuote(value, '\0');
            }
            else {
                this.writeFieldValueStringWithDoubleQuoteCheck(seperator, name, value);
            }
        }
        else {
            this.write(seperator);
            this.writeFieldName(name);
            if (value == null) {
                this.writeNull();
            }
            else {
                this.writeString(value);
            }
        }
    }
    
    public void writeFieldValueStringWithDoubleQuoteCheck(final char seperator, final String name, final String value) {
        final int nameLen = name.length();
        int newcount = this.count;
        int valueLen;
        if (value == null) {
            valueLen = 4;
            newcount += nameLen + 8;
        }
        else {
            valueLen = value.length();
            newcount += nameLen + valueLen + 6;
        }
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                this.write(seperator);
                this.writeStringWithDoubleQuote(name, ':');
                this.writeStringWithDoubleQuote(value, '\0');
                return;
            }
            this.expandCapacity(newcount);
        }
        this.buf[this.count] = seperator;
        final int nameStart = this.count + 2;
        final int nameEnd = nameStart + nameLen;
        this.buf[this.count + 1] = '\"';
        name.getChars(0, nameLen, this.buf, nameStart);
        this.count = newcount;
        this.buf[nameEnd] = '\"';
        int index = nameEnd + 1;
        this.buf[index++] = ':';
        if (value == null) {
            this.buf[index++] = 'n';
            this.buf[index++] = 'u';
            this.buf[index++] = 'l';
            this.buf[index++] = 'l';
            return;
        }
        this.buf[index++] = '\"';
        final int valueStart = index;
        int valueEnd = valueStart + valueLen;
        value.getChars(0, valueLen, this.buf, valueStart);
        int specialCount = 0;
        int lastSpecialIndex = -1;
        int firstSpecialIndex = -1;
        char lastSpecial = '\0';
        for (int i = valueStart; i < valueEnd; ++i) {
            final char ch = this.buf[i];
            if (ch >= ']') {
                if (ch >= '\u007f' && (ch == '\u2028' || ch == '\u2029' || ch < ' ')) {
                    if (firstSpecialIndex == -1) {
                        firstSpecialIndex = i;
                    }
                    ++specialCount;
                    lastSpecialIndex = i;
                    lastSpecial = ch;
                    newcount += 4;
                }
            }
            else {
                final boolean special = (ch < '@' && (this.sepcialBits & 1L << ch) != 0x0L) || ch == '\\';
                if (special) {
                    ++specialCount;
                    lastSpecialIndex = i;
                    lastSpecial = ch;
                    if (ch == '(' || ch == ')' || ch == '<' || ch == '>' || (ch < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch] == 4)) {
                        newcount += 4;
                    }
                    if (firstSpecialIndex == -1) {
                        firstSpecialIndex = i;
                    }
                }
            }
        }
        if (specialCount > 0) {
            newcount += specialCount;
            if (newcount > this.buf.length) {
                this.expandCapacity(newcount);
            }
            this.count = newcount;
            if (specialCount == 1) {
                if (lastSpecial == '\u2028') {
                    final int srcPos = lastSpecialIndex + 1;
                    final int destPos = lastSpecialIndex + 6;
                    final int LengthOfCopy = valueEnd - lastSpecialIndex - 1;
                    System.arraycopy(this.buf, srcPos, this.buf, destPos, LengthOfCopy);
                    this.buf[lastSpecialIndex] = '\\';
                    this.buf[++lastSpecialIndex] = 'u';
                    this.buf[++lastSpecialIndex] = '2';
                    this.buf[++lastSpecialIndex] = '0';
                    this.buf[++lastSpecialIndex] = '2';
                    this.buf[++lastSpecialIndex] = '8';
                }
                else if (lastSpecial == '\u2029') {
                    final int srcPos = lastSpecialIndex + 1;
                    final int destPos = lastSpecialIndex + 6;
                    final int LengthOfCopy = valueEnd - lastSpecialIndex - 1;
                    System.arraycopy(this.buf, srcPos, this.buf, destPos, LengthOfCopy);
                    this.buf[lastSpecialIndex] = '\\';
                    this.buf[++lastSpecialIndex] = 'u';
                    this.buf[++lastSpecialIndex] = '2';
                    this.buf[++lastSpecialIndex] = '0';
                    this.buf[++lastSpecialIndex] = '2';
                    this.buf[++lastSpecialIndex] = '9';
                }
                else if (lastSpecial == '(' || lastSpecial == ')' || lastSpecial == '<' || lastSpecial == '>') {
                    final char ch2 = lastSpecial;
                    final int srcPos2 = lastSpecialIndex + 1;
                    final int destPos2 = lastSpecialIndex + 6;
                    final int LengthOfCopy2 = valueEnd - lastSpecialIndex - 1;
                    System.arraycopy(this.buf, srcPos2, this.buf, destPos2, LengthOfCopy2);
                    int bufIndex = lastSpecialIndex;
                    this.buf[bufIndex++] = '\\';
                    this.buf[bufIndex++] = 'u';
                    this.buf[bufIndex++] = IOUtils.DIGITS[ch2 >>> 12 & 0xF];
                    this.buf[bufIndex++] = IOUtils.DIGITS[ch2 >>> 8 & 0xF];
                    this.buf[bufIndex++] = IOUtils.DIGITS[ch2 >>> 4 & 0xF];
                    this.buf[bufIndex++] = IOUtils.DIGITS[ch2 & '\u000f'];
                }
                else {
                    final char ch2 = lastSpecial;
                    if (ch2 < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch2] == 4) {
                        final int srcPos2 = lastSpecialIndex + 1;
                        final int destPos2 = lastSpecialIndex + 6;
                        final int LengthOfCopy2 = valueEnd - lastSpecialIndex - 1;
                        System.arraycopy(this.buf, srcPos2, this.buf, destPos2, LengthOfCopy2);
                        int bufIndex = lastSpecialIndex;
                        this.buf[bufIndex++] = '\\';
                        this.buf[bufIndex++] = 'u';
                        this.buf[bufIndex++] = IOUtils.DIGITS[ch2 >>> 12 & 0xF];
                        this.buf[bufIndex++] = IOUtils.DIGITS[ch2 >>> 8 & 0xF];
                        this.buf[bufIndex++] = IOUtils.DIGITS[ch2 >>> 4 & 0xF];
                        this.buf[bufIndex++] = IOUtils.DIGITS[ch2 & '\u000f'];
                    }
                    else {
                        final int srcPos2 = lastSpecialIndex + 1;
                        final int destPos2 = lastSpecialIndex + 2;
                        final int LengthOfCopy2 = valueEnd - lastSpecialIndex - 1;
                        System.arraycopy(this.buf, srcPos2, this.buf, destPos2, LengthOfCopy2);
                        this.buf[lastSpecialIndex] = '\\';
                        this.buf[++lastSpecialIndex] = IOUtils.replaceChars[ch2];
                    }
                }
            }
            else if (specialCount > 1) {
                final int textIndex = firstSpecialIndex - valueStart;
                int bufIndex2 = firstSpecialIndex;
                for (int j = textIndex; j < value.length(); ++j) {
                    final char ch3 = value.charAt(j);
                    if (this.browserSecure && (ch3 == '(' || ch3 == ')' || ch3 == '<' || ch3 == '>')) {
                        this.buf[bufIndex2++] = '\\';
                        this.buf[bufIndex2++] = 'u';
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch3 >>> 12 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch3 >>> 8 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch3 >>> 4 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch3 & '\u000f'];
                        valueEnd += 5;
                    }
                    else if ((ch3 < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch3] != 0) || (ch3 == '/' && this.isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                        this.buf[bufIndex2++] = '\\';
                        if (IOUtils.specicalFlags_doubleQuotes[ch3] == 4) {
                            this.buf[bufIndex2++] = 'u';
                            this.buf[bufIndex2++] = IOUtils.DIGITS[ch3 >>> 12 & 0xF];
                            this.buf[bufIndex2++] = IOUtils.DIGITS[ch3 >>> 8 & 0xF];
                            this.buf[bufIndex2++] = IOUtils.DIGITS[ch3 >>> 4 & 0xF];
                            this.buf[bufIndex2++] = IOUtils.DIGITS[ch3 & '\u000f'];
                            valueEnd += 5;
                        }
                        else {
                            this.buf[bufIndex2++] = IOUtils.replaceChars[ch3];
                            ++valueEnd;
                        }
                    }
                    else if (ch3 == '\u2028' || ch3 == '\u2029') {
                        this.buf[bufIndex2++] = '\\';
                        this.buf[bufIndex2++] = 'u';
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch3 >>> 12 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch3 >>> 8 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch3 >>> 4 & 0xF];
                        this.buf[bufIndex2++] = IOUtils.DIGITS[ch3 & '\u000f'];
                        valueEnd += 5;
                    }
                    else {
                        this.buf[bufIndex2++] = ch3;
                    }
                }
            }
        }
        this.buf[this.count - 1] = '\"';
    }
    
    public void writeFieldValueStringWithDoubleQuote(final char seperator, final String name, final String value) {
        final int nameLen = name.length();
        int newcount = this.count;
        final int valueLen = value.length();
        newcount += nameLen + valueLen + 6;
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                this.write(seperator);
                this.writeStringWithDoubleQuote(name, ':');
                this.writeStringWithDoubleQuote(value, '\0');
                return;
            }
            this.expandCapacity(newcount);
        }
        this.buf[this.count] = seperator;
        final int nameStart = this.count + 2;
        final int nameEnd = nameStart + nameLen;
        this.buf[this.count + 1] = '\"';
        name.getChars(0, nameLen, this.buf, nameStart);
        this.count = newcount;
        this.buf[nameEnd] = '\"';
        int index = nameEnd + 1;
        this.buf[index++] = ':';
        this.buf[index++] = '\"';
        final int valueStart = index;
        value.getChars(0, valueLen, this.buf, valueStart);
        this.buf[this.count - 1] = '\"';
    }
    
    public void writeFieldValue(final char seperator, final String name, final Enum<?> value) {
        if (value == null) {
            this.write(seperator);
            this.writeFieldName(name);
            this.writeNull();
            return;
        }
        if (this.writeEnumUsingName && !this.writeEnumUsingToString) {
            this.writeEnumFieldValue(seperator, name, value.name());
        }
        else if (this.writeEnumUsingToString) {
            this.writeEnumFieldValue(seperator, name, value.toString());
        }
        else {
            this.writeFieldValue(seperator, name, value.ordinal());
        }
    }
    
    private void writeEnumFieldValue(final char seperator, final String name, final String value) {
        if (this.useSingleQuotes) {
            this.writeFieldValue(seperator, name, value);
        }
        else {
            this.writeFieldValueStringWithDoubleQuote(seperator, name, value);
        }
    }
    
    public void writeFieldValue(final char seperator, final String name, final BigDecimal value) {
        this.write(seperator);
        this.writeFieldName(name);
        if (value == null) {
            this.writeNull();
        }
        else {
            final int scale = value.scale();
            this.write((this.isEnabled(SerializerFeature.WriteBigDecimalAsPlain) && scale >= -100 && scale < 100) ? value.toPlainString() : value.toString());
        }
    }
    
    public void writeString(final String text, final char seperator) {
        if (this.useSingleQuotes) {
            this.writeStringWithSingleQuote(text);
            this.write(seperator);
        }
        else {
            this.writeStringWithDoubleQuote(text, seperator);
        }
    }
    
    public void writeString(final String text) {
        if (this.useSingleQuotes) {
            this.writeStringWithSingleQuote(text);
        }
        else {
            this.writeStringWithDoubleQuote(text, '\0');
        }
    }
    
    public void writeString(final char[] chars) {
        if (this.useSingleQuotes) {
            this.writeStringWithSingleQuote(chars);
        }
        else {
            final String text = new String(chars);
            this.writeStringWithDoubleQuote(text, '\0');
        }
    }
    
    protected void writeStringWithSingleQuote(final String text) {
        if (text == null) {
            final int newcount = this.count + 4;
            if (newcount > this.buf.length) {
                this.expandCapacity(newcount);
            }
            "null".getChars(0, 4, this.buf, this.count);
            this.count = newcount;
            return;
        }
        final int len = text.length();
        int newcount2 = this.count + len + 2;
        if (newcount2 > this.buf.length) {
            if (this.writer != null) {
                this.write(39);
                for (int i = 0; i < text.length(); ++i) {
                    final char ch = text.charAt(i);
                    if (ch <= '\r' || ch == '\\' || ch == '\'' || (ch == '/' && this.isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                        this.write(92);
                        this.write(IOUtils.replaceChars[ch]);
                    }
                    else {
                        this.write(ch);
                    }
                }
                this.write(39);
                return;
            }
            this.expandCapacity(newcount2);
        }
        final int start = this.count + 1;
        int end = start + len;
        this.buf[this.count] = '\'';
        text.getChars(0, len, this.buf, start);
        this.count = newcount2;
        int specialCount = 0;
        int lastSpecialIndex = -1;
        char lastSpecial = '\0';
        for (int j = start; j < end; ++j) {
            final char ch2 = this.buf[j];
            if (ch2 <= '\r' || ch2 == '\\' || ch2 == '\'' || (ch2 == '/' && this.isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                ++specialCount;
                lastSpecialIndex = j;
                lastSpecial = ch2;
            }
        }
        newcount2 += specialCount;
        if (newcount2 > this.buf.length) {
            this.expandCapacity(newcount2);
        }
        this.count = newcount2;
        if (specialCount == 1) {
            System.arraycopy(this.buf, lastSpecialIndex + 1, this.buf, lastSpecialIndex + 2, end - lastSpecialIndex - 1);
            this.buf[lastSpecialIndex] = '\\';
            this.buf[++lastSpecialIndex] = IOUtils.replaceChars[lastSpecial];
        }
        else if (specialCount > 1) {
            System.arraycopy(this.buf, lastSpecialIndex + 1, this.buf, lastSpecialIndex + 2, end - lastSpecialIndex - 1);
            this.buf[lastSpecialIndex] = '\\';
            this.buf[++lastSpecialIndex] = IOUtils.replaceChars[lastSpecial];
            ++end;
            for (int j = lastSpecialIndex - 2; j >= start; --j) {
                final char ch2 = this.buf[j];
                if (ch2 <= '\r' || ch2 == '\\' || ch2 == '\'' || (ch2 == '/' && this.isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                    System.arraycopy(this.buf, j + 1, this.buf, j + 2, end - j - 1);
                    this.buf[j] = '\\';
                    this.buf[j + 1] = IOUtils.replaceChars[ch2];
                    ++end;
                }
            }
        }
        this.buf[this.count - 1] = '\'';
    }
    
    protected void writeStringWithSingleQuote(final char[] chars) {
        if (chars == null) {
            final int newcount = this.count + 4;
            if (newcount > this.buf.length) {
                this.expandCapacity(newcount);
            }
            "null".getChars(0, 4, this.buf, this.count);
            this.count = newcount;
            return;
        }
        final int len = chars.length;
        int newcount2 = this.count + len + 2;
        if (newcount2 > this.buf.length) {
            if (this.writer != null) {
                this.write(39);
                for (int i = 0; i < chars.length; ++i) {
                    final char ch = chars[i];
                    if (ch <= '\r' || ch == '\\' || ch == '\'' || (ch == '/' && this.isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                        this.write(92);
                        this.write(IOUtils.replaceChars[ch]);
                    }
                    else {
                        this.write(ch);
                    }
                }
                this.write(39);
                return;
            }
            this.expandCapacity(newcount2);
        }
        final int start = this.count + 1;
        int end = start + len;
        this.buf[this.count] = '\'';
        System.arraycopy(chars, 0, this.buf, start, chars.length);
        this.count = newcount2;
        int specialCount = 0;
        int lastSpecialIndex = -1;
        char lastSpecial = '\0';
        for (int j = start; j < end; ++j) {
            final char ch2 = this.buf[j];
            if (ch2 <= '\r' || ch2 == '\\' || ch2 == '\'' || (ch2 == '/' && this.isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                ++specialCount;
                lastSpecialIndex = j;
                lastSpecial = ch2;
            }
        }
        newcount2 += specialCount;
        if (newcount2 > this.buf.length) {
            this.expandCapacity(newcount2);
        }
        this.count = newcount2;
        if (specialCount == 1) {
            System.arraycopy(this.buf, lastSpecialIndex + 1, this.buf, lastSpecialIndex + 2, end - lastSpecialIndex - 1);
            this.buf[lastSpecialIndex] = '\\';
            this.buf[++lastSpecialIndex] = IOUtils.replaceChars[lastSpecial];
        }
        else if (specialCount > 1) {
            System.arraycopy(this.buf, lastSpecialIndex + 1, this.buf, lastSpecialIndex + 2, end - lastSpecialIndex - 1);
            this.buf[lastSpecialIndex] = '\\';
            this.buf[++lastSpecialIndex] = IOUtils.replaceChars[lastSpecial];
            ++end;
            for (int j = lastSpecialIndex - 2; j >= start; --j) {
                final char ch2 = this.buf[j];
                if (ch2 <= '\r' || ch2 == '\\' || ch2 == '\'' || (ch2 == '/' && this.isEnabled(SerializerFeature.WriteSlashAsSpecial))) {
                    System.arraycopy(this.buf, j + 1, this.buf, j + 2, end - j - 1);
                    this.buf[j] = '\\';
                    this.buf[j + 1] = IOUtils.replaceChars[ch2];
                    ++end;
                }
            }
        }
        this.buf[this.count - 1] = '\'';
    }
    
    public void writeFieldName(final String key) {
        this.writeFieldName(key, false);
    }
    
    public void writeFieldName(final String key, final boolean checkSpecial) {
        if (key == null) {
            this.write("null:");
            return;
        }
        if (this.useSingleQuotes) {
            if (this.quoteFieldNames) {
                this.writeStringWithSingleQuote(key);
                this.write(58);
            }
            else {
                this.writeKeyWithSingleQuoteIfHasSpecial(key);
            }
        }
        else if (this.quoteFieldNames) {
            this.writeStringWithDoubleQuote(key, ':');
        }
        else {
            boolean hashSpecial = key.length() == 0;
            for (int i = 0; i < key.length(); ++i) {
                final char ch = key.charAt(i);
                final boolean special = (ch < '@' && (this.sepcialBits & 1L << ch) != 0x0L) || ch == '\\';
                if (special) {
                    hashSpecial = true;
                    break;
                }
            }
            if (hashSpecial) {
                this.writeStringWithDoubleQuote(key, ':');
            }
            else {
                this.write(key);
                this.write(58);
            }
        }
    }
    
    private void writeKeyWithSingleQuoteIfHasSpecial(final String text) {
        final byte[] specicalFlags_singleQuotes = IOUtils.specicalFlags_singleQuotes;
        final int len = text.length();
        int newcount = this.count + len + 1;
        if (newcount > this.buf.length) {
            if (this.writer != null) {
                if (len == 0) {
                    this.write(39);
                    this.write(39);
                    this.write(58);
                    return;
                }
                boolean hasSpecial = false;
                for (int i = 0; i < len; ++i) {
                    final char ch = text.charAt(i);
                    if (ch < specicalFlags_singleQuotes.length && specicalFlags_singleQuotes[ch] != 0) {
                        hasSpecial = true;
                        break;
                    }
                }
                if (hasSpecial) {
                    this.write(39);
                }
                for (int i = 0; i < len; ++i) {
                    final char ch = text.charAt(i);
                    if (ch < specicalFlags_singleQuotes.length && specicalFlags_singleQuotes[ch] != 0) {
                        this.write(92);
                        this.write(IOUtils.replaceChars[ch]);
                    }
                    else {
                        this.write(ch);
                    }
                }
                if (hasSpecial) {
                    this.write(39);
                }
                this.write(58);
                return;
            }
            else {
                this.expandCapacity(newcount);
            }
        }
        if (len == 0) {
            final int newCount = this.count + 3;
            if (newCount > this.buf.length) {
                this.expandCapacity(this.count + 3);
            }
            this.buf[this.count++] = '\'';
            this.buf[this.count++] = '\'';
            this.buf[this.count++] = ':';
            return;
        }
        final int start = this.count;
        int end = start + len;
        text.getChars(0, len, this.buf, start);
        this.count = newcount;
        boolean hasSpecial2 = false;
        for (int j = start; j < end; ++j) {
            final char ch2 = this.buf[j];
            if (ch2 < specicalFlags_singleQuotes.length && specicalFlags_singleQuotes[ch2] != 0) {
                if (!hasSpecial2) {
                    newcount += 3;
                    if (newcount > this.buf.length) {
                        this.expandCapacity(newcount);
                    }
                    this.count = newcount;
                    System.arraycopy(this.buf, j + 1, this.buf, j + 3, end - j - 1);
                    System.arraycopy(this.buf, 0, this.buf, 1, j);
                    this.buf[start] = '\'';
                    this.buf[++j] = '\\';
                    this.buf[++j] = IOUtils.replaceChars[ch2];
                    end += 2;
                    this.buf[this.count - 2] = '\'';
                    hasSpecial2 = true;
                }
                else {
                    if (++newcount > this.buf.length) {
                        this.expandCapacity(newcount);
                    }
                    this.count = newcount;
                    System.arraycopy(this.buf, j + 1, this.buf, j + 2, end - j);
                    this.buf[j] = '\\';
                    this.buf[++j] = IOUtils.replaceChars[ch2];
                    ++end;
                }
            }
        }
        this.buf[newcount - 1] = ':';
    }
    
    @Override
    public void flush() {
        if (this.writer == null) {
            return;
        }
        try {
            this.writer.write(this.buf, 0, this.count);
            this.writer.flush();
        }
        catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
        this.count = 0;
    }
    
    @Deprecated
    public void reset() {
        this.count = 0;
    }
    
    static {
        bufLocal = new ThreadLocal<char[]>();
        bytesBufLocal = new ThreadLocal<byte[]>();
        SerializeWriter.BUFFER_THRESHOLD = 131072;
        try {
            final String prop = IOUtils.getStringProperty("fastjson.serializer_buffer_threshold");
            if (prop != null && prop.length() > 0) {
                final int serializer_buffer_threshold = Integer.parseInt(prop);
                if (serializer_buffer_threshold >= 64 && serializer_buffer_threshold <= 65536) {
                    SerializeWriter.BUFFER_THRESHOLD = serializer_buffer_threshold * 1024;
                }
            }
        }
        catch (Throwable t) {}
        nonDirectFeatures = (0x0 | SerializerFeature.UseSingleQuotes.mask | SerializerFeature.BrowserCompatible.mask | SerializerFeature.PrettyFormat.mask | SerializerFeature.WriteEnumUsingToString.mask | SerializerFeature.WriteNonStringValueAsString.mask | SerializerFeature.WriteSlashAsSpecial.mask | SerializerFeature.IgnoreErrorGetter.mask | SerializerFeature.WriteClassName.mask | SerializerFeature.NotWriteDefaultValue.mask);
    }
}
