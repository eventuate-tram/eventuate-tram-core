package io.eventuate.tram.consumer.rabbitmq;

import io.eventuate.tram.consumer.common.coordinator.MemberGroupManager;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class ZkMemberGroupManager implements MemberGroupManager {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private String path;
  private TreeCache treeCache;

  public ZkMemberGroupManager(CuratorFramework curatorFramework, String subscriberId, Consumer<Set<String>> groupMembersUpdatedCallback) {
    this.path = String.format("/eventuate-tram/rabbitmq/consumer-groups/%s", subscriberId);;
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

  @Override
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
