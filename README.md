# PluginData
Bukkit plugin for easy database connection management and sending data between plugins.

# Database connection manager
1. Set up any number of MySQL databases in the config.yml
2. Get connection using the getConnection() method (thread safe).
```java
DatabaseManager.getInstance().getConnection("name");
```
