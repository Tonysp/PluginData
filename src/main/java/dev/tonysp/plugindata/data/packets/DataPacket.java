package dev.tonysp.plugindata.data.packets;

import dev.tonysp.plugindata.data.DataPacketManager;
import dev.tonysp.plugindata.data.pipelines.Pipeline;
import dev.tonysp.plugindata.data.pipelines.jedis.PubSubPipelineManager;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public abstract class DataPacket implements Serializable {

    private Pipeline pipeline;
    private final String applicationId;
    private String sender;
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
        this.pipeline = Pipeline.PUBSUB;
        this.pipeline.getPipelineManager().sendPacket(this);
    }

    public void send (Pipeline pipeline) {
        this.pipeline = pipeline;
        this.pipeline.getPipelineManager().sendPacket(this);
    }

    public DataPacket (String applicationId, Set<String> receivers) {
        this.applicationId = applicationId;
        this.sender = DataPacketManager.getInstance().SERVER_ID;
        this.receivers = receivers;
    }

    public DataPacket (String applicationId) {
        this.applicationId = applicationId;
        this.sender = DataPacketManager.getInstance().SERVER_ID;
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
