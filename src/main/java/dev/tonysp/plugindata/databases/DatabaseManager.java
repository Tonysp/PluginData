package dev.tonysp.plugindata.databases;

import dev.tonysp.plugindata.PluginData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

    private static DatabaseManager instance;

    private final Map<String, Database> databases = new HashMap<>();

    public DatabaseManager (FileConfiguration config) {
        instance = this;

        ConfigurationSection mysqlConnectionsConfig = config.getConfigurationSection("mysql-connections");
        if (mysqlConnectionsConfig == null) {
            return;
        }

        for (String databaseName : mysqlConnectionsConfig.getKeys(false)) {
            String url, username, password;
            url = config.getString("mysql-connections." + databaseName + ".url", "");
            username = config.getString("mysql-connections." + databaseName + ".username", "");
            password = config.getString("mysql-connections." + databaseName + ".password", "");
            Database database;
            try {
                database = new Database(databaseName.toLowerCase(), url, username, password);
            } catch (Exception exception) {
                PluginData.logWarning("Error while initializing database: " + databaseName.toLowerCase());
                return;
            }
            PluginData.log("Initialized database connection: " + databaseName.toLowerCase());
            database.test();
            databases.put(databaseName.toLowerCase(), database);
        }
    }

    public static DatabaseManager getInstance () {
        return instance;
    }

    public Connection getConnection (String databaseName) throws SQLException {
        return databases.get(databaseName.toLowerCase()).getConnection();
    }

    public void shutDown () {
        databases.values().forEach(Database::closeDataSource);
    }
}
