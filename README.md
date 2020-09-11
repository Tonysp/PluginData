# PluginData
Bukkit plugin for easy database connection management and sending data between plugins.

## Database connection manager
1. Set up any number of MySQL databases in the config.yml
2. Get connection using the getConnection() method (thread safe).
```java
DatabaseManager.getInstance().getConnection("name");
```

## DataPacket API for sending data between servers
You can use the provided [BasicDataPacket](https://github.com/Tonysp/PluginData/blob/master/src/main/java/cz/goldminer/tonysp/plugindata/data/packets/BasicDataPacket.java), or make your own implementation (extend) of the [DataPacket](https://github.com/Tonysp/PluginData/blob/master/src/main/java/cz/goldminer/tonysp/plugindata/data/packets/DataPacket.java) class.
Then you can simply create the packet (Builder patter is useful) and send it.
Example:
```java
BasicDataPacket.newBuilder("my plugin")
        .addReceiver("server 2")
        .string("test message")
        .integer(45)
        .buildPacket()
        .send();
```
In this example, the id of the packet is "my plugin", the contents are one String and Integer and the packed would be received by "server 2".
If you want to send the packet to every server, you can omit the addReceiver method.
