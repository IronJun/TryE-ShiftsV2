package com.ispw.tryeshifts.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBconnection {
    private static final String URL = "jdbc:mysql://localhost:3306/eshifts_v2";
    private static final String USER = "root";
    private static final String PASSWORD = "SQL!4Pwd";

    private DBconnection(){
        throw new IllegalStateException("Utility class");
    }
    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(URL,USER,PASSWORD);
    }
}
