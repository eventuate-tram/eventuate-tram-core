package io.eventuate.tram.consumer.rabbitmq;

import io.eventuate.tram.consumer.common.coordinator.GroupMember;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkGroupMember implements GroupMember {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private CuratorFramework curatorFramework;
  private String path;

  public ZkGroupMember(CuratorFramework curatorFramework, String groupId, String memberId) {
    this.curatorFramework = curatorFramework;
    this.path = ZkUtil.pathForGroupMember(groupId, memberId);

    try {
      curatorFramework
              .create()
              .creatingParentsIfNeeded()
              .withMode(CreateMode.EPHEMERAL)
              .forPath(path, new byte[0]);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void remove() {
    try {
      curatorFramework.delete().forPath(path);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}
