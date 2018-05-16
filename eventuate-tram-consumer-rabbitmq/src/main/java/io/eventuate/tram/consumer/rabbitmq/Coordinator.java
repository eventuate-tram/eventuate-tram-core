package io.eventuate.tram.consumer.rabbitmq;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.nodes.GroupMember;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Coordinator {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private String instanceId = UUID.randomUUID().toString();
  private String subscriberId;
  private String channelName;

  private Runnable leaderSelectedCallback;
  private Runnable leaderRemovedCallback;
  private Consumer<AssignmentData> assignmentUpdatedCallback;
  private Consumer<List<AssignmentData>> manageAssignmentsCallback;


  private String assignmentPath;
  private String groupPath;
  private String leaderPath;

  private volatile boolean running = true;

  private CuratorFramework curatorFramework;
  private GroupMember groupMember;
  private NodeCache nodeCache;
  private LeaderSelector leaderSelector;

  public Coordinator(String zkUrl,
                     String subscriberId,
                     String channelName,
                     Runnable leaderSelectedCallback,
                     Runnable leaderRemovedCallback,
                     Consumer<AssignmentData> assignmentUpdatedCallback,
                     Consumer<List<AssignmentData>> manageAssignmentsCallback) {

    this.subscriberId = subscriberId;
    this.channelName = channelName;

    this.leaderSelectedCallback = leaderSelectedCallback;
    this.leaderRemovedCallback = leaderRemovedCallback;
    this.assignmentUpdatedCallback = assignmentUpdatedCallback;
    this.manageAssignmentsCallback = manageAssignmentsCallback;

    assignmentPath = String.format("/rabbit/assignment/%s/%s/%s", instanceId, channelName, subscriberId);
    groupPath = String.format("/rabbit/group/%s/%s", channelName, subscriberId);
    leaderPath = String.format("/rabbit/leader/%s/%s", subscriberId, channelName);

    curatorFramework = CuratorFrameworkFactory.newClient(zkUrl,
            new ExponentialBackoffRetry(1000, 10));

    curatorFramework.start();

    createAssignmentNode();
    createGroupMember();
    createNodeCache();
    createLeaderSelector();
  }

  private void createAssignmentNode() {
    try {
      curatorFramework
              .create()
              .creatingParentsIfNeeded()
              .withMode(CreateMode.EPHEMERAL)
              .forPath(assignmentPath,
                      JSonMapper.toJson(new AssignmentData(instanceId)).getBytes("UTF-8"));
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private void createGroupMember() {
    groupMember = new GroupMember(curatorFramework,
            groupPath,
            instanceId,
            new byte[0]);

    groupMember.start();
  }

  private void createNodeCache() {
    nodeCache = new NodeCache(curatorFramework, assignmentPath);

    try {
      nodeCache.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    nodeCache.getListenable().addListener(() -> {
      AssignmentData assignmentData = JSonMapper.fromJson(new String(nodeCache.getCurrentData().getData()),
              AssignmentData.class);

      if (assignmentData.getState() == AssignmentState.REBALANSING) {
        assignmentUpdatedCallback.accept(assignmentData);
        saveAssignment(assignmentData);
      }
    });
  }

  private void createLeaderSelector() {
    Set<String> previousGroupMembers = new HashSet<>();

    leaderSelector = new LeaderSelector(curatorFramework, leaderPath, new LeaderSelectorListener() {
      @Override
      public void takeLeadership(CuratorFramework client) {
        leaderSelectedCallback.run();

        while (running) {
          Set<String> currentGroupMembers = groupMember.getCurrentMembers().keySet();

          if (previousGroupMembers.equals(currentGroupMembers)) {
            waitIteration();
            continue;
          }

          previousGroupMembers.clear();
          previousGroupMembers.addAll(currentGroupMembers);

          List<AssignmentData> assignments = currentGroupMembers
                  .stream()
                  .map(Coordinator.this::readAssignment)
                  .collect(Collectors.toList());

          if (assignments.stream().anyMatch(assignment -> assignment.getState() != AssignmentState.NORMAL)) {
            waitIteration();
            continue;
          }

          manageAssignmentsCallback.accept(assignments);

          assignments.forEach(Coordinator.this::saveAssignment);

          waitIteration();
        }
      }

      @Override
      public void stateChanged(CuratorFramework client, ConnectionState newState) {

      }
    });

    leaderSelector.start();
  }

  private AssignmentData readAssignment(String memberId) {
    try {
      String assignmentPath = makeAssignmentPath(memberId, channelName, subscriberId);
      byte[] binaryData = curatorFramework.getData().forPath(assignmentPath);
      return JSonMapper.fromJson(new String(binaryData, "UTF-8"), AssignmentData.class);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private void saveAssignment(AssignmentData assignment) {
    try {
      String assignmentPath = makeAssignmentPath(assignment.getInstanceId(), channelName, subscriberId);
      byte[] binaryData = JSonMapper.toJson(assignment).getBytes("UTF-8");
      curatorFramework.setData().forPath(assignmentPath, binaryData);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private void waitIteration() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private String makeAssignmentPath(String instanceId, String channelName, String subscriberId) {
    return String.format("/rabbit/assignment/%s/%s/%s", instanceId, channelName, subscriberId);
  }

  public void close() {
    running = false;
    leaderSelector.close();
    leaderRemovedCallback.run();

    try {
      nodeCache.close();
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }

    groupMember.close();
    try {
      nodeCache.close();
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
    curatorFramework.close();
  }
}
