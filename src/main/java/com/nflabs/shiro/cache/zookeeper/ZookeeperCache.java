package com.nflabs.shiro.cache.zookeeper;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.util.CollectionUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * Shiro Cache implementation that wraps an Zookeeper instance.
 *
 * @author anthonycorbacho
 *
 * @param <K>
 * @param <V>
 */
public class ZookeeperCache<K, V> implements Cache<K, V> {

  private static final Logger LOG = LoggerFactory.getLogger(ZookeeperCache.class);
  private ZooKeeper zookeeperClient;
  private final String zookeeperBasePath;

  public ZookeeperCache(ZooKeeper newZookeeperClient, String path) {

    if (newZookeeperClient == null) {
      throw new IllegalArgumentException("Cache argument cannot be null.");
    }
    if (StringUtils.isBlank(path)) {
      LOG.debug("Patch is null or empty. Adding default base path.");
      path = "/shiro-cache";
    }

    LOG.debug("Creating new cache client from {}.", newZookeeperClient);
    zookeeperClient = newZookeeperClient;
    zookeeperBasePath = path;
  }

  /**
  * Gets a value of an element which matches the given key.
  *
  * @param key the key of the element to return.
  * @return The value placed into the cache with an earlier put, or null if not found or expired
  */
  @Override
  public V get(K key) throws CacheException {
    LOG.debug("Getting object from cache {} for key {}", zookeeperClient.getSessionId(), key);
    if (key == null) {
      LOG.debug("Invalid key, abort");
      return null;
    }
    if (!isCacheExist(key)) {
      LOG.debug("Couldnt find the path, abort");
      return null;
    }
    return getData(key);
  }

  /**
   * Puts an object into the cache.
   *
   * @param key the key.
   * @param value the value.
   */
  @Override
  public V put(K key, V value) throws CacheException {
    LOG.debug("Putting object from cache {} for key {}", zookeeperClient.getSessionId(), key);
    if (!isBaseDirectoryExist()) {
      LOG.debug("The Zookeeper base dir doesnt exist, creating it.");
      createDirectory(zookeeperBasePath);
    }
    if (!isCacheExist(key)) {
      LOG.debug("The session dir doesnt exist, creating it.");
      createDirectory(getDataPath(key));
    }
    V previous = getData(key);
    persistData(key, value);
    /**
     * I am not sure about this, I checked ehCache and they return the previous entry.
     * I guess this should be okay...
     */
    return previous;
  }

  @Override
  public V remove(K key) throws CacheException {
    LOG.debug("Removing object from cache {} for key {}", zookeeperClient.getSessionId(), key);
    if (!isCacheExist(key)) {
      LOG.debug("The session dir doesnt exist, abort.");
      return null;
    }
    try {
      V previous = getData(key);
      zookeeperClient.delete(getDataPath(key), -1);
      return previous;
    } catch (InterruptedException | KeeperException e) {
      //throw new CacheException(e);
      LOG.error("Error: {}", e.getMessage());
      return null;
    }
  }

  @Override
  public void clear() throws CacheException {
    LOG.debug("Removing object from cache {}", zookeeperClient.getSessionId());
    if (!isBaseDirectoryExist()) {
      LOG.debug("The Zookeeper base dir doesnt exist, abort.");
      return;
    }
    try {
      zookeeperClient.delete(zookeeperBasePath, -1);
    } catch (InterruptedException | KeeperException e) {
      //throw new CacheException(e);
      LOG.error("Error: {}", e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public int size() {
    try {
      List<String> childrens = zookeeperClient.getChildren(zookeeperBasePath, null);
      int size = 0;
      for (String child : childrens) {
        size += zookeeperClient.exists(getDataPath((K) child), false).getDataLength();
      }
      return size;
    } catch (KeeperException | InterruptedException e) {
      return 0;
    }
  }

  @Override
  public Set<K> keys() {
    if (!isBaseDirectoryExist()) {
      LOG.debug("The Zookeeper base dir doesnt exist, abort.");
      return Collections.emptySet();
    }

    try {
      @SuppressWarnings("unchecked")
      List<K> childrens = (List<K>) zookeeperClient.getChildren(zookeeperBasePath, null);
      if (!CollectionUtils.isEmpty(childrens)) {
        return Collections.unmodifiableSet(new LinkedHashSet<K>(childrens));
      } else {
        return Collections.emptySet();
      }
    } catch (InterruptedException | KeeperException e) {
      //throw new CacheException(e);
      LOG.error("Error: {}", e.getMessage());
      return Collections.emptySet();
    }
  }

  @Override
  public Collection<V> values() {
    if (!isBaseDirectoryExist()) {
      LOG.debug("The Zookeeper base dir doesnt exist, abort.");
      return Collections.emptySet();
    }
    try {
      @SuppressWarnings("unchecked")
      List<K> childrens = (List<K>) zookeeperClient.getChildren(zookeeperBasePath, null);
      if (!CollectionUtils.isEmpty(childrens)) {
        List<V> values = new ArrayList<V>(childrens.size());
        for (K key : childrens) {
          V value = get(key);
          if (value != null) {
            values.add(value);
          }
        }
        return Collections.unmodifiableList(values);
      } else {
        return Collections.emptyList();
      }
    } catch (InterruptedException | KeeperException e) {
      //throw new CacheException(e);
      LOG.error("Error: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  private String getDataPath(K key) {
    return Joiner.on(File.separator).join(zookeeperBasePath, key);
  }

  private boolean isCacheExist(K key) {
    try {
      return !(zookeeperClient.exists(getDataPath(key), false) == null);
    } catch (KeeperException | InterruptedException e) {
      //throw new CacheException(e);
      LOG.error("Error: {}", e.getMessage());
      return false;
    }
  }

  private boolean isBaseDirectoryExist() {
    try {
      return !(zookeeperClient.exists(zookeeperBasePath, false) == null);
    } catch (KeeperException | InterruptedException e) {
      //throw new CacheException(e);
      LOG.error("Error: {}", e.getMessage());
      return false;
    }
  }

  private void createDirectory(String path) {
    try {
      zookeeperClient.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    } catch (KeeperException | InterruptedException e) {
      //throw new CacheException(e);
      LOG.error("Error: {}", e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  private V getData(K key) {
    try {
      byte[] result = zookeeperClient.getData(getDataPath(key), false, new Stat());
      if (result == null) {
        return  null;
      }
      return (V) SerializationUtils.deserialize(result);
    } catch (KeeperException | InterruptedException e) {
      //throw new CacheException(e);
      LOG.error("Error: {}", e.getMessage());
      return  null;
    }
  }

  private void persistData(K key, V value) {
    try {
      zookeeperClient.setData(getDataPath(key),
                              SerializationUtils.serialize((Serializable) value),
                              -1);
    } catch (KeeperException | InterruptedException e) {
      //throw new CacheException(e);
      LOG.error("Error: {}", e.getMessage());
    }
  }

}
