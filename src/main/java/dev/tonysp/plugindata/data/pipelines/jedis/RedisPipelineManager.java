package dev.tonysp.plugindata.data.pipelines.jedis;

import dev.tonysp.plugindata.data.packets.DataPacket;
import dev.tonysp.plugindata.data.pipelines.PipelineManager;
import redis.clients.jedis.JedisPool;

import java.util.AbstractQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class RedisPipelineManager implements PipelineManager {

    protected JedisPool jedisPool;
    protected final String redisPassword;

    AbstractQueue<DataPacket> readyToSend;

    protected RedisPipelineManager (String redisPassword) {
        this.redisPassword = redisPassword;
        this.readyToSend = new LinkedBlockingQueue<>();
    }

    public AbstractQueue<DataPacket> getReadyToSend () {
        return readyToSend;
    }

    @Override
    public void sendPacket (DataPacket dataPacket) {
        readyToSend.add(dataPacket);
    }

    public void setJedisPool (JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }
}
