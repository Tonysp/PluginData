package cz.goldminer.tonysp.plugindata;

import cz.goldminer.tonysp.plugindata.data.DataPacketManager;
import cz.goldminer.tonysp.plugindata.databases.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class PluginData extends JavaPlugin {

    private static PluginData plugin;

    private DataPacketManager dataManager;
    private DatabaseManager databaseManager;


    @Override
    public void onEnable() {
        plugin = this;
        loadConfig();
    }

    @Override
    public void onDisable() {
        databaseManager.shutDown();
    }

    private void loadConfig() {

        if (!(new File(getDataFolder() + File.separator + "config.yml").exists())) {
            saveDefaultConfig();
        }

        try {
            new YamlConfiguration().load(new File(getDataFolder() + File.separator + "config.yml"));
        } catch (Exception e) {
            System.out.println("There was a problem loading the config. More details bellow.");
            System.out.println("-----------------------------------------------");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        reloadConfig();

        log("Loading database connections...");

        String serverId = getConfig().getString("server-id");
        if (serverId == null) {
            log("Missing server-id, data packet functionality disabled");
        }

        String redisIp = getConfig().getString("redis.ip", "localhost");
        int redisPort = getConfig().getInt("redis.port", 6379);
        String redisPassword = getConfig().getString("redis.password", "");

        if (serverId != null) {
            dataManager = new DataPacketManager(plugin, redisIp, redisPort, redisPassword, serverId, getConfig().getStringList("server-id-list"));
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
        return plugin;
    }
}
