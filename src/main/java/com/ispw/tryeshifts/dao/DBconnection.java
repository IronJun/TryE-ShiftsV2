package com.ispw.tryeshifts.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBconnection {
    private static final Logger LOGGER = Logger.getLogger(DBconnection.class.getName());
    private static final Properties props = new Properties();

    private Connection connection;

    static {
        // Carichiamo il file una sola volta all'avvio della classe
        try (InputStream input = DBconnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                LOGGER.log(Level.SEVERE, "Sorry, could not find db.properties");
            } else {
                props.load(input);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during the DB configuration ", e);
        }
    }

    private DBconnection(){
        try{
            connect();
        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "error during DB connection", e);
        }
    }

    private static class LazyContainer {
        public static final DBconnection instance = new DBconnection();
    }

    // singleton method
    public static DBconnection getInstance() {
        return LazyContainer.instance;
    }

    private void connect() throws SQLException {
        String password = System.getenv("DB_password");
        if (password == null || password.isEmpty()){
            LOGGER.warning("CRITICAL ERROR: environment variable DB_password is null or empty");
        }
        this.connection = DriverManager.getConnection(props.getProperty("db.url"),
            props.getProperty("db.user"),password);
    }

    public Connection getConnection() throws SQLException {
        if(connection == null || connection.isClosed()) {
            LOGGER.warning("CRITICAL ERROR: connection is expired or closed, i try to open it again...");
            connect();
        }
        return connection;
    }
}