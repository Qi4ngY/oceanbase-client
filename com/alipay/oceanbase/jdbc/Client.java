package com.alipay.oceanbase.jdbc;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import java.sql.DriverManager;
import java.util.Comparator;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.BasicParser;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import java.sql.ResultSet;

public class Client
{
    private static void printResult(final ResultSet resultSet) throws Exception {
        final ResultSetMetaData resultSetMetaData = (ResultSetMetaData)resultSet.getMetaData();
        final int columnsNumber = resultSetMetaData.getColumnCount();
        for (int i = 1; i <= columnsNumber; ++i) {
            if (i > 1) {
                System.out.print(", ");
            }
            System.out.print(resultSetMetaData.getColumnName(i));
        }
        System.out.println("");
        while (resultSet.next()) {
            for (int i = 1; i <= columnsNumber; ++i) {
                if (i > 1) {
                    System.out.print(", ");
                }
                System.out.print(resultSet.getString(i));
            }
            System.out.println("");
        }
    }
    
    private static void printJsonResult(final ResultSet resultSet) throws Exception {
        final ResultSetMetaData resultSetMetaData = (ResultSetMetaData)resultSet.getMetaData();
        final int columnsNumber = resultSetMetaData.getColumnCount();
        final JSONArray result = new JSONArray();
        while (resultSet.next()) {
            final JSONObject jsonObject = new JSONObject();
            for (int i = 1; i <= columnsNumber; ++i) {
                jsonObject.put(resultSetMetaData.getColumnName(i), resultSet.getObject(i));
            }
            result.add(jsonObject);
        }
        if (result.size() == 1) {
            System.out.println(result.get(0).toString());
        }
        else {
            System.out.println(result.toJSONString());
        }
    }
    
    public static void main(final String[] args) throws Exception {
        final CommandLineParser parser = new BasicParser();
        final Options options = new Options();
        options.addOption("h", "host", true, "Connect to host.");
        options.addOption("P", "port", true, "Port number to use for connection.");
        options.addOption("u", "user", true, "User for login if not current user.");
        options.addOption("p", "password", true, "Password to use when connecting to server.");
        options.addOption("D", "database", true, "Database to use.");
        options.addOption("s", "sql", true, "Sql to execute.");
        options.addOption("j", "json", false, "Result to JSON String.");
        final CommandLine commandLine = parser.parse(options, args);
        if (!commandLine.hasOption("host") || !commandLine.hasOption("user") || !commandLine.hasOption("password") || !commandLine.hasOption("database") || !commandLine.hasOption("sql")) {
            final HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.setOptionComparator(new Comparator() {
                private int[] a = { 1, 1, -1, 1, -1, -1, -1, 1, -1, -1, -1, 1, 1 };
                private int i = 0;
                
                public int compare(final Object o1, final Object o2) {
                    return this.a[this.i++];
                }
            });
            helpFormatter.printHelp("java -jar oceanbase-client.jar [OPTIONS]", options);
            System.exit(0);
        }
        final String host = commandLine.getOptionValue("host");
        int port = 3306;
        if (commandLine.hasOption("port")) {
            port = Integer.parseInt(commandLine.getOptionValue("port"));
        }
        final String user = commandLine.getOptionValue("user");
        final String password = commandLine.getOptionValue("password");
        final String database = commandLine.getOptionValue("database");
        final String sql = commandLine.getOptionValue("sql");
        try {
            final Connection conn = (Connection)DriverManager.getConnection("jdbc:oceanbase://" + host + ":" + port + "/" + database, user, password);
            final Statement statement = (Statement)conn.createStatement();
            final ResultSet resultSet = statement.executeQuery(sql);
            if (commandLine.hasOption("json")) {
                printJsonResult(resultSet);
            }
            else {
                printResult(resultSet);
            }
        }
        catch (Exception e) {
            System.out.println("Connect to Mysql Server error.");
        }
    }
}
