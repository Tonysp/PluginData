package dev.tonysp.plugindata;

import dev.tonysp.plugindata.connections.ConnectionsManager;
import dev.tonysp.plugindata.connections.redis.RedisConnection;
import dev.tonysp.plugindata.data.DataPacketManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.logging.Level;

public class PluginData extends JavaPlugin {

    private DataPacketManager dataPacketManager;

    @Override
    public void onEnable() {
        load();
    }

    @Override
    public void onDisable() {
        if (dataPacketManager != null)
            dataPacketManager.shutDown(true);
        ConnectionsManager.getInstance().shutDown();
    }

    private void load() {
        saveDefaultConfig();
        reloadConfig();

        log("Loading connections...");
        ConfigurationSection connectionsConfig = getConfig().getConfigurationSection("connections");
        if (connectionsConfig != null)
            for (String connectionName : connectionsConfig.getKeys(false)) {
                ConfigurationSection connectionConfig = connectionsConfig.getConfigurationSection(connectionName);
                if (connectionConfig == null)
                    continue;
                ConnectionsManager.getInstance().loadConnection(this, connectionName, connectionConfig);
            }
        log("...done");

        if (!getConfig().getBoolean("data-packets.enabled", false)) {
            return;
        }

        boolean enableDataPacket = true;
        String clusterId = getConfig().getString("data-packets.cluster-id", null);
        if (clusterId == null || clusterId.equalsIgnoreCase("")) {
            log("Missing cluster-id, data packet functionality disabled");
            enableDataPacket = false;
        }

        String serverId = getConfig().getString("data-packets.server-id", null);
        if (serverId == null || serverId.equalsIgnoreCase("")) {
            log("Missing server-id, data packet functionality disabled");
            enableDataPacket = false;
        }

        String redisConnectionName = getConfig().getString("data-packets.redis-connection-name");
        if (redisConnectionName == null || redisConnectionName.equalsIgnoreCase("")) {
            log("Missing redis-connection-name, data packet functionality disabled");
            enableDataPacket = false;
        }

        Optional<RedisConnection> redisConnection = RedisConnection.byName(redisConnectionName);
        if (!redisConnection.isPresent()) {
            log("Invalid redis-connection-name, data packet functionality disabled");
            enableDataPacket = false;
        }

        int packetSendAndRetrieveInterval = getConfig().getInt("data-packets.batch-packet-send-and-retrieve-interval", 5);
        boolean clearOldPackets = getConfig().getBoolean("data-packets.batch-clear-old-packets", true);

        if (enableDataPacket) {
            dataPacketManager = new DataPacketManager(getInstance(), redisConnection.get(), clusterId, serverId, packetSendAndRetrieveInterval, clearOldPackets);
        }
    }

    public static void log (Level level, String message) {
        Bukkit.getLogger().log(level, "[PluginData] " + message);
    }

    public static void log (String message) {
        log(Level.INFO, message);
    }

    public static void logWarning (String message) {
        log(Level.WARNING, message);
    }

    public static PluginData getInstance () {
        return getPlugin(PluginData.class);
    }

    public DataPacketManager getDataPackets () {
        return this.dataPacketManager;
    }
}
