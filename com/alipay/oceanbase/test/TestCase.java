package com.alipay.oceanbase.test;

import java.sql.DriverManager;

public class TestCase
{
    public static void main(final String[] args) throws Exception {
        try {
            Class.forName("com.alipay.oceanbase.jdbc.Driver");
        }
        catch (Exception ex) {}
        //DriverManager.getConnection("jdbc:oceanbase://47.99.54.123:3306/test");
        DriverManager.getConnection("jdbc:oceanbase://127.0.0.1:3306/test?username=root&password=Mybank@2019");
    }
}
