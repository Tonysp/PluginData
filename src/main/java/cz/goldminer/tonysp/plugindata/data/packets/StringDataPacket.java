package cz.goldminer.tonysp.plugindata.data.packets;

import java.util.HashSet;

public class StringDataPacket extends DataPacket {

    public static Builder newBuilder (String applicationId) {
        return new Builder(applicationId);
    }

    public static class Builder {

        private final String applicationId;
        private HashSet<String> receivers;
        private String string;

        public Builder (String applicationId) {
            this.applicationId = applicationId;
        }

        public StringDataPacket buildPacket () {
            return new StringDataPacket(applicationId, receivers, string);
        }

        public Builder addReceiver (String serverName) {
            if (this.receivers == null) {
                this.receivers = new HashSet<>();
            }
            this.receivers.add(serverName);
            return this;
        }

        public Builder setString (String string) {
            this.string = string;
            return this;
        }
    }

    private final String string;

    public StringDataPacket (String applicationId, HashSet<String> receivers, String string) {
        super(applicationId, receivers);
        this.string = string;
    }

    public String getString () {
        return string;
    }
}
