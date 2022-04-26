package dev.tonysp.plugindata.data.runnables;

import dev.tonysp.plugindata.PluginData;
import dev.tonysp.plugindata.connections.redis.RedisConnection;
import dev.tonysp.plugindata.data.DataPacketManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class SubscriberRunnable implements Runnable {

    private final RedisConnection redisConnection;
    private final DataPacketManager dataPacketManager;
    private final JedisPubSub jedisPubSub;

    public SubscriberRunnable (RedisConnection redisConnection, DataPacketManager dataPacketManager, JedisPubSub jedisPubSub) {
        this.redisConnection = redisConnection;
        this.dataPacketManager = dataPacketManager;
        this.jedisPubSub = jedisPubSub;
    }

    @Override
    public void run() {
        try (Jedis jedis = redisConnection.getResource()) {
            String channelName = dataPacketManager.getReceivePacketsKey();
            PluginData.log("Subscribing to " + channelName);
            jedis.subscribe(jedisPubSub, channelName);
            PluginData.log("... returned from subscribe()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
