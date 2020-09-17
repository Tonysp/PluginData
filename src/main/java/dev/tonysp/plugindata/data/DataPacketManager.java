package dev.tonysp.plugindata.data;

import dev.tonysp.plugindata.PluginData;
import dev.tonysp.plugindata.data.exceptions.WrongPipelineManagerException;
import dev.tonysp.plugindata.data.pipelines.Pipeline;
import dev.tonysp.plugindata.data.pipelines.PipelineManager;
import dev.tonysp.plugindata.data.pipelines.jedis.BatchPipelineManager;
import dev.tonysp.plugindata.data.pipelines.jedis.PubSubPipelineManager;
import dev.tonysp.plugindata.data.pipelines.jedis.RedisPipelineManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.*;

import java.util.*;
import java.util.concurrent.*;

public class DataPacketManager {

    private static DataPacketManager instance;

    private final String redisIp, redisPassword;
    private final int redisPort;
    private boolean isConnected = false;
    private JedisPool jedisPool;

    private final BukkitTask reconnectTask, scanServersTask;

    public final String CLUSTER_ID;
    public final String SERVER_ID;
    public final Set<String> SERVERS;
    public final String GLOBAL_KEY_PREFIX = "plugindata";
    private final int RECONNECT_TICKS = 1200;

    private final List<PipelineManager> pipelineManagers = new ArrayList<>();

    public DataPacketManager (PluginData plugin, String redisIp, int redisPort, String redisPassword, String CLUSTER_ID, String SERVER_ID, int packet_send_and_retrieve_interval, boolean clear_old_packets) {
        instance = this;
        this.redisPassword = redisPassword;
        this.redisIp = redisIp;
        this.redisPort = redisPort;

        this.CLUSTER_ID = CLUSTER_ID;
        this.SERVER_ID = SERVER_ID;
        this.SERVERS = Collections.newSetFromMap(new ConcurrentHashMap<>());

        registerPipelineManager(new BatchPipelineManager(redisPassword, clear_old_packets, packet_send_and_retrieve_interval));
        registerPipelineManager(new PubSubPipelineManager(redisPassword));

        reconnectTask = new BukkitRunnable() {
            @Override
            public void run() {
                isConnected = connect();

                if (isConnected) {
                    startUp();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, RECONNECT_TICKS);

        scanServersTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (isConnected) {
                    scanServers();
                }
            }
        }.runTaskTimer(plugin, 0, 40);
    }

    public static DataPacketManager getInstance () {
        return instance;
    }

    public void registerPipelineManager (PipelineManager pipelineManager) {
        Pipeline.getPipelineByManager(pipelineManager.getClass()).ifPresent(pipeline -> {
            try {
                pipeline.setPipelineManagerObject(pipelineManager);
            } catch (WrongPipelineManagerException exception) {
                exception.printStackTrace();
            }
        });
        getPipelineManagers().add(pipelineManager);
    }

    public List<PipelineManager> getPipelineManagers () {
        return pipelineManagers;
    }

    private void startUp () {
        getPipelineManagers().stream()
                .filter(pipelineManager -> pipelineManager instanceof RedisPipelineManager)
                .forEach(pipelineManager -> ((RedisPipelineManager) pipelineManager).setJedisPool(jedisPool));
        getPipelineManagers().forEach(PipelineManager::startUp);

        registerServer();
    }

    public void shutDown () {
        getPipelineManagers().forEach(PipelineManager::shutDown);

        reconnectTask.cancel();
        scanServersTask.cancel();
        unregisterServer();
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

    private void registerServer () {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(redisPassword);
            jedis.sadd(getClusterServersSetKey(), SERVER_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unregisterServer () {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(redisPassword);
            jedis.srem(getClusterServersSetKey(), SERVER_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scanServers () {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(redisPassword);

            SERVERS.clear();
            SERVERS.addAll(jedis.smembers(getClusterServersSetKey()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
