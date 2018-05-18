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
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toMap;

public class Coordinator {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private String instanceId = UUID.randomUUID().toString();
  private String subscriberId;
  private Set<String> channels;

  private Runnable leaderSelectedCallback;
  private Runnable leaderRemovedCallback;
  private Consumer<Assignment> assignmentUpdatedCallback;
  private Consumer<List<Assignment>> manageAssignmentsCallback;


  private String groupPath;
  private String leaderPath;

  private volatile boolean running = true;

  private CuratorFramework curatorFramework;
  private GroupMember groupMember;
  private NodeCache nodeCache;
  private LeaderSelector leaderSelector;

  public Coordinator(String zkUrl,
                     String subscriberId,
                     Set<String> channels,
                     Runnable leaderSelectedCallback,
                     Runnable leaderRemovedCallback,
                     Consumer<Assignment> assignmentUpdatedCallback,
                     Consumer<List<Assignment>> manageAssignmentsCallback) {

    this.subscriberId = subscriberId;
    this.channels = channels;

    this.leaderSelectedCallback = leaderSelectedCallback;
    this.leaderRemovedCallback = leaderRemovedCallback;
    this.assignmentUpdatedCallback = assignmentUpdatedCallback;
    this.manageAssignmentsCallback = manageAssignmentsCallback;

    groupPath = String.format("/eventuate-tram/rabbitmq/consumer-groups/%s", subscriberId);
    leaderPath = String.format("/eventuate-tram/rabbitmq/consumer-leaders/%s", subscriberId);

    curatorFramework = CuratorFrameworkFactory.newClient(zkUrl,
            new ExponentialBackoffRetry(1000, 10));

    curatorFramework.start();

    createAssignmentNodes();
    createGroupMember();
    createNodeCaches();
    createLeaderSelector();
  }

  private void createAssignmentNodes() {
    channels.forEach(channel -> {
      try {
        curatorFramework
                .create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(makeAssignmentPath(instanceId, channel, subscriberId),
                        stringToByteArray(JSonMapper.toJson(new Assignment(instanceId, channel))));
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        throw new RuntimeException(e);
      }
    });
  }

  private void createGroupMember() {
      groupMember = new GroupMember(curatorFramework,
              groupPath,
              instanceId,
              stringToByteArray(JSonMapper.toJson(channels)));

      groupMember.start();
  }

  private void createNodeCaches() {
    channels.forEach(channel -> {
      nodeCache = new NodeCache(curatorFramework, makeAssignmentPath(instanceId, channel, subscriberId));

      try {
        nodeCache.start();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      nodeCache.getListenable().addListener(() -> {
        Assignment assignment = JSonMapper.fromJson(byteArrayToString(nodeCache.getCurrentData().getData()),
                Assignment.class);

        if (assignment.getState() == AssignmentState.REBALANSING) {
          assignmentUpdatedCallback.accept(assignment);
          saveAssignment(assignment, channel);
        }
      });
    });
  }

  private void createLeaderSelector() {
    Map<String, Set<String>>  previousGroupMembers = new HashMap<>();

    leaderSelector = new LeaderSelector(curatorFramework, leaderPath, new LeaderSelectorListener() {
      @Override
      public void takeLeadership(CuratorFramework client) {
        leaderSelectedCallback.run();

        while (running) {

          Map<String, Set<String>> currentGroupMembers = groupMember
                  .getCurrentMembers()
                  .entrySet()
                  .stream()
                  .collect(toMap(Map.Entry::getKey,
                          e -> JSonMapper.fromJson(byteArrayToString(e.getValue()), Set.class)));

          if (previousGroupMembers.equals(currentGroupMembers)) {
            waitIteration();
            continue;
          }

          previousGroupMembers.clear();
          previousGroupMembers.putAll(currentGroupMembers);


          Map<String, List<Assignment>> assignments = new HashMap<>();

          currentGroupMembers.keySet().forEach(memberId -> {
            currentGroupMembers.get(memberId).forEach(channel -> {
              assignments.putIfAbsent(channel, new ArrayList<>());
              assignments.get(channel).add(readAssignment(memberId, channel));
            });
          });

          for (String channel : assignments.keySet()) {
            if (assignments
                    .get(channel)
                    .stream()
                    .map(Assignment::getState)
                    .noneMatch(state -> state == AssignmentState.REBALANSING)) {

              manageAssignmentsCallback.accept(assignments.get(channel));
              assignments.get(channel).forEach(a -> saveAssignment(a, channel));
            }
          }

          waitIteration();
        }
      }

      @Override
      public void stateChanged(CuratorFramework client, ConnectionState newState) {

      }
    });

    leaderSelector.start();
  }

  private Assignment readAssignment(String memberId, String channel) {
    try {
      String assignmentPath = makeAssignmentPath(memberId, channel, subscriberId);
      byte[] binaryData = curatorFramework.getData().forPath(assignmentPath);
      return JSonMapper.fromJson(byteArrayToString(binaryData), Assignment.class);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private void saveAssignment(Assignment assignment, String channel) {
    try {
      String assignmentPath = makeAssignmentPath(assignment.getInstanceId(), channel, subscriberId);
      byte[] binaryData = stringToByteArray(JSonMapper.toJson(assignment));
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
    return String.format("/eventuate-tram/rabbitmq/consumer-assignments/%s/%s/%s", subscriberId, channelName, instanceId);
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
