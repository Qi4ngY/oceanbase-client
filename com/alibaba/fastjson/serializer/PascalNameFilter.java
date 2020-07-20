package com.alibaba.fastjson.serializer;

public class PascalNameFilter implements NameFilter
{
    @Override
    public String process(final Object source, final String name, final Object value) {
        if (name == null || name.length() == 0) {
            return name;
        }
        final char[] chars = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        final String pascalName = new String(chars);
        return pascalName;
    }
}
