package com.alibaba.fastjson.parser;

import java.io.Closeable;
import java.math.BigDecimal;
import com.alibaba.fastjson.util.IOUtils;
import java.io.CharArrayReader;
import java.io.IOException;
import com.alibaba.fastjson.JSONException;
import java.io.StringReader;
import com.alibaba.fastjson.JSON;
import java.io.Reader;

public final class JSONReaderScanner extends JSONLexerBase
{
    private static final ThreadLocal<char[]> BUF_LOCAL;
    private Reader reader;
    private char[] buf;
    private int bufLength;
    
    public JSONReaderScanner(final String input) {
        this(input, JSON.DEFAULT_PARSER_FEATURE);
    }
    
    public JSONReaderScanner(final String input, final int features) {
        this(new StringReader(input), features);
    }
    
    public JSONReaderScanner(final char[] input, final int inputLength) {
        this(input, inputLength, JSON.DEFAULT_PARSER_FEATURE);
    }
    
    public JSONReaderScanner(final Reader reader) {
        this(reader, JSON.DEFAULT_PARSER_FEATURE);
    }
    
    public JSONReaderScanner(final Reader reader, final int features) {
        super(features);
        this.reader = reader;
        this.buf = JSONReaderScanner.BUF_LOCAL.get();
        if (this.buf != null) {
            JSONReaderScanner.BUF_LOCAL.set(null);
        }
        if (this.buf == null) {
            this.buf = new char[16384];
        }
        try {
            this.bufLength = reader.read(this.buf);
        }
        catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
        this.bp = -1;
        this.next();
        if (this.ch == '\ufeff') {
            this.next();
        }
    }
    
    public JSONReaderScanner(final char[] input, final int inputLength, final int features) {
        this(new CharArrayReader(input, 0, inputLength), features);
    }
    
    @Override
    public final char charAt(int index) {
        if (index >= this.bufLength) {
            if (this.bufLength == -1) {
                if (index < this.sp) {
                    return this.buf[index];
                }
                return '\u001a';
            }
            else if (this.bp == 0) {
                final char[] buf = new char[this.buf.length * 3 / 2];
                System.arraycopy(this.buf, this.bp, buf, 0, this.bufLength);
                final int rest = buf.length - this.bufLength;
                try {
                    final int len = this.reader.read(buf, this.bufLength, rest);
                    this.bufLength += len;
                    this.buf = buf;
                }
                catch (IOException e) {
                    throw new JSONException(e.getMessage(), e);
                }
            }
            else {
                final int rest2 = this.bufLength - this.bp;
                if (rest2 > 0) {
                    System.arraycopy(this.buf, this.bp, this.buf, 0, rest2);
                }
                try {
                    this.bufLength = this.reader.read(this.buf, rest2, this.buf.length - rest2);
                }
                catch (IOException e2) {
                    throw new JSONException(e2.getMessage(), e2);
                }
                if (this.bufLength == 0) {
                    throw new JSONException("illegal state, textLength is zero");
                }
                if (this.bufLength == -1) {
                    return '\u001a';
                }
                this.bufLength += rest2;
                index -= this.bp;
                this.np -= this.bp;
                this.bp = 0;
            }
        }
        return this.buf[index];
    }
    
    @Override
    public final int indexOf(final char ch, final int startIndex) {
        int offset = startIndex - this.bp;
        while (true) {
            final int index = this.bp + offset;
            final char chLoal = this.charAt(index);
            if (ch == chLoal) {
                return offset + this.bp;
            }
            if (chLoal == '\u001a') {
                return -1;
            }
            ++offset;
        }
    }
    
    @Override
    public final String addSymbol(final int offset, final int len, final int hash, final SymbolTable symbolTable) {
        return symbolTable.addSymbol(this.buf, offset, len, hash);
    }
    
