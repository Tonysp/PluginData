package cz.goldminer.tonysp.plugindata.data;

import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

public abstract class DataPacket implements Serializable {

    private final String pluginId;
    private final String messageId;
    private String sender;
    private HashSet<String> receivers;

    public String getPluginId () {
        return pluginId;
    }

    public String getMessageId () {
        return messageId;
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

    public DataPacket (String pluginId, String messageId, HashSet<String> receivers) {
        this.pluginId = pluginId;
        this.messageId = messageId;
        this.sender = DataPacketManager.getInstance().SERVER_ID;
        this.receivers = receivers;
    }

    public DataPacket (String pluginId, String messageId) {
        this.pluginId = pluginId;
        this.messageId = messageId;
        this.sender = DataPacketManager.getInstance().SERVER_ID;
    }

    public static DataPacket fromString (String s ) throws IOException, ClassNotFoundException {
        byte [] data = Base64Coder.decode( s );
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        DataPacket o  = (DataPacket) ois.readObject();
        ois.close();
        return o;
    }
}
