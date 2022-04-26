package dev.tonysp.plugindata.connections.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.tonysp.plugindata.PluginData;
import dev.tonysp.plugindata.connections.ConnectionType;
import dev.tonysp.plugindata.connections.ConnectionsManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class MysqlConnection extends dev.tonysp.plugindata.connections.Connection {

    String url, username, password;
    HikariConfig hikariConfig;
    HikariDataSource hikariDataSource;

    public MysqlConnection (String connectionName, String url, String username, String password) {
        super(connectionName, ConnectionType.MYSQL);
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

    public static Optional<MysqlConnection> byName (String connectionName) {
        Optional<dev.tonysp.plugindata.connections.Connection> connection = ConnectionsManager.getInstance().getConnection(connectionName);
        if (connection.isPresent() && !(connection.get() instanceof MysqlConnection)) {
            return Optional.empty();
        }
        return connection.map(value -> (MysqlConnection) value);
    }

    public void closeDataSource() {
        hikariDataSource.close();
    }

    public Connection getConnection () throws SQLException {
        return hikariDataSource.getConnection();
    }

    @Override
    public void shutDown () {
        hikariDataSource.close();
    }

    @Override
    public boolean test () {
        try (Connection ignored = hikariDataSource.getConnection()){
            PluginData.log("Testing connection: " + getName() + ", SUCCESS!");
            return true;
        } catch (SQLException exception) {
            PluginData.logWarning("Testing connection: " + getName() + ", FAILED!");
            exception.printStackTrace();
            return false;
        }
    }
}
