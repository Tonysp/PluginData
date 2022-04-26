package dev.tonysp.plugindata.data.packets;

import dev.tonysp.plugindata.PluginData;
import dev.tonysp.plugindata.data.DataPacketManager;
import dev.tonysp.plugindata.data.pipelines.Pipeline;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.Optional;
import java.util.Set;

public abstract class DataPacket implements Serializable {

    private transient DataPacketManager dataPacketManager;
    private Pipeline pipeline;
    private final String applicationId;
    private String sender, serverId;
    private Set<String> receivers;

    public String getApplicationId () {
        return applicationId;
    }

    public String getSender () {
        return sender;
    }

    public Optional<Set<String>> getReceivers () {
        return Optional.ofNullable(receivers);
    }

    public void setSender (String sender) {
        this.sender = sender;
    }

    public Pipeline getPipeline () {
        return pipeline;
    }

    public void send () {
        send(Pipeline.PUBSUB);
    }

    public void send (Pipeline pipeline) {
        this.pipeline = pipeline;
        if (dataPacketManager == null) {
            this.pipeline.getPipelineManager().sendPacket(this);
        } else {
            this.dataPacketManager.getPipelineManagers().get(this.pipeline).sendPacket(this);
        }
    }

    public DataPacket (String applicationId, Set<String> receivers) {
        this.applicationId = applicationId;
        this.sender = PluginData.getDataPacketManager().SERVER_ID;
        this.receivers = receivers;
    }

    public DataPacket (String applicationId) {
        this.applicationId = applicationId;
        this.sender = PluginData.getDataPacketManager().SERVER_ID;
    }

    public DataPacket (DataPacketManager dataPacketManager, String applicationId, Set<String> receivers) {
        this.applicationId = applicationId;
        this.dataPacketManager = dataPacketManager;
        this.sender = dataPacketManager.SERVER_ID;
        this.receivers = receivers;
    }

    public DataPacket (DataPacketManager dataPacketManager, String applicationId) {
        this.applicationId = applicationId;
        this.dataPacketManager = dataPacketManager;
        this.sender = dataPacketManager.SERVER_ID;
    }

    public static DataPacket fromString (String string) throws IOException, ClassNotFoundException {
        byte [] data = Base64Coder.decode(string);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data) );
        DataPacket o  = (DataPacket) ois.readObject();
        ois.close();
        return o;
    }

    @Override
    public String toString () {
        String message;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();
            message = new String( Base64Coder.encode(baos.toByteArray()) );
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }

        return message;
    }
}
