package com.alibaba.fastjson.parser;

public enum Feature
{
    AutoCloseSource, 
    AllowComment, 
    AllowUnQuotedFieldNames, 
    AllowSingleQuotes, 
    InternFieldNames, 
    AllowISO8601DateFormat, 
    AllowArbitraryCommas, 
    UseBigDecimal, 
    IgnoreNotMatch, 
    SortFeidFastMatch, 
    DisableASM, 
    DisableCircularReferenceDetect, 
    InitStringFieldAsEmpty, 
    SupportArrayToBean, 
    OrderedField, 
    DisableSpecialKeyDetect, 
    UseObjectArray, 
    SupportNonPublicField, 
    IgnoreAutoType, 
    DisableFieldSmartMatch, 
    SupportAutoType, 
    NonStringKeyAsString, 
    CustomMapDeserializer, 
    ErrorOnEnumNotMatch, 
    SafeMode;
    
    public final int mask;
    
    private Feature() {
        this.mask = 1 << this.ordinal();
    }
    
    public final int getMask() {
        return this.mask;
    }
    
    public static boolean isEnabled(final int features, final Feature feature) {
        return (features & feature.mask) != 0x0;
    }
    
    public static int config(int features, final Feature feature, final boolean state) {
        if (state) {
            features |= feature.mask;
        }
        else {
            features &= ~feature.mask;
        }
        return features;
    }
    
    public static int of(final Feature[] features) {
        if (features == null) {
            return 0;
        }
        int value = 0;
        for (final Feature feature : features) {
            value |= feature.mask;
        }
        return value;
    }
}
