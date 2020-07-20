package com.alibaba.fastjson.parser;

import java.util.UUID;
import java.util.Date;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;
import com.alibaba.fastjson.util.IOUtils;
import java.math.BigInteger;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSON;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Calendar;
import java.io.Closeable;

public abstract class JSONLexerBase implements JSONLexer, Closeable
{
    protected int token;
    protected int pos;
    protected int features;
    protected char ch;
    protected int bp;
    protected int eofPos;
    protected char[] sbuf;
    protected int sp;
    protected int np;
    protected boolean hasSpecial;
    protected Calendar calendar;
    protected TimeZone timeZone;
    protected Locale locale;
    public int matchStat;
    private static final ThreadLocal<char[]> SBUF_LOCAL;
    protected String stringDefaultValue;
    protected int nanos;
    protected static final char[] typeFieldName;
    protected static final long MULTMIN_RADIX_TEN = -922337203685477580L;
    protected static final int INT_MULTMIN_RADIX_TEN = -214748364;
    protected static final int[] digits;
    
    protected void lexError(final String key, final Object... args) {
        this.token = 1;
    }
    
    public JSONLexerBase(final int features) {
        this.calendar = null;
        this.timeZone = JSON.defaultTimeZone;
        this.locale = JSON.defaultLocale;
        this.matchStat = 0;
        this.stringDefaultValue = null;
        this.nanos = 0;
        this.features = features;
        if ((features & Feature.InitStringFieldAsEmpty.mask) != 0x0) {
            this.stringDefaultValue = "";
        }
        this.sbuf = JSONLexerBase.SBUF_LOCAL.get();
        if (this.sbuf == null) {
            this.sbuf = new char[512];
        }
    }
    
    public final int matchStat() {
        return this.matchStat;
    }
    
    public void setToken(final int token) {
        this.token = token;
    }
    
    @Override
    public final void nextToken() {
        this.sp = 0;
    Label_0591:
        while (true) {
            this.pos = this.bp;
            if (this.ch == '/') {
                this.skipComment();
            }
            else {
                if (this.ch == '\"') {
                    this.scanString();
                    return;
                }
                if (this.ch == ',') {
                    this.next();
                    this.token = 16;
                    return;
                }
                if (this.ch >= '0' && this.ch <= '9') {
                    this.scanNumber();
                    return;
                }
                if (this.ch == '-') {
                    this.scanNumber();
                    return;
                }
                switch (this.ch) {
                    case '\'': {
                        if (!this.isEnabled(Feature.AllowSingleQuotes)) {
                            throw new JSONException("Feature.AllowSingleQuotes is false");
                        }
                        this.scanStringSingleQuote();
                        return;
                    }
                    case '\b':
                    case '\t':
                    case '\n':
                    case '\f':
                    case '\r':
                    case ' ': {
                        this.next();
                        continue;
                    }
                    case 't': {
                        this.scanTrue();
                        return;
                    }
                    case 'f': {
                        this.scanFalse();
                        return;
                    }
                    case 'n': {
                        this.scanNullOrNew();
                        return;
                    }
                    case 'N':
                    case 'S':
                    case 'T':
                    case 'u': {
                        this.scanIdent();
                        return;
                    }
                    case '(': {
                        this.next();
                        this.token = 10;
                        return;
                    }
                    case ')': {
                        this.next();
                        this.token = 11;
                        return;
                    }
                    case '[': {
                        this.next();
                        this.token = 14;
                        return;
                    }
                    case ']': {
                        this.next();
                        this.token = 15;
                        return;
                    }
                    case '{': {
                        this.next();
                        this.token = 12;
                        return;
                    }
                    case '}': {
                        this.next();
                        this.token = 13;
                        return;
                    }
                    case ':': {
                        this.next();
                        this.token = 17;
                        return;
                    }
                    case ';': {
                        this.next();
                        this.token = 24;
                        return;
                    }
                    case '.': {
                        this.next();
                        this.token = 25;
                        return;
                    }
                    case '+': {
                        this.next();
                        this.scanNumber();
                        return;
                    }
                    case 'x': {
                        this.scanHex();
                        return;
                    }
                    default: {
                        if (this.isEOF()) {
                            if (this.token == 20) {
                                throw new JSONException("EOF error");
                            }
                            this.token = 20;
                            final int bp = this.bp;
                            this.pos = bp;
                            this.eofPos = bp;
                            break Label_0591;
                        }
                        else {
                            if (this.ch <= '\u001f' || this.ch == '\u007f') {
                                this.next();
                                continue;
                            }
                            this.lexError("illegal.char", String.valueOf((int)this.ch));
                            this.next();
                            break Label_0591;
                        }
                        break;
                    }
                }
            }
        }
    }
    
