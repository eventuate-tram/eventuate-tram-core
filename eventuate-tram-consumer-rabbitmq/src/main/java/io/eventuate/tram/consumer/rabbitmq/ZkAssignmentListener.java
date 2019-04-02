package io.eventuate.tram.consumer.rabbitmq;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.coordinator.Assignment;
import io.eventuate.tram.consumer.common.coordinator.AssignmentListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

public class ZkAssignmentListener implements AssignmentListener {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private NodeCache nodeCache;

  public ZkAssignmentListener(CuratorFramework curatorFramework,
                              String groupId,
                              String memberId,
                              Consumer<Assignment> assignmentUpdatedCallback) {
    nodeCache = new NodeCache(curatorFramework, ZkUtil.pathForAssignment(groupId, memberId));

    try {
      nodeCache.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    nodeCache.getListenable().addListener(() -> {
      Assignment assignment = JSonMapper.fromJson(ZkUtil.byteArrayToString(nodeCache.getCurrentData().getData()),
              Assignment.class);

      assignmentUpdatedCallback.accept(assignment);
    });
  }

  @Override
  public void remove() {
    try {
      nodeCache.close();
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }
}
