package org.apache.commons.cli;

public class UnrecognizedOptionException extends ParseException
{
    private String option;
    
    public UnrecognizedOptionException(final String message) {
        super(message);
    }
    
    public UnrecognizedOptionException(final String message, final String option) {
        this(message);
        this.option = option;
    }
    
    public String getOption() {
        return this.option;
    }
}
