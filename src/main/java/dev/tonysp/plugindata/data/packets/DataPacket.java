package dev.tonysp.plugindata.data.packets;

import dev.tonysp.plugindata.data.DataPacketManager;
import dev.tonysp.plugindata.data.Pipeline;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.HashSet;
import java.util.Optional;

public abstract class DataPacket implements Serializable {

    private Pipeline pipeline;
    private final String applicationId;
    private String sender;
    private HashSet<String> receivers;

    public String getApplicationId () {
        return applicationId;
    }

    public String getSender () {
        return sender;
    }

    public Optional<HashSet<String>> getReceivers () {
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
        DataPacketManager.getInstance().sendPacket(this);
    }

    public void send (Pipeline pipeline) {
        this.pipeline = pipeline;
        DataPacketManager.getInstance().sendPacket(this);
    }

    public DataPacket (String applicationId, HashSet<String> receivers) {
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
