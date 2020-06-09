package cz.goldminer.tonysp.plugindata.databases;

import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

public class DatabaseManager {

    private static DatabaseManager instance;

    private HashMap<String, Database> databases = new HashMap<>();

    public DatabaseManager (FileConfiguration config) {
        instance = this;

        for (String databaseName : config.getConfigurationSection("mysql-connections").getKeys(false)) {
            String url, username, password;
            url = config.getString("mysql-connections." + databaseName + ".url", "");
            username = config.getString("mysql-connections." + databaseName + ".username", "");
            password = config.getString("mysql-connections." + databaseName + ".password", "");
            Database database = new Database(databaseName.toLowerCase(), url, username, password);
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
