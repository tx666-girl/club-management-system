package com.zeroone.star.util;

import com.alibaba.druid.pool.DruidDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Druid 数据源工具类。
 * 连接池只创建一次，DAO 使用时从连接池借连接。
 */
public final class DruidUtil {

    private static final DruidDataSource DATA_SOURCE = new DruidDataSource();

    static {
        try (InputStream input = DruidUtil.class
                .getClassLoader()
                .getResourceAsStream("jdbc.properties")) {

            if (input == null) {
                throw new IllegalStateException("找不到 jdbc.properties");
            }

            Properties properties = new Properties();
            properties.load(input);

            DATA_SOURCE.setDriverClassName(
                    properties.getProperty("druid.driverClassName")
            );
            DATA_SOURCE.setUrl(
                    properties.getProperty("druid.url")
            );
            DATA_SOURCE.setUsername(
                    properties.getProperty("druid.username")
            );
            DATA_SOURCE.setPassword(
                    properties.getProperty("druid.password")
            );

            DATA_SOURCE.setInitialSize(
                    Integer.parseInt(properties.getProperty("druid.initialSize", "2"))
            );
            DATA_SOURCE.setMinIdle(
                    Integer.parseInt(properties.getProperty("druid.minIdle", "2"))
            );
            DATA_SOURCE.setMaxActive(
                    Integer.parseInt(properties.getProperty("druid.maxActive", "10"))
            );
            DATA_SOURCE.setMaxWait(
                    Long.parseLong(properties.getProperty("druid.maxWait", "3000"))
            );

            DATA_SOURCE.setValidationQuery("SELECT 1");
            DATA_SOURCE.setTestWhileIdle(true);
            DATA_SOURCE.setTestOnBorrow(false);
            DATA_SOURCE.setTestOnReturn(false);
            DATA_SOURCE.setTimeBetweenEvictionRunsMillis(60000);
            DATA_SOURCE.setMinEvictableIdleTimeMillis(300000);

        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DruidUtil() {
    }

    /**
     * 从连接池获取连接。
     * 用完必须 close()，close() 会把连接归还连接池。
     */
    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    public static void close() {
        DATA_SOURCE.close();
    }
}