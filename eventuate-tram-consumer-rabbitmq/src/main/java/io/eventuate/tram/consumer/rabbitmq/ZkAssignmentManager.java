package io.eventuate.tram.consumer.rabbitmq;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.coordinator.Assignment;
import io.eventuate.tram.consumer.common.coordinator.AssignmentManager;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

public class ZkAssignmentManager implements AssignmentManager  {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private CuratorFramework curatorFramework;

  public ZkAssignmentManager(CuratorFramework curatorFramework) {
    this.curatorFramework = curatorFramework;
  }

  @Override
  public void initializeAssignment(String groupId, String memberId, Assignment assignment) {
    try {
      curatorFramework
              .create()
              .creatingParentsIfNeeded()
              .withMode(CreateMode.EPHEMERAL)
              .forPath(makeAssignmentPath(groupId, memberId),
                      stringToByteArray(JSonMapper.toJson(assignment)));

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public Assignment readAssignment(String groupId, String memberId) {
    try {
      String assignmentPath = makeAssignmentPath(groupId, memberId);
      byte[] binaryData = curatorFramework.getData().forPath(assignmentPath);
      return JSonMapper.fromJson(byteArrayToString(binaryData), Assignment.class);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void saveAssignment(String groupId, String memberId, Assignment assignment) {
    try {
      String assignmentPath = makeAssignmentPath(groupId, memberId);
      byte[] binaryData = stringToByteArray(JSonMapper.toJson(assignment));
      curatorFramework.setData().forPath(assignmentPath, binaryData);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private String makeAssignmentPath(String groupId, String memberId) {
    return String.format("/eventuate-tram/rabbitmq/consumer-assignments/%s/%s", groupId, memberId);
  }

  private String byteArrayToString(byte[] bytes) {
    try {
      return new String(bytes, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private byte[] stringToByteArray(String string) {
    try {
      return string.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}
