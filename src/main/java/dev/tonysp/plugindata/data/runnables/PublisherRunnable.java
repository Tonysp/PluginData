package dev.tonysp.plugindata.data.runnables;

import dev.tonysp.plugindata.connections.redis.RedisConnection;
import dev.tonysp.plugindata.data.DataPacketManager;
import dev.tonysp.plugindata.data.packets.DataPacket;
import dev.tonysp.plugindata.data.pipelines.jedis.PubSubPipelineManager;
import redis.clients.jedis.Jedis;

import java.util.concurrent.LinkedBlockingQueue;

public class PublisherRunnable implements Runnable {

    private final RedisConnection redisConnection;
    private final PubSubPipelineManager pubSubPipelineManager;
    private final DataPacketManager dataPacketManager;

    public PublisherRunnable (RedisConnection redisConnection, PubSubPipelineManager pubSubPipelineManager, DataPacketManager dataPacketManager) {
        this.redisConnection = redisConnection;
        this.pubSubPipelineManager = pubSubPipelineManager;
        this.dataPacketManager = dataPacketManager;
    }

    @Override
    public void run() {
        try (Jedis jedis = redisConnection.getResource()) {
            Thread.sleep(2000);

            while (true) {
                DataPacket message = ((LinkedBlockingQueue<DataPacket>) pubSubPipelineManager.getReadyToSend()).take();
                String messageString = message.toString();

                if (messageString == null)
                    continue;

                String keyPrefix = dataPacketManager.getSendPacketsKeyPrefix();

                if (message.getReceivers().isPresent()) {
                    message.getReceivers().get().forEach(receiver -> jedis.publish(keyPrefix + receiver, messageString));
                } else {
                    dataPacketManager.SERVERS.stream()
                            .filter(serverId -> !serverId.equalsIgnoreCase(dataPacketManager.SERVER_ID))
                            .forEach(receiver -> jedis.publish(keyPrefix + receiver, messageString));
                }
            }
        } catch (InterruptedException ignored) {
            // the thread will be interrupted when the plugin shuts down
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
