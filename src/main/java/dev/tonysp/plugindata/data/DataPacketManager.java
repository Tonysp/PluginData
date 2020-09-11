package dev.tonysp.plugindata.data;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import dev.tonysp.plugindata.PluginData;
import dev.tonysp.plugindata.data.packets.DataPacket;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class DataPacketManager {

    private static DataPacketManager instance;

    private final String redisIp, redisPassword;
    private final int redisPort;
    private JedisPool jedisPool;
    private boolean isConnected = false;

    private final BukkitTask reconnectTask, scanServersTask;
    private TaskChainFactory taskChainFactory;
    private boolean sendAndReceivePackets = true;

    public final String CLUSTER_ID;
    public final String SERVER_ID;
    public final Set<String> SERVERS;
    public final String GLOBAL_KEY_PREFIX = "plugindata-";
    private final int RECONNECT_TICKS = 1200;
    private final int PACKET_SEND_AND_RETRIEVE_INTERVAL;
    private final boolean CLEAR_OLD_PACKETS;
    public int PACKETS_PER_QUERY = 1000;

    private final HashMap<String, ConcurrentLinkedQueue<DataPacket>> packets = new HashMap<>();
    private final ConcurrentLinkedQueue<DataPacket> readyToSend = new ConcurrentLinkedQueue<>();

    public DataPacketManager (PluginData plugin, String redisIp, int redisPort, String redisPassword, String CLUSTER_ID, String SERVER_ID, int PACKET_SEND_AND_RETRIEVE_INTERVAL, boolean CLEAR_OLD_PACKETS) {
        instance = this;
        this.redisPassword = redisPassword;
        this.redisIp = redisIp;
        this.redisPort = redisPort;

        this.CLUSTER_ID = CLUSTER_ID;
        this.SERVER_ID = SERVER_ID;
        this.SERVERS = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.PACKET_SEND_AND_RETRIEVE_INTERVAL = PACKET_SEND_AND_RETRIEVE_INTERVAL;
        this.CLEAR_OLD_PACKETS = CLEAR_OLD_PACKETS;

        taskChainFactory = BukkitTaskChainFactory.create(plugin);

        reconnectTask = new BukkitRunnable() {
            @Override
            public void run() {
                isConnected = connect();

                if (isConnected) {
                    startPacketStream();
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

    private void startPacketStream () {
        registerServer();
        clearPackets();

        Bukkit.getServer().getScheduler().runTaskTimer(PluginData.getInstance(), () -> {
            if (!sendAndReceivePackets) {
                return;
            }
            sendAndReceivePackets = false;
            newChain()
              .asyncLast(task -> sendPackets())
              .asyncLast(task -> receivePackets())
              .delay(PACKET_SEND_AND_RETRIEVE_INTERVAL * 50, TimeUnit.MILLISECONDS)
              .asyncLast(task -> sendAndReceivePackets = true)
              .execute();
        }, 0L, 1L);
    }

    public void shutDown () {
        reconnectTask.cancel();
        scanServersTask.cancel();
        unregisterServer();
    }

    private String getReceivePacketsKey () {
        return GLOBAL_KEY_PREFIX + "-" + CLUSTER_ID + "-" + SERVER_ID;
    }

    private String getSendPacketsKeyPrefix () {
        return GLOBAL_KEY_PREFIX + "-" + CLUSTER_ID + "-";
    }

    private String getClusterServersSetKey () {
        return GLOBAL_KEY_PREFIX + "-" + CLUSTER_ID + "-servers";
    }

    private String getDataPacketKeyFromClass (String applicationId, Class<?> dataPacketClass) {
        return applicationId.concat("-").concat(dataPacketClass.getName());
    }

    public <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
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

    public <T> LinkedList<T> getReceivedPackets (String applicationId, Class<T> dataPacketClass) {
        String key = getDataPacketKeyFromClass(applicationId, dataPacketClass);
        if (!packets.containsKey(key)) {
            ConcurrentLinkedQueue<DataPacket> dataPackets = new ConcurrentLinkedQueue<>();
            packets.put(key, dataPackets);
            return new LinkedList<>();
        }

        LinkedList<T> dataPackets = new LinkedList<>();
        DataPacket dataPacket = packets.get(key).poll();
        while (dataPacket != null) {
            dataPackets.add((T) dataPacket);
            dataPacket = packets.get(key).poll();
        }
        return dataPackets;
    }

    public LinkedList<? extends DataPacket> getReceivedPackets (String applicationId) {
        LinkedList<DataPacket> dataPackets = new LinkedList<>();
        packets.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(applicationId))
                .forEach(entry -> {
            DataPacket dataPacket = entry.getValue().poll();
            while (dataPacket != null) {
                dataPackets.add(dataPacket);
                dataPacket = entry.getValue().poll();
            }
        });

        return dataPackets;
    }

    public void sendPacket (DataPacket message) {
        readyToSend.add(message);
    }

    private void clearPackets () {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(redisPassword);

            jedis.del(getReceivePacketsKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receivePackets () {
        List<String> stringList = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(redisPassword);

            String key = getReceivePacketsKey();
            Transaction transaction = jedis.multi();
            Response<List<String>> response = transaction.lrange(key, 0, PACKETS_PER_QUERY);
            transaction.del(key);
            transaction.exec();
            stringList = response.get();
            if (stringList.size() == 0) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String messageString : stringList) {
            try {
                DataPacket dataPacket = DataPacket.fromString(messageString);
                String key = getDataPacketKeyFromClass(dataPacket.getApplicationId(), dataPacket.getClass());
                if (packets.containsKey(key)) {
                    packets.get(key).add(dataPacket);
                } else {
                    ConcurrentLinkedQueue<DataPacket> packetQueue = new ConcurrentLinkedQueue<>();
                    packetQueue.add(dataPacket);
                    packets.put(key, packetQueue);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendPackets () {
        if (readyToSend.isEmpty()) {
            return;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(redisPassword);

            while (!readyToSend.isEmpty()) {
                DataPacket message = readyToSend.remove();
                String messageString = message.toString();

                if (messageString == null)
                    continue;

                String keyPrefix = getSendPacketsKeyPrefix();
                if (message.getReceivers().isPresent()) {
                    message.getReceivers().get()
                            .forEach(receiver -> jedis.rpush(keyPrefix + receiver, messageString));
                } else {
                    SERVERS.stream()
                            .filter(serverId -> !serverId.equalsIgnoreCase(SERVER_ID))
                            .forEach(receiver -> jedis.rpush(keyPrefix + receiver, messageString));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
