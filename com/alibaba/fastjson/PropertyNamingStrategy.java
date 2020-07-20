package com.alibaba.fastjson;

public enum PropertyNamingStrategy
{
    CamelCase, 
    PascalCase, 
    SnakeCase, 
    KebabCase, 
    NoChange;
    
    public String translate(final String propertyName) {
        switch (this) {
            case SnakeCase: {
                final StringBuilder buf = new StringBuilder();
                for (int i = 0; i < propertyName.length(); ++i) {
                    final char ch = propertyName.charAt(i);
                    if (ch >= 'A' && ch <= 'Z') {
                        final char ch_ucase = (char)(ch + ' ');
                        if (i > 0) {
                            buf.append('_');
                        }
                        buf.append(ch_ucase);
                    }
                    else {
                        buf.append(ch);
                    }
                }
                return buf.toString();
            }
            case KebabCase: {
                final StringBuilder buf = new StringBuilder();
                for (int i = 0; i < propertyName.length(); ++i) {
                    final char ch = propertyName.charAt(i);
                    if (ch >= 'A' && ch <= 'Z') {
                        final char ch_ucase = (char)(ch + ' ');
                        if (i > 0) {
                            buf.append('-');
                        }
                        buf.append(ch_ucase);
                    }
                    else {
                        buf.append(ch);
                    }
                }
                return buf.toString();
            }
            case PascalCase: {
                final char ch2 = propertyName.charAt(0);
                if (ch2 >= 'a' && ch2 <= 'z') {
                    final char[] charArray;
                    final char[] chars = charArray = propertyName.toCharArray();
                    final int n = 0;
                    charArray[n] -= ' ';
                    return new String(chars);
                }
                return propertyName;
            }
            case CamelCase: {
                final char ch2 = propertyName.charAt(0);
                if (ch2 >= 'A' && ch2 <= 'Z') {
                    final char[] charArray2;
                    final char[] chars = charArray2 = propertyName.toCharArray();
                    final int n2 = 0;
                    charArray2[n2] += ' ';
                    return new String(chars);
                }
                return propertyName;
            }
            default: {
                return propertyName;
            }
        }
    }
}
