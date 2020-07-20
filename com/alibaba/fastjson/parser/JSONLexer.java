package com.alibaba.fastjson.parser;

import java.util.Locale;
import java.util.TimeZone;
import java.util.Collection;
import java.math.BigDecimal;

public interface JSONLexer
{
    public static final char EOI = '\u001a';
    public static final int NOT_MATCH = -1;
    public static final int NOT_MATCH_NAME = -2;
    public static final int UNKNOWN = 0;
    public static final int OBJECT = 1;
    public static final int ARRAY = 2;
    public static final int VALUE = 3;
    public static final int END = 4;
    public static final int VALUE_NULL = 5;
    
    int token();
    
    String tokenName();
    
    void skipWhitespace();
    
    void nextToken();
    
    void nextToken(final int p0);
    
    char getCurrent();
    
    char next();
    
    String scanSymbol(final SymbolTable p0);
    
    String scanSymbol(final SymbolTable p0, final char p1);
    
    void resetStringPosition();
    
    void scanNumber();
    
    int pos();
    
    Number integerValue();
    
    BigDecimal decimalValue();
    
    Number decimalValue(final boolean p0);
    
    String scanSymbolUnQuoted(final SymbolTable p0);
    
    String stringVal();
    
    boolean isEnabled(final int p0);
    
    boolean isEnabled(final Feature p0);
    
    void config(final Feature p0, final boolean p1);
    
    void scanString();
    
    int intValue();
    
    void nextTokenWithColon();
    
    void nextTokenWithColon(final int p0);
    
    boolean isBlankInput();
    
    void close();
    
    long longValue();
    
    boolean isRef();
    
    String scanTypeName(final SymbolTable p0);
    
    String numberString();
    
    byte[] bytesValue();
    
    float floatValue();
    
    int scanInt(final char p0);
    
    long scanLong(final char p0);
    
    float scanFloat(final char p0);
    
    double scanDouble(final char p0);
    
    boolean scanBoolean(final char p0);
    
    BigDecimal scanDecimal(final char p0);
    
    String scanString(final char p0);
    
    Enum<?> scanEnum(final Class<?> p0, final SymbolTable p1, final char p2);
    
    String scanSymbolWithSeperator(final SymbolTable p0, final char p1);
    
    void scanStringArray(final Collection<String> p0, final char p1);
    
    TimeZone getTimeZone();
    
    void setTimeZone(final TimeZone p0);
    
    Locale getLocale();
    
    void setLocale(final Locale p0);
    
    String info();
    
    int getFeatures();
}
