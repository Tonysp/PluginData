package dev.tonysp.plugindata.data.pipelines.jedis;

import dev.tonysp.plugindata.data.events.DataPacketReceiveEvent;
import dev.tonysp.plugindata.data.packets.DataPacket;
import dev.tonysp.plugindata.data.runnables.PublisherRunnable;
import dev.tonysp.plugindata.data.runnables.SubscriberRunnable;
import org.bukkit.Bukkit;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;

public class PubSubPipelineManager extends RedisPipelineManager {

    private static PubSubPipelineManager instance;

    private JedisPubSub jedisPubSub;
    private Thread publisherThread, subscriberThread;

    public PubSubPipelineManager (String redisPassword) {
        super(redisPassword);
        instance = this;
    }

    public static PubSubPipelineManager getInstance () {
        return instance;
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
        PublisherRunnable publisherRunnable = new PublisherRunnable(jedisPool, redisPassword);
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

        SubscriberRunnable subscriberRunnable = new SubscriberRunnable(jedisPool, redisPassword, jedisPubSub);
        subscriberThread = new Thread(subscriberRunnable);
        subscriberThread.start();

        return jedisPubSub;
    }
}
