package dev.tonysp.plugindata.data.pipelines.jedis;

import dev.tonysp.plugindata.connections.redis.RedisConnection;
import dev.tonysp.plugindata.data.DataPacketManager;
import dev.tonysp.plugindata.data.events.DataPacketReceiveEvent;
import dev.tonysp.plugindata.data.packets.DataPacket;
import dev.tonysp.plugindata.data.runnables.PublisherRunnable;
import dev.tonysp.plugindata.data.runnables.SubscriberRunnable;
import org.bukkit.Bukkit;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;

public class PubSubPipelineManager extends RedisPipelineManager {

    private JedisPubSub jedisPubSub;
    private Thread publisherThread, subscriberThread;

    public PubSubPipelineManager (RedisConnection redisConnection, DataPacketManager dataPacketManager) {
        super(redisConnection, dataPacketManager);
    }

    @Override
    public void startUp () {
        startSubscriber();
        startPublisher();
    }

    @Override
    public void shutDown () {
        if (jedisPubSub != null) {
            jedisPubSub.unsubscribe();
        }

        if (subscriberThread != null) {
            try {
                subscriberThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (publisherThread != null) {
            publisherThread.interrupt();
            try {
                publisherThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void startPublisher () {
        PublisherRunnable publisherRunnable = new PublisherRunnable(redisConnection, this, dataPacketManager);
        publisherThread = new Thread(publisherRunnable);
        publisherThread.start();
    }

    public JedisPubSub startSubscriber () {
        jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String messageString) {
                try {
                    DataPacket dataPacket = DataPacket.fromString(messageString);
                    DataPacketReceiveEvent event = new DataPacketReceiveEvent(true, dataPacket);
                    Bukkit.getPluginManager().callEvent(event);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };

        SubscriberRunnable subscriberRunnable = new SubscriberRunnable(redisConnection, dataPacketManager, jedisPubSub);
        subscriberThread = new Thread(subscriberRunnable);
        subscriberThread.start();

        return jedisPubSub;
    }
}
