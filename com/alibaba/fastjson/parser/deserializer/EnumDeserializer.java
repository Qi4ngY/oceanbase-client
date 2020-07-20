package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.JSONException;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.util.Iterator;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Arrays;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.annotation.JSONField;
import java.util.HashMap;

public class EnumDeserializer implements ObjectDeserializer
{
    protected final Class<?> enumClass;
    protected final Enum[] enums;
    protected final Enum[] ordinalEnums;
    protected long[] enumNameHashCodes;
    
    public EnumDeserializer(final Class<?> enumClass) {
        this.enumClass = enumClass;
        this.ordinalEnums = (Enum[])enumClass.getEnumConstants();
        final Map<Long, Enum> enumMap = new HashMap<Long, Enum>();
        for (int i = 0; i < this.ordinalEnums.length; ++i) {
            final Enum e = this.ordinalEnums[i];
            String name = e.name();
            JSONField jsonField = null;
            try {
                final Field field = enumClass.getField(name);
                jsonField = TypeUtils.getAnnotation(field, JSONField.class);
                if (jsonField != null) {
                    final String jsonFieldName = jsonField.name();
                    if (jsonFieldName != null && jsonFieldName.length() > 0) {
                        name = jsonFieldName;
                    }
                }
            }
            catch (Exception ex) {}
            long hash = -3750763034362895579L;
            long hash_lower = -3750763034362895579L;
            for (int j = 0; j < name.length(); ++j) {
                final char ch = name.charAt(j);
                hash ^= ch;
                hash_lower ^= ((ch >= 'A' && ch <= 'Z') ? (ch + ' ') : ch);
                hash *= 1099511628211L;
                hash_lower *= 1099511628211L;
            }
            enumMap.put(hash, e);
            if (hash != hash_lower) {
                enumMap.put(hash_lower, e);
            }
            if (jsonField != null) {
                for (final String alterName : jsonField.alternateNames()) {
                    long alterNameHash = -3750763034362895579L;
                    for (int k = 0; k < alterName.length(); ++k) {
                        final char ch2 = alterName.charAt(k);
                        alterNameHash ^= ch2;
                        alterNameHash *= 1099511628211L;
                    }
                    if (alterNameHash != hash && alterNameHash != hash_lower) {
                        enumMap.put(alterNameHash, e);
                    }
                }
            }
        }
        this.enumNameHashCodes = new long[enumMap.size()];
        int i = 0;
        for (final Long h : enumMap.keySet()) {
            this.enumNameHashCodes[i++] = h;
        }
        Arrays.sort(this.enumNameHashCodes);
        this.enums = new Enum[this.enumNameHashCodes.length];
        for (i = 0; i < this.enumNameHashCodes.length; ++i) {
            final long hash2 = this.enumNameHashCodes[i];
            final Enum e2 = enumMap.get(hash2);
            this.enums[i] = e2;
        }
    }
    
    public Enum getEnumByHashCode(final long hashCode) {
        if (this.enums == null) {
            return null;
        }
        final int enumIndex = Arrays.binarySearch(this.enumNameHashCodes, hashCode);
        if (enumIndex < 0) {
            return null;
        }
        return this.enums[enumIndex];
    }
    
    public Enum<?> valueOf(final int ordinal) {
        return (Enum<?>)this.ordinalEnums[ordinal];
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        try {
            final JSONLexer lexer = parser.lexer;
            final int token = lexer.token();
            if (token == 2) {
                final int intValue = lexer.intValue();
                lexer.nextToken(16);
                if (intValue < 0 || intValue > this.ordinalEnums.length) {
                    throw new JSONException("parse enum " + this.enumClass.getName() + " error, value : " + intValue);
                }
                return (T)this.ordinalEnums[intValue];
            }
            else if (token == 4) {
                final String name = lexer.stringVal();
                lexer.nextToken(16);
                if (name.length() == 0) {
                    return null;
                }
                long hash = -3750763034362895579L;
                long hash_lower = -3750763034362895579L;
                for (int j = 0; j < name.length(); ++j) {
                    final char ch = name.charAt(j);
                    hash ^= ch;
                    hash_lower ^= ((ch >= 'A' && ch <= 'Z') ? (ch + ' ') : ch);
                    hash *= 1099511628211L;
                    hash_lower *= 1099511628211L;
                }
                Enum e = this.getEnumByHashCode(hash);
                if (e == null && hash_lower != hash) {
                    e = this.getEnumByHashCode(hash_lower);
                }
                if (e == null && lexer.isEnabled(Feature.ErrorOnEnumNotMatch)) {
                    throw new JSONException("not match enum value, " + this.enumClass.getName() + " : " + name);
                }
                return (T)e;
            }
            else {
                if (token == 8) {
                    final Object value = null;
                    lexer.nextToken(16);
                    return null;
                }
                final Object value = parser.parse();
                throw new JSONException("parse enum " + this.enumClass.getName() + " error, value : " + value);
            }
        }
        catch (JSONException e2) {
            throw e2;
        }
        catch (Exception e3) {
            throw new JSONException(e3.getMessage(), e3);
        }
    }
    
    @Override
    public int getFastMatchToken() {
        return 2;
    }
}
