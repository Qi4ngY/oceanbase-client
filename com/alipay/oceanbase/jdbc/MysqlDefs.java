package com.alipay.oceanbase.jdbc;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

public final class MysqlDefs
{
    static final int COM_BINLOG_DUMP = 18;
    static final int COM_CHANGE_USER = 17;
    static final int COM_CLOSE_STATEMENT = 25;
    static final int COM_CONNECT_OUT = 20;
    static final int COM_END = 29;
    static final int COM_EXECUTE = 23;
    static final int COM_FETCH = 28;
    static final int COM_LONG_DATA = 24;
    static final int COM_PREPARE = 22;
    static final int COM_REGISTER_SLAVE = 21;
    static final int COM_RESET_STMT = 26;
    static final int COM_SET_OPTION = 27;
    static final int COM_TABLE_DUMP = 19;
    static final int CONNECT = 11;
    static final int CREATE_DB = 5;
    static final int DEBUG = 13;
    static final int DELAYED_INSERT = 16;
    static final int DROP_DB = 6;
    static final int FIELD_LIST = 4;
    static final int FIELD_TYPE_BIT = 16;
    public static final int FIELD_TYPE_BLOB = 252;
    static final int FIELD_TYPE_DATE = 10;
    static final int FIELD_TYPE_DATETIME = 12;
    static final int FIELD_TYPE_DECIMAL = 0;
    static final int FIELD_TYPE_DOUBLE = 5;
    static final int FIELD_TYPE_ENUM = 247;
    static final int FIELD_TYPE_FLOAT = 4;
    static final int FIELD_TYPE_GEOMETRY = 255;
    static final int FIELD_TYPE_INT24 = 9;
    static final int FIELD_TYPE_LONG = 3;
    static final int FIELD_TYPE_LONG_BLOB = 251;
    static final int FIELD_TYPE_LONGLONG = 8;
    static final int FIELD_TYPE_MEDIUM_BLOB = 250;
    static final int FIELD_TYPE_NEW_DECIMAL = 246;
    static final int FIELD_TYPE_NEWDATE = 14;
    static final int FIELD_TYPE_NULL = 6;
    static final int FIELD_TYPE_SET = 248;
    static final int FIELD_TYPE_SHORT = 2;
    static final int FIELD_TYPE_STRING = 254;
    static final int FIELD_TYPE_TIME = 11;
    static final int FIELD_TYPE_TIMESTAMP = 7;
    static final int FIELD_TYPE_TIMESTAMPTZ = 200;
    static final int FIELD_TYPE_TIMESTAMPLTZ = 201;
    static final int FIELD_TYPE_TIMESTAMP_NANO = 202;
    static final int FIELD_TYPE_OB_RAW = 203;
    static final int FIELD_TYPE_COMPLEX = 160;
    static final int FIELD_TYPE_ARRAR = 161;
    static final int FIELD_TYPE_STRUCT = 162;
    static final int FIELD_TYPE_TINY = 1;
    static final int FIELD_TYPE_TINY_BLOB = 249;
    static final int FIELD_TYPE_VAR_STRING = 253;
    static final int FIELD_TYPE_VARCHAR = 15;
    static final int FIELD_TYPE_YEAR = 13;
    static final int FIELD_TYPE_JSON = 245;
    static final int FIELD_TYPE_INTERVALYM = 204;
    static final int FIELD_TYPE_INTERVALDS = 205;
    static final int FIELD_TYPE_NUMBER_FLOAT = 206;
    static final int FIELD_TYPE_NVARCHAR2 = 207;
    static final int FIELD_TYPE_NCHAR = 208;
    static final int INIT_DB = 2;
    static final long LENGTH_BLOB = 65535L;
    static final long LENGTH_LONGBLOB = 4294967295L;
    static final long LENGTH_MEDIUMBLOB = 16777215L;
    static final long LENGTH_TINYBLOB = 255L;
    static final int MAX_ROWS = 50000000;
    public static final int NO_CHARSET_INFO = -1;
    static final byte OPEN_CURSOR_FLAG = 1;
    static final int PING = 14;
    static final int PROCESS_INFO = 10;
    static final int PROCESS_KILL = 12;
    static final int QUERY = 3;
    static final int QUIT = 1;
    static final int RELOAD = 7;
    static final int SHUTDOWN = 8;
    static final int SLEEP = 0;
    static final int STATISTICS = 9;
    static final int TIME = 15;
    private static Map<String, Integer> mysqlToJdbcTypesMap;
    
