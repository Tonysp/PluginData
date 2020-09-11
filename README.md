# PluginData
Bukkit plugin for easy database connection management and sending data between plugins.

### Database connection manager
1. Set up any number of MySQL databases in the config.yml
2. Get connection using the getConnection() method (thread safe).
```java
DatabaseManager.getInstance().getConnection("name in config");
```

### DataPacket API for sending data between servers
You can use the provided [BasicDataPacket](https://github.com/Tonysp/PluginData/blob/master/src/main/java/dev/tonysp/plugindata/data/packets/BasicDataPacket.java), or make your own implementation (extend) of the [DataPacket](https://github.com/Tonysp/PluginData/blob/master/src/main/java/dev/tonysp/plugindata/data/packets/DataPacket.java) class.
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
In this example, the id of the application is "my plugin", the contents are one String and Integer and the packed would be received by "server 2".
If you want to send the packet to every server, you can omit the addReceiver method.

To receive all DataPackets which were sent to you, you can do the following:
```java
DataPacketManager.getInstance().getReceivedPackets("my plugin");
```
Or you can specify which packets you want to process like this:
```java
DataPacketManager.getInstance().getReceivedPackets("my plugin", TestDataPacket.class);
```
