package org.apache.commons.cli;

public class BasicParser extends Parser
{
    protected String[] flatten(final Options options, final String[] arguments, final boolean stopAtNonOption) {
        return arguments;
    }
}
