package io.eventuate.tram.consumer.rabbitmq;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.coordinator.Assignment;
import io.eventuate.tram.consumer.common.coordinator.AssignmentManager;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
              .forPath(ZkUtil.pathForAssignment(groupId, memberId),
                      ZkUtil.stringToByteArray(JSonMapper.toJson(assignment)));

    }
    catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public Assignment readAssignment(String groupId, String memberId) {
    try {
      String assignmentPath = ZkUtil.pathForAssignment(groupId, memberId);
      byte[] binaryData = curatorFramework.getData().forPath(assignmentPath);
      return JSonMapper.fromJson(ZkUtil.byteArrayToString(binaryData), Assignment.class);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void saveAssignment(String groupId, String memberId, Assignment assignment) {
    try {
      String assignmentPath = ZkUtil.pathForAssignment(groupId, memberId);
      byte[] binaryData = ZkUtil.stringToByteArray(JSonMapper.toJson(assignment));
      curatorFramework.setData().forPath(assignmentPath, binaryData);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}
