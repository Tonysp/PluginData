package dev.tonysp.plugindata.data.pipelines;

import dev.tonysp.plugindata.data.packets.DataPacket;

public interface PipelineManager {

    void startUp ();
    void shutDown ();
    void sendPacket (DataPacket dataPacket);

}
