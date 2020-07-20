package com.alipay.oceanbase.jdbc.util;

import com.alipay.oceanbase.jdbc.SQLError;

public class ErrorMappingsDocGenerator
{
    public static void main(final String[] args) throws Exception {
        SQLError.dumpSqlStatesMappingsAsXml();
    }
}
