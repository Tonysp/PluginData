package dev.tonysp.plugindata.databases;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.tonysp.plugindata.PluginData;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {

    String connectionName, url, username, password;
    HikariConfig hikariConfig;
    HikariDataSource hikariDataSource;

    public Database (String connectionName, String url, String username, String password) {
        this.connectionName = connectionName;
        this.url = url;
        this.username = username;
        this.password = password;

        hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public void closeDataSource() {
        hikariDataSource.close();
    }

    public Connection getConnection () throws SQLException {
        return hikariDataSource.getConnection();
    }

    public boolean test () {
        try (Connection testConnection = hikariDataSource.getConnection()){
            PluginData.log("Testing connection: " + connectionName + ", SUCCESS!");
            return true;
        } catch (SQLException e) {
            PluginData.logWarning("Testing connection: " + connectionName + ", FAILED!");
            e.printStackTrace();
            return false;
        }
    }
}
