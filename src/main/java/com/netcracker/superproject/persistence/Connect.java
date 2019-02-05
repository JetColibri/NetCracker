package com.netcracker.superproject.persistence;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {
    private static Connection conn = null;
    private static final Logger log = Logger.getLogger(Connect.class);

    private Connect() {
        try {
            String url = "jdbc:postgresql://localhost:5432/project";
            String user = "postgres";
            String password = "epifanik";
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
            log.info(sqlEx);
        }
    }

    public static synchronized Connection getConnection() {
        if (conn == null) {
            new Connect();
        }
        return conn;
    }
}
