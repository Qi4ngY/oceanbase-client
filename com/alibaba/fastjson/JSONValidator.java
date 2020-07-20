package com.alibaba.fastjson;

import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;

public abstract class JSONValidator implements Cloneable
{
    protected boolean eof;
    protected int pos;
    protected char ch;
    protected Type type;
    protected int count;
    protected boolean supportMultiValue;
    
    public JSONValidator() {
        this.pos = -1;
        this.count = 0;
        this.supportMultiValue = true;
    }
    
    public static JSONValidator fromUtf8(final byte[] jsonBytes) {
        return new UTF8Validator(jsonBytes);
    }
    
    public static JSONValidator fromUtf8(final InputStream is) {
        return new UTF8InputStreamValidator(is);
    }
    
    public static JSONValidator from(final String jsonStr) {
        return new UTF16Validator(jsonStr);
    }
    
    public static JSONValidator from(final Reader r) {
        return new ReaderValidator(r);
    }
    
    public Type getType() {
        return this.type;
    }
    
    abstract void next();
    
    public boolean validate() {
        do {
            this.any();
            ++this.count;
            if (!this.supportMultiValue || this.eof) {
                break;
            }
            this.skipWhiteSpace();
        } while (!this.eof);
        return true;
    }
    
    public void close() throws IOException {
    }
    
