/**
 * Copyright 2015. NFLabs, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nflabs.shiro.cache.zookeeper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.shiro.ShiroException;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.util.Destroyable;
import org.apache.shiro.util.Initializable;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shiro {@code CacheManager} implementation utilizing the Zookeeper for all cache functionality.
 * <p/> <p/>
 *
 * @author anthonycorbacho
 *
 */
public class ZookeeperManager implements CacheManager, Initializable, Destroyable {

  private static final Logger LOG = LoggerFactory.getLogger(ZookeeperManager.class);
  private Map<String, ZooKeeper> zookeeperClients = new LinkedHashMap<String, ZooKeeper>();

  /** Zookerper hostnames. */
  private String zookeeperServer = "localhost:2181";

  /** Set a session timeout. */
  private int sessionTimeout = 3000;

  /** Zookeeper path. */
  private String zookkeeperPath = "/shiro-zookeeper";

  public ZookeeperManager() {}

  @Override
  public <K, V> Cache<K, V> getCache(String name) throws CacheException {
    LOG.info("Acquiring Zookeeper instance named [{}].", name);

    ZooKeeper client = zookeeperClients.get(name);
    if (client == null) {
      LOG.info("Cache with name '{}' does not yet exist.  Creating now.", name);
      try {
        client = createNewZookeeperClient();
        zookeeperClients.put(name, client);
      } catch (IOException e) {
        throw new CacheException(e);
      }
    }
    return new ZookeeperCache<K, V>(client, getZookkeeperPath());
  }

  @Override
  public void init() throws ShiroException {
    /** Nothing to do here */
  }

  @Override
  public void destroy() throws Exception {
    if (!zookeeperClients.isEmpty()) {
      LOG.info("Shutting down all Zookeeper cache.");
      for (Entry<String, ZooKeeper> entry : zookeeperClients.entrySet()) {
        entry.getValue().close();
      }
      zookeeperClients.clear();
    }
  }

  private ZooKeeper createNewZookeeperClient() throws IOException {
    return new ZooKeeper(getZookeeperServer(), getSessionTimeout(), new ZookeeperWatcher());
  }

  public String getZookeeperServer() {
    return zookeeperServer;
  }

  public void setZookeeperServer(String zookeeperServer) {
    this.zookeeperServer = zookeeperServer;
  }

  public int getSessionTimeout() {
    return sessionTimeout;
  }

  public void setSessionTimeout(int sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
  }

  public String getZookkeeperPath() {
    return zookkeeperPath;
  }

  public void setZookkeeperPath(String zookkeeperPath) {
    this.zookkeeperPath = zookkeeperPath;
  }

}
