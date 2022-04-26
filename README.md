# PluginData
Bukkit library for easy mysqlConnection connection management and sending data between server instances.

### Database connection manager
1. Set up any number of MySQL databases in the config.yml
2. Get connection using the getConnection() method (thread safe).
```java
MysqlConnection.byName("name in config");
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

When you receive DataPacket, the event DataPacketReceivePubSubEvent will be called asynchronously.

#### Batch packet sending
You can use the batch option to send packets. The packets send and received with this option will be processed on a defined interval (batch-packet-send-and-retrieve-interval in config). To send a packet with the batch option, pass it to the send() method.
To receive all DataPackets which were sent to you with the batch option, you can do the following:
```java
BatchPipelineManager.getInstance().getReceivedPackets("my plugin");
```
Or you can specify which packets you want to process like this:
```java
BatchPipelineManager.getInstance().getReceivedPackets("my plugin", TestDataPacket.class);
```


### License
Copyright (C) 2020 Antonín Sůva

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>. 
