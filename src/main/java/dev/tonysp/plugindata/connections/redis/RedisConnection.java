package dev.tonysp.plugindata.connections.redis;

import dev.tonysp.plugindata.PluginData;
import dev.tonysp.plugindata.connections.Connection;
import dev.tonysp.plugindata.connections.ConnectionType;
import dev.tonysp.plugindata.connections.ConnectionsManager;
import dev.tonysp.plugindata.data.DataPacketManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Optional;

public class RedisConnection extends Connection {

    private final String redisIp, redisPassword;
    private final int redisPort;
    private boolean isConnected = false;
    private JedisPool jedisPool;
    private final BukkitTask reconnectTask;

    private static final int RECONNECT_TICKS = 1200;

    public RedisConnection (JavaPlugin plugin, String connectionName, String redisIp, String redisPassword, int redisPort) {
        super(connectionName, ConnectionType.REDIS);
        this.redisIp = redisIp;
        this.redisPassword = redisPassword;
        this.redisPort = redisPort;

        isConnected = connect();

        reconnectTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isConnected) {
                    isConnected = connect();
                }
            }
        }.runTaskTimer(plugin, 0, RECONNECT_TICKS);
    }

    public static Optional<RedisConnection> byName (String connectionName) {
        Optional<dev.tonysp.plugindata.connections.Connection> connection = ConnectionsManager.getInstance().getConnection(connectionName);
        if (connection.isPresent() && !(connection.get() instanceof RedisConnection)) {
            return Optional.empty();
        }
        return connection.map(value -> (RedisConnection) value);
    }

    public boolean isConnected () {
        return isConnected;
    }

    private boolean connect () {
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(DataPacketManager.class.getClassLoader());
        try {
            jedisPool = new JedisPool(redisIp, redisPort);
            jedisPool.getResource();
            PluginData.log("Successfully connected to Redis!");
        } catch (Exception e) {
            PluginData.log("Error while connecting to Redis! Trying again in " + (RECONNECT_TICKS / 20) + " seconds...");
            e.printStackTrace();
            return false;
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }

        return true;
    }

    @Override
    public void shutDown () {
        reconnectTask.cancel();
    }

    public JedisPool getConnection () {
        return jedisPool;
    }

    public Jedis getResource () {
        Jedis jedis = jedisPool.getResource();
        jedis.auth(redisPassword);
        return jedis;
    }

    @Override
    public boolean test () {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(redisPassword);
            PluginData.log("Testing connection: " + getName() + ", SUCCESS!");
            return true;
        } catch (Exception exception) {
            PluginData.logWarning("Testing connection: " + getName() + ", FAILED!");
            exception.printStackTrace();
            return false;
        }
    }
}
