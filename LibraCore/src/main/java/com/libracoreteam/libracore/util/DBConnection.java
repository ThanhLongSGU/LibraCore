package com.libracoreteam.libracore.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        String url;
        String user;
        String password;
        try {
            url = requiredConfig("db.url");
            user = requiredConfig("db.username");
            password = requiredConfig("db.password");
        } catch (IllegalStateException e) {
            throw new DatabaseConfigurationException("Không thể tải cấu hình cơ sở dữ liệu.", e);
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static String requiredConfig(String key) throws DatabaseConfigurationException {
        String value = ConfigUtility.getConfig(key);
        if (value == null || value.isBlank()) {
            throw new DatabaseConfigurationException("Thiếu cấu hình bắt buộc: " + key);
        }
        return value;
    }
}
