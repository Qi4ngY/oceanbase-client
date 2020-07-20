package com.alibaba.fastjson.serializer;

public enum SerializerFeature
{
    QuoteFieldNames, 
    UseSingleQuotes, 
    WriteMapNullValue, 
    WriteEnumUsingToString, 
    WriteEnumUsingName, 
    UseISO8601DateFormat, 
    WriteNullListAsEmpty, 
    WriteNullStringAsEmpty, 
    WriteNullNumberAsZero, 
    WriteNullBooleanAsFalse, 
    SkipTransientField, 
    SortField, 
    @Deprecated
    WriteTabAsSpecial, 
    PrettyFormat, 
    WriteClassName, 
    DisableCircularReferenceDetect, 
    WriteSlashAsSpecial, 
    BrowserCompatible, 
    WriteDateUseDateFormat, 
    NotWriteRootClassName, 
    @Deprecated
    DisableCheckSpecialChar, 
    BeanToArray, 
    WriteNonStringKeyAsString, 
    NotWriteDefaultValue, 
    BrowserSecure, 
    IgnoreNonFieldGetter, 
    WriteNonStringValueAsString, 
    IgnoreErrorGetter, 
    WriteBigDecimalAsPlain, 
    MapSortField;
    
    public final int mask;
    public static final SerializerFeature[] EMPTY;
    public static final int WRITE_MAP_NULL_FEATURES;
    
    private SerializerFeature() {
        this.mask = 1 << this.ordinal();
    }
    
    public final int getMask() {
        return this.mask;
    }
    
    public static boolean isEnabled(final int features, final SerializerFeature feature) {
        return (features & feature.mask) != 0x0;
    }
    
    public static boolean isEnabled(final int features, final int fieaturesB, final SerializerFeature feature) {
        final int mask = feature.mask;
        return (features & mask) != 0x0 || (fieaturesB & mask) != 0x0;
    }
    
    public static int config(int features, final SerializerFeature feature, final boolean state) {
        if (state) {
            features |= feature.mask;
        }
        else {
            features &= ~feature.mask;
        }
        return features;
    }
    
    public static int of(final SerializerFeature[] features) {
        if (features == null) {
            return 0;
        }
        int value = 0;
        for (final SerializerFeature feature : features) {
            value |= feature.mask;
        }
        return value;
    }
    
    static {
        EMPTY = new SerializerFeature[0];
        WRITE_MAP_NULL_FEATURES = (SerializerFeature.WriteMapNullValue.getMask() | SerializerFeature.WriteNullBooleanAsFalse.getMask() | SerializerFeature.WriteNullListAsEmpty.getMask() | SerializerFeature.WriteNullNumberAsZero.getMask() | SerializerFeature.WriteNullStringAsEmpty.getMask());
    }
}