    @Override
    public final char next() {
        int index = ++this.bp;
        if (index >= this.bufLength) {
            if (this.bufLength == -1) {
                return '\u001a';
            }
            if (this.sp > 0) {
                int offset = this.bufLength - this.sp;
                if (this.ch == '\"' && offset > 0) {
                    --offset;
                }
                System.arraycopy(this.buf, offset, this.buf, 0, this.sp);
            }
            this.np = -1;
            final int sp = this.sp;
            this.bp = sp;
            index = sp;
            try {
                final int startPos = this.bp;
                int readLength = this.buf.length - startPos;
                if (readLength == 0) {
                    final char[] newBuf = new char[this.buf.length * 2];
                    System.arraycopy(this.buf, 0, newBuf, 0, this.buf.length);
                    this.buf = newBuf;
                    readLength = this.buf.length - startPos;
                }
                this.bufLength = this.reader.read(this.buf, this.bp, readLength);
            }
            catch (IOException e) {
                throw new JSONException(e.getMessage(), e);
            }
            if (this.bufLength == 0) {
                throw new JSONException("illegal stat, textLength is zero");
            }
            if (this.bufLength == -1) {
                return this.ch = '\u001a';
            }
            this.bufLength += this.bp;
        }
        return this.ch = this.buf[index];
    }
    
    @Override
    protected final void copyTo(final int offset, final int count, final char[] dest) {
        System.arraycopy(this.buf, offset, dest, 0, count);
    }
    
    public final boolean charArrayCompare(final char[] chars) {
        for (int i = 0; i < chars.length; ++i) {
            if (this.charAt(this.bp + i) != chars[i]) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public byte[] bytesValue() {
        if (this.token == 26) {
            throw new JSONException("TODO");
        }
        return IOUtils.decodeBase64(this.buf, this.np + 1, this.sp);
    }
    
    @Override
    protected final void arrayCopy(final int srcPos, final char[] dest, final int destPos, final int length) {
        System.arraycopy(this.buf, srcPos, dest, destPos, length);
    }
    
    @Override
    public final String stringVal() {
        if (this.hasSpecial) {
            return new String(this.sbuf, 0, this.sp);
        }
        final int offset = this.np + 1;
        if (offset < 0) {
            throw new IllegalStateException();
        }
        if (offset > this.buf.length - this.sp) {
            throw new IllegalStateException();
        }
        return new String(this.buf, offset, this.sp);
    }
    
    @Override
    public final String subString(final int offset, final int count) {
        if (count < 0) {
            throw new StringIndexOutOfBoundsException(count);
        }
        return new String(this.buf, offset, count);
    }
    
    public final char[] sub_chars(final int offset, final int count) {
        if (count < 0) {
            throw new StringIndexOutOfBoundsException(count);
        }
        if (offset == 0) {
            return this.buf;
        }
        final char[] chars = new char[count];
        System.arraycopy(this.buf, offset, chars, 0, count);
        return chars;
    }
    
    @Override
    public final String numberString() {
        int offset = this.np;
        if (offset == -1) {
            offset = 0;
        }
        final char chLocal = this.charAt(offset + this.sp - 1);
        int sp = this.sp;
        if (chLocal == 'L' || chLocal == 'S' || chLocal == 'B' || chLocal == 'F' || chLocal == 'D') {
            --sp;
        }
        final String value = new String(this.buf, offset, sp);
        return value;
    }
    
    @Override
    public final BigDecimal decimalValue() {
        int offset = this.np;
        if (offset == -1) {
            offset = 0;
        }
        final char chLocal = this.charAt(offset + this.sp - 1);
        int sp = this.sp;
        if (chLocal == 'L' || chLocal == 'S' || chLocal == 'B' || chLocal == 'F' || chLocal == 'D') {
            --sp;
        }
        return new BigDecimal(this.buf, offset, sp);
    }
    
    @Override
    public void close() {
        super.close();
        if (this.buf.length <= 65536) {
            JSONReaderScanner.BUF_LOCAL.set(this.buf);
        }
        this.buf = null;
        IOUtils.close(this.reader);
    }
    
    @Override
    public boolean isEOF() {
        return this.bufLength == -1 || this.bp == this.buf.length || (this.ch == '\u001a' && this.bp + 1 >= this.buf.length);
    }
    
    @Override
    public final boolean isBlankInput() {
        int i = 0;
        while (true) {
            final char chLocal = this.buf[i];
            if (chLocal == '\u001a') {
                this.token = 20;
                return true;
            }
            if (!JSONLexerBase.isWhitespace(chLocal)) {
                return false;
            }
            ++i;
        }
    }
    
    static {
        BUF_LOCAL = new ThreadLocal<char[]>();
    }
}
