package io.eventuate.tram.consumer.rabbitmq;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class GroupManager {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private String path;
  private TreeCache treeCache;

  public GroupManager(CuratorFramework curatorFramework, String path, Consumer<Set<String>> groupMembersUpdatedCallback) {
    this.path = path;
    treeCache = new TreeCache(curatorFramework, path);

    treeCache.getListenable().addListener((client, event) ->
      groupMembersUpdatedCallback.accept(getCurrentMemberIds()));

    try {
      treeCache.start();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    treeCache.close();
  }

  private Set<String> getCurrentMemberIds() {
    return Optional
            .ofNullable(treeCache.getCurrentChildren(path))
            .map(Map::keySet)
            .orElseGet(Collections::emptySet);
  }
}
