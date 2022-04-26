package dev.tonysp.plugindata.connections;

public abstract class Connection {

    private String name;
    private ConnectionType connectionType;

    public Connection (String name, ConnectionType connectionType) {
        this.name = name.toLowerCase();
        this.connectionType = connectionType;
    }

    public abstract boolean test ();
    public abstract void shutDown ();

    public String getName () {
        return name;
    }
}
