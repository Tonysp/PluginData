package dev.tonysp.plugindata.data.events;

import dev.tonysp.plugindata.data.packets.DataPacket;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DataPacketReceiveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private DataPacket dataPacket;

    public DataPacketReceiveEvent (boolean isAsync, DataPacket dataPacket) {
        super(isAsync);
        this.dataPacket = dataPacket;
    }

    public DataPacketReceiveEvent (DataPacket dataPacket) {
        this.dataPacket = dataPacket;
    }

    public DataPacket getDataPacket () {
        return dataPacket;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
