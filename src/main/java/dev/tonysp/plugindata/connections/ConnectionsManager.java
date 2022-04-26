package dev.tonysp.plugindata.connections;

import dev.tonysp.plugindata.PluginData;
import dev.tonysp.plugindata.connections.mysql.MysqlConnection;
import dev.tonysp.plugindata.connections.redis.RedisConnection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConnectionsManager {

    private static ConnectionsManager instance;

    private final Map<String, Connection> connections = new HashMap<>();

    public static ConnectionsManager getInstance() {
        if (instance == null) {
            instance = new ConnectionsManager();
        }
        return instance;
    }

    public void loadConnection (JavaPlugin plugin, String connectionName, ConfigurationSection config) {
        Connection connection;
        ConnectionType connectionType = ConnectionType.valueOf(config.getString("type"));
        if (connectionType == ConnectionType.MYSQL) {
            String url, username, password;
            url = config.getString("url", "");
            username = config.getString("username", "");
            password = config.getString("password", "");
            MysqlConnection mysqlConnection;
            try {
                mysqlConnection = new MysqlConnection(connectionName, url, username, password);
            } catch (Exception exception) {
                PluginData.logWarning("Error while initializing MYSQL connection: " + connectionName);
                return;
            }
            PluginData.log("Initialized MYSQL connection: " + connectionName);
            mysqlConnection.test();
            connection = mysqlConnection;
        } else if (connectionType == ConnectionType.REDIS) {
            String ip, password;
            int port;
            ip = config.getString("ip", "127.0.0.1");
            port = config.getInt("port", 6379);
            password = config.getString("password", "");
            RedisConnection redisConnection;
            try {
                redisConnection = new RedisConnection(plugin, connectionName, ip, password, port);
            } catch (Exception exception) {
                PluginData.logWarning("Error while initializing REDIS connection: " + connectionName);
                return;
            }
            PluginData.log("Initialized REDIS connection: " + connectionName);
            redisConnection.test();
            connection = redisConnection;
        } else {
            return;
        }
        addConnection(connectionName, connection);
    }

    public void addConnection (String connectionName, Connection connection) {
        String connectionFinalName = connectionName.toLowerCase();
        if (connections.containsKey(connectionFinalName)) {
            PluginData.logWarning("Connection named " + connectionFinalName + " was already present and has been overridden.");
        }
        connections.put(connectionFinalName, connection);
    }

    public Optional<Connection> getConnection (String connectionName) {
        return Optional.ofNullable(connections.get(connectionName.toLowerCase()));
    }

    public void shutDown () {
        connections.values().forEach(Connection::shutDown);
    }
}
