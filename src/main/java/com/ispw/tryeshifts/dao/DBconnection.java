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

    static {
        // Carichiamo il file una sola volta all'avvio della classe
        try (InputStream input = DBconnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                LOGGER.log(Level.SEVERE, "Spiacente, impossibile trovare db.properties");
            } else {
                props.load(input);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento della configurazione DB", e);
        }
    }


    private DBconnection(){
        throw new IllegalStateException("Utility class");
    }
    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(props.getProperty("db.url"),props.getProperty("db.user"),props.getProperty("db.password"));
    }
}