    static int mysqlToJavaType(final int mysqlType) {
        int jdbcType = 0;
        switch (mysqlType) {
            case 0:
            case 246: {
                jdbcType = 3;
                break;
            }
            case 1: {
                jdbcType = -6;
                break;
            }
            case 2: {
                jdbcType = 5;
                break;
            }
            case 3: {
                jdbcType = 4;
                break;
            }
            case 4: {
                jdbcType = 7;
                break;
            }
            case 5: {
                jdbcType = 8;
                break;
            }
            case 6: {
                jdbcType = 0;
                break;
            }
            case 7:
            case 202: {
                jdbcType = 93;
                break;
            }
            case 8: {
                jdbcType = -5;
                break;
            }
            case 9: {
                jdbcType = 4;
                break;
            }
            case 10: {
                jdbcType = 91;
                break;
            }
            case 11: {
                jdbcType = 92;
                break;
            }
            case 12: {
                jdbcType = 93;
                break;
            }
            case 13: {
                jdbcType = 91;
                break;
            }
            case 14: {
                jdbcType = 91;
                break;
            }
            case 247: {
                jdbcType = 1;
                break;
            }
            case 248: {
                jdbcType = 1;
                break;
            }
            case 203:
            case 249: {
                jdbcType = -3;
                break;
            }
            case 250: {
                jdbcType = -4;
                break;
            }
            case 251: {
                jdbcType = -4;
                break;
            }
            case 252: {
                jdbcType = -4;
                break;
            }
            case 15:
            case 253: {
                jdbcType = 12;
                break;
            }
            case 245:
            case 254: {
                jdbcType = 1;
                break;
            }
            case 255: {
                jdbcType = -2;
                break;
            }
            case 16: {
                jdbcType = -7;
                break;
            }
            case 161: {
                jdbcType = 2003;
                break;
            }
            case 162: {
                jdbcType = 2002;
                break;
            }
            case 200: {
                jdbcType = -101;
                break;
            }
            case 201: {
                jdbcType = -102;
                break;
            }
            default: {
                jdbcType = 12;
                break;
            }
        }
        return jdbcType;
    }
    
    static int mysqlToJavaType(final String mysqlType) {
        if (mysqlType.equalsIgnoreCase("BIT")) {
            return mysqlToJavaType(16);
        }
        if (mysqlType.equalsIgnoreCase("TINYINT")) {
            return mysqlToJavaType(1);
        }
        if (mysqlType.equalsIgnoreCase("SMALLINT")) {
            return mysqlToJavaType(2);
        }
        if (mysqlType.equalsIgnoreCase("MEDIUMINT")) {
            return mysqlToJavaType(9);
        }
        if (mysqlType.equalsIgnoreCase("INT") || mysqlType.equalsIgnoreCase("INTEGER")) {
            return mysqlToJavaType(3);
        }
        if (mysqlType.equalsIgnoreCase("BIGINT")) {
            return mysqlToJavaType(8);
        }
        if (mysqlType.equalsIgnoreCase("INT24")) {
            return mysqlToJavaType(9);
        }
        if (mysqlType.equalsIgnoreCase("REAL")) {
            return mysqlToJavaType(5);
        }
        if (mysqlType.equalsIgnoreCase("FLOAT")) {
            return mysqlToJavaType(4);
        }
        if (mysqlType.equalsIgnoreCase("DECIMAL")) {
            return mysqlToJavaType(0);
        }
        if (mysqlType.equalsIgnoreCase("NUMERIC")) {
            return mysqlToJavaType(0);
        }
        if (mysqlType.equalsIgnoreCase("DOUBLE")) {
            return mysqlToJavaType(5);
        }
        if (mysqlType.equalsIgnoreCase("CHAR")) {
            return mysqlToJavaType(254);
        }
        if (mysqlType.equalsIgnoreCase("VARCHAR")) {
            return mysqlToJavaType(253);
        }
        if (mysqlType.equalsIgnoreCase("DATE")) {
            return mysqlToJavaType(10);
        }
        if (mysqlType.equalsIgnoreCase("TIME")) {
            return mysqlToJavaType(11);
        }
        if (mysqlType.equalsIgnoreCase("YEAR")) {
            return mysqlToJavaType(13);
        }
        if (mysqlType.equalsIgnoreCase("TIMESTAMP")) {
            return mysqlToJavaType(7);
        }
        if (mysqlType.equalsIgnoreCase("DATETIME")) {
            return mysqlToJavaType(12);
        }
        if (mysqlType.equalsIgnoreCase("TINYBLOB")) {
            return -2;
        }
        if (mysqlType.equalsIgnoreCase("BLOB")) {
            return -4;
        }
        if (mysqlType.equalsIgnoreCase("MEDIUMBLOB")) {
            return -4;
        }
        if (mysqlType.equalsIgnoreCase("LONGBLOB")) {
            return -4;
        }
        if (mysqlType.equalsIgnoreCase("TINYTEXT")) {
            return 12;
        }
        if (mysqlType.equalsIgnoreCase("TEXT")) {
            return -1;
        }
        if (mysqlType.equalsIgnoreCase("MEDIUMTEXT")) {
            return -1;
        }
        if (mysqlType.equalsIgnoreCase("LONGTEXT")) {
            return -1;
        }
        if (mysqlType.equalsIgnoreCase("ENUM")) {
            return mysqlToJavaType(247);
        }
        if (mysqlType.equalsIgnoreCase("SET")) {
            return mysqlToJavaType(248);
        }
        if (mysqlType.equalsIgnoreCase("GEOMETRY")) {
            return mysqlToJavaType(255);
        }
        if (mysqlType.equalsIgnoreCase("BINARY")) {
            return -2;
        }
        if (mysqlType.equalsIgnoreCase("VARBINARY")) {
            return -3;
        }
        if (mysqlType.equalsIgnoreCase("RAW")) {
            return -3;
        }
        if (mysqlType.equalsIgnoreCase("BIT")) {
            return mysqlToJavaType(16);
        }
        if (mysqlType.equalsIgnoreCase("JSON")) {
            return mysqlToJavaType(245);
        }
        if (mysqlType.equalsIgnoreCase("TIMESTAMP WITH TIME ZONE")) {
            return mysqlToJavaType(200);
        }
        if (mysqlType.equalsIgnoreCase("TIMESTAMP WITH LOCAL TIME ZONE")) {
            return mysqlToJavaType(201);
        }
        return 1111;
    }
    
    public static String typeToName(final int mysqlType) {
        switch (mysqlType) {
            case 0: {
                return "FIELD_TYPE_DECIMAL";
            }
            case 1: {
                return "FIELD_TYPE_TINY";
            }
            case 2: {
                return "FIELD_TYPE_SHORT";
            }
            case 3: {
                return "FIELD_TYPE_LONG";
            }
            case 4: {
                return "FIELD_TYPE_FLOAT";
            }
            case 5: {
                return "FIELD_TYPE_DOUBLE";
            }
            case 6: {
                return "FIELD_TYPE_NULL";
            }
            case 7: {
                return "FIELD_TYPE_TIMESTAMP";
            }
            case 8: {
                return "FIELD_TYPE_LONGLONG";
            }
            case 9: {
                return "FIELD_TYPE_INT24";
            }
            case 16: {
                return "FIELD_TYPE_BIT";
            }
            case 10: {
                return "FIELD_TYPE_DATE";
            }
            case 11: {
                return "FIELD_TYPE_TIME";
            }
            case 12: {
                return "FIELD_TYPE_DATETIME";
            }
            case 13: {
                return "FIELD_TYPE_YEAR";
            }
            case 14: {
                return "FIELD_TYPE_NEWDATE";
            }
            case 247: {
                return "FIELD_TYPE_ENUM";
            }
            case 248: {
                return "FIELD_TYPE_SET";
            }
            case 249: {
                return "FIELD_TYPE_TINY_BLOB";
            }
            case 250: {
                return "FIELD_TYPE_MEDIUM_BLOB";
            }
            case 251: {
                return "FIELD_TYPE_LONG_BLOB";
            }
            case 203: {
                return "FIELD_TYPE_OB_RAW";
            }
            case 252: {
                return "FIELD_TYPE_BLOB";
            }
            case 253: {
                return "FIELD_TYPE_VAR_STRING";
            }
            case 254: {
                return "FIELD_TYPE_STRING";
            }
            case 15: {
                return "FIELD_TYPE_VARCHAR";
            }
            case 255: {
                return "FIELD_TYPE_GEOMETRY";
            }
            case 245: {
                return "FIELD_TYPE_JSON";
            }
            case 202: {
                return "FIELD_TYPE_TIMESTAMP_NANO";
            }
            case 200: {
                return "FIELD_TYPE_TIMESTAMPTZ";
            }
            case 201: {
                return "FIELD_TYPE_TIMESTAMPLTZ";
            }
            default: {
                return " Unknown MySQL Type # " + mysqlType;
            }
        }
    }
    
