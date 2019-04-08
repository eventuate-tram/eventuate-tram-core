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

  private String groupId;
  private String memberId;
  private String path;
  private TreeCache treeCache;

  public ZkMemberGroupManager(CuratorFramework curatorFramework,
                              String groupId,
                              String memberId,
                              Consumer<Set<String>> groupMembersUpdatedCallback) {

    this.groupId = groupId;
    this.memberId = memberId;
    path = ZkUtil.pathForMemberGroup(groupId);
    treeCache = new TreeCache(curatorFramework, path);

    treeCache.getListenable().addListener((client, event) -> {
      Set<String> members = getCurrentMemberIds();
      logger.info("Calling groupMembersUpdatedCallback.accept, members: {}, group: {}, member: {}", members, groupId, memberId);
      groupMembersUpdatedCallback.accept(members);
      logger.info("Called groupMembersUpdatedCallback.accept, members: {}, group: {}, member: {}", members, groupId, memberId);
    });

    try {
      logger.info("Starting group manager, group: {}, member: {}", groupId, memberId);
      treeCache.start();
      logger.info("Started group manager, group: {}, member: {}", groupId, memberId);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stop() {
    logger.info("Stopping group manager, group: {}, member: {}", groupId, memberId);
    treeCache.close();
    logger.info("Stopped group manager, group: {}, member: {}", groupId, memberId);
  }

  private Set<String> getCurrentMemberIds() {
    return Optional
            .ofNullable(treeCache.getCurrentChildren(path))
            .map(Map::keySet)
            .orElseGet(Collections::emptySet);
  }
}
