package dev.tonysp.plugindata.data.pipelines.jedis;

import dev.tonysp.plugindata.connections.redis.RedisConnection;
import dev.tonysp.plugindata.data.DataPacketManager;
import dev.tonysp.plugindata.data.packets.DataPacket;
import dev.tonysp.plugindata.data.pipelines.PipelineManager;

import java.util.AbstractQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class RedisPipelineManager implements PipelineManager {

    protected final RedisConnection redisConnection;
    protected final DataPacketManager dataPacketManager;
    AbstractQueue<DataPacket> readyToSend;

    protected RedisPipelineManager (RedisConnection redisConnection, DataPacketManager dataPacketManager) {
        this.redisConnection = redisConnection;
        this.dataPacketManager = dataPacketManager;
        this.readyToSend = new LinkedBlockingQueue<>();
    }

    public AbstractQueue<DataPacket> getReadyToSend () {
        return readyToSend;
    }

    @Override
    public void sendPacket (DataPacket dataPacket) {
        readyToSend.add(dataPacket);
    }
}
