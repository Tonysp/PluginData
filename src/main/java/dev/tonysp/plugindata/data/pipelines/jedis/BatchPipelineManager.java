package dev.tonysp.plugindata.data.pipelines.jedis;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import dev.tonysp.plugindata.PluginData;
import dev.tonysp.plugindata.connections.redis.RedisConnection;
import dev.tonysp.plugindata.data.DataPacketManager;
import dev.tonysp.plugindata.data.packets.DataPacket;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class BatchPipelineManager extends RedisPipelineManager {

    private JavaPlugin plugin;

    private final boolean CLEAR_OLD_PACKETS;
    private final int PACKET_SEND_AND_RETRIEVE_INTERVAL;
    public final int PACKETS_PER_QUERY = 1000;

    private boolean sendAndReceivePackets = true;
    private final TaskChainFactory taskChainFactory;
    private final Map<String, ConcurrentLinkedQueue<DataPacket>> received = new HashMap<>();
    private BukkitTask mainTask;

    public BatchPipelineManager (JavaPlugin plugin, RedisConnection redisConnection, DataPacketManager dataPacketManager, boolean clear_old_packets, int packet_send_and_retrieve_interval) {
        super(redisConnection, dataPacketManager);
        this.plugin = plugin;
        CLEAR_OLD_PACKETS = clear_old_packets;
        PACKET_SEND_AND_RETRIEVE_INTERVAL = packet_send_and_retrieve_interval;
        this.taskChainFactory = BukkitTaskChainFactory.create(plugin);
    }

    @Override
    public void startUp () {
        if (CLEAR_OLD_PACKETS) {
            clearPackets();
        }
        sendAndReceivePackets = true;

        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!sendAndReceivePackets) {
                    return;
                }
                sendAndReceivePackets = false;
                taskChainFactory.newChain()
                        .asyncLast(task -> sendPackets())
                        .asyncLast(task -> receivePackets())
                        .delay(PACKET_SEND_AND_RETRIEVE_INTERVAL * 50, TimeUnit.MILLISECONDS)
                        .asyncLast(task -> sendAndReceivePackets = true)
                        .execute();
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @Override
    public void shutDown () {
        if (mainTask != null) {
            mainTask.cancel();
        }
    }

    private void clearPackets () {
        try (Jedis jedis = redisConnection.getResource()) {
            jedis.del(dataPacketManager.getReceivePacketsKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPackets () {
        if (readyToSend.isEmpty()) {
            return;
        }

        try (Jedis jedis = redisConnection.getResource()) {
            while (!readyToSend.isEmpty()) {
                DataPacket message = readyToSend.remove();
                String messageString = message.toString();

                if (messageString == null)
                    continue;

                String keyPrefix = dataPacketManager.getSendPacketsKeyPrefix();

                if (message.getReceivers().isPresent()) {
                    message.getReceivers().get().forEach(receiver -> jedis.rpush(keyPrefix + receiver, messageString));
                } else {
                    dataPacketManager.SERVERS.stream()
                            .filter(serverId -> !serverId.equalsIgnoreCase(dataPacketManager.SERVER_ID))
                            .forEach(receiver -> jedis.rpush(keyPrefix + receiver, messageString));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receivePackets () {
        List<String> stringList = new ArrayList<>();
        try (Jedis jedis = redisConnection.getResource()) {
            String key = dataPacketManager.getReceivePacketsKey();
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
                String key = dataPacketManager.getDataPacketKeyFromClass(dataPacket.getApplicationId(), dataPacket.getClass());
                if (received.containsKey(key)) {
                    received.get(key).add(dataPacket);
                } else {
                    ConcurrentLinkedQueue<DataPacket> packetQueue = new ConcurrentLinkedQueue<>();
                    packetQueue.add(dataPacket);
                    received.put(key, packetQueue);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public <T> LinkedList<T> getReceivedPackets (String applicationId, Class<T> dataPacketClass) {
        String key = dataPacketManager.getDataPacketKeyFromClass(applicationId, dataPacketClass);
        if (!received.containsKey(key)) {
            ConcurrentLinkedQueue<DataPacket> dataPackets = new ConcurrentLinkedQueue<>();
            received.put(key, dataPackets);
            return new LinkedList<>();
        }

        LinkedList<T> dataPackets = new LinkedList<>();
        DataPacket dataPacket = received.get(key).poll();
        while (dataPacket != null) {
            dataPackets.add((T) dataPacket);
            dataPacket = received.get(key).poll();
        }
        return dataPackets;
    }

    public LinkedList<? extends DataPacket> getReceivedPackets (String applicationId) {
        LinkedList<DataPacket> dataPackets = new LinkedList<>();
        received.entrySet().stream()
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
}
