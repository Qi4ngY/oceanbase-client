package org.apache.commons.cli;

import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

public class PatternOptionBuilder
{
    public static final Class STRING_VALUE;
    public static final Class OBJECT_VALUE;
    public static final Class NUMBER_VALUE;
    public static final Class DATE_VALUE;
    public static final Class CLASS_VALUE;
    public static final Class EXISTING_FILE_VALUE;
    public static final Class FILE_VALUE;
    public static final Class FILES_VALUE;
    public static final Class URL_VALUE;
    
    public static Object getValueClass(final char ch) {
        switch (ch) {
            case '@': {
                return PatternOptionBuilder.OBJECT_VALUE;
            }
            case ':': {
                return PatternOptionBuilder.STRING_VALUE;
            }
            case '%': {
                return PatternOptionBuilder.NUMBER_VALUE;
            }
            case '+': {
                return PatternOptionBuilder.CLASS_VALUE;
            }
            case '#': {
                return PatternOptionBuilder.DATE_VALUE;
            }
            case '<': {
                return PatternOptionBuilder.EXISTING_FILE_VALUE;
            }
            case '>': {
                return PatternOptionBuilder.FILE_VALUE;
            }
            case '*': {
                return PatternOptionBuilder.FILES_VALUE;
            }
            case '/': {
                return PatternOptionBuilder.URL_VALUE;
            }
            default: {
                return null;
            }
        }
    }
    
    public static boolean isValueCode(final char ch) {
        return ch == '@' || ch == ':' || ch == '%' || ch == '+' || ch == '#' || ch == '<' || ch == '>' || ch == '*' || ch == '/' || ch == '!';
    }
    
    public static Options parsePattern(final String pattern) {
        char opt = ' ';
        boolean required = false;
        Object type = null;
        final Options options = new Options();
        for (int i = 0; i < pattern.length(); ++i) {
            final char ch = pattern.charAt(i);
            if (!isValueCode(ch)) {
                if (opt != ' ') {
                    OptionBuilder.hasArg(type != null);
                    OptionBuilder.isRequired(required);
                    OptionBuilder.withType(type);
                    options.addOption(OptionBuilder.create(opt));
                    required = false;
                    type = null;
                    opt = ' ';
                }
                opt = ch;
            }
            else if (ch == '!') {
                required = true;
            }
            else {
                type = getValueClass(ch);
            }
        }
        if (opt != ' ') {
            OptionBuilder.hasArg(type != null);
            OptionBuilder.isRequired(required);
            OptionBuilder.withType(type);
            options.addOption(OptionBuilder.create(opt));
        }
        return options;
    }
    
    static {
        STRING_VALUE = String.class;
        OBJECT_VALUE = Object.class;
        NUMBER_VALUE = Number.class;
        DATE_VALUE = Date.class;
        CLASS_VALUE = Class.class;
        EXISTING_FILE_VALUE = FileInputStream.class;
        FILE_VALUE = File.class;
        FILES_VALUE = File[].class;
        URL_VALUE = URL.class;
    }
}
