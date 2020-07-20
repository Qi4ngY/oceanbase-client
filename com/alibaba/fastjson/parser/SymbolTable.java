package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSON;

public class SymbolTable
{
    private final String[] symbols;
    private final int indexMask;
    
    public SymbolTable(final int tableSize) {
        this.indexMask = tableSize - 1;
        this.symbols = new String[tableSize];
        this.addSymbol("$ref", 0, 4, "$ref".hashCode());
        this.addSymbol(JSON.DEFAULT_TYPE_KEY, 0, JSON.DEFAULT_TYPE_KEY.length(), JSON.DEFAULT_TYPE_KEY.hashCode());
    }
    
    public String addSymbol(final char[] buffer, final int offset, final int len) {
        final int hash = hash(buffer, offset, len);
        return this.addSymbol(buffer, offset, len, hash);
    }
    
    public String addSymbol(final char[] buffer, final int offset, final int len, final int hash) {
        final int bucket = hash & this.indexMask;
        String symbol = this.symbols[bucket];
        if (symbol == null) {
            symbol = new String(buffer, offset, len).intern();
            return this.symbols[bucket] = symbol;
        }
        boolean eq = true;
        if (hash == symbol.hashCode() && len == symbol.length()) {
            for (int i = 0; i < len; ++i) {
                if (buffer[offset + i] != symbol.charAt(i)) {
                    eq = false;
                    break;
                }
            }
        }
        else {
            eq = false;
        }
        if (eq) {
            return symbol;
        }
        return new String(buffer, offset, len);
    }
    
    public String addSymbol(final String buffer, final int offset, final int len, final int hash) {
        return this.addSymbol(buffer, offset, len, hash, false);
    }
    
    public String addSymbol(final String buffer, final int offset, final int len, final int hash, final boolean replace) {
        final int bucket = hash & this.indexMask;
        String symbol = this.symbols[bucket];
        if (symbol == null) {
            symbol = ((len == buffer.length()) ? buffer : subString(buffer, offset, len));
            symbol = symbol.intern();
            return this.symbols[bucket] = symbol;
        }
        if (hash == symbol.hashCode() && len == symbol.length() && buffer.startsWith(symbol, offset)) {
            return symbol;
        }
        final String str = subString(buffer, offset, len);
        if (replace) {
            this.symbols[bucket] = str;
        }
        return str;
    }
    
    private static String subString(final String src, final int offset, final int len) {
        final char[] chars = new char[len];
        src.getChars(offset, offset + len, chars, 0);
        return new String(chars);
    }
    
    public static int hash(final char[] buffer, final int offset, final int len) {
        int h = 0;
        int off = offset;
        for (int i = 0; i < len; ++i) {
            h = 31 * h + buffer[off++];
        }
        return h;
    }
}
