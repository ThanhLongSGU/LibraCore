package com.libracoreteam.libracore.util;

import java.sql.SQLException;

public class DatabaseConfigurationException extends SQLException {
    private static final long serialVersionUID = 1L;

    public DatabaseConfigurationException(String message) {
        super(message);
    }

    public DatabaseConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
