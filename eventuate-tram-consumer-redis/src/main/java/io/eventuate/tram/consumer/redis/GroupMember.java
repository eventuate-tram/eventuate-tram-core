package io.eventuate.tram.consumer.redis;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupMember {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private CuratorFramework curatorFramework;
  private String path;
  private String id;

  public GroupMember(CuratorFramework curatorFramework, String path, String id) {
    this.curatorFramework = curatorFramework;
    this.path = path;
    this.id = id;

    try {
      curatorFramework
              .create()
              .creatingParentsIfNeeded()
              .withMode(CreateMode.EPHEMERAL)
              .forPath(String.format("%s/%s", path, id), new byte[0]);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  public void remove() {
    try {
      curatorFramework
              .delete()
              .forPath(String.format("%s/%s", path, id));
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}
