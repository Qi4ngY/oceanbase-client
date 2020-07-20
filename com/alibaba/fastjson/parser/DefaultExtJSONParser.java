package com.alibaba.fastjson.parser;

@Deprecated
public class DefaultExtJSONParser extends DefaultJSONParser
{
    public DefaultExtJSONParser(final String input) {
        this(input, ParserConfig.getGlobalInstance());
    }
    
    public DefaultExtJSONParser(final String input, final ParserConfig mapping) {
        super(input, mapping);
    }
    
    public DefaultExtJSONParser(final String input, final ParserConfig mapping, final int features) {
        super(input, mapping, features);
    }
    
    public DefaultExtJSONParser(final char[] input, final int length, final ParserConfig mapping, final int features) {
        super(input, length, mapping, features);
    }
}
