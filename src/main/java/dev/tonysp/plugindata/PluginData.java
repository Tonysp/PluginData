package dev.tonysp.plugindata;

import dev.tonysp.plugindata.data.DataPacketManager;
import dev.tonysp.plugindata.databases.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class PluginData extends JavaPlugin {

    private DataPacketManager dataPacketManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        load();
    }

    @Override
    public void onDisable() {
        databaseManager.shutDown();
        dataPacketManager.shutDown();
    }

    private void load() {
        saveDefaultConfig();
        reloadConfig();

        log("Loading database connections...");

        String clusterId = getConfig().getString("cluster-id");
        if (clusterId == null) {
            log("Missing cluster-id, data packet functionality disabled");
        }

        String serverId = getConfig().getString("server-id");
        if (serverId == null) {
            log("Missing server-id, data packet functionality disabled");
        }

        String redisIp = getConfig().getString("redis.ip", "localhost");
        int redisPort = getConfig().getInt("redis.port", 6379);
        String redisPassword = getConfig().getString("redis.password", "");

        int packetSendAndRetrieveInterval = getConfig().getInt("batch-packet-send-and-retrieve-interval", 5);
        boolean clearOldPackets = getConfig().getBoolean("batch-clear-old-packets", true);

        if (clusterId != null && serverId != null) {
            dataPacketManager = new DataPacketManager(getInstance(), redisIp, redisPort, redisPassword, clusterId, serverId, packetSendAndRetrieveInterval, clearOldPackets);
        }
        databaseManager = new DatabaseManager(getConfig());

        log("...done");
    }

    public static void log(String text) {
        Bukkit.getLogger().log(Level.INFO, "[PluginData] " + text);
    }

    public static void logWarning(String text) {
        Bukkit.getLogger().log(Level.WARNING, "[PluginData] " + text);
    }

    public static PluginData getInstance () {
        return getPlugin(PluginData.class);
    }
}
