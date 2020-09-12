package dev.tonysp.plugindata.data.runnables;

import dev.tonysp.plugindata.data.DataPacketManager;
import dev.tonysp.plugindata.data.packets.DataPacket;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class PublisherRunnable implements Runnable {

    private final JedisPool jedisPool;
    private final String redisPassword;

    public PublisherRunnable (JedisPool jedisPool, String redisPassword) {
        this.jedisPool = jedisPool;
        this.redisPassword = redisPassword;
    }

    @Override
    public void run() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(redisPassword);

            while (true) {
                DataPacket message = DataPacketManager.getInstance().getReadyToSendPubSub().take();
                String messageString = message.toString();

                if (messageString == null)
                    continue;

                String keyPrefix = DataPacketManager.getInstance().getSendPacketsKeyPrefix();

                if (message.getReceivers().isPresent()) {
                    message.getReceivers().get().forEach(receiver -> jedis.publish(keyPrefix + receiver, messageString));
                } else {
                    DataPacketManager.getInstance().SERVERS.stream()
                            .filter(serverId -> !serverId.equalsIgnoreCase(DataPacketManager.getInstance().SERVER_ID))
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
