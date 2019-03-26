package io.eventuate.tram.consumer.rabbitmq;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.coordinator.Assignment;
import io.eventuate.tram.consumer.common.coordinator.AssignmentListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

public class ZkAssignmentListener implements AssignmentListener {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private NodeCache nodeCache;

  public ZkAssignmentListener(CuratorFramework curatorFramework,
                              String subscriberId,
                              String groupMemberId,
                              Consumer<Assignment> assignmentUpdatedCallback) {
    nodeCache = new NodeCache(curatorFramework, makeAssignmentPath(subscriberId, groupMemberId));

    try {
      nodeCache.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    nodeCache.getListenable().addListener(() -> {
      Assignment assignment = JSonMapper.fromJson(byteArrayToString(nodeCache.getCurrentData().getData()),
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

  private String makeAssignmentPath(String subscriberId, String groupMemberId) {
    return String.format("/eventuate-tram/rabbitmq/consumer-assignments/%s/%s", subscriberId, groupMemberId);
  }

  private String byteArrayToString(byte[] bytes) {
    try {
      return new String(bytes, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}