    void any() {
        switch (this.ch) {
            case '{': {
                this.next();
                this.skipWhiteSpace();
                if (this.ch == '}') {
                    this.next();
                    this.type = Type.Object;
                    return;
                }
                while (true) {
                    if (this.ch == '\"') {
                        this.fieldName();
                    }
                    else {
                        this.error();
                    }
                    this.skipWhiteSpace();
                    if (this.ch == ':') {
                        this.next();
                    }
                    else {
                        this.error();
                    }
                    this.skipWhiteSpace();
                    this.any();
                    this.skipWhiteSpace();
                    if (this.ch == ',') {
                        this.next();
                        this.skipWhiteSpace();
                    }
                    else {
                        if (this.ch == '}') {
                            break;
                        }
                        continue;
                    }
                }
                this.next();
                this.type = Type.Object;
                return;
            }
            case '[': {
                this.next();
                this.skipWhiteSpace();
                if (this.ch == ']') {
                    this.next();
                    this.type = Type.Array;
                    return;
                }
                while (true) {
                    this.any();
                    this.skipWhiteSpace();
                    if (this.ch == ',') {
                        this.next();
                        this.skipWhiteSpace();
                    }
                    else {
                        if (this.ch == ']') {
                            break;
                        }
                        this.error();
                    }
                }
                this.next();
                this.type = Type.Array;
                return;
            }
            case '+':
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9': {
                if (this.ch == '-' || this.ch == '+') {
                    this.next();
                    this.skipWhiteSpace();
                    if (this.ch < '0' || this.ch > '9') {
                        this.error();
                    }
                }
                do {
                    this.next();
                } while (this.ch >= '0' && this.ch <= '9');
                if (this.ch == '.') {
                    this.next();
                    if (this.ch < '0' || this.ch > '9') {
                        this.error();
                    }
                    while (this.ch >= '0' && this.ch <= '9') {
                        this.next();
                    }
                }
                if (this.ch == 'e' || this.ch == 'E') {
                    this.next();
                    if (this.ch == '-' || this.ch == '+') {
                        this.next();
                    }
                    if (this.ch >= '0' && this.ch <= '9') {
                        this.next();
                    }
                    else {
                        this.error();
                    }
                    while (this.ch >= '0' && this.ch <= '9') {
                        this.next();
                    }
                }
                this.type = Type.Value;
                return;
            }
            case '\"': {
                this.next();
                while (true) {
                    if (this.ch == '\\') {
                        this.next();
                        if (this.ch == 'u') {
                            this.next();
                            this.next();
                            this.next();
                            this.next();
                            this.next();
                        }
                        else {
                            this.next();
                        }
                    }
                    else {
                        if (this.ch == '\"') {
                            break;
                        }
                        this.next();
                    }
                }
                this.next();
                this.type = Type.Value;
                return;
            }
            case 't': {
                this.next();
                if (this.ch != 'r') {
                    this.error();
                }
                this.next();
                if (this.ch != 'u') {
                    this.error();
                }
                this.next();
                if (this.ch != 'e') {
                    this.error();
                }
                this.next();
                if (isWhiteSpace(this.ch) || this.ch == ',' || this.ch == ']' || this.ch == '}' || this.ch == '\0') {
                    this.type = Type.Value;
                    return;
                }
                this.error();
            }
            case 'f': {
                this.next();
                if (this.ch != 'a') {
                    this.error();
                }
                this.next();
                if (this.ch != 'l') {
                    this.error();
                }
                this.next();
                if (this.ch != 's') {
                    this.error();
                }
                this.next();
                if (this.ch != 'e') {
                    this.error();
                }
                this.next();
                if (isWhiteSpace(this.ch) || this.ch == ',' || this.ch == ']' || this.ch == '}' || this.ch == '\0') {
                    this.type = Type.Value;
                    return;
                }
                this.error();
            }
            case 'n': {
                this.next();
                if (this.ch != 'u') {
                    this.error();
                }
                this.next();
                if (this.ch != 'l') {
                    this.error();
                }
                this.next();
                if (this.ch != 'l') {
                    this.error();
                }
                this.next();
                if (isWhiteSpace(this.ch) || this.ch == ',' || this.ch == ']' || this.ch == '}' || this.ch == '\0') {
                    this.type = Type.Value;
                    return;
                }
                this.error();
                break;
            }
        }
        this.error();
    }
    
    protected void fieldName() {
        this.next();
        while (true) {
            if (this.ch == '\\') {
                this.next();
                if (this.ch == 'u') {
                    this.next();
                    this.next();
                    this.next();
                    this.next();
                    this.next();
                }
                else {
                    this.next();
                }
            }
            else {
                if (this.ch == '\"') {
                    break;
                }
                this.next();
            }
        }
        this.next();
    }
    
    void error() {
        throw new JSONException("error : " + this.pos);
    }
    
    void skipWhiteSpace() {
        while (isWhiteSpace(this.ch)) {
            this.next();
        }
    }
    
    static final boolean isWhiteSpace(final char ch) {
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n' || ch == '\f' || ch == '\b';
    }
    
    public enum Type
    {
        Object, 
        Array, 
        Value;
    }
    
    static class UTF8Validator extends JSONValidator
    {
        private final byte[] bytes;
        
        public UTF8Validator(final byte[] bytes) {
            this.bytes = bytes;
            this.next();
            this.skipWhiteSpace();
        }
        
        @Override
        void next() {
            ++this.pos;
            if (this.pos >= this.bytes.length) {
                this.ch = '\0';
                this.eof = true;
            }
            else {
                this.ch = (char)this.bytes[this.pos];
            }
        }
    }
    
    static class UTF8InputStreamValidator extends JSONValidator
    {
        private static final ThreadLocal<byte[]> bufLocal;
        private final InputStream is;
        private byte[] buf;
        private int end;
        private int readCount;
        
        public UTF8InputStreamValidator(final InputStream is) {
            this.end = -1;
            this.readCount = 0;
            this.is = is;
            this.buf = UTF8InputStreamValidator.bufLocal.get();
            if (this.buf != null) {
                UTF8InputStreamValidator.bufLocal.set(null);
            }
            else {
                this.buf = new byte[8192];
            }
            this.next();
            this.skipWhiteSpace();
        }
        
        @Override
        void next() {
            if (this.pos < this.end) {
                this.ch = (char)this.buf[++this.pos];
            }
            else if (!this.eof) {
                int len;
                try {
                    len = this.is.read(this.buf, 0, this.buf.length);
                    ++this.readCount;
                }
                catch (IOException ex) {
                    throw new JSONException("read error");
                }
                if (len > 0) {
                    this.ch = (char)this.buf[0];
                    this.pos = 0;
                    this.end = len - 1;
                }
                else {
                    if (len != -1) {
                        this.pos = 0;
                        this.end = 0;
                        this.buf = null;
                        this.ch = '\0';
                        this.eof = true;
                        throw new JSONException("read error");
                    }
                    this.pos = 0;
                    this.end = 0;
                    this.buf = null;
                    this.ch = '\0';
                    this.eof = true;
                }
            }
        }
        
        @Override
        void error() {
            throw new JSONException("error, readCount " + this.readCount + ", valueCount : " + this.count + ", pos " + this.pos);
        }
        
        @Override
        public void close() throws IOException {
            UTF8InputStreamValidator.bufLocal.set(this.buf);
            this.is.close();
        }
        
        static {
            bufLocal = new ThreadLocal<byte[]>();
        }
    }
    
    static class UTF16Validator extends JSONValidator
    {
        private final String str;
        
        public UTF16Validator(final String str) {
            this.str = str;
            this.next();
            this.skipWhiteSpace();
        }
        
        @Override
        void next() {
            ++this.pos;
            if (this.pos >= this.str.length()) {
                this.ch = '\0';
                this.eof = true;
            }
            else {
                this.ch = this.str.charAt(this.pos);
            }
        }
    }
    
    static class ReaderValidator extends JSONValidator
    {
        private static final ThreadLocal<char[]> bufLocal;
        final Reader r;
        private char[] buf;
        private int end;
        private int readCount;
        
        ReaderValidator(final Reader r) {
            this.end = -1;
            this.readCount = 0;
            this.r = r;
            this.buf = ReaderValidator.bufLocal.get();
            if (this.buf != null) {
                ReaderValidator.bufLocal.set(null);
            }
            else {
                this.buf = new char[8192];
            }
            this.next();
            this.skipWhiteSpace();
        }
        
        @Override
        void next() {
            if (this.pos < this.end) {
                this.ch = this.buf[++this.pos];
            }
            else if (!this.eof) {
                int len;
                try {
                    len = this.r.read(this.buf, 0, this.buf.length);
                    ++this.readCount;
                }
                catch (IOException ex) {
                    throw new JSONException("read error");
                }
                if (len > 0) {
                    this.ch = this.buf[0];
                    this.pos = 0;
                    this.end = len - 1;
                }
                else {
                    if (len != -1) {
                        this.pos = 0;
                        this.end = 0;
                        this.buf = null;
                        this.ch = '\0';
                        this.eof = true;
                        throw new JSONException("read error");
                    }
                    this.pos = 0;
                    this.end = 0;
                    this.buf = null;
                    this.ch = '\0';
                    this.eof = true;
                }
            }
        }
        
        @Override
        void error() {
            throw new JSONException("error, readCount " + this.readCount + ", valueCount : " + this.count + ", pos " + this.pos);
        }
        
        @Override
        public void close() throws IOException {
            ReaderValidator.bufLocal.set(this.buf);
            this.r.close();
        }
        
        static {
            bufLocal = new ThreadLocal<char[]>();
        }
    }
}
