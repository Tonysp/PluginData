package dev.tonysp.plugindata.data;

import dev.tonysp.plugindata.PluginData;
import dev.tonysp.plugindata.connections.redis.RedisConnection;
import dev.tonysp.plugindata.data.exceptions.WrongPipelineManagerException;
import dev.tonysp.plugindata.data.pipelines.Pipeline;
import dev.tonysp.plugindata.data.pipelines.PipelineManager;
import dev.tonysp.plugindata.data.pipelines.jedis.BatchPipelineManager;
import dev.tonysp.plugindata.data.pipelines.jedis.PubSubPipelineManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.*;

import java.util.*;
import java.util.concurrent.*;

public class DataPacketManager {

    private final JavaPlugin plugin;
    private final RedisConnection redisConnection;
    private final BukkitTask startupTask;

    private boolean running = false;
    private BukkitTask scanServersTask;

    public final String CLUSTER_ID;
    public final String SERVER_ID;
    public final Set<String> SERVERS;
    public final String GLOBAL_KEY_PREFIX = "plugindata";

    public static final int DEFAULT_PACKET_SEND_RECEIVE_INTERVAL = 5;
    public static final boolean DEFAULT_CLEAR_OLD_PACKETS = true;

    private final Map<Pipeline, PipelineManager> pipelineManagers = new HashMap<>();

    public DataPacketManager (JavaPlugin plugin, RedisConnection redisConnection, String CLUSTER_ID, String SERVER_ID, int packet_send_and_retrieve_interval, boolean clear_old_packets) {
        this.plugin = plugin;
        this.redisConnection = redisConnection;

        this.CLUSTER_ID = CLUSTER_ID;
        this.SERVER_ID = SERVER_ID;
        this.SERVERS = Collections.newSetFromMap(new ConcurrentHashMap<>());

        registerPipelineManager(new BatchPipelineManager(plugin, redisConnection, this, clear_old_packets, packet_send_and_retrieve_interval));
        registerPipelineManager(new PubSubPipelineManager(redisConnection, this));

        startupTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (redisConnection.isConnected() && !running) {
                    startUp();
                }
                if (!redisConnection.isConnected() && running) {
                    shutDown(false);
                }
            }
        }.runTaskTimer(plugin, 0, 40);
    }

    public void registerPipelineManager (PipelineManager pipelineManager) {
        Optional<Pipeline> pipeline = Pipeline.getPipelineByManager(pipelineManager.getClass());
        if (!pipeline.isPresent())
            return;

        try {
            pipeline.get().setPipelineManagerObject(pipelineManager);
        } catch (WrongPipelineManagerException exception) {
            exception.printStackTrace();
        }

        getPipelineManagers().put(pipeline.get(), pipelineManager);
    }

    public Map<Pipeline, PipelineManager> getPipelineManagers () {
        return pipelineManagers;
    }

    private void startUp () {
        getPipelineManagers().values().forEach(PipelineManager::startUp);

        scanServersTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (redisConnection.isConnected()) {
                    scanServers();
                }
            }
        }.runTaskTimer(plugin, 0, 40);

        registerServer();
        running = true;
    }

    public void shutDown (boolean killStartupTask) {
        getPipelineManagers().values().forEach(PipelineManager::shutDown);

        if (scanServersTask != null)
            scanServersTask.cancel();
        unregisterServer();
        running = false;
        if (killStartupTask) {
            startupTask.cancel();
        }
    }

    public String getReceivePacketsKey () {
        return GLOBAL_KEY_PREFIX + "-" + CLUSTER_ID + "-" + SERVER_ID;
    }

    public String getSendPacketsKeyPrefix () {
        return GLOBAL_KEY_PREFIX + "-" + CLUSTER_ID + "-";
    }

    public String getClusterServersSetKey () {
        return GLOBAL_KEY_PREFIX + "-" + CLUSTER_ID + "-servers";
    }

    public String getDataPacketKeyFromClass (String applicationId, Class<?> dataPacketClass) {
        return applicationId.concat("-").concat(dataPacketClass.getName());
    }

    private void registerServer () {
        try (Jedis jedis = redisConnection.getResource()) {
            jedis.sadd(getClusterServersSetKey(), SERVER_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unregisterServer () {
        try (Jedis jedis = redisConnection.getResource()) {
            jedis.srem(getClusterServersSetKey(), SERVER_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scanServers () {
        try (Jedis jedis = redisConnection.getResource()) {
            SERVERS.clear();
            SERVERS.addAll(jedis.smembers(getClusterServersSetKey()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