    @Override
    public final void nextToken(final int expect) {
        this.sp = 0;
        while (true) {
            switch (expect) {
                case 12: {
                    if (this.ch == '{') {
                        this.token = 12;
                        this.next();
                        return;
                    }
                    if (this.ch == '[') {
                        this.token = 14;
                        this.next();
                        return;
                    }
                    break;
                }
                case 16: {
                    if (this.ch == ',') {
                        this.token = 16;
                        this.next();
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
                    if (this.ch == '\u001a') {
                        this.token = 20;
                        return;
                    }
                    if (this.ch == 'n') {
                        this.scanNullOrNew(false);
                        return;
                    }
                    break;
                }
                case 2: {
                    if (this.ch >= '0' && this.ch <= '9') {
                        this.pos = this.bp;
                        this.scanNumber();
                        return;
                    }
                    if (this.ch == '\"') {
                        this.pos = this.bp;
                        this.scanString();
                        return;
                    }
                    if (this.ch == '[') {
                        this.token = 14;
                        this.next();
                        return;
                    }
                    if (this.ch == '{') {
                        this.token = 12;
                        this.next();
                        return;
                    }
                    break;
                }
                case 4: {
                    if (this.ch == '\"') {
                        this.pos = this.bp;
                        this.scanString();
                        return;
                    }
                    if (this.ch >= '0' && this.ch <= '9') {
                        this.pos = this.bp;
                        this.scanNumber();
                        return;
                    }
                    if (this.ch == '[') {
                        this.token = 14;
                        this.next();
                        return;
                    }
                    if (this.ch == '{') {
                        this.token = 12;
                        this.next();
                        return;
                    }
                    break;
                }
                case 14: {
                    if (this.ch == '[') {
                        this.token = 14;
                        this.next();
                        return;
                    }
                    if (this.ch == '{') {
                        this.token = 12;
                        this.next();
                        return;
                    }
                    break;
                }
                case 15: {
                    if (this.ch == ']') {
                        this.token = 15;
                        this.next();
                        return;
                    }
                }
                case 20: {
                    if (this.ch == '\u001a') {
                        this.token = 20;
                        return;
                    }
                    break;
                }
                case 18: {
                    this.nextIdent();
                    return;
                }
            }
            if (this.ch != ' ' && this.ch != '\n' && this.ch != '\r' && this.ch != '\t' && this.ch != '\f' && this.ch != '\b') {
                this.nextToken();
                return;
            }
            this.next();
        }
    }
    
    public final void nextIdent() {
        while (isWhitespace(this.ch)) {
            this.next();
        }
        if (this.ch == '_' || this.ch == '$' || Character.isLetter(this.ch)) {
            this.scanIdent();
        }
        else {
            this.nextToken();
        }
    }
    
    @Override
    public final void nextTokenWithColon() {
        this.nextTokenWithChar(':');
    }
    
    public final void nextTokenWithChar(final char expect) {
        this.sp = 0;
        while (this.ch != expect) {
            if (this.ch != ' ' && this.ch != '\n' && this.ch != '\r' && this.ch != '\t' && this.ch != '\f' && this.ch != '\b') {
                throw new JSONException("not match " + expect + " - " + this.ch + ", info : " + this.info());
            }
            this.next();
        }
        this.next();
        this.nextToken();
    }
    
    @Override
    public final int token() {
        return this.token;
    }
    
    @Override
    public final String tokenName() {
        return JSONToken.name(this.token);
    }
    
    @Override
    public final int pos() {
        return this.pos;
    }
    
    public final String stringDefaultValue() {
        return this.stringDefaultValue;
    }
    
    @Override
    public final Number integerValue() throws NumberFormatException {
        long result = 0L;
        boolean negative = false;
        if (this.np == -1) {
            this.np = 0;
        }
        int i = this.np;
        int max = this.np + this.sp;
        char type = ' ';
        switch (this.charAt(max - 1)) {
            case 'L': {
                --max;
                type = 'L';
                break;
            }
            case 'S': {
                --max;
                type = 'S';
                break;
            }
            case 'B': {
                --max;
                type = 'B';
                break;
            }
        }
        long limit;
        if (this.charAt(this.np) == '-') {
            negative = true;
            limit = Long.MIN_VALUE;
            ++i;
        }
        else {
            limit = -9223372036854775807L;
        }
        final long multmin = -922337203685477580L;
        if (i < max) {
            final int digit = this.charAt(i++) - '0';
            result = -digit;
        }
        while (i < max) {
            final int digit = this.charAt(i++) - '0';
            if (result < multmin) {
                return new BigInteger(this.numberString());
            }
            result *= 10L;
            if (result < limit + digit) {
                return new BigInteger(this.numberString());
            }
            result -= digit;
        }
        if (negative) {
            if (i <= this.np + 1) {
                throw new NumberFormatException(this.numberString());
            }
            if (result < -2147483648L || type == 'L') {
                return result;
            }
            if (type == 'S') {
                return (short)result;
            }
            if (type == 'B') {
                return (byte)result;
            }
            return (int)result;
        }
        else {
            result = -result;
            if (result > 2147483647L || type == 'L') {
                return result;
            }
            if (type == 'S') {
                return (short)result;
            }
            if (type == 'B') {
                return (byte)result;
            }
            return (int)result;
        }
    }
    
    @Override
    public final void nextTokenWithColon(final int expect) {
        this.nextTokenWithChar(':');
    }
    
    @Override
    public float floatValue() {
        final String strVal = this.numberString();
        final float floatValue = Float.parseFloat(strVal);
        if (floatValue == 0.0f || floatValue == Float.POSITIVE_INFINITY) {
            final char c0 = strVal.charAt(0);
            if (c0 > '0' && c0 <= '9') {
                throw new JSONException("float overflow : " + strVal);
            }
        }
        return floatValue;
    }
    
    public double doubleValue() {
        return Double.parseDouble(this.numberString());
    }
    
    @Override
    public void config(final Feature feature, final boolean state) {
        this.features = Feature.config(this.features, feature, state);
        if ((this.features & Feature.InitStringFieldAsEmpty.mask) != 0x0) {
            this.stringDefaultValue = "";
        }
    }
    
    @Override
    public final boolean isEnabled(final Feature feature) {
        return this.isEnabled(feature.mask);
    }
    
    @Override
    public final boolean isEnabled(final int feature) {
        return (this.features & feature) != 0x0;
    }
    
    public final boolean isEnabled(final int features, final int feature) {
        return (this.features & feature) != 0x0 || (features & feature) != 0x0;
    }
    
    @Override
    public abstract String numberString();
    
    public abstract boolean isEOF();
    
    @Override
    public final char getCurrent() {
        return this.ch;
    }
    
    public abstract char charAt(final int p0);
    
    @Override
    public abstract char next();
    
    protected void skipComment() {
        this.next();
        if (this.ch == '/') {
            do {
                this.next();
                if (this.ch == '\n') {
                    this.next();
                }
            } while (this.ch != '\u001a');
            return;
        }
        if (this.ch == '*') {
            this.next();
            while (this.ch != '\u001a') {
                if (this.ch == '*') {
                    this.next();
                    if (this.ch == '/') {
                        this.next();
                        return;
                    }
                    continue;
                }
                else {
                    this.next();
                }
            }
            return;
        }
        throw new JSONException("invalid comment");
    }
    
    @Override
    public final String scanSymbol(final SymbolTable symbolTable) {
        this.skipWhitespace();
        if (this.ch == '\"') {
            return this.scanSymbol(symbolTable, '\"');
        }
        if (this.ch == '\'') {
            if (!this.isEnabled(Feature.AllowSingleQuotes)) {
                throw new JSONException("syntax error");
            }
            return this.scanSymbol(symbolTable, '\'');
        }
        else {
            if (this.ch == '}') {
                this.next();
                this.token = 13;
                return null;
            }
            if (this.ch == ',') {
                this.next();
                this.token = 16;
                return null;
            }
            if (this.ch == '\u001a') {
                this.token = 20;
                return null;
            }
            if (!this.isEnabled(Feature.AllowUnQuotedFieldNames)) {
                throw new JSONException("syntax error");
            }
            return this.scanSymbolUnQuoted(symbolTable);
        }
    }
    
    protected abstract void arrayCopy(final int p0, final char[] p1, final int p2, final int p3);
    
    @Override
    public final String scanSymbol(final SymbolTable symbolTable, final char quote) {
        int hash = 0;
        this.np = this.bp;
        this.sp = 0;
        boolean hasSpecial = false;
        while (true) {
            char chLocal = this.next();
            if (chLocal == quote) {
                this.token = 4;
                String value;
                if (!hasSpecial) {
                    int offset;
                    if (this.np == -1) {
                        offset = 0;
                    }
                    else {
                        offset = this.np + 1;
                    }
                    value = this.addSymbol(offset, this.sp, hash, symbolTable);
                }
                else {
                    value = symbolTable.addSymbol(this.sbuf, 0, this.sp, hash);
                }
                this.sp = 0;
                this.next();
                return value;
            }
            if (chLocal == '\u001a') {
                throw new JSONException("unclosed.str");
            }
            if (chLocal == '\\') {
                if (!hasSpecial) {
                    hasSpecial = true;
                    if (this.sp >= this.sbuf.length) {
                        int newCapcity = this.sbuf.length * 2;
                        if (this.sp > newCapcity) {
                            newCapcity = this.sp;
                        }
                        final char[] newsbuf = new char[newCapcity];
                        System.arraycopy(this.sbuf, 0, newsbuf, 0, this.sbuf.length);
                        this.sbuf = newsbuf;
                    }
                    this.arrayCopy(this.np + 1, this.sbuf, 0, this.sp);
                }
                chLocal = this.next();
                switch (chLocal) {
                    case '0': {
                        hash = 31 * hash + chLocal;
                        this.putChar('\0');
                        continue;
                    }
                    case '1': {
                        hash = 31 * hash + chLocal;
                        this.putChar('\u0001');
                        continue;
                    }
                    case '2': {
                        hash = 31 * hash + chLocal;
                        this.putChar('\u0002');
                        continue;
                    }
                    case '3': {
                        hash = 31 * hash + chLocal;
                        this.putChar('\u0003');
                        continue;
                    }
                    case '4': {
                        hash = 31 * hash + chLocal;
                        this.putChar('\u0004');
                        continue;
                    }
                    case '5': {
                        hash = 31 * hash + chLocal;
                        this.putChar('\u0005');
                        continue;
                    }
                    case '6': {
                        hash = 31 * hash + chLocal;
                        this.putChar('\u0006');
                        continue;
                    }
                    case '7': {
                        hash = 31 * hash + chLocal;
                        this.putChar('\u0007');
                        continue;
                    }
                    case 'b': {
                        hash = 31 * hash + 8;
                        this.putChar('\b');
                        continue;
                    }
                    case 't': {
                        hash = 31 * hash + 9;
                        this.putChar('\t');
                        continue;
                    }
                    case 'n': {
                        hash = 31 * hash + 10;
                        this.putChar('\n');
                        continue;
                    }
                    case 'v': {
                        hash = 31 * hash + 11;
                        this.putChar('\u000b');
                        continue;
                    }
                    case 'F':
                    case 'f': {
                        hash = 31 * hash + 12;
                        this.putChar('\f');
                        continue;
                    }
                    case 'r': {
                        hash = 31 * hash + 13;
                        this.putChar('\r');
                        continue;
                    }
                    case '\"': {
                        hash = 31 * hash + 34;
                        this.putChar('\"');
                        continue;
                    }
                    case '\'': {
                        hash = 31 * hash + 39;
                        this.putChar('\'');
                        continue;
                    }
                    case '/': {
                        hash = 31 * hash + 47;
                        this.putChar('/');
                        continue;
                    }
                    case '\\': {
                        hash = 31 * hash + 92;
                        this.putChar('\\');
                        continue;
                    }
                    case 'x': {
                        final char next = this.next();
                        this.ch = next;
                        final char x1 = next;
                        final char next2 = this.next();
                        this.ch = next2;
                        final char x2 = next2;
                        final int x_val = JSONLexerBase.digits[x1] * 16 + JSONLexerBase.digits[x2];
                        final char x_char = (char)x_val;
                        hash = 31 * hash + x_char;
                        this.putChar(x_char);
                        continue;
                    }
                    case 'u': {
                        final char c1;
                        chLocal = (c1 = this.next());
                        final char c2;
                        chLocal = (c2 = this.next());
                        final char c3;
                        chLocal = (c3 = this.next());
                        final char c4;
                        chLocal = (c4 = this.next());
                        final int val = Integer.parseInt(new String(new char[] { c1, c2, c3, c4 }), 16);
                        hash = 31 * hash + val;
                        this.putChar((char)val);
                        continue;
                    }
                    default: {
                        this.ch = chLocal;
                        throw new JSONException("unclosed.str.lit");
                    }
                }
            }
            else {
                hash = 31 * hash + chLocal;
                if (!hasSpecial) {
                    ++this.sp;
                }
                else if (this.sp == this.sbuf.length) {
                    this.putChar(chLocal);
                }
                else {
                    this.sbuf[this.sp++] = chLocal;
                }
            }
        }
    }
    
    @Override
    public final void resetStringPosition() {
        this.sp = 0;
    }
    
    @Override
    public String info() {
        return "";
    }
    
    @Override
    public final String scanSymbolUnQuoted(final SymbolTable symbolTable) {
        if (this.token == 1 && this.pos == 0 && this.bp == 1) {
            this.bp = 0;
        }
        final boolean[] firstIdentifierFlags = IOUtils.firstIdentifierFlags;
        final char first = this.ch;
        final boolean firstFlag = this.ch >= firstIdentifierFlags.length || firstIdentifierFlags[first];
        if (!firstFlag) {
            throw new JSONException("illegal identifier : " + this.ch + this.info());
        }
        final boolean[] identifierFlags = IOUtils.identifierFlags;
        int hash = first;
        this.np = this.bp;
        this.sp = 1;
        while (true) {
            final char chLocal = this.next();
            if (chLocal < identifierFlags.length && !identifierFlags[chLocal]) {
                break;
            }
            hash = 31 * hash + chLocal;
            ++this.sp;
        }
        this.ch = this.charAt(this.bp);
        this.token = 18;
        final int NULL_HASH = 3392903;
        if (this.sp == 4 && hash == 3392903 && this.charAt(this.np) == 'n' && this.charAt(this.np + 1) == 'u' && this.charAt(this.np + 2) == 'l' && this.charAt(this.np + 3) == 'l') {
            return null;
        }
        if (symbolTable == null) {
            return this.subString(this.np, this.sp);
        }
        return this.addSymbol(this.np, this.sp, hash, symbolTable);
    }
    
    protected abstract void copyTo(final int p0, final int p1, final char[] p2);
    
    @Override
    public final void scanString() {
        this.np = this.bp;
        this.hasSpecial = false;
        while (true) {
            char ch = this.next();
            if (ch == '\"') {
                this.token = 4;
                this.ch = this.next();
                return;
            }
            if (ch == '\u001a') {
                if (this.isEOF()) {
                    throw new JSONException("unclosed string : " + ch);
                }
                this.putChar('\u001a');
            }
            else if (ch == '\\') {
                if (!this.hasSpecial) {
                    this.hasSpecial = true;
                    if (this.sp >= this.sbuf.length) {
                        int newCapcity = this.sbuf.length * 2;
                        if (this.sp > newCapcity) {
                            newCapcity = this.sp;
                        }
                        final char[] newsbuf = new char[newCapcity];
                        System.arraycopy(this.sbuf, 0, newsbuf, 0, this.sbuf.length);
                        this.sbuf = newsbuf;
                    }
                    this.copyTo(this.np + 1, this.sp, this.sbuf);
                }
                ch = this.next();
                switch (ch) {
                    case '0': {
                        this.putChar('\0');
                        continue;
                    }
                    case '1': {
                        this.putChar('\u0001');
                        continue;
                    }
                    case '2': {
                        this.putChar('\u0002');
                        continue;
                    }
                    case '3': {
                        this.putChar('\u0003');
                        continue;
                    }
                    case '4': {
                        this.putChar('\u0004');
                        continue;
                    }
                    case '5': {
                        this.putChar('\u0005');
                        continue;
                    }
                    case '6': {
                        this.putChar('\u0006');
                        continue;
                    }
                    case '7': {
                        this.putChar('\u0007');
                        continue;
                    }
                    case 'b': {
                        this.putChar('\b');
                        continue;
                    }
                    case 't': {
                        this.putChar('\t');
                        continue;
                    }
                    case 'n': {
                        this.putChar('\n');
                        continue;
                    }
                    case 'v': {
                        this.putChar('\u000b');
                        continue;
                    }
                    case 'F':
                    case 'f': {
                        this.putChar('\f');
                        continue;
                    }
                    case 'r': {
                        this.putChar('\r');
                        continue;
                    }
                    case '\"': {
                        this.putChar('\"');
                        continue;
                    }
                    case '\'': {
                        this.putChar('\'');
                        continue;
                    }
                    case '/': {
                        this.putChar('/');
                        continue;
                    }
                    case '\\': {
                        this.putChar('\\');
                        continue;
                    }
                    case 'x': {
                        final char x1 = this.next();
                        final char x2 = this.next();
                        final boolean hex1 = (x1 >= '0' && x1 <= '9') || (x1 >= 'a' && x1 <= 'f') || (x1 >= 'A' && x1 <= 'F');
                        final boolean hex2 = (x2 >= '0' && x2 <= '9') || (x2 >= 'a' && x2 <= 'f') || (x2 >= 'A' && x2 <= 'F');
                        if (!hex1 || !hex2) {
                            throw new JSONException("invalid escape character \\x" + x1 + x2);
                        }
                        final char x_char = (char)(JSONLexerBase.digits[x1] * 16 + JSONLexerBase.digits[x2]);
                        this.putChar(x_char);
                        continue;
                    }
                    case 'u': {
                        final char u1 = this.next();
                        final char u2 = this.next();
                        final char u3 = this.next();
                        final char u4 = this.next();
                        final int val = Integer.parseInt(new String(new char[] { u1, u2, u3, u4 }), 16);
                        this.putChar((char)val);
                        continue;
                    }
                    default: {
                        this.ch = ch;
                        throw new JSONException("unclosed string : " + ch);
                    }
                }
            }
            else if (!this.hasSpecial) {
                ++this.sp;
            }
            else if (this.sp == this.sbuf.length) {
                this.putChar(ch);
            }
            else {
                this.sbuf[this.sp++] = ch;
            }
        }
    }
    
    public Calendar getCalendar() {
        return this.calendar;
    }
    
    @Override
    public TimeZone getTimeZone() {
        return this.timeZone;
    }
    
    @Override
    public void setTimeZone(final TimeZone timeZone) {
        this.timeZone = timeZone;
    }
    
    @Override
    public Locale getLocale() {
        return this.locale;
    }
    
    @Override
    public void setLocale(final Locale locale) {
        this.locale = locale;
    }
    
    @Override
    public final int intValue() {
        if (this.np == -1) {
            this.np = 0;
        }
        int result = 0;
        boolean negative = false;
        int i = this.np;
        final int max = this.np + this.sp;
        int limit;
        if (this.charAt(this.np) == '-') {
            negative = true;
            limit = Integer.MIN_VALUE;
            ++i;
        }
        else {
            limit = -2147483647;
        }
        final long multmin = -214748364L;
        if (i < max) {
            final int digit = this.charAt(i++) - '0';
            result = -digit;
        }
        while (i < max) {
            final char chLocal = this.charAt(i++);
            if (chLocal == 'L' || chLocal == 'S') {
                break;
            }
            if (chLocal == 'B') {
                break;
            }
            final int digit = chLocal - '0';
            if (result < multmin) {
                throw new NumberFormatException(this.numberString());
            }
            result *= 10;
            if (result < limit + digit) {
                throw new NumberFormatException(this.numberString());
            }
            result -= digit;
        }
        if (!negative) {
            return -result;
        }
        if (i > this.np + 1) {
            return result;
        }
        throw new NumberFormatException(this.numberString());
    }
    
    @Override
    public abstract byte[] bytesValue();
    
    @Override
    public void close() {
        if (this.sbuf.length <= 8192) {
            JSONLexerBase.SBUF_LOCAL.set(this.sbuf);
        }
        this.sbuf = null;
    }
    
    @Override
    public final boolean isRef() {
        return this.sp == 4 && this.charAt(this.np + 1) == '$' && this.charAt(this.np + 2) == 'r' && this.charAt(this.np + 3) == 'e' && this.charAt(this.np + 4) == 'f';
    }
    
    @Override
    public String scanTypeName(final SymbolTable symbolTable) {
        return null;
    }
    
    public final int scanType(final String type) {
        this.matchStat = 0;
        if (!this.charArrayCompare(JSONLexerBase.typeFieldName)) {
            return -2;
        }
        int bpLocal = this.bp + JSONLexerBase.typeFieldName.length;
        final int typeLength = type.length();
        for (int i = 0; i < typeLength; ++i) {
            if (type.charAt(i) != this.charAt(bpLocal + i)) {
                return -1;
            }
        }
        bpLocal += typeLength;
        if (this.charAt(bpLocal) != '\"') {
            return -1;
        }
        this.ch = this.charAt(++bpLocal);
        if (this.ch == ',') {
            this.ch = this.charAt(++bpLocal);
            this.bp = bpLocal;
            this.token = 16;
            return 3;
        }
        if (this.ch == '}') {
            this.ch = this.charAt(++bpLocal);
            if (this.ch == ',') {
                this.token = 16;
                this.ch = this.charAt(++bpLocal);
            }
            else if (this.ch == ']') {
                this.token = 15;
                this.ch = this.charAt(++bpLocal);
            }
            else if (this.ch == '}') {
                this.token = 13;
                this.ch = this.charAt(++bpLocal);
            }
            else {
                if (this.ch != '\u001a') {
                    return -1;
                }
                this.token = 20;
            }
            this.matchStat = 4;
        }
        this.bp = bpLocal;
        return this.matchStat;
    }
    
    public final boolean matchField(final char[] fieldName) {
        while (!this.charArrayCompare(fieldName)) {
            if (!isWhitespace(this.ch)) {
                return false;
            }
            this.next();
        }
        this.bp += fieldName.length;
        this.ch = this.charAt(this.bp);
        if (this.ch == '{') {
            this.next();
            this.token = 12;
        }
        else if (this.ch == '[') {
            this.next();
            this.token = 14;
        }
        else if (this.ch == 'S' && this.charAt(this.bp + 1) == 'e' && this.charAt(this.bp + 2) == 't' && this.charAt(this.bp + 3) == '[') {
            this.bp += 3;
            this.ch = this.charAt(this.bp);
            this.token = 21;
        }
        else {
            this.nextToken();
        }
        return true;
    }
    
    public int matchField(final long fieldNameHash) {
        throw new UnsupportedOperationException();
    }
    
    public boolean seekArrayToItem(final int index) {
        throw new UnsupportedOperationException();
    }
    
    public int seekObjectToField(final long fieldNameHash, final boolean deepScan) {
        throw new UnsupportedOperationException();
    }
    
    public int seekObjectToField(final long[] fieldNameHash) {
        throw new UnsupportedOperationException();
    }
    
    public int seekObjectToFieldDeepScan(final long fieldNameHash) {
        throw new UnsupportedOperationException();
    }
    
    public void skipObject() {
        throw new UnsupportedOperationException();
    }
    
    public void skipObject(final boolean valid) {
        throw new UnsupportedOperationException();
    }
    
    public void skipArray() {
        throw new UnsupportedOperationException();
    }
    
    public abstract int indexOf(final char p0, final int p1);
    
    public abstract String addSymbol(final int p0, final int p1, final int p2, final SymbolTable p3);
    
    public String scanFieldString(final char[] fieldName) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return this.stringDefaultValue();
        }
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        if (chLocal != '\"') {
            this.matchStat = -1;
            return this.stringDefaultValue();
        }
        final int startIndex = this.bp + fieldName.length + 1;
        int endIndex = this.indexOf('\"', startIndex);
        if (endIndex == -1) {
            throw new JSONException("unclosed str");
        }
        final int startIndex2 = this.bp + fieldName.length + 1;
        String stringVal = this.subString(startIndex2, endIndex - startIndex2);
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
            stringVal = readString(chars, chars_len);
        }
        offset += endIndex - (this.bp + fieldName.length + 1) + 1;
        chLocal = this.charAt(this.bp + offset++);
        final String strVal = stringVal;
        if (chLocal == ',') {
            this.bp += offset;
            this.ch = this.charAt(this.bp);
            this.matchStat = 3;
            return strVal;
        }
        if (chLocal == '}') {
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal == ',') {
                this.token = 16;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == ']') {
                this.token = 15;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == '}') {
                this.token = 13;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else {
                if (chLocal != '\u001a') {
                    this.matchStat = -1;
                    return this.stringDefaultValue();
                }
                this.token = 20;
                this.bp += offset - 1;
                this.ch = '\u001a';
            }
            this.matchStat = 4;
            return strVal;
        }
        this.matchStat = -1;
        return this.stringDefaultValue();
    }
    
    @Override
    public String scanString(final char expectNextChar) {
        this.matchStat = 0;
        int offset = 0;
        char chLocal = this.charAt(this.bp + offset++);
        if (chLocal == 'n') {
            if (this.charAt(this.bp + offset) != 'u' || this.charAt(this.bp + offset + 1) != 'l' || this.charAt(this.bp + offset + 2) != 'l') {
                this.matchStat = -1;
                return null;
            }
            offset += 3;
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal == expectNextChar) {
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 3;
                return null;
            }
            this.matchStat = -1;
            return null;
        }
        else {
            while (chLocal != '\"') {
                if (!isWhitespace(chLocal)) {
                    this.matchStat = -1;
                    return this.stringDefaultValue();
                }
                chLocal = this.charAt(this.bp + offset++);
            }
            final int startIndex = this.bp + offset;
            int endIndex = this.indexOf('\"', startIndex);
            if (endIndex == -1) {
                throw new JSONException("unclosed str");
            }
            String stringVal = this.subString(this.bp + offset, endIndex - startIndex);
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
                final char[] chars = this.sub_chars(this.bp + 1, chars_len);
                stringVal = readString(chars, chars_len);
            }
            offset += endIndex - startIndex + 1;
            chLocal = this.charAt(this.bp + offset++);
            final String strVal = stringVal;
            while (chLocal != expectNextChar) {
                if (!isWhitespace(chLocal)) {
                    if (chLocal == ']') {
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                        this.matchStat = -1;
                    }
                    return strVal;
                }
                chLocal = this.charAt(this.bp + offset++);
            }
            this.bp += offset;
            this.ch = this.charAt(this.bp);
            this.matchStat = 3;
            this.token = 16;
            return strVal;
        }
    }
    
    public long scanFieldSymbol(final char[] fieldName) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return 0L;
        }
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        if (chLocal != '\"') {
            this.matchStat = -1;
            return 0L;
        }
        long hash = -3750763034362895579L;
        do {
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal == '\"') {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal == ',') {
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                    this.matchStat = 3;
                    return hash;
                }
                if (chLocal == '}') {
                    chLocal = this.charAt(this.bp + offset++);
                    if (chLocal == ',') {
                        this.token = 16;
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                    }
                    else if (chLocal == ']') {
                        this.token = 15;
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                    }
                    else if (chLocal == '}') {
                        this.token = 13;
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                    }
                    else {
                        if (chLocal != '\u001a') {
                            this.matchStat = -1;
                            return 0L;
                        }
                        this.token = 20;
                        this.bp += offset - 1;
                        this.ch = '\u001a';
                    }
                    this.matchStat = 4;
                    return hash;
                }
                this.matchStat = -1;
                return 0L;
            }
            else {
                hash ^= chLocal;
                hash *= 1099511628211L;
            }
        } while (chLocal != '\\');
        this.matchStat = -1;
        return 0L;
    }
    
    public long scanEnumSymbol(final char[] fieldName) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return 0L;
        }
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        if (chLocal != '\"') {
            this.matchStat = -1;
            return 0L;
        }
        long hash = -3750763034362895579L;
        do {
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal == '\"') {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal == ',') {
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                    this.matchStat = 3;
                    return hash;
                }
                if (chLocal == '}') {
                    chLocal = this.charAt(this.bp + offset++);
                    if (chLocal == ',') {
                        this.token = 16;
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                    }
                    else if (chLocal == ']') {
                        this.token = 15;
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                    }
                    else if (chLocal == '}') {
                        this.token = 13;
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                    }
                    else {
                        if (chLocal != '\u001a') {
                            this.matchStat = -1;
                            return 0L;
                        }
                        this.token = 20;
                        this.bp += offset - 1;
                        this.ch = '\u001a';
                    }
                    this.matchStat = 4;
                    return hash;
                }
                this.matchStat = -1;
                return 0L;
            }
            else {
                hash ^= ((chLocal >= 'A' && chLocal <= 'Z') ? (chLocal + ' ') : chLocal);
                hash *= 1099511628211L;
            }
        } while (chLocal != '\\');
        this.matchStat = -1;
        return 0L;
    }
    
    @Override
    public Enum<?> scanEnum(final Class<?> enumClass, final SymbolTable symbolTable, final char serperator) {
        final String name = this.scanSymbolWithSeperator(symbolTable, serperator);
        if (name == null) {
            return null;
        }
        return Enum.valueOf(enumClass, name);
    }
    
    @Override
    public String scanSymbolWithSeperator(final SymbolTable symbolTable, final char serperator) {
        this.matchStat = 0;
        int offset = 0;
        char chLocal = this.charAt(this.bp + offset++);
        if (chLocal == 'n') {
            if (this.charAt(this.bp + offset) != 'u' || this.charAt(this.bp + offset + 1) != 'l' || this.charAt(this.bp + offset + 2) != 'l') {
                this.matchStat = -1;
                return null;
            }
            offset += 3;
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal == serperator) {
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 3;
                return null;
            }
            this.matchStat = -1;
            return null;
        }
        else {
            if (chLocal != '\"') {
                this.matchStat = -1;
                return null;
            }
            int hash = 0;
            do {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal == '\"') {
                    final int start = this.bp + 0 + 1;
                    final int len = this.bp + offset - start - 1;
                    final String strVal = this.addSymbol(start, len, hash, symbolTable);
                    for (chLocal = this.charAt(this.bp + offset++); chLocal != serperator; chLocal = this.charAt(this.bp + offset++)) {
                        if (!isWhitespace(chLocal)) {
                            this.matchStat = -1;
                            return strVal;
                        }
                    }
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                    this.matchStat = 3;
                    return strVal;
                }
                hash = 31 * hash + chLocal;
            } while (chLocal != '\\');
            this.matchStat = -1;
            return null;
        }
    }
    
    public Collection<String> newCollectionByType(final Class<?> type) {
        if (type.isAssignableFrom(HashSet.class)) {
            return new HashSet<String>();
        }
        if (type.isAssignableFrom(ArrayList.class)) {
            return new ArrayList<String>();
        }
        if (type.isAssignableFrom(LinkedList.class)) {
            return new LinkedList<String>();
        }
        try {
            return (Collection<String>)type.newInstance();
        }
        catch (Exception e) {
            throw new JSONException(e.getMessage(), e);
        }
    }
    
    public Collection<String> scanFieldStringArray(final char[] fieldName, final Class<?> type) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return null;
        }
        final Collection<String> list = this.newCollectionByType(type);
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        if (chLocal != '[') {
            this.matchStat = -1;
            return null;
        }
        chLocal = this.charAt(this.bp + offset++);
        while (true) {
            if (chLocal == '\"') {
                final int startIndex = this.bp + offset;
                int endIndex = this.indexOf('\"', startIndex);
                if (endIndex == -1) {
                    throw new JSONException("unclosed str");
                }
                final int startIndex2 = this.bp + offset;
                String stringVal = this.subString(startIndex2, endIndex - startIndex2);
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
                    final int chars_len = endIndex - (this.bp + offset);
                    final char[] chars = this.sub_chars(this.bp + offset, chars_len);
                    stringVal = readString(chars, chars_len);
                }
                offset += endIndex - (this.bp + offset) + 1;
                chLocal = this.charAt(this.bp + offset++);
                list.add(stringVal);
            }
            else if (chLocal == 'n' && this.charAt(this.bp + offset) == 'u' && this.charAt(this.bp + offset + 1) == 'l' && this.charAt(this.bp + offset + 2) == 'l') {
                offset += 3;
                chLocal = this.charAt(this.bp + offset++);
                list.add(null);
            }
            else {
                if (chLocal == ']' && list.size() == 0) {
                    chLocal = this.charAt(this.bp + offset++);
                    break;
                }
                throw new JSONException("illega str");
            }
            if (chLocal == ',') {
                chLocal = this.charAt(this.bp + offset++);
            }
            else {
                if (chLocal == ']') {
                    chLocal = this.charAt(this.bp + offset++);
                    break;
                }
                this.matchStat = -1;
                return null;
            }
        }
        if (chLocal == ',') {
            this.bp += offset;
            this.ch = this.charAt(this.bp);
            this.matchStat = 3;
            return list;
        }
        if (chLocal == '}') {
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal == ',') {
                this.token = 16;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == ']') {
                this.token = 15;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == '}') {
                this.token = 13;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else {
                if (chLocal != '\u001a') {
                    this.matchStat = -1;
                    return null;
                }
                this.bp += offset - 1;
                this.token = 20;
                this.ch = '\u001a';
            }
            this.matchStat = 4;
            return list;
        }
        this.matchStat = -1;
        return null;
    }
    
    @Override
    public void scanStringArray(final Collection<String> list, final char seperator) {
        this.matchStat = 0;
        int offset = 0;
        char chLocal = this.charAt(this.bp + offset++);
        if (chLocal == 'n' && this.charAt(this.bp + offset) == 'u' && this.charAt(this.bp + offset + 1) == 'l' && this.charAt(this.bp + offset + 2) == 'l' && this.charAt(this.bp + offset + 3) == seperator) {
            this.bp += 5;
            this.ch = this.charAt(this.bp);
            this.matchStat = 5;
            return;
        }
        if (chLocal != '[') {
            this.matchStat = -1;
            return;
        }
        chLocal = this.charAt(this.bp + offset++);
        while (true) {
            if (chLocal == 'n' && this.charAt(this.bp + offset) == 'u' && this.charAt(this.bp + offset + 1) == 'l' && this.charAt(this.bp + offset + 2) == 'l') {
                offset += 3;
                chLocal = this.charAt(this.bp + offset++);
                list.add(null);
            }
            else {
                if (chLocal == ']' && list.size() == 0) {
                    chLocal = this.charAt(this.bp + offset++);
                    break;
                }
                if (chLocal != '\"') {
                    this.matchStat = -1;
                    return;
                }
                final int startIndex = this.bp + offset;
                int endIndex = this.indexOf('\"', startIndex);
                if (endIndex == -1) {
                    throw new JSONException("unclosed str");
                }
                String stringVal = this.subString(this.bp + offset, endIndex - startIndex);
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
                    final char[] chars = this.sub_chars(this.bp + offset, chars_len);
                    stringVal = readString(chars, chars_len);
                }
                offset += endIndex - (this.bp + offset) + 1;
                chLocal = this.charAt(this.bp + offset++);
                list.add(stringVal);
            }
            if (chLocal == ',') {
                chLocal = this.charAt(this.bp + offset++);
            }
            else {
                if (chLocal == ']') {
                    chLocal = this.charAt(this.bp + offset++);
                    break;
                }
                this.matchStat = -1;
                return;
            }
        }
        if (chLocal == seperator) {
            this.bp += offset;
            this.ch = this.charAt(this.bp);
            this.matchStat = 3;
            return;
        }
        this.matchStat = -1;
    }
    
    public int scanFieldInt(final char[] fieldName) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return 0;
        }
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        final boolean negative = chLocal == '-';
        if (negative) {
            chLocal = this.charAt(this.bp + offset++);
        }
        if (chLocal < '0' || chLocal > '9') {
            this.matchStat = -1;
            return 0;
        }
        int value = chLocal - '0';
        while (true) {
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal < '0' || chLocal > '9') {
                break;
            }
            value = value * 10 + (chLocal - '0');
        }
        if (chLocal == '.') {
            this.matchStat = -1;
            return 0;
        }
        if ((value < 0 || offset > 14 + fieldName.length) && (value != Integer.MIN_VALUE || offset != 17 || !negative)) {
            this.matchStat = -1;
            return 0;
        }
        if (chLocal == ',') {
            this.bp += offset;
            this.ch = this.charAt(this.bp);
            this.matchStat = 3;
            this.token = 16;
            return negative ? (-value) : value;
        }
        if (chLocal == '}') {
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal == ',') {
                this.token = 16;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == ']') {
                this.token = 15;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == '}') {
                this.token = 13;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else {
                if (chLocal != '\u001a') {
                    this.matchStat = -1;
                    return 0;
                }
                this.token = 20;
                this.bp += offset - 1;
                this.ch = '\u001a';
            }
            this.matchStat = 4;
            return negative ? (-value) : value;
        }
        this.matchStat = -1;
        return 0;
    }
    
    public final int[] scanFieldIntArray(final char[] fieldName) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return null;
        }
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        if (chLocal != '[') {
            this.matchStat = -2;
            return null;
        }
        chLocal = this.charAt(this.bp + offset++);
        int[] array = new int[16];
        int arrayIndex = 0;
        if (chLocal == ']') {
            chLocal = this.charAt(this.bp + offset++);
        }
        else {
            while (true) {
                boolean nagative = false;
                if (chLocal == '-') {
                    chLocal = this.charAt(this.bp + offset++);
                    nagative = true;
                }
                if (chLocal < '0' || chLocal > '9') {
                    this.matchStat = -1;
                    return null;
                }
                int value = chLocal - '0';
                while (true) {
                    chLocal = this.charAt(this.bp + offset++);
                    if (chLocal < '0' || chLocal > '9') {
                        break;
                    }
                    value = value * 10 + (chLocal - '0');
                }
                if (arrayIndex >= array.length) {
                    final int[] tmp = new int[array.length * 3 / 2];
                    System.arraycopy(array, 0, tmp, 0, arrayIndex);
                    array = tmp;
                }
                array[arrayIndex++] = (nagative ? (-value) : value);
                if (chLocal == ',') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                else {
                    if (chLocal == ']') {
                        chLocal = this.charAt(this.bp + offset++);
                        break;
                    }
                    continue;
                }
            }
        }
        if (arrayIndex != array.length) {
            final int[] tmp2 = new int[arrayIndex];
            System.arraycopy(array, 0, tmp2, 0, arrayIndex);
            array = tmp2;
        }
        if (chLocal == ',') {
            this.bp += offset - 1;
            this.next();
            this.matchStat = 3;
            this.token = 16;
            return array;
        }
        if (chLocal == '}') {
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal == ',') {
                this.token = 16;
                this.bp += offset - 1;
                this.next();
            }
            else if (chLocal == ']') {
                this.token = 15;
                this.bp += offset - 1;
                this.next();
            }
            else if (chLocal == '}') {
                this.token = 13;
                this.bp += offset - 1;
                this.next();
            }
            else {
                if (chLocal != '\u001a') {
                    this.matchStat = -1;
                    return null;
                }
                this.bp += offset - 1;
                this.token = 20;
                this.ch = '\u001a';
            }
            this.matchStat = 4;
            return array;
        }
        this.matchStat = -1;
        return null;
    }
    
    @Override
    public boolean scanBoolean(final char expectNext) {
        this.matchStat = 0;
        int offset = 0;
        char chLocal = this.charAt(this.bp + offset++);
        boolean value = false;
        if (chLocal == 't') {
            if (this.charAt(this.bp + offset) != 'r' || this.charAt(this.bp + offset + 1) != 'u' || this.charAt(this.bp + offset + 2) != 'e') {
                this.matchStat = -1;
                return false;
            }
            offset += 3;
            chLocal = this.charAt(this.bp + offset++);
            value = true;
        }
        else if (chLocal == 'f') {
            if (this.charAt(this.bp + offset) != 'a' || this.charAt(this.bp + offset + 1) != 'l' || this.charAt(this.bp + offset + 2) != 's' || this.charAt(this.bp + offset + 3) != 'e') {
                this.matchStat = -1;
                return false;
            }
            offset += 4;
            chLocal = this.charAt(this.bp + offset++);
            value = false;
        }
        else if (chLocal == '1') {
            chLocal = this.charAt(this.bp + offset++);
            value = true;
        }
        else if (chLocal == '0') {
            chLocal = this.charAt(this.bp + offset++);
            value = false;
        }
        while (chLocal != expectNext) {
            if (!isWhitespace(chLocal)) {
                this.matchStat = -1;
                return value;
            }
            chLocal = this.charAt(this.bp + offset++);
        }
        this.bp += offset;
        this.ch = this.charAt(this.bp);
        this.matchStat = 3;
        return value;
    }
    
    @Override
    public int scanInt(final char expectNext) {
        this.matchStat = 0;
        int offset = 0;
        char chLocal = this.charAt(this.bp + offset++);
        final boolean quote = chLocal == '\"';
        if (quote) {
            chLocal = this.charAt(this.bp + offset++);
        }
        final boolean negative = chLocal == '-';
        if (negative) {
            chLocal = this.charAt(this.bp + offset++);
        }
        if (chLocal >= '0' && chLocal <= '9') {
            int value = chLocal - '0';
            while (true) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal < '0' || chLocal > '9') {
                    break;
                }
                value = value * 10 + (chLocal - '0');
            }
            if (chLocal == '.') {
                this.matchStat = -1;
                return 0;
            }
            if (value < 0) {
                this.matchStat = -1;
                return 0;
            }
            while (chLocal != expectNext) {
                if (!isWhitespace(chLocal)) {
                    this.matchStat = -1;
                    return negative ? (-value) : value;
                }
                chLocal = this.charAt(this.bp + offset++);
            }
            this.bp += offset;
            this.ch = this.charAt(this.bp);
            this.matchStat = 3;
            this.token = 16;
            return negative ? (-value) : value;
        }
        else {
            if (chLocal == 'n' && this.charAt(this.bp + offset) == 'u' && this.charAt(this.bp + offset + 1) == 'l' && this.charAt(this.bp + offset + 2) == 'l') {
                this.matchStat = 5;
                final int value = 0;
                offset += 3;
                chLocal = this.charAt(this.bp + offset++);
                if (quote && chLocal == '\"') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal != ',') {
                    if (chLocal == ']') {
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                        this.matchStat = 5;
                        this.token = 15;
                        return value;
                    }
                    if (!isWhitespace(chLocal)) {
                        this.matchStat = -1;
                        return 0;
                    }
                    chLocal = this.charAt(this.bp + offset++);
                }
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 5;
                this.token = 16;
                return value;
            }
            this.matchStat = -1;
            return 0;
        }
    }
    
    public boolean scanFieldBoolean(final char[] fieldName) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return false;
        }
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        boolean value;
        if (chLocal == 't') {
            if (this.charAt(this.bp + offset++) != 'r') {
                this.matchStat = -1;
                return false;
            }
            if (this.charAt(this.bp + offset++) != 'u') {
                this.matchStat = -1;
                return false;
            }
            if (this.charAt(this.bp + offset++) != 'e') {
                this.matchStat = -1;
                return false;
            }
            value = true;
        }
        else {
            if (chLocal != 'f') {
                this.matchStat = -1;
                return false;
            }
            if (this.charAt(this.bp + offset++) != 'a') {
                this.matchStat = -1;
                return false;
            }
            if (this.charAt(this.bp + offset++) != 'l') {
                this.matchStat = -1;
                return false;
            }
            if (this.charAt(this.bp + offset++) != 's') {
                this.matchStat = -1;
                return false;
            }
            if (this.charAt(this.bp + offset++) != 'e') {
                this.matchStat = -1;
                return false;
            }
            value = false;
        }
        chLocal = this.charAt(this.bp + offset++);
        if (chLocal == ',') {
            this.bp += offset;
            this.ch = this.charAt(this.bp);
            this.matchStat = 3;
            this.token = 16;
            return value;
        }
        if (chLocal == '}') {
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal == ',') {
                this.token = 16;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == ']') {
                this.token = 15;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == '}') {
                this.token = 13;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else {
                if (chLocal != '\u001a') {
                    this.matchStat = -1;
                    return false;
                }
                this.token = 20;
                this.bp += offset - 1;
                this.ch = '\u001a';
            }
            this.matchStat = 4;
            return value;
        }
        this.matchStat = -1;
        return false;
    }
    
    public long scanFieldLong(final char[] fieldName) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return 0L;
        }
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        boolean negative = false;
        if (chLocal == '-') {
            chLocal = this.charAt(this.bp + offset++);
            negative = true;
        }
        if (chLocal < '0' || chLocal > '9') {
            this.matchStat = -1;
            return 0L;
        }
        long value = chLocal - '0';
        while (true) {
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal < '0' || chLocal > '9') {
                break;
            }
            value = value * 10L + (chLocal - '0');
        }
        if (chLocal == '.') {
            this.matchStat = -1;
            return 0L;
        }
        final boolean valid = offset - fieldName.length < 21 && (value >= 0L || (value == Long.MIN_VALUE && negative));
        if (!valid) {
            this.matchStat = -1;
            return 0L;
        }
        if (chLocal == ',') {
            this.bp += offset;
            this.ch = this.charAt(this.bp);
            this.matchStat = 3;
            this.token = 16;
            return negative ? (-value) : value;
        }
        if (chLocal == '}') {
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal == ',') {
                this.token = 16;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == ']') {
                this.token = 15;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == '}') {
                this.token = 13;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else {
                if (chLocal != '\u001a') {
                    this.matchStat = -1;
                    return 0L;
                }
                this.token = 20;
                this.bp += offset - 1;
                this.ch = '\u001a';
            }
            this.matchStat = 4;
            return negative ? (-value) : value;
        }
        this.matchStat = -1;
        return 0L;
    }
    
    @Override
    public long scanLong(final char expectNextChar) {
        this.matchStat = 0;
        int offset = 0;
        char chLocal = this.charAt(this.bp + offset++);
        final boolean quote = chLocal == '\"';
        if (quote) {
            chLocal = this.charAt(this.bp + offset++);
        }
        final boolean negative = chLocal == '-';
        if (negative) {
            chLocal = this.charAt(this.bp + offset++);
        }
        if (chLocal >= '0' && chLocal <= '9') {
            long value = chLocal - '0';
            while (true) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal < '0' || chLocal > '9') {
                    break;
                }
                value = value * 10L + (chLocal - '0');
            }
            if (chLocal == '.') {
                this.matchStat = -1;
                return 0L;
            }
            final boolean valid = value >= 0L || (value == Long.MIN_VALUE && negative);
            if (!valid) {
                final String val = this.subString(this.bp, offset - 1);
                throw new NumberFormatException(val);
            }
            if (quote) {
                if (chLocal != '\"') {
                    this.matchStat = -1;
                    return 0L;
                }
                chLocal = this.charAt(this.bp + offset++);
            }
            while (chLocal != expectNextChar) {
                if (!isWhitespace(chLocal)) {
                    this.matchStat = -1;
                    return value;
                }
                chLocal = this.charAt(this.bp + offset++);
            }
            this.bp += offset;
            this.ch = this.charAt(this.bp);
            this.matchStat = 3;
            this.token = 16;
            return negative ? (-value) : value;
        }
        else {
            if (chLocal == 'n' && this.charAt(this.bp + offset) == 'u' && this.charAt(this.bp + offset + 1) == 'l' && this.charAt(this.bp + offset + 2) == 'l') {
                this.matchStat = 5;
                final long value = 0L;
                offset += 3;
                chLocal = this.charAt(this.bp + offset++);
                if (quote && chLocal == '\"') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal != ',') {
                    if (chLocal == ']') {
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                        this.matchStat = 5;
                        this.token = 15;
                        return value;
                    }
                    if (!isWhitespace(chLocal)) {
                        this.matchStat = -1;
                        return 0L;
                    }
                    chLocal = this.charAt(this.bp + offset++);
                }
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 5;
                this.token = 16;
                return value;
            }
            this.matchStat = -1;
            return 0L;
        }
    }
    
    public final float scanFieldFloat(final char[] fieldName) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return 0.0f;
        }
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        final boolean quote = chLocal == '\"';
        if (quote) {
            chLocal = this.charAt(this.bp + offset++);
        }
        final boolean negative = chLocal == '-';
        if (negative) {
            chLocal = this.charAt(this.bp + offset++);
        }
        if (chLocal >= '0' && chLocal <= '9') {
            long intVal = chLocal - '0';
            while (true) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal < '0' || chLocal > '9') {
                    break;
                }
                intVal = intVal * 10L + (chLocal - '0');
            }
            long power = 1L;
            final boolean small = chLocal == '.';
            if (small) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal < '0' || chLocal > '9') {
                    this.matchStat = -1;
                    return 0.0f;
                }
                intVal = intVal * 10L + (chLocal - '0');
                power = 10L;
                while (true) {
                    chLocal = this.charAt(this.bp + offset++);
                    if (chLocal < '0' || chLocal > '9') {
                        break;
                    }
                    intVal = intVal * 10L + (chLocal - '0');
                    power *= 10L;
                }
            }
            final boolean exp = chLocal == 'e' || chLocal == 'E';
            if (exp) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal == '+' || chLocal == '-') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal >= '0' && chLocal <= '9') {
                    chLocal = this.charAt(this.bp + offset++);
                }
            }
            int start;
            int count;
            if (quote) {
                if (chLocal != '\"') {
                    this.matchStat = -1;
                    return 0.0f;
                }
                chLocal = this.charAt(this.bp + offset++);
                start = this.bp + fieldName.length + 1;
                count = this.bp + offset - start - 2;
            }
            else {
                start = this.bp + fieldName.length;
                count = this.bp + offset - start - 1;
            }
            float value;
            if (!exp && count < 17) {
                value = (float)(intVal / (double)power);
                if (negative) {
                    value = -value;
                }
            }
            else {
                final String text = this.subString(start, count);
                value = Float.parseFloat(text);
            }
            if (chLocal == ',') {
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 3;
                this.token = 16;
                return value;
            }
            if (chLocal == '}') {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal == ',') {
                    this.token = 16;
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                }
                else if (chLocal == ']') {
                    this.token = 15;
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                }
                else if (chLocal == '}') {
                    this.token = 13;
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                }
                else {
                    if (chLocal != '\u001a') {
                        this.matchStat = -1;
                        return 0.0f;
                    }
                    this.bp += offset - 1;
                    this.token = 20;
                    this.ch = '\u001a';
                }
                this.matchStat = 4;
                return value;
            }
            this.matchStat = -1;
            return 0.0f;
        }
        else {
            if (chLocal == 'n' && this.charAt(this.bp + offset) == 'u' && this.charAt(this.bp + offset + 1) == 'l' && this.charAt(this.bp + offset + 2) == 'l') {
                this.matchStat = 5;
                final float value = 0.0f;
                offset += 3;
                chLocal = this.charAt(this.bp + offset++);
                if (quote && chLocal == '\"') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal != ',') {
                    if (chLocal == '}') {
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                        this.matchStat = 5;
                        this.token = 13;
                        return value;
                    }
                    if (!isWhitespace(chLocal)) {
                        this.matchStat = -1;
                        return 0.0f;
                    }
                    chLocal = this.charAt(this.bp + offset++);
                }
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 5;
                this.token = 16;
                return value;
            }
            this.matchStat = -1;
            return 0.0f;
        }
    }
    
    @Override
    public final float scanFloat(final char seperator) {
        this.matchStat = 0;
        int offset = 0;
        char chLocal = this.charAt(this.bp + offset++);
        final boolean quote = chLocal == '\"';
        if (quote) {
            chLocal = this.charAt(this.bp + offset++);
        }
        final boolean negative = chLocal == '-';
        if (negative) {
            chLocal = this.charAt(this.bp + offset++);
        }
        if (chLocal >= '0' && chLocal <= '9') {
            long intVal = chLocal - '0';
            while (true) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal < '0' || chLocal > '9') {
                    break;
                }
                intVal = intVal * 10L + (chLocal - '0');
            }
            long power = 1L;
            final boolean small = chLocal == '.';
            if (small) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal < '0' || chLocal > '9') {
                    this.matchStat = -1;
                    return 0.0f;
                }
                intVal = intVal * 10L + (chLocal - '0');
                power = 10L;
                while (true) {
                    chLocal = this.charAt(this.bp + offset++);
                    if (chLocal < '0' || chLocal > '9') {
                        break;
                    }
                    intVal = intVal * 10L + (chLocal - '0');
                    power *= 10L;
                }
            }
            final boolean exp = chLocal == 'e' || chLocal == 'E';
            if (exp) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal == '+' || chLocal == '-') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal >= '0' && chLocal <= '9') {
                    chLocal = this.charAt(this.bp + offset++);
                }
            }
            int start;
            int count;
            if (quote) {
                if (chLocal != '\"') {
                    this.matchStat = -1;
                    return 0.0f;
                }
                chLocal = this.charAt(this.bp + offset++);
                start = this.bp + 1;
                count = this.bp + offset - start - 2;
            }
            else {
                start = this.bp;
                count = this.bp + offset - start - 1;
            }
            float value;
            if (!exp && count < 17) {
                value = (float)(intVal / (double)power);
                if (negative) {
                    value = -value;
                }
            }
            else {
                final String text = this.subString(start, count);
                value = Float.parseFloat(text);
            }
            if (chLocal == seperator) {
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 3;
                this.token = 16;
                return value;
            }
            this.matchStat = -1;
            return value;
        }
        else {
            if (chLocal == 'n' && this.charAt(this.bp + offset) == 'u' && this.charAt(this.bp + offset + 1) == 'l' && this.charAt(this.bp + offset + 2) == 'l') {
                this.matchStat = 5;
                final float value = 0.0f;
                offset += 3;
                chLocal = this.charAt(this.bp + offset++);
                if (quote && chLocal == '\"') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal != ',') {
                    if (chLocal == ']') {
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                        this.matchStat = 5;
                        this.token = 15;
                        return value;
                    }
                    if (!isWhitespace(chLocal)) {
                        this.matchStat = -1;
                        return 0.0f;
                    }
                    chLocal = this.charAt(this.bp + offset++);
                }
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 5;
                this.token = 16;
                return value;
            }
            this.matchStat = -1;
            return 0.0f;
        }
    }
    
    @Override
    public double scanDouble(final char seperator) {
        this.matchStat = 0;
        int offset = 0;
        char chLocal = this.charAt(this.bp + offset++);
        final boolean quote = chLocal == '\"';
        if (quote) {
            chLocal = this.charAt(this.bp + offset++);
        }
        final boolean negative = chLocal == '-';
        if (negative) {
            chLocal = this.charAt(this.bp + offset++);
        }
        if (chLocal >= '0' && chLocal <= '9') {
            long intVal = chLocal - '0';
            while (true) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal < '0' || chLocal > '9') {
                    break;
                }
                intVal = intVal * 10L + (chLocal - '0');
            }
            long power = 1L;
            final boolean small = chLocal == '.';
            if (small) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal < '0' || chLocal > '9') {
                    this.matchStat = -1;
                    return 0.0;
                }
                intVal = intVal * 10L + (chLocal - '0');
                power = 10L;
                while (true) {
                    chLocal = this.charAt(this.bp + offset++);
                    if (chLocal < '0' || chLocal > '9') {
                        break;
                    }
                    intVal = intVal * 10L + (chLocal - '0');
                    power *= 10L;
                }
            }
            final boolean exp = chLocal == 'e' || chLocal == 'E';
            if (exp) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal == '+' || chLocal == '-') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal >= '0' && chLocal <= '9') {
                    chLocal = this.charAt(this.bp + offset++);
                }
            }
            int start;
            int count;
            if (quote) {
                if (chLocal != '\"') {
                    this.matchStat = -1;
                    return 0.0;
                }
                chLocal = this.charAt(this.bp + offset++);
                start = this.bp + 1;
                count = this.bp + offset - start - 2;
            }
            else {
                start = this.bp;
                count = this.bp + offset - start - 1;
            }
            double value;
            if (!exp && count < 17) {
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
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 3;
                this.token = 16;
                return value;
            }
            this.matchStat = -1;
            return value;
        }
        else {
            if (chLocal == 'n' && this.charAt(this.bp + offset) == 'u' && this.charAt(this.bp + offset + 1) == 'l' && this.charAt(this.bp + offset + 2) == 'l') {
                this.matchStat = 5;
                final double value = 0.0;
                offset += 3;
                chLocal = this.charAt(this.bp + offset++);
                if (quote && chLocal == '\"') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal != ',') {
                    if (chLocal == ']') {
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                        this.matchStat = 5;
                        this.token = 15;
                        return value;
                    }
                    if (!isWhitespace(chLocal)) {
                        this.matchStat = -1;
                        return 0.0;
                    }
                    chLocal = this.charAt(this.bp + offset++);
                }
                this.bp += offset;
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
    public BigDecimal scanDecimal(final char seperator) {
        this.matchStat = 0;
        int offset = 0;
        char chLocal = this.charAt(this.bp + offset++);
        final boolean quote = chLocal == '\"';
        if (quote) {
            chLocal = this.charAt(this.bp + offset++);
        }
        final boolean negative = chLocal == '-';
        if (negative) {
            chLocal = this.charAt(this.bp + offset++);
        }
        if (chLocal >= '0' && chLocal <= '9') {
            do {
                chLocal = this.charAt(this.bp + offset++);
            } while (chLocal >= '0' && chLocal <= '9');
            final boolean small = chLocal == '.';
            if (small) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal < '0' || chLocal > '9') {
                    this.matchStat = -1;
                    return null;
                }
                do {
                    chLocal = this.charAt(this.bp + offset++);
                } while (chLocal >= '0' && chLocal <= '9');
            }
            final boolean exp = chLocal == 'e' || chLocal == 'E';
            if (exp) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal == '+' || chLocal == '-') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal >= '0' && chLocal <= '9') {
                    chLocal = this.charAt(this.bp + offset++);
                }
            }
            int start;
            int count;
            if (quote) {
                if (chLocal != '\"') {
                    this.matchStat = -1;
                    return null;
                }
                chLocal = this.charAt(this.bp + offset++);
                start = this.bp + 1;
                count = this.bp + offset - start - 2;
            }
            else {
                start = this.bp;
                count = this.bp + offset - start - 1;
            }
            final char[] chars = this.sub_chars(start, count);
            final BigDecimal value = new BigDecimal(chars);
            if (chLocal == ',') {
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 3;
                this.token = 16;
                return value;
            }
            if (chLocal == ']') {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal == ',') {
                    this.token = 16;
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                }
                else if (chLocal == ']') {
                    this.token = 15;
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                }
                else if (chLocal == '}') {
                    this.token = 13;
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                }
                else {
                    if (chLocal != '\u001a') {
                        this.matchStat = -1;
                        return null;
                    }
                    this.token = 20;
                    this.bp += offset - 1;
                    this.ch = '\u001a';
                }
                this.matchStat = 4;
                return value;
            }
            this.matchStat = -1;
            return null;
        }
        else {
            if (chLocal == 'n' && this.charAt(this.bp + offset) == 'u' && this.charAt(this.bp + offset + 1) == 'l' && this.charAt(this.bp + offset + 2) == 'l') {
                this.matchStat = 5;
                final BigDecimal value = null;
                offset += 3;
                chLocal = this.charAt(this.bp + offset++);
                if (quote && chLocal == '\"') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal != ',') {
                    if (chLocal == '}') {
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                        this.matchStat = 5;
                        this.token = 13;
                        return value;
                    }
                    if (!isWhitespace(chLocal)) {
                        this.matchStat = -1;
                        return null;
                    }
                    chLocal = this.charAt(this.bp + offset++);
                }
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 5;
                this.token = 16;
                return value;
            }
            this.matchStat = -1;
            return null;
        }
    }
    
    public final float[] scanFieldFloatArray(final char[] fieldName) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return null;
        }
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        if (chLocal != '[') {
            this.matchStat = -2;
            return null;
        }
        chLocal = this.charAt(this.bp + offset++);
        float[] array = new float[16];
        int arrayIndex = 0;
        while (true) {
            final int start = this.bp + offset - 1;
            final boolean negative = chLocal == '-';
            if (negative) {
                chLocal = this.charAt(this.bp + offset++);
            }
            if (chLocal < '0' || chLocal > '9') {
                this.matchStat = -1;
                return null;
            }
            int intVal = chLocal - '0';
            while (true) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal < '0' || chLocal > '9') {
                    break;
                }
                intVal = intVal * 10 + (chLocal - '0');
            }
            int power = 1;
            final boolean small = chLocal == '.';
            if (small) {
                chLocal = this.charAt(this.bp + offset++);
                power = 10;
                if (chLocal < '0' || chLocal > '9') {
                    this.matchStat = -1;
                    return null;
                }
                intVal = intVal * 10 + (chLocal - '0');
                while (true) {
                    chLocal = this.charAt(this.bp + offset++);
                    if (chLocal < '0' || chLocal > '9') {
                        break;
                    }
                    intVal = intVal * 10 + (chLocal - '0');
                    power *= 10;
                }
            }
            final boolean exp = chLocal == 'e' || chLocal == 'E';
            if (exp) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal == '+' || chLocal == '-') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal >= '0' && chLocal <= '9') {
                    chLocal = this.charAt(this.bp + offset++);
                }
            }
            final int count = this.bp + offset - start - 1;
            float value;
            if (!exp && count < 10) {
                value = intVal / (float)power;
                if (negative) {
                    value = -value;
                }
            }
            else {
                final String text = this.subString(start, count);
                value = Float.parseFloat(text);
            }
            if (arrayIndex >= array.length) {
                final float[] tmp = new float[array.length * 3 / 2];
                System.arraycopy(array, 0, tmp, 0, arrayIndex);
                array = tmp;
            }
            array[arrayIndex++] = value;
            if (chLocal == ',') {
                chLocal = this.charAt(this.bp + offset++);
            }
            else {
                if (chLocal != ']') {
                    continue;
                }
                chLocal = this.charAt(this.bp + offset++);
                if (arrayIndex != array.length) {
                    final float[] tmp2 = new float[arrayIndex];
                    System.arraycopy(array, 0, tmp2, 0, arrayIndex);
                    array = tmp2;
                }
                if (chLocal == ',') {
                    this.bp += offset - 1;
                    this.next();
                    this.matchStat = 3;
                    this.token = 16;
                    return array;
                }
                if (chLocal == '}') {
                    chLocal = this.charAt(this.bp + offset++);
                    if (chLocal == ',') {
                        this.token = 16;
                        this.bp += offset - 1;
                        this.next();
                    }
                    else if (chLocal == ']') {
                        this.token = 15;
                        this.bp += offset - 1;
                        this.next();
                    }
                    else if (chLocal == '}') {
                        this.token = 13;
                        this.bp += offset - 1;
                        this.next();
                    }
                    else {
                        if (chLocal != '\u001a') {
                            this.matchStat = -1;
                            return null;
                        }
                        this.bp += offset - 1;
                        this.token = 20;
                        this.ch = '\u001a';
                    }
                    this.matchStat = 4;
                    return array;
                }
                this.matchStat = -1;
                return null;
            }
        }
    }
    
    public final float[][] scanFieldFloatArray2(final char[] fieldName) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return null;
        }
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        if (chLocal != '[') {
            this.matchStat = -2;
            return null;
        }
        chLocal = this.charAt(this.bp + offset++);
        float[][] arrayarray = new float[16][];
        int arrayarrayIndex = 0;
    Label_0707:
        while (chLocal == '[') {
            chLocal = this.charAt(this.bp + offset++);
            float[] array = new float[16];
            int arrayIndex = 0;
            while (true) {
                final int start = this.bp + offset - 1;
                final boolean negative = chLocal == '-';
                if (negative) {
                    chLocal = this.charAt(this.bp + offset++);
                }
                if (chLocal < '0' || chLocal > '9') {
                    this.matchStat = -1;
                    return null;
                }
                int intVal = chLocal - '0';
                while (true) {
                    chLocal = this.charAt(this.bp + offset++);
                    if (chLocal < '0' || chLocal > '9') {
                        break;
                    }
                    intVal = intVal * 10 + (chLocal - '0');
                }
                int power = 1;
                if (chLocal == '.') {
                    chLocal = this.charAt(this.bp + offset++);
                    if (chLocal < '0' || chLocal > '9') {
                        this.matchStat = -1;
                        return null;
                    }
                    intVal = intVal * 10 + (chLocal - '0');
                    power = 10;
                    while (true) {
                        chLocal = this.charAt(this.bp + offset++);
                        if (chLocal < '0' || chLocal > '9') {
                            break;
                        }
                        intVal = intVal * 10 + (chLocal - '0');
                        power *= 10;
                    }
                }
                final boolean exp = chLocal == 'e' || chLocal == 'E';
                if (exp) {
                    chLocal = this.charAt(this.bp + offset++);
                    if (chLocal == '+' || chLocal == '-') {
                        chLocal = this.charAt(this.bp + offset++);
                    }
                    while (chLocal >= '0' && chLocal <= '9') {
                        chLocal = this.charAt(this.bp + offset++);
                    }
                }
                final int count = this.bp + offset - start - 1;
                float value;
                if (!exp && count < 10) {
                    value = intVal / (float)power;
                    if (negative) {
                        value = -value;
                    }
                }
                else {
                    final String text = this.subString(start, count);
                    value = Float.parseFloat(text);
                }
                if (arrayIndex >= array.length) {
                    final float[] tmp = new float[array.length * 3 / 2];
                    System.arraycopy(array, 0, tmp, 0, arrayIndex);
                    array = tmp;
                }
                array[arrayIndex++] = value;
                if (chLocal == ',') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                else {
                    if (chLocal == ']') {
                        chLocal = this.charAt(this.bp + offset++);
                        if (arrayIndex != array.length) {
                            final float[] tmp2 = new float[arrayIndex];
                            System.arraycopy(array, 0, tmp2, 0, arrayIndex);
                            array = tmp2;
                        }
                        if (arrayarrayIndex >= arrayarray.length) {
                            final float[][] tmp3 = new float[arrayarray.length * 3 / 2][];
                            System.arraycopy(array, 0, tmp3, 0, arrayIndex);
                            arrayarray = tmp3;
                        }
                        arrayarray[arrayarrayIndex++] = array;
                        if (chLocal == ',') {
                            chLocal = this.charAt(this.bp + offset++);
                        }
                        else if (chLocal == ']') {
                            chLocal = this.charAt(this.bp + offset++);
                            break Label_0707;
                        }
                        break;
                    }
                    continue;
                }
            }
        }
        if (arrayarrayIndex != arrayarray.length) {
            final float[][] tmp4 = new float[arrayarrayIndex][];
            System.arraycopy(arrayarray, 0, tmp4, 0, arrayarrayIndex);
            arrayarray = tmp4;
        }
        if (chLocal == ',') {
            this.bp += offset - 1;
            this.next();
            this.matchStat = 3;
            this.token = 16;
            return arrayarray;
        }
        if (chLocal == '}') {
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal == ',') {
                this.token = 16;
                this.bp += offset - 1;
                this.next();
            }
            else if (chLocal == ']') {
                this.token = 15;
                this.bp += offset - 1;
                this.next();
            }
            else if (chLocal == '}') {
                this.token = 13;
                this.bp += offset - 1;
                this.next();
            }
            else {
                if (chLocal != '\u001a') {
                    this.matchStat = -1;
                    return null;
                }
                this.bp += offset - 1;
                this.token = 20;
                this.ch = '\u001a';
            }
            this.matchStat = 4;
            return arrayarray;
        }
        this.matchStat = -1;
        return null;
    }
    
    public final double scanFieldDouble(final char[] fieldName) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return 0.0;
        }
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        final boolean quote = chLocal == '\"';
        if (quote) {
            chLocal = this.charAt(this.bp + offset++);
        }
        final boolean negative = chLocal == '-';
        if (negative) {
            chLocal = this.charAt(this.bp + offset++);
        }
        if (chLocal >= '0' && chLocal <= '9') {
            long intVal = chLocal - '0';
            while (true) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal < '0' || chLocal > '9') {
                    break;
                }
                intVal = intVal * 10L + (chLocal - '0');
            }
            long power = 1L;
            final boolean small = chLocal == '.';
            if (small) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal < '0' || chLocal > '9') {
                    this.matchStat = -1;
                    return 0.0;
                }
                intVal = intVal * 10L + (chLocal - '0');
                power = 10L;
                while (true) {
                    chLocal = this.charAt(this.bp + offset++);
                    if (chLocal < '0' || chLocal > '9') {
                        break;
                    }
                    intVal = intVal * 10L + (chLocal - '0');
                    power *= 10L;
                }
            }
            final boolean exp = chLocal == 'e' || chLocal == 'E';
            if (exp) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal == '+' || chLocal == '-') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal >= '0' && chLocal <= '9') {
                    chLocal = this.charAt(this.bp + offset++);
                }
            }
            int start;
            int count;
            if (quote) {
                if (chLocal != '\"') {
                    this.matchStat = -1;
                    return 0.0;
                }
                chLocal = this.charAt(this.bp + offset++);
                start = this.bp + fieldName.length + 1;
                count = this.bp + offset - start - 2;
            }
            else {
                start = this.bp + fieldName.length;
                count = this.bp + offset - start - 1;
            }
            double value;
            if (!exp && count < 17) {
                value = intVal / (double)power;
                if (negative) {
                    value = -value;
                }
            }
            else {
                final String text = this.subString(start, count);
                value = Double.parseDouble(text);
            }
            if (chLocal == ',') {
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 3;
                this.token = 16;
                return value;
            }
            if (chLocal == '}') {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal == ',') {
                    this.token = 16;
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                }
                else if (chLocal == ']') {
                    this.token = 15;
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                }
                else if (chLocal == '}') {
                    this.token = 13;
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                }
                else {
                    if (chLocal != '\u001a') {
                        this.matchStat = -1;
                        return 0.0;
                    }
                    this.token = 20;
                    this.bp += offset - 1;
                    this.ch = '\u001a';
                }
                this.matchStat = 4;
                return value;
            }
            this.matchStat = -1;
            return 0.0;
        }
        else {
            if (chLocal == 'n' && this.charAt(this.bp + offset) == 'u' && this.charAt(this.bp + offset + 1) == 'l' && this.charAt(this.bp + offset + 2) == 'l') {
                this.matchStat = 5;
                final double value = 0.0;
                offset += 3;
                chLocal = this.charAt(this.bp + offset++);
                if (quote && chLocal == '\"') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal != ',') {
                    if (chLocal == '}') {
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                        this.matchStat = 5;
                        this.token = 13;
                        return value;
                    }
                    if (!isWhitespace(chLocal)) {
                        this.matchStat = -1;
                        return 0.0;
                    }
                    chLocal = this.charAt(this.bp + offset++);
                }
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 5;
                this.token = 16;
                return value;
            }
            this.matchStat = -1;
            return 0.0;
        }
    }
    
    public BigDecimal scanFieldDecimal(final char[] fieldName) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return null;
        }
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        final boolean quote = chLocal == '\"';
        if (quote) {
            chLocal = this.charAt(this.bp + offset++);
        }
        final boolean negative = chLocal == '-';
        if (negative) {
            chLocal = this.charAt(this.bp + offset++);
        }
        if (chLocal >= '0' && chLocal <= '9') {
            do {
                chLocal = this.charAt(this.bp + offset++);
            } while (chLocal >= '0' && chLocal <= '9');
            final boolean small = chLocal == '.';
            if (small) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal < '0' || chLocal > '9') {
                    this.matchStat = -1;
                    return null;
                }
                do {
                    chLocal = this.charAt(this.bp + offset++);
                } while (chLocal >= '0' && chLocal <= '9');
            }
            final boolean exp = chLocal == 'e' || chLocal == 'E';
            if (exp) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal == '+' || chLocal == '-') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal >= '0' && chLocal <= '9') {
                    chLocal = this.charAt(this.bp + offset++);
                }
            }
            int start;
            int count;
            if (quote) {
                if (chLocal != '\"') {
                    this.matchStat = -1;
                    return null;
                }
                chLocal = this.charAt(this.bp + offset++);
                start = this.bp + fieldName.length + 1;
                count = this.bp + offset - start - 2;
            }
            else {
                start = this.bp + fieldName.length;
                count = this.bp + offset - start - 1;
            }
            final char[] chars = this.sub_chars(start, count);
            final BigDecimal value = new BigDecimal(chars);
            if (chLocal == ',') {
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 3;
                this.token = 16;
                return value;
            }
            if (chLocal == '}') {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal == ',') {
                    this.token = 16;
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                }
                else if (chLocal == ']') {
                    this.token = 15;
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                }
                else if (chLocal == '}') {
                    this.token = 13;
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                }
                else {
                    if (chLocal != '\u001a') {
                        this.matchStat = -1;
                        return null;
                    }
                    this.token = 20;
                    this.bp += offset - 1;
                    this.ch = '\u001a';
                }
                this.matchStat = 4;
                return value;
            }
            this.matchStat = -1;
            return null;
        }
        else {
            if (chLocal == 'n' && this.charAt(this.bp + offset) == 'u' && this.charAt(this.bp + offset + 1) == 'l' && this.charAt(this.bp + offset + 2) == 'l') {
                this.matchStat = 5;
                final BigDecimal value = null;
                offset += 3;
                chLocal = this.charAt(this.bp + offset++);
                if (quote && chLocal == '\"') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal != ',') {
                    if (chLocal == '}') {
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                        this.matchStat = 5;
                        this.token = 13;
                        return value;
                    }
                    if (!isWhitespace(chLocal)) {
                        this.matchStat = -1;
                        return null;
                    }
                    chLocal = this.charAt(this.bp + offset++);
                }
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 5;
                this.token = 16;
                return value;
            }
            this.matchStat = -1;
            return null;
        }
    }
    
    public BigInteger scanFieldBigInteger(final char[] fieldName) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return null;
        }
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        final boolean quote = chLocal == '\"';
        if (quote) {
            chLocal = this.charAt(this.bp + offset++);
        }
        final boolean negative = chLocal == '-';
        if (negative) {
            chLocal = this.charAt(this.bp + offset++);
        }
        if (chLocal >= '0' && chLocal <= '9') {
            long intVal = chLocal - '0';
            boolean overflow = false;
            while (true) {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal < '0' || chLocal > '9') {
                    break;
                }
                final long temp = intVal * 10L + (chLocal - '0');
                if (temp < intVal) {
                    overflow = true;
                    break;
                }
                intVal = temp;
            }
            int start;
            int count;
            if (quote) {
                if (chLocal != '\"') {
                    this.matchStat = -1;
                    return null;
                }
                chLocal = this.charAt(this.bp + offset++);
                start = this.bp + fieldName.length + 1;
                count = this.bp + offset - start - 2;
            }
            else {
                start = this.bp + fieldName.length;
                count = this.bp + offset - start - 1;
            }
            BigInteger value;
            if (!overflow && (count < 20 || (negative && count < 21))) {
                value = BigInteger.valueOf(negative ? (-intVal) : intVal);
            }
            else {
                final String strVal = this.subString(start, count);
                value = new BigInteger(strVal);
            }
            if (chLocal == ',') {
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 3;
                this.token = 16;
                return value;
            }
            if (chLocal == '}') {
                chLocal = this.charAt(this.bp + offset++);
                if (chLocal == ',') {
                    this.token = 16;
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                }
                else if (chLocal == ']') {
                    this.token = 15;
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                }
                else if (chLocal == '}') {
                    this.token = 13;
                    this.bp += offset;
                    this.ch = this.charAt(this.bp);
                }
                else {
                    if (chLocal != '\u001a') {
                        this.matchStat = -1;
                        return null;
                    }
                    this.token = 20;
                    this.bp += offset - 1;
                    this.ch = '\u001a';
                }
                this.matchStat = 4;
                return value;
            }
            this.matchStat = -1;
            return null;
        }
        else {
            if (chLocal == 'n' && this.charAt(this.bp + offset) == 'u' && this.charAt(this.bp + offset + 1) == 'l' && this.charAt(this.bp + offset + 2) == 'l') {
                this.matchStat = 5;
                final BigInteger value = null;
                offset += 3;
                chLocal = this.charAt(this.bp + offset++);
                if (quote && chLocal == '\"') {
                    chLocal = this.charAt(this.bp + offset++);
                }
                while (chLocal != ',') {
                    if (chLocal == '}') {
                        this.bp += offset;
                        this.ch = this.charAt(this.bp);
                        this.matchStat = 5;
                        this.token = 13;
                        return value;
                    }
                    if (!isWhitespace(chLocal)) {
                        this.matchStat = -1;
                        return null;
                    }
                    chLocal = this.charAt(this.bp + offset++);
                }
                this.bp += offset;
                this.ch = this.charAt(this.bp);
                this.matchStat = 5;
                this.token = 16;
                return value;
            }
            this.matchStat = -1;
            return null;
        }
    }
    
    public Date scanFieldDate(final char[] fieldName) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return null;
        }
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        Date dateVal;
        if (chLocal == '\"') {
            final int startIndex = this.bp + fieldName.length + 1;
            int endIndex = this.indexOf('\"', startIndex);
            if (endIndex == -1) {
                throw new JSONException("unclosed str");
            }
            final int startIndex2 = this.bp + fieldName.length + 1;
            String stringVal = this.subString(startIndex2, endIndex - startIndex2);
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
                stringVal = readString(chars, chars_len);
            }
            offset += endIndex - (this.bp + fieldName.length + 1) + 1;
            chLocal = this.charAt(this.bp + offset++);
            final JSONScanner dateLexer = new JSONScanner(stringVal);
            try {
                if (!dateLexer.scanISO8601DateIfMatch(false)) {
                    this.matchStat = -1;
                    return null;
                }
                final Calendar calendar = dateLexer.getCalendar();
                dateVal = calendar.getTime();
            }
            finally {
                dateLexer.close();
            }
        }
        else {
            if (chLocal != '-' && (chLocal < '0' || chLocal > '9')) {
                this.matchStat = -1;
                return null;
            }
            long millis = 0L;
            boolean negative = false;
            if (chLocal == '-') {
                chLocal = this.charAt(this.bp + offset++);
                negative = true;
            }
            if (chLocal >= '0' && chLocal <= '9') {
                millis = chLocal - '0';
                while (true) {
                    chLocal = this.charAt(this.bp + offset++);
                    if (chLocal < '0' || chLocal > '9') {
                        break;
                    }
                    millis = millis * 10L + (chLocal - '0');
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
        if (chLocal == ',') {
            this.bp += offset;
            this.ch = this.charAt(this.bp);
            this.matchStat = 3;
            return dateVal;
        }
        if (chLocal == '}') {
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal == ',') {
                this.token = 16;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == ']') {
                this.token = 15;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == '}') {
                this.token = 13;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else {
                if (chLocal != '\u001a') {
                    this.matchStat = -1;
                    return null;
                }
                this.token = 20;
                this.bp += offset - 1;
                this.ch = '\u001a';
            }
            this.matchStat = 4;
            return dateVal;
        }
        this.matchStat = -1;
        return null;
    }
    
    public Date scanDate(final char seperator) {
        this.matchStat = 0;
        int offset = 0;
        char chLocal = this.charAt(this.bp + offset++);
        Date dateVal;
        if (chLocal == '\"') {
            final int startIndex = this.bp + 1;
            int endIndex = this.indexOf('\"', startIndex);
            if (endIndex == -1) {
                throw new JSONException("unclosed str");
            }
            final int startIndex2 = this.bp + 1;
            String stringVal = this.subString(startIndex2, endIndex - startIndex2);
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
                final int chars_len = endIndex - (this.bp + 1);
                final char[] chars = this.sub_chars(this.bp + 1, chars_len);
                stringVal = readString(chars, chars_len);
            }
            offset += endIndex - (this.bp + 1) + 1;
            chLocal = this.charAt(this.bp + offset++);
            final JSONScanner dateLexer = new JSONScanner(stringVal);
            try {
                if (!dateLexer.scanISO8601DateIfMatch(false)) {
                    this.matchStat = -1;
                    return null;
                }
                final Calendar calendar = dateLexer.getCalendar();
                dateVal = calendar.getTime();
            }
            finally {
                dateLexer.close();
            }
        }
        else if (chLocal == '-' || (chLocal >= '0' && chLocal <= '9')) {
            long millis = 0L;
            boolean negative = false;
            if (chLocal == '-') {
                chLocal = this.charAt(this.bp + offset++);
                negative = true;
            }
            if (chLocal >= '0' && chLocal <= '9') {
                millis = chLocal - '0';
                while (true) {
                    chLocal = this.charAt(this.bp + offset++);
                    if (chLocal < '0' || chLocal > '9') {
                        break;
                    }
                    millis = millis * 10L + (chLocal - '0');
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
        else {
            if (chLocal != 'n' || this.charAt(this.bp + offset) != 'u' || this.charAt(this.bp + offset + 1) != 'l' || this.charAt(this.bp + offset + 2) != 'l') {
                this.matchStat = -1;
                return null;
            }
            this.matchStat = 5;
            dateVal = null;
            offset += 3;
            chLocal = this.charAt(this.bp + offset++);
        }
        if (chLocal == ',') {
            this.bp += offset;
            this.ch = this.charAt(this.bp);
            this.matchStat = 3;
            this.token = 16;
            return dateVal;
        }
        if (chLocal == ']') {
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal == ',') {
                this.token = 16;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == ']') {
                this.token = 15;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == '}') {
                this.token = 13;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else {
                if (chLocal != '\u001a') {
                    this.matchStat = -1;
                    return null;
                }
                this.token = 20;
                this.bp += offset - 1;
                this.ch = '\u001a';
            }
            this.matchStat = 4;
            return dateVal;
        }
        this.matchStat = -1;
        return null;
    }
    
    public UUID scanFieldUUID(final char[] fieldName) {
        this.matchStat = 0;
        if (!this.charArrayCompare(fieldName)) {
            this.matchStat = -2;
            return null;
        }
        int offset = fieldName.length;
        char chLocal = this.charAt(this.bp + offset++);
        UUID uuid;
        if (chLocal == '\"') {
            final int startIndex = this.bp + fieldName.length + 1;
            final int endIndex = this.indexOf('\"', startIndex);
            if (endIndex == -1) {
                throw new JSONException("unclosed str");
            }
            final int startIndex2 = this.bp + fieldName.length + 1;
            final int len = endIndex - startIndex2;
            if (len == 36) {
                long mostSigBits = 0L;
                long leastSigBits = 0L;
                for (int i = 0; i < 8; ++i) {
                    final char ch = this.charAt(startIndex2 + i);
                    int num;
                    if (ch >= '0' && ch <= '9') {
                        num = ch - '0';
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        num = 10 + (ch - 'a');
                    }
                    else {
                        if (ch < 'A' || ch > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch - 'A');
                    }
                    mostSigBits <<= 4;
                    mostSigBits |= num;
                }
                for (int i = 9; i < 13; ++i) {
                    final char ch = this.charAt(startIndex2 + i);
                    int num;
                    if (ch >= '0' && ch <= '9') {
                        num = ch - '0';
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        num = 10 + (ch - 'a');
                    }
                    else {
                        if (ch < 'A' || ch > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch - 'A');
                    }
                    mostSigBits <<= 4;
                    mostSigBits |= num;
                }
                for (int i = 14; i < 18; ++i) {
                    final char ch = this.charAt(startIndex2 + i);
                    int num;
                    if (ch >= '0' && ch <= '9') {
                        num = ch - '0';
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        num = 10 + (ch - 'a');
                    }
                    else {
                        if (ch < 'A' || ch > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch - 'A');
                    }
                    mostSigBits <<= 4;
                    mostSigBits |= num;
                }
                for (int i = 19; i < 23; ++i) {
                    final char ch = this.charAt(startIndex2 + i);
                    int num;
                    if (ch >= '0' && ch <= '9') {
                        num = ch - '0';
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        num = 10 + (ch - 'a');
                    }
                    else {
                        if (ch < 'A' || ch > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch - 'A');
                    }
                    leastSigBits <<= 4;
                    leastSigBits |= num;
                }
                for (int i = 24; i < 36; ++i) {
                    final char ch = this.charAt(startIndex2 + i);
                    int num;
                    if (ch >= '0' && ch <= '9') {
                        num = ch - '0';
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        num = 10 + (ch - 'a');
                    }
                    else {
                        if (ch < 'A' || ch > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch - 'A');
                    }
                    leastSigBits <<= 4;
                    leastSigBits |= num;
                }
                uuid = new UUID(mostSigBits, leastSigBits);
                offset += endIndex - (this.bp + fieldName.length + 1) + 1;
                chLocal = this.charAt(this.bp + offset++);
            }
            else {
                if (len != 32) {
                    this.matchStat = -1;
                    return null;
                }
                long mostSigBits = 0L;
                long leastSigBits = 0L;
                for (int i = 0; i < 16; ++i) {
                    final char ch = this.charAt(startIndex2 + i);
                    int num;
                    if (ch >= '0' && ch <= '9') {
                        num = ch - '0';
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        num = 10 + (ch - 'a');
                    }
                    else {
                        if (ch < 'A' || ch > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch - 'A');
                    }
                    mostSigBits <<= 4;
                    mostSigBits |= num;
                }
                for (int i = 16; i < 32; ++i) {
                    final char ch = this.charAt(startIndex2 + i);
                    int num;
                    if (ch >= '0' && ch <= '9') {
                        num = ch - '0';
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        num = 10 + (ch - 'a');
                    }
                    else {
                        if (ch < 'A' || ch > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch - 'A');
                    }
                    leastSigBits <<= 4;
                    leastSigBits |= num;
                }
                uuid = new UUID(mostSigBits, leastSigBits);
                offset += endIndex - (this.bp + fieldName.length + 1) + 1;
                chLocal = this.charAt(this.bp + offset++);
            }
        }
        else {
            if (chLocal != 'n' || this.charAt(this.bp + offset++) != 'u' || this.charAt(this.bp + offset++) != 'l' || this.charAt(this.bp + offset++) != 'l') {
                this.matchStat = -1;
                return null;
            }
            uuid = null;
            chLocal = this.charAt(this.bp + offset++);
        }
        if (chLocal == ',') {
            this.bp += offset;
            this.ch = this.charAt(this.bp);
            this.matchStat = 3;
            return uuid;
        }
        if (chLocal == '}') {
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal == ',') {
                this.token = 16;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == ']') {
                this.token = 15;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == '}') {
                this.token = 13;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else {
                if (chLocal != '\u001a') {
                    this.matchStat = -1;
                    return null;
                }
                this.token = 20;
                this.bp += offset - 1;
                this.ch = '\u001a';
            }
            this.matchStat = 4;
            return uuid;
        }
        this.matchStat = -1;
        return null;
    }
    
    public UUID scanUUID(final char seperator) {
        this.matchStat = 0;
        int offset = 0;
        char chLocal = this.charAt(this.bp + offset++);
        UUID uuid;
        if (chLocal == '\"') {
            final int startIndex = this.bp + 1;
            final int endIndex = this.indexOf('\"', startIndex);
            if (endIndex == -1) {
                throw new JSONException("unclosed str");
            }
            final int startIndex2 = this.bp + 1;
            final int len = endIndex - startIndex2;
            if (len == 36) {
                long mostSigBits = 0L;
                long leastSigBits = 0L;
                for (int i = 0; i < 8; ++i) {
                    final char ch = this.charAt(startIndex2 + i);
                    int num;
                    if (ch >= '0' && ch <= '9') {
                        num = ch - '0';
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        num = 10 + (ch - 'a');
                    }
                    else {
                        if (ch < 'A' || ch > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch - 'A');
                    }
                    mostSigBits <<= 4;
                    mostSigBits |= num;
                }
                for (int i = 9; i < 13; ++i) {
                    final char ch = this.charAt(startIndex2 + i);
                    int num;
                    if (ch >= '0' && ch <= '9') {
                        num = ch - '0';
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        num = 10 + (ch - 'a');
                    }
                    else {
                        if (ch < 'A' || ch > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch - 'A');
                    }
                    mostSigBits <<= 4;
                    mostSigBits |= num;
                }
                for (int i = 14; i < 18; ++i) {
                    final char ch = this.charAt(startIndex2 + i);
                    int num;
                    if (ch >= '0' && ch <= '9') {
                        num = ch - '0';
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        num = 10 + (ch - 'a');
                    }
                    else {
                        if (ch < 'A' || ch > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch - 'A');
                    }
                    mostSigBits <<= 4;
                    mostSigBits |= num;
                }
                for (int i = 19; i < 23; ++i) {
                    final char ch = this.charAt(startIndex2 + i);
                    int num;
                    if (ch >= '0' && ch <= '9') {
                        num = ch - '0';
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        num = 10 + (ch - 'a');
                    }
                    else {
                        if (ch < 'A' || ch > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch - 'A');
                    }
                    leastSigBits <<= 4;
                    leastSigBits |= num;
                }
                for (int i = 24; i < 36; ++i) {
                    final char ch = this.charAt(startIndex2 + i);
                    int num;
                    if (ch >= '0' && ch <= '9') {
                        num = ch - '0';
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        num = 10 + (ch - 'a');
                    }
                    else {
                        if (ch < 'A' || ch > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch - 'A');
                    }
                    leastSigBits <<= 4;
                    leastSigBits |= num;
                }
                uuid = new UUID(mostSigBits, leastSigBits);
                offset += endIndex - (this.bp + 1) + 1;
                chLocal = this.charAt(this.bp + offset++);
            }
            else {
                if (len != 32) {
                    this.matchStat = -1;
                    return null;
                }
                long mostSigBits = 0L;
                long leastSigBits = 0L;
                for (int i = 0; i < 16; ++i) {
                    final char ch = this.charAt(startIndex2 + i);
                    int num;
                    if (ch >= '0' && ch <= '9') {
                        num = ch - '0';
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        num = 10 + (ch - 'a');
                    }
                    else {
                        if (ch < 'A' || ch > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch - 'A');
                    }
                    mostSigBits <<= 4;
                    mostSigBits |= num;
                }
                for (int i = 16; i < 32; ++i) {
                    final char ch = this.charAt(startIndex2 + i);
                    int num;
                    if (ch >= '0' && ch <= '9') {
                        num = ch - '0';
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        num = 10 + (ch - 'a');
                    }
                    else {
                        if (ch < 'A' || ch > 'F') {
                            this.matchStat = -2;
                            return null;
                        }
                        num = 10 + (ch - 'A');
                    }
                    leastSigBits <<= 4;
                    leastSigBits |= num;
                }
                uuid = new UUID(mostSigBits, leastSigBits);
                offset += endIndex - (this.bp + 1) + 1;
                chLocal = this.charAt(this.bp + offset++);
            }
        }
        else {
            if (chLocal != 'n' || this.charAt(this.bp + offset++) != 'u' || this.charAt(this.bp + offset++) != 'l' || this.charAt(this.bp + offset++) != 'l') {
                this.matchStat = -1;
                return null;
            }
            uuid = null;
            chLocal = this.charAt(this.bp + offset++);
        }
        if (chLocal == ',') {
            this.bp += offset;
            this.ch = this.charAt(this.bp);
            this.matchStat = 3;
            return uuid;
        }
        if (chLocal == ']') {
            chLocal = this.charAt(this.bp + offset++);
            if (chLocal == ',') {
                this.token = 16;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == ']') {
                this.token = 15;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else if (chLocal == '}') {
                this.token = 13;
                this.bp += offset;
                this.ch = this.charAt(this.bp);
            }
            else {
                if (chLocal != '\u001a') {
                    this.matchStat = -1;
                    return null;
                }
                this.token = 20;
                this.bp += offset - 1;
                this.ch = '\u001a';
            }
            this.matchStat = 4;
            return uuid;
        }
        this.matchStat = -1;
        return null;
    }
    
    public final void scanTrue() {
        if (this.ch != 't') {
            throw new JSONException("error parse true");
        }
        this.next();
        if (this.ch != 'r') {
            throw new JSONException("error parse true");
        }
        this.next();
        if (this.ch != 'u') {
            throw new JSONException("error parse true");
        }
        this.next();
        if (this.ch != 'e') {
            throw new JSONException("error parse true");
        }
        this.next();
        if (this.ch == ' ' || this.ch == ',' || this.ch == '}' || this.ch == ']' || this.ch == '\n' || this.ch == '\r' || this.ch == '\t' || this.ch == '\u001a' || this.ch == '\f' || this.ch == '\b' || this.ch == ':' || this.ch == '/') {
            this.token = 6;
            return;
        }
        throw new JSONException("scan true error");
    }
    
    public final void scanNullOrNew() {
        this.scanNullOrNew(true);
    }
    
    public final void scanNullOrNew(final boolean acceptColon) {
        if (this.ch != 'n') {
            throw new JSONException("error parse null or new");
        }
        this.next();
        if (this.ch == 'u') {
            this.next();
            if (this.ch != 'l') {
                throw new JSONException("error parse null");
            }
            this.next();
            if (this.ch != 'l') {
                throw new JSONException("error parse null");
            }
            this.next();
            if (this.ch == ' ' || this.ch == ',' || this.ch == '}' || this.ch == ']' || this.ch == '\n' || this.ch == '\r' || this.ch == '\t' || this.ch == '\u001a' || (this.ch == ':' && acceptColon) || this.ch == '\f' || this.ch == '\b') {
                this.token = 8;
                return;
            }
            throw new JSONException("scan null error");
        }
        else {
            if (this.ch != 'e') {
                throw new JSONException("error parse new");
            }
            this.next();
            if (this.ch != 'w') {
                throw new JSONException("error parse new");
            }
            this.next();
            if (this.ch == ' ' || this.ch == ',' || this.ch == '}' || this.ch == ']' || this.ch == '\n' || this.ch == '\r' || this.ch == '\t' || this.ch == '\u001a' || this.ch == '\f' || this.ch == '\b') {
                this.token = 9;
                return;
            }
            throw new JSONException("scan new error");
        }
    }
    
    public final void scanFalse() {
        if (this.ch != 'f') {
            throw new JSONException("error parse false");
        }
        this.next();
        if (this.ch != 'a') {
            throw new JSONException("error parse false");
        }
        this.next();
        if (this.ch != 'l') {
            throw new JSONException("error parse false");
        }
        this.next();
        if (this.ch != 's') {
            throw new JSONException("error parse false");
        }
        this.next();
        if (this.ch != 'e') {
            throw new JSONException("error parse false");
        }
        this.next();
        if (this.ch == ' ' || this.ch == ',' || this.ch == '}' || this.ch == ']' || this.ch == '\n' || this.ch == '\r' || this.ch == '\t' || this.ch == '\u001a' || this.ch == '\f' || this.ch == '\b' || this.ch == ':' || this.ch == '/') {
            this.token = 7;
            return;
        }
        throw new JSONException("scan false error");
    }
    
    public final void scanIdent() {
        this.np = this.bp - 1;
        this.hasSpecial = false;
        do {
            ++this.sp;
            this.next();
        } while (Character.isLetterOrDigit(this.ch));
        final String ident = this.stringVal();
        if ("null".equalsIgnoreCase(ident)) {
            this.token = 8;
        }
        else if ("new".equals(ident)) {
            this.token = 9;
        }
        else if ("true".equals(ident)) {
            this.token = 6;
        }
        else if ("false".equals(ident)) {
            this.token = 7;
        }
        else if ("undefined".equals(ident)) {
            this.token = 23;
        }
        else if ("Set".equals(ident)) {
            this.token = 21;
        }
        else if ("TreeSet".equals(ident)) {
            this.token = 22;
        }
        else {
            this.token = 18;
        }
    }
    
    @Override
    public abstract String stringVal();
    
    public abstract String subString(final int p0, final int p1);
    
    protected abstract char[] sub_chars(final int p0, final int p1);
    
    public static String readString(final char[] chars, final int chars_len) {
        final char[] sbuf = new char[chars_len];
        int len = 0;
        for (int i = 0; i < chars_len; ++i) {
            char ch = chars[i];
            if (ch != '\\') {
                sbuf[len++] = ch;
            }
            else {
                ch = chars[++i];
                switch (ch) {
                    case '0': {
                        sbuf[len++] = '\0';
                        break;
                    }
                    case '1': {
                        sbuf[len++] = '\u0001';
                        break;
                    }
                    case '2': {
                        sbuf[len++] = '\u0002';
                        break;
                    }
                    case '3': {
                        sbuf[len++] = '\u0003';
                        break;
                    }
                    case '4': {
                        sbuf[len++] = '\u0004';
                        break;
                    }
                    case '5': {
                        sbuf[len++] = '\u0005';
                        break;
                    }
                    case '6': {
                        sbuf[len++] = '\u0006';
                        break;
                    }
                    case '7': {
                        sbuf[len++] = '\u0007';
                        break;
                    }
                    case 'b': {
                        sbuf[len++] = '\b';
                        break;
                    }
                    case 't': {
                        sbuf[len++] = '\t';
                        break;
                    }
                    case 'n': {
                        sbuf[len++] = '\n';
                        break;
                    }
                    case 'v': {
                        sbuf[len++] = '\u000b';
                        break;
                    }
                    case 'F':
                    case 'f': {
                        sbuf[len++] = '\f';
                        break;
                    }
                    case 'r': {
                        sbuf[len++] = '\r';
                        break;
                    }
                    case '\"': {
                        sbuf[len++] = '\"';
                        break;
                    }
                    case '\'': {
                        sbuf[len++] = '\'';
                        break;
                    }
                    case '/': {
                        sbuf[len++] = '/';
                        break;
                    }
                    case '\\': {
                        sbuf[len++] = '\\';
                        break;
                    }
                    case 'x': {
                        sbuf[len++] = (char)(JSONLexerBase.digits[chars[++i]] * 16 + JSONLexerBase.digits[chars[++i]]);
                        break;
                    }
                    case 'u': {
                        sbuf[len++] = (char)Integer.parseInt(new String(new char[] { chars[++i], chars[++i], chars[++i], chars[++i] }), 16);
                        break;
                    }
                    default: {
                        throw new JSONException("unclosed.str.lit");
                    }
                }
            }
        }
        return new String(sbuf, 0, len);
    }
    
    protected abstract boolean charArrayCompare(final char[] p0);
    
    @Override
    public boolean isBlankInput() {
        int i = 0;
        while (true) {
            final char chLocal = this.charAt(i);
            if (chLocal == '\u001a') {
                this.token = 20;
                return true;
            }
            if (!isWhitespace(chLocal)) {
                return false;
            }
            ++i;
        }
    }
    
    @Override
    public final void skipWhitespace() {
        while (this.ch <= '/') {
            if (this.ch == ' ' || this.ch == '\r' || this.ch == '\n' || this.ch == '\t' || this.ch == '\f' || this.ch == '\b') {
                this.next();
            }
            else {
                if (this.ch != '/') {
                    break;
                }
                this.skipComment();
            }
        }
    }
    
    private void scanStringSingleQuote() {
        this.np = this.bp;
        this.hasSpecial = false;
        while (true) {
            char chLocal = this.next();
            if (chLocal == '\'') {
                this.token = 4;
                this.next();
                return;
            }
            if (chLocal == '\u001a') {
                if (this.isEOF()) {
                    throw new JSONException("unclosed single-quote string");
                }
                this.putChar('\u001a');
            }
            else if (chLocal == '\\') {
                if (!this.hasSpecial) {
                    this.hasSpecial = true;
                    if (this.sp > this.sbuf.length) {
                        final char[] newsbuf = new char[this.sp * 2];
                        System.arraycopy(this.sbuf, 0, newsbuf, 0, this.sbuf.length);
                        this.sbuf = newsbuf;
                    }
                    this.copyTo(this.np + 1, this.sp, this.sbuf);
                }
                chLocal = this.next();
                switch (chLocal) {
                    case '0': {
                        this.putChar('\0');
                        continue;
                    }
                    case '1': {
                        this.putChar('\u0001');
                        continue;
                    }
                    case '2': {
                        this.putChar('\u0002');
                        continue;
                    }
                    case '3': {
                        this.putChar('\u0003');
                        continue;
                    }
                    case '4': {
                        this.putChar('\u0004');
                        continue;
                    }
                    case '5': {
                        this.putChar('\u0005');
                        continue;
                    }
                    case '6': {
                        this.putChar('\u0006');
                        continue;
                    }
                    case '7': {
                        this.putChar('\u0007');
                        continue;
                    }
                    case 'b': {
                        this.putChar('\b');
                        continue;
                    }
                    case 't': {
                        this.putChar('\t');
                        continue;
                    }
                    case 'n': {
                        this.putChar('\n');
                        continue;
                    }
                    case 'v': {
                        this.putChar('\u000b');
                        continue;
                    }
                    case 'F':
                    case 'f': {
                        this.putChar('\f');
                        continue;
                    }
                    case 'r': {
                        this.putChar('\r');
                        continue;
                    }
                    case '\"': {
                        this.putChar('\"');
                        continue;
                    }
                    case '\'': {
                        this.putChar('\'');
                        continue;
                    }
                    case '/': {
                        this.putChar('/');
                        continue;
                    }
                    case '\\': {
                        this.putChar('\\');
                        continue;
                    }
                    case 'x': {
                        final char x1 = this.next();
                        final char x2 = this.next();
                        final boolean hex1 = (x1 >= '0' && x1 <= '9') || (x1 >= 'a' && x1 <= 'f') || (x1 >= 'A' && x1 <= 'F');
                        final boolean hex2 = (x2 >= '0' && x2 <= '9') || (x2 >= 'a' && x2 <= 'f') || (x2 >= 'A' && x2 <= 'F');
                        if (!hex1 || !hex2) {
                            throw new JSONException("invalid escape character \\x" + x1 + x2);
                        }
                        this.putChar((char)(JSONLexerBase.digits[x1] * 16 + JSONLexerBase.digits[x2]));
                        continue;
                    }
                    case 'u': {
                        this.putChar((char)Integer.parseInt(new String(new char[] { this.next(), this.next(), this.next(), this.next() }), 16));
                        continue;
                    }
                    default: {
                        this.ch = chLocal;
                        throw new JSONException("unclosed single-quote string");
                    }
                }
            }
            else if (!this.hasSpecial) {
                ++this.sp;
            }
            else if (this.sp == this.sbuf.length) {
                this.putChar(chLocal);
            }
            else {
                this.sbuf[this.sp++] = chLocal;
            }
        }
    }
    
    protected final void putChar(final char ch) {
        if (this.sp == this.sbuf.length) {
            final char[] newsbuf = new char[this.sbuf.length * 2];
            System.arraycopy(this.sbuf, 0, newsbuf, 0, this.sbuf.length);
            this.sbuf = newsbuf;
        }
        this.sbuf[this.sp++] = ch;
    }
    
    public final void scanHex() {
        if (this.ch != 'x') {
            throw new JSONException("illegal state. " + this.ch);
        }
        this.next();
        if (this.ch != '\'') {
            throw new JSONException("illegal state. " + this.ch);
        }
        this.np = this.bp;
        this.next();
        if (this.ch == '\'') {
            this.next();
            this.token = 26;
            return;
        }
        int i = 0;
        char ch;
        while (true) {
            ch = this.next();
            if ((ch < '0' || ch > '9') && (ch < 'A' || ch > 'F')) {
                break;
            }
            ++this.sp;
            ++i;
        }
        if (ch == '\'') {
            ++this.sp;
            this.next();
            this.token = 26;
            return;
        }
        throw new JSONException("illegal state. " + ch);
    }
    
    @Override
    public final void scanNumber() {
        this.np = this.bp;
        if (this.ch == '-') {
            ++this.sp;
            this.next();
        }
        while (this.ch >= '0' && this.ch <= '9') {
            ++this.sp;
            this.next();
        }
        boolean isDouble = false;
        if (this.ch == '.') {
            ++this.sp;
            this.next();
            isDouble = true;
            while (this.ch >= '0' && this.ch <= '9') {
                ++this.sp;
                this.next();
            }
        }
        if (this.ch == 'L') {
            ++this.sp;
            this.next();
        }
        else if (this.ch == 'S') {
            ++this.sp;
            this.next();
        }
        else if (this.ch == 'B') {
            ++this.sp;
            this.next();
        }
        else if (this.ch == 'F') {
            ++this.sp;
            this.next();
            isDouble = true;
        }
        else if (this.ch == 'D') {
            ++this.sp;
            this.next();
            isDouble = true;
        }
        else if (this.ch == 'e' || this.ch == 'E') {
            ++this.sp;
            this.next();
            if (this.ch == '+' || this.ch == '-') {
                ++this.sp;
                this.next();
            }
            while (this.ch >= '0' && this.ch <= '9') {
                ++this.sp;
                this.next();
            }
            if (this.ch == 'D' || this.ch == 'F') {
                ++this.sp;
                this.next();
            }
            isDouble = true;
        }
        if (isDouble) {
            this.token = 3;
        }
        else {
            this.token = 2;
        }
    }
    
    @Override
    public final long longValue() throws NumberFormatException {
        long result = 0L;
        boolean negative = false;
        if (this.np == -1) {
            this.np = 0;
        }
        int i = this.np;
        final int max = this.np + this.sp;
        long limit;
        if (this.charAt(this.np) == '-') {
            negative = true;
            limit = Long.MIN_VALUE;
            ++i;
        }
        else {
            limit = -9223372036854775807L;
        }
        final long multmin = -922337203685477580L;
        if (i < max) {
            final int digit = this.charAt(i++) - '0';
            result = -digit;
        }
        while (i < max) {
            final char chLocal = this.charAt(i++);
            if (chLocal == 'L' || chLocal == 'S') {
                break;
            }
            if (chLocal == 'B') {
                break;
            }
            final int digit = chLocal - '0';
            if (result < multmin) {
                throw new NumberFormatException(this.numberString());
            }
            result *= 10L;
            if (result < limit + digit) {
                throw new NumberFormatException(this.numberString());
            }
            result -= digit;
        }
        if (!negative) {
            return -result;
        }
        if (i > this.np + 1) {
            return result;
        }
        throw new NumberFormatException(this.numberString());
    }
    
    @Override
    public final Number decimalValue(final boolean decimal) {
        final char chLocal = this.charAt(this.np + this.sp - 1);
        try {
            if (chLocal == 'F') {
                return Float.parseFloat(this.numberString());
            }
            if (chLocal == 'D') {
                return Double.parseDouble(this.numberString());
            }
            if (decimal) {
                return this.decimalValue();
            }
            return this.doubleValue();
        }
        catch (NumberFormatException ex) {
            throw new JSONException(ex.getMessage() + ", " + this.info());
        }
    }
    
    @Override
    public abstract BigDecimal decimalValue();
    
    public static boolean isWhitespace(final char ch) {
        return ch <= ' ' && (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '\f' || ch == '\b');
    }
    
    public String[] scanFieldStringArray(final char[] fieldName, final int argTypesCount, final SymbolTable typeSymbolTable) {
        throw new UnsupportedOperationException();
    }
    
    public boolean matchField2(final char[] fieldName) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int getFeatures() {
        return this.features;
    }
    
    static {
        SBUF_LOCAL = new ThreadLocal<char[]>();
        typeFieldName = ("\"" + JSON.DEFAULT_TYPE_KEY + "\":\"").toCharArray();
        digits = new int[103];
        for (int i = 48; i <= 57; ++i) {
            JSONLexerBase.digits[i] = i - 48;
        }
        for (int i = 97; i <= 102; ++i) {
            JSONLexerBase.digits[i] = i - 97 + 10;
        }
        for (int i = 65; i <= 70; ++i) {
            JSONLexerBase.digits[i] = i - 65 + 10;
        }
    }
}
