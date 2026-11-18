
package com.libracoreteam.libracore.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtility {
    private static Properties properties;

    public static String getConfig(String key) {
        return getProperties().getProperty(key);
    }

    private static synchronized Properties getProperties() {
        if (properties != null) {
            return properties;
        }

        try (InputStream input = ConfigUtility.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IllegalStateException("Không tìm thấy config.properties");
            }

            Properties loaded = new Properties();
            loaded.load(input);
            properties = loaded;
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Không thể đọc config.properties", e);
        }
    }
}
