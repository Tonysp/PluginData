package cz.goldminer.tonysp.plugindata.data;

import cz.goldminer.tonysp.plugindata.PluginData;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataPacketManager {

    private static DataPacketManager instance;

    private final String redisIp, redisPassword;
    private final int redisPort;
    private JedisPool jedisPool;
    private final int reconnectTicks = 1200;
    private final BukkitTask reconnectTask;

    public final String SERVER_ID;
    public final List<String> SERVERS;
    public final String KEY_PREFIX = "plugindata-";
    public int PACKETS_PER_QUERY = 1000;
    private boolean isConnected = false;

    private final HashMap<String, ConcurrentLinkedQueue<DataPacket>> packets = new HashMap<>();
    private final ConcurrentLinkedQueue<DataPacket> readyToSend = new ConcurrentLinkedQueue<>();

    public DataPacketManager (PluginData plugin, String redisIp, int redisPort, String redisPassword, String SERVER_ID, List<String> SERVERS) {
        instance = this;
        this.redisPassword = redisPassword;
        this.redisIp = redisIp;
        this.redisPort = redisPort;

        this.SERVER_ID = SERVER_ID;
        this.SERVERS = SERVERS;
        if (!this.SERVERS.contains(this.SERVER_ID)) {
            this.SERVERS.add(this.SERVER_ID);
        }

        reconnectTask = new BukkitRunnable() {
            @Override
            public void run(){
                if (isConnected) {
                    startPacketStream();
                    this.cancel();
                    return;
                }

                isConnected = connect();
            }
        }.runTaskTimer(plugin, 0, reconnectTicks);
    }

    public static DataPacketManager getInstance () {
        return instance;
    }

    public void shutDown () {
        reconnectTask.cancel();
    }

    private boolean connect () {
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(DataPacketManager.class.getClassLoader());
        try {
            jedisPool = new JedisPool(redisIp, redisPort);
            jedisPool.getResource();
            PluginData.log("Successfully connected to Redis!");
        } catch (Exception e) {
            PluginData.log("Error while connecting to Redis! Trying again in " + (reconnectTicks / 20) + " seconds...");
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(previous);

        return true;
    }

    private void startPacketStream () {
        clearPackets();

        Bukkit.getServer().getScheduler().runTaskTimer(PluginData.getInstance(), () -> {
            sendPackets();
            receivePackets();
        }, 0L, 5L);
    }

    public LinkedList<DataPacket> getReceivedPackets (String pluginId) {
        if (!packets.containsKey(pluginId)) {
            ConcurrentLinkedQueue<DataPacket> dataPackets = new ConcurrentLinkedQueue<>();
            packets.put(pluginId, dataPackets);
            return new LinkedList<>();
        }

        LinkedList<DataPacket> dataPackets = new LinkedList<>();
        DataPacket dataPacket = packets.get(pluginId).poll();
        while (dataPacket != null) {
            dataPackets.add(dataPacket);
            dataPacket = packets.get(pluginId).poll();
        }
        return dataPackets;
    }

    public void sendPacket (DataPacket message) {
        readyToSend.add(message);
    }

    private void clearPackets () {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(redisPassword);

            jedis.del(KEY_PREFIX + SERVER_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receivePackets () {
        List<String> stringList = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(redisPassword);

            Transaction transaction = jedis.multi();
            Response<List<String>> response = transaction.lrange(KEY_PREFIX + SERVER_ID, 0, PACKETS_PER_QUERY);
            transaction.del(KEY_PREFIX + SERVER_ID);
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
                if (packets.containsKey(dataPacket.getPluginId())) {
                    packets.get(dataPacket.getPluginId()).add(dataPacket);
                } else {
                    ConcurrentLinkedQueue<DataPacket> packetQueue = new ConcurrentLinkedQueue<>();
                    packetQueue.add(dataPacket);
                    packets.put(dataPacket.getPluginId(), packetQueue);
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

                if (message.getReceivers().isPresent()) {
                    message.getReceivers().get()
                            .forEach(receiver -> jedis.rpush(KEY_PREFIX + receiver, messageString));
                } else {
                    for (String serverId : SERVERS) {
                        if (!serverId.equalsIgnoreCase(SERVER_ID))
                            jedis.rpush(KEY_PREFIX + serverId, messageString);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
