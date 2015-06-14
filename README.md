# Shiro-Zookeeper

A Apache Shiro session cache for Apache Zookeeper, it will allow your application to save your users session in Zookeeper FS.

## Build and use in your Application

### Build
Simple run `./gradlew build`, once its done, copy the jar from `lib` to your lib application folder.

### Configure

In your `siro.ini` file, configure the session manager with the following parameters:

```
## Zookeeper as user session cache manager
cacheManager = com.nflabs.shiro.cache.zookeeper.ZookeeperManager
cacheManager.zookeeperServer = __YOUR_ZOOKEEPER_QUARUM__
cacheManager.zookkeeperPath = /my_app_sessions
securityManager.cacheManager = $cacheManager
```

