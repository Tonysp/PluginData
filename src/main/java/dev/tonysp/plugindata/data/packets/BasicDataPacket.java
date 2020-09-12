package dev.tonysp.plugindata.data.packets;

import dev.tonysp.plugindata.data.DataPacketManager;

import java.time.LocalDateTime;
import java.util.*;

public class BasicDataPacket extends DataPacket {

    public static Builder newBuilder (String applicationId) {
        return new Builder(applicationId);
    }

    public static class Builder {

        private final String applicationId;
        private HashSet<String> receivers;

        private String action;
        private int integer;
        private int integer2;
        private long longValue;
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

        public Builder (String applicationId) {
            this.applicationId = applicationId;
        }

        public BasicDataPacket buildPacket () {
            return new BasicDataPacket(
                    applicationId,
                    DataPacketManager.getInstance().SERVER_ID,
                    receivers,
                    action,
                    integer,
                    integer2,
                    longValue,
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

        public Builder addReceiver (String serverName) {
            if (this.receivers == null) {
                this.receivers = new HashSet<>();
            }
            this.receivers.add(serverName);
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

        public Builder integer (int integer) {
            this.integer = integer;
            return this;
        }

        public Builder integer2 (int integer2) {
            this.integer2 = integer2;
            return this;
        }

        public Builder longValue (long longValue) {
            this.longValue = longValue;
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

    private final String action;
    private final int integer;
    private final int integer2;
    private final long longValue;
    private final double doubleValue;
    private final String string;
    private final String string2;
    private final boolean bool;
    private final boolean bool2;
    private final LocalDateTime dateTime;

    private final ArrayList<String> stringList;
    private final HashMap<String, String> stringData;
    private final HashMap<String, Integer> intData;
    private final HashMap<String, Double> doubleData;
    private final HashMap<String, UUID> uuidData;
    private final HashMap<UUID, String> uuidDataInverted;

    private BasicDataPacket (
            String applicationId,
            String sender,
            HashSet<String> receivers,
            String action,
            int integer,
            int integer2,
            long longValue,
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
        super(applicationId, receivers);
        this.setSender(sender);
        this.action = action;
        this.integer = integer;
        this.integer2 = integer2;
        this.longValue = longValue;
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

    public long getLong () {
        return longValue;
    }

    public double getDouble() {
        return doubleValue;
    }

    public String getAction() {
        return action;
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
}
