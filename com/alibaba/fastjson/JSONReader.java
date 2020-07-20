package com.alibaba.fastjson;

import java.util.Map;
import java.lang.reflect.Type;
import com.alibaba.fastjson.util.TypeUtils;
import java.util.Locale;
import java.util.TimeZone;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONReaderScanner;
import com.alibaba.fastjson.parser.Feature;
import java.io.Reader;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.io.Closeable;

public class JSONReader implements Closeable
{
    private final DefaultJSONParser parser;
    private JSONStreamContext context;
    
    public JSONReader(final Reader reader) {
        this(reader, new Feature[0]);
    }
    
    public JSONReader(final Reader reader, final Feature... features) {
        this(new JSONReaderScanner(reader));
        for (final Feature feature : features) {
            this.config(feature, true);
        }
    }
    
    public JSONReader(final JSONLexer lexer) {
        this(new DefaultJSONParser(lexer));
    }
    
    public JSONReader(final DefaultJSONParser parser) {
        this.parser = parser;
    }
    
    public void setTimzeZone(final TimeZone timezone) {
        this.parser.lexer.setTimeZone(timezone);
    }
    
    public void setLocale(final Locale locale) {
        this.parser.lexer.setLocale(locale);
    }
    
    public void config(final Feature feature, final boolean state) {
        this.parser.config(feature, state);
    }
    
    public Locale getLocal() {
        return this.parser.lexer.getLocale();
    }
    
    public TimeZone getTimzeZone() {
        return this.parser.lexer.getTimeZone();
    }
    
    public void startObject() {
        if (this.context == null) {
            this.context = new JSONStreamContext(null, 1001);
        }
        else {
            this.startStructure();
            this.context = new JSONStreamContext(this.context, 1001);
        }
        this.parser.accept(12, 18);
    }
    
    public void endObject() {
        this.parser.accept(13);
        this.endStructure();
    }
    
    public void startArray() {
        if (this.context == null) {
            this.context = new JSONStreamContext(null, 1004);
        }
        else {
            this.startStructure();
            this.context = new JSONStreamContext(this.context, 1004);
        }
        this.parser.accept(14);
    }
    
    public void endArray() {
        this.parser.accept(15);
        this.endStructure();
    }
    
    private void startStructure() {
        final int state = this.context.state;
        switch (state) {
            case 1002: {
                this.parser.accept(17);
                break;
            }
            case 1003:
            case 1005: {
                this.parser.accept(16);
                break;
            }
            case 1001:
            case 1004: {
                break;
            }
            default: {
                throw new JSONException("illegal state : " + this.context.state);
            }
        }
    }
    
    private void endStructure() {
        this.context = this.context.parent;
        if (this.context == null) {
            return;
        }
        final int state = this.context.state;
        int newState = -1;
        switch (state) {
            case 1002: {
                newState = 1003;
                break;
            }
            case 1004: {
                newState = 1005;
                break;
            }
            case 1001:
            case 1003: {
                newState = 1002;
                break;
            }
        }
        if (newState != -1) {
            this.context.state = newState;
        }
    }
    
    public boolean hasNext() {
        if (this.context == null) {
            throw new JSONException("context is null");
        }
        final int token = this.parser.lexer.token();
        final int state = this.context.state;
        switch (state) {
            case 1004:
            case 1005: {
                return token != 15;
            }
            case 1001:
            case 1003: {
                return token != 13;
            }
            default: {
                throw new JSONException("illegal state : " + state);
            }
        }
    }
    
    public int peek() {
        return this.parser.lexer.token();
    }
    
    @Override
    public void close() {
        this.parser.close();
    }
    
    public Integer readInteger() {
        Object object;
        if (this.context == null) {
            object = this.parser.parse();
        }
        else {
            this.readBefore();
            object = this.parser.parse();
            this.readAfter();
        }
        return TypeUtils.castToInt(object);
    }
    
    public Long readLong() {
        Object object;
        if (this.context == null) {
            object = this.parser.parse();
        }
        else {
            this.readBefore();
            object = this.parser.parse();
            this.readAfter();
        }
        return TypeUtils.castToLong(object);
    }
    
    public String readString() {
        Object object;
        if (this.context == null) {
            object = this.parser.parse();
        }
        else {
            this.readBefore();
            final JSONLexer lexer = this.parser.lexer;
            if (this.context.state == 1001 && lexer.token() == 18) {
                object = lexer.stringVal();
                lexer.nextToken();
            }
            else {
                object = this.parser.parse();
            }
            this.readAfter();
        }
        return TypeUtils.castToString(object);
    }
    
    public <T> T readObject(final TypeReference<T> typeRef) {
        return this.readObject(typeRef.getType());
    }
    
    public <T> T readObject(final Type type) {
        if (this.context == null) {
            return this.parser.parseObject(type);
        }
        this.readBefore();
        final T object = this.parser.parseObject(type);
        this.readAfter();
        return object;
    }
    
    public <T> T readObject(final Class<T> type) {
        if (this.context == null) {
            return this.parser.parseObject(type);
        }
        this.readBefore();
        final T object = this.parser.parseObject(type);
        this.readAfter();
        return object;
    }
    
    public void readObject(final Object object) {
        if (this.context == null) {
            this.parser.parseObject(object);
            return;
        }
        this.readBefore();
        this.parser.parseObject(object);
        this.readAfter();
    }
    
    public Object readObject() {
        if (this.context == null) {
            return this.parser.parse();
        }
        this.readBefore();
        Object object = null;
        switch (this.context.state) {
            case 1001:
            case 1003: {
                object = this.parser.parseKey();
                break;
            }
            default: {
                object = this.parser.parse();
                break;
            }
        }
        this.readAfter();
        return object;
    }
    
    public Object readObject(final Map object) {
        if (this.context == null) {
            return this.parser.parseObject(object);
        }
        this.readBefore();
        final Object value = this.parser.parseObject(object);
        this.readAfter();
        return value;
    }
    
    private void readBefore() {
        final int state = this.context.state;
        switch (state) {
            case 1002: {
                this.parser.accept(17);
                break;
            }
            case 1003: {
                this.parser.accept(16, 18);
                break;
            }
            case 1005: {
                this.parser.accept(16);
                break;
            }
            case 1001: {
                break;
            }
            case 1004: {
                break;
            }
            default: {
                throw new JSONException("illegal state : " + state);
            }
        }
    }
    
    private void readAfter() {
        final int state = this.context.state;
        int newStat = -1;
        switch (state) {
            case 1001: {
                newStat = 1002;
                break;
            }
            case 1002: {
                newStat = 1003;
                break;
            }
            case 1003: {
                newStat = 1002;
                break;
            }
            case 1005: {
                break;
            }
            case 1004: {
                newStat = 1005;
                break;
            }
            default: {
                throw new JSONException("illegal state : " + state);
            }
        }
        if (newStat != -1) {
            this.context.state = newStat;
        }
    }
}