    static final void appendJdbcTypeMappingQuery(final StringBuilder buf, final String mysqlTypeColumnName) {
        buf.append("CASE ");
        final Map<String, Integer> typesMap = new HashMap<String, Integer>();
        typesMap.putAll(MysqlDefs.mysqlToJdbcTypesMap);
        typesMap.put("BINARY", -2);
        typesMap.put("VARBINARY", -3);
        for (final String mysqlTypeName : typesMap.keySet()) {
            buf.append(" WHEN UPPER(");
            buf.append(mysqlTypeColumnName);
            buf.append(")='");
            buf.append(mysqlTypeName);
            buf.append("' THEN ");
            buf.append(typesMap.get(mysqlTypeName));
            if (mysqlTypeName.equalsIgnoreCase("DOUBLE") || mysqlTypeName.equalsIgnoreCase("FLOAT") || mysqlTypeName.equalsIgnoreCase("DECIMAL") || mysqlTypeName.equalsIgnoreCase("NUMERIC")) {
                buf.append(" WHEN ");
                buf.append(mysqlTypeColumnName);
                buf.append("='");
                buf.append(mysqlTypeName);
                buf.append(" UNSIGNED' THEN ");
                buf.append(typesMap.get(mysqlTypeName));
            }
        }
        buf.append(" ELSE ");
        buf.append(1111);
        buf.append(" END ");
    }
    
    static {
        (MysqlDefs.mysqlToJdbcTypesMap = new HashMap<String, Integer>()).put("BIT", mysqlToJavaType(16));
        MysqlDefs.mysqlToJdbcTypesMap.put("TINYINT", mysqlToJavaType(1));
        MysqlDefs.mysqlToJdbcTypesMap.put("SMALLINT", mysqlToJavaType(2));
        MysqlDefs.mysqlToJdbcTypesMap.put("MEDIUMINT", mysqlToJavaType(9));
        MysqlDefs.mysqlToJdbcTypesMap.put("INT", mysqlToJavaType(3));
        MysqlDefs.mysqlToJdbcTypesMap.put("INTEGER", mysqlToJavaType(3));
        MysqlDefs.mysqlToJdbcTypesMap.put("BIGINT", mysqlToJavaType(8));
        MysqlDefs.mysqlToJdbcTypesMap.put("INT24", mysqlToJavaType(9));
        MysqlDefs.mysqlToJdbcTypesMap.put("REAL", mysqlToJavaType(5));
        MysqlDefs.mysqlToJdbcTypesMap.put("FLOAT", mysqlToJavaType(4));
        MysqlDefs.mysqlToJdbcTypesMap.put("DECIMAL", mysqlToJavaType(0));
        MysqlDefs.mysqlToJdbcTypesMap.put("NUMERIC", mysqlToJavaType(0));
        MysqlDefs.mysqlToJdbcTypesMap.put("DOUBLE", mysqlToJavaType(5));
        MysqlDefs.mysqlToJdbcTypesMap.put("CHAR", mysqlToJavaType(254));
        MysqlDefs.mysqlToJdbcTypesMap.put("VARCHAR", mysqlToJavaType(253));
        MysqlDefs.mysqlToJdbcTypesMap.put("DATE", mysqlToJavaType(10));
        MysqlDefs.mysqlToJdbcTypesMap.put("TIME", mysqlToJavaType(11));
        MysqlDefs.mysqlToJdbcTypesMap.put("YEAR", mysqlToJavaType(13));
        MysqlDefs.mysqlToJdbcTypesMap.put("TIMESTAMP", mysqlToJavaType(7));
        MysqlDefs.mysqlToJdbcTypesMap.put("DATETIME", mysqlToJavaType(12));
        MysqlDefs.mysqlToJdbcTypesMap.put("TINYBLOB", -2);
        MysqlDefs.mysqlToJdbcTypesMap.put("BLOB", -4);
        MysqlDefs.mysqlToJdbcTypesMap.put("RAW", -3);
        MysqlDefs.mysqlToJdbcTypesMap.put("MEDIUMBLOB", -4);
        MysqlDefs.mysqlToJdbcTypesMap.put("LONGBLOB", -4);
        MysqlDefs.mysqlToJdbcTypesMap.put("TINYTEXT", 12);
        MysqlDefs.mysqlToJdbcTypesMap.put("TEXT", -1);
        MysqlDefs.mysqlToJdbcTypesMap.put("MEDIUMTEXT", -1);
        MysqlDefs.mysqlToJdbcTypesMap.put("LONGTEXT", -1);
        MysqlDefs.mysqlToJdbcTypesMap.put("ENUM", mysqlToJavaType(247));
        MysqlDefs.mysqlToJdbcTypesMap.put("SET", mysqlToJavaType(248));
        MysqlDefs.mysqlToJdbcTypesMap.put("GEOMETRY", mysqlToJavaType(255));
        MysqlDefs.mysqlToJdbcTypesMap.put("JSON", mysqlToJavaType(245));
    }
}
