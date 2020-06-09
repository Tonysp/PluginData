package cz.goldminer.tonysp.plugindata.data;

import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class DataPacket implements Serializable {

    public static class Builder {

        private String pluginId = "";
        private String messageId = "";
        private String fromServer = DataPacketManager.getInstance().SERVER_ID;
        private String toServer = "all";

        private String action;
        private int integer;
        private int integer2;
        private double doubleValue;
        private String string;
        private String string2;
        private boolean bool;
        private boolean bool2;
        private LocalDateTime dateTime;

        private ArrayList<String> stringList = new ArrayList<>();
        private HashMap<String, String> stringData = new HashMap<>();
        private HashMap<String, Integer> intData = new HashMap<>();
        private HashMap<String, Double> doubleData = new HashMap<>();
        private HashMap<String, UUID> uuidData = new HashMap<>();
        private HashMap<UUID, String> uuidDataInverted = new HashMap<>();

        public Builder () {}

        public DataPacket buildMessage () {
            return new DataPacket(
                    pluginId,
                    messageId,
                    fromServer,
                    toServer,
                    action,
                    integer,
                    integer2,
                    doubleValue,
                    string,
                    string2,
                    bool,
                    bool2,
                    dateTime,
                    stringList,
                    stringData,
                    intData,
                    doubleData,
                    uuidData,
                    uuidDataInverted
            );
        }

        public Builder pluginId (String pluginId) {
            this.pluginId = pluginId;
            return this;
        }

        public Builder id (String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder dateTime (LocalDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public Builder action (String action) {
            this.action = action;
            return this;
        }

        public Builder toServer (String toServer) {
            this.toServer = toServer;
            return this;
        }

        public Builder integer (int integer) {
            this.integer = integer;
            return this;
        }

        public Builder integer2 (int integer2) {
            this.integer2 = integer2;
            return this;
        }

        public Builder doubleValue (double doubleValue) {
            this.doubleValue = doubleValue;
            return this;
        }

        public Builder string (String string) {
            this.string = string;
            return this;
        }

        public Builder string2 (String string2) {
            this.string2 = string2;
            return this;
        }

        public Builder stringList (ArrayList<String> stringList) {
            this.stringList = stringList;
            return this;
        }

        public Builder bool (boolean bool) {
            this.bool = bool;
            return this;
        }

        public Builder bool2 (boolean bool2) {
            this.bool2 = bool2;
            return this;
        }

        public Builder stringData (HashMap<String, String> stringData) {
            this.stringData = stringData;
            return this;
        }

        public Builder intData (HashMap<String, Integer> intData) {
            this.intData = intData;
            return this;
        }

        public Builder doubleData (HashMap<String, Double> doubleData) {
            this.doubleData = doubleData;
            return this;
        }

        public Builder uuidData (HashMap<String, UUID> uuidData) {
            this.uuidData = uuidData;
            return this;
        }

        public Builder uuidDataInverted (HashMap<UUID, String> uuidDataInverted) {
            this.uuidDataInverted = uuidDataInverted;
            return this;
        }
    }

    private String pluginId;
    private String messageId;
    private String fromServer;
    private String toServer;

    private String action;
    private int integer;
    private int integer2;
    private double doubleValue;
    private String string;
    private String string2;
    private boolean bool;
    private boolean bool2;
    private LocalDateTime dateTime;

    private ArrayList<String> stringList;
    private HashMap<String, String> stringData;
    private HashMap<String, Integer> intData;
    private HashMap<String, Double> doubleData;
    private HashMap<String, UUID> uuidData;
    private HashMap<UUID, String> uuidDataInverted;

    private DataPacket (
            String pluginId,
            String messageId,
            String fromServer,
            String toServer,
            String action,
            int integer,
            int integer2,
            double doubleValue,
            String string,
            String string2,
            boolean bool,
            boolean bool2,
            LocalDateTime dateTime,
            ArrayList<String> stringList,
            HashMap<String, String> stringData,
            HashMap<String, Integer> intData,
            HashMap<String, Double> doubleData,
            HashMap<String, UUID> uuidData,
            HashMap<UUID, String> uuidDataInverted
    ) {
        this.pluginId = pluginId;
        this.messageId = messageId;
        this.fromServer = fromServer;
        this.toServer = toServer;
        this.action = action;
        this.integer = integer;
        this.integer2 = integer2;
        this.doubleValue = doubleValue;
        this.string = string;
        this.string2 = string2;
        this.bool = bool;
        this.bool2 = bool2;
        this.dateTime = dateTime;
        this.stringList = stringList;
        this.stringData = stringData;
        this.intData = intData;
        this.doubleData = doubleData;
        this.uuidData = uuidData;
        this.uuidDataInverted = uuidDataInverted;
    }

    @Override
    public String toString () {
        String message;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream( baos );
            oos.writeObject(this);
            oos.close();
            message = new String( Base64Coder.encode(baos.toByteArray()) );
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }

        return message;
    }

    @Override
    public int hashCode () {
        return Objects.hash(
                pluginId,
                messageId,
                action,
                fromServer,
                toServer,
                integer,
                integer2,
                doubleValue,
                string,
                string2,
                bool,
                bool2,
                dateTime,
                stringList,
                stringData,
                intData,
                doubleData,
                uuidData,
                uuidDataInverted
        );
    }

    public static DataPacket fromString (String s ) throws IOException, ClassNotFoundException {
        byte [] data = Base64Coder.decode( s );
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        DataPacket o  = (DataPacket) ois.readObject();
        ois.close();
        return o;
    }

    public String getPluginId () {
        return pluginId;
    }

    public void send () {
        DataPacketManager.getInstance().sendPacket(this);
    }

    public LocalDateTime getDateTime () {
        return dateTime;
    }

    public ArrayList<String> getStringList () {
        return stringList;
    }

    public HashMap<UUID, String> getUuidDataInverted() {
        return uuidDataInverted;
    }

    public boolean getBool () {
        return bool;
    }

    public boolean getBool2 () {
        return bool2;
    }

    public String getString() {
        return string;
    }

    public HashMap<String, UUID> getUuidData() {
        return uuidData;
    }

    public int getInteger() {
        return integer;
    }

    public int getInteger2() {
        return integer2;
    }

    public double getDouble() {
        return doubleValue;
    }

    public String getAction() {
        return action;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getFromServer() {
        return fromServer;
    }

    public String getToServer() {
        return toServer;
    }

    public HashMap<String, String> getStringData() {
        return stringData;
    }

    public HashMap<String, Integer> getIntData() {
        return intData;
    }

    public HashMap<String, Double> getDoubleData() {
        return doubleData;
    }

    public String getString2 () {
        return string2;
    }

    public void setFromServer (String fromServer) {
        this.fromServer = fromServer;
    }
}
