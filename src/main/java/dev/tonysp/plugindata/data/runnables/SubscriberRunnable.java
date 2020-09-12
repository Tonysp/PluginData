package dev.tonysp.plugindata.data.runnables;

import dev.tonysp.plugindata.PluginData;
import dev.tonysp.plugindata.data.DataPacketManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

public class SubscriberRunnable implements Runnable {

    private final JedisPool jedisPool;
    private final String redisPassword;
    private final JedisPubSub jedisPubSub;

    public SubscriberRunnable (JedisPool jedisPool, String redisPassword, JedisPubSub jedisPubSub) {
        this.jedisPool = jedisPool;
        this.redisPassword = redisPassword;
        this.jedisPubSub = jedisPubSub;
    }

    @Override
    public void run() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(redisPassword);
            String channelName = DataPacketManager.getInstance().getReceivePacketsKey();
            PluginData.log("Subscribing to " + channelName);
            jedis.subscribe(jedisPubSub, channelName);
            PluginData.log("... returned from subscribe()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
