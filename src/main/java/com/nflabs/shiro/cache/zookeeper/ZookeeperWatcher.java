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
