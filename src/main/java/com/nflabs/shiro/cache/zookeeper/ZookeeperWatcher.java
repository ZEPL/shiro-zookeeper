package com.nflabs.shiro.cache.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zookeeper event watcher requirend by the Zookeeper Client class.
 * 
 * @author anthonycorbacho
 *
 */
public class ZookeeperWatcher implements Watcher {

  public static final Logger LOG = LoggerFactory.getLogger(ZookeeperWatcher.class);
  
  @Override
  public void process(WatchedEvent event) {
    LOG.info("Zookeerper event: {}", event);
  }

}
