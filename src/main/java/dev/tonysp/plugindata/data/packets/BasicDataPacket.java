package dev.tonysp.plugindata.data.packets;

import java.time.LocalDateTime;
import java.util.*;

public class BasicDataPacket extends DataPacket {

    public static Builder newBuilder (String applicationId) {
        return new Builder(applicationId);
    }

    public static class Builder {

        private final String applicationId;
        private HashSet<String> receivers;

        private int integer;
        private int integer2;
        private long longValue;
        private double doubleValue;
        private String string;
        private String string2;
        private boolean bool;
        private boolean bool2;
        private LocalDateTime dateTime;

        private List<String> stringList = new ArrayList<>();
        private Map<String, String> stringData = new HashMap<>();
        private Map<String, Integer> intData = new HashMap<>();
        private Map<String, Double> doubleData = new HashMap<>();
        private Map<String, UUID> uuidData = new HashMap<>();
        private Map<UUID, String> uuidDataInverted = new HashMap<>();

        public Builder (String applicationId) {
            this.applicationId = applicationId;
        }

        public BasicDataPacket buildPacket () {
            return new BasicDataPacket(
                    applicationId,
                    receivers,
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

        public Builder stringList (List<String> stringList) {
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

        public Builder stringData (Map<String, String> stringData) {
            this.stringData = stringData;
            return this;
        }

        public Builder intData (Map<String, Integer> intData) {
            this.intData = intData;
            return this;
        }

        public Builder doubleData (Map<String, Double> doubleData) {
            this.doubleData = doubleData;
            return this;
        }

        public Builder uuidData (Map<String, UUID> uuidData) {
            this.uuidData = uuidData;
            return this;
        }

        public Builder uuidDataInverted (Map<UUID, String> uuidDataInverted) {
            this.uuidDataInverted = uuidDataInverted;
            return this;
        }
    }

    private final int integer;
    private final int integer2;
    private final long longValue;
    private final double doubleValue;
    private final String string;
    private final String string2;
    private final boolean bool;
    private final boolean bool2;
    private final LocalDateTime dateTime;

    private final List<String> stringList;
    private final Map<String, String> stringData;
    private final Map<String, Integer> intData;
    private final Map<String, Double> doubleData;
    private final Map<String, UUID> uuidData;
    private final Map<UUID, String> uuidDataInverted;

    private BasicDataPacket (
            String applicationId,
            HashSet<String> receivers,
            int integer,
            int integer2,
            long longValue,
            double doubleValue,
            String string,
            String string2,
            boolean bool,
            boolean bool2,
            LocalDateTime dateTime,
            List<String> stringList,
            Map<String, String> stringData,
            Map<String, Integer> intData,
            Map<String, Double> doubleData,
            Map<String, UUID> uuidData,
            Map<UUID, String> uuidDataInverted
    ) {
        super(applicationId, receivers);
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

    public List<String> getStringList () {
        return stringList;
    }

    public Map<UUID, String> getUuidDataInverted() {
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

    public Map<String, UUID> getUuidData() {
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

    public Map<String, String> getStringData() {
        return stringData;
    }

    public Map<String, Integer> getIntData() {
        return intData;
    }

    public Map<String, Double> getDoubleData() {
        return doubleData;
    }

    public String getString2 () {
        return string2;
    }
}
