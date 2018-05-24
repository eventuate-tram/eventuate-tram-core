package io.eventuate.tram.consumer.rabbitmq;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.leader.CancelLeadershipException;
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

  private final String groupMemberId;
  private String subscriberId;
  private Set<String> channels;

  private Runnable leaderSelectedCallback;
  private Runnable leaderRemovedCallback;
  private Consumer<Assignment> assignmentUpdatedCallback;
  private Consumer<Map<String, Assignment>> manageAssignmentsCallback;

  private String groupPath;
  private String leaderPath;

  private volatile boolean running = true;

  private CuratorFramework curatorFramework;
  private GroupMember groupMember;
  private NodeCache nodeCache;
  private LeaderSelector leaderSelector;

  public Coordinator(String groupMemberId,
                     String zkUrl,
                     String subscriberId,
                     Set<String> channels,
                     Runnable leaderSelectedCallback,
                     Runnable leaderRemovedCallback,
                     Consumer<Assignment> assignmentUpdatedCallback,
                     Consumer<Map<String, Assignment>> manageAssignmentsCallback) {

    this.groupMemberId = groupMemberId;
    this.subscriberId = subscriberId;
    this.channels = channels;

    this.leaderSelectedCallback = leaderSelectedCallback;
    this.leaderRemovedCallback = leaderRemovedCallback;
    this.assignmentUpdatedCallback = assignmentUpdatedCallback;
    this.manageAssignmentsCallback = manageAssignmentsCallback;

    groupPath = String.format("/eventuate-tram/rabbitmq/consumer-groups/%s", subscriberId);
    leaderPath = String.format("/eventuate-tram/rabbitmq/consumer-leaders/%s", subscriberId);

    curatorFramework = CuratorFrameworkFactory.newClient(zkUrl,
            new ExponentialBackoffRetry(1000, 5));

    curatorFramework.start();

    createAssignmentNodes();
    createGroupMember();
    createNodeCaches();
    createLeaderSelector();
  }

  private void createAssignmentNodes() {
    try {
      Map<String, Set<Integer>> partitionAssignmentsByChannel = new HashMap<>();
      channels.forEach(channel -> partitionAssignmentsByChannel.put(channel, new HashSet<>()));
      Assignment assignment = new Assignment(channels, partitionAssignmentsByChannel);

      curatorFramework
              .create()
              .creatingParentsIfNeeded()
              .withMode(CreateMode.EPHEMERAL)
              .forPath(makeAssignmentPath(groupMemberId, subscriberId),
                      stringToByteArray(JSonMapper.toJson(assignment)));

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private void createGroupMember() {
      groupMember = new GroupMember(curatorFramework,
              groupPath,
              groupMemberId,
              stringToByteArray(JSonMapper.toJson(channels)));

      groupMember.start();
  }

  private void createNodeCaches() {
    channels.forEach(channel -> {
      nodeCache = new NodeCache(curatorFramework, makeAssignmentPath(groupMemberId, subscriberId));

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
    });
  }

  private void createLeaderSelector() {
    Map<String, Set<String>>  previousGroupMembers = new HashMap<>();

    leaderSelector = new LeaderSelector(curatorFramework, leaderPath, new LeaderSelectorListener() {
      @Override
      public void takeLeadership(CuratorFramework client) {
        try {

          leaderSelectedCallback.run();

          while (running) {

            Map<String, Set<String>> currentGroupMembers = groupMember
                    .getCurrentMembers()
                    .entrySet()
                    .stream()
                    .collect(toMap(Map.Entry::getKey,
                            entry -> JSonMapper.fromJson(byteArrayToString(entry.getValue()), Set.class)));

            if (previousGroupMembers.equals(currentGroupMembers)) {
              if (waitIterationAndCheckIfLeadershipShouldBeReturned()) {
                break;
              }
              continue;
            }

            previousGroupMembers.clear();
            previousGroupMembers.putAll(currentGroupMembers);

            HashMap<String, Assignment> assignments = new HashMap<>();
            currentGroupMembers.keySet().forEach(groupMemberId -> assignments.put(groupMemberId, readAssignment(groupMemberId)));
            manageAssignmentsCallback.accept(assignments);
            assignments.forEach((memberId, assignment) -> saveAssignment(assignment, memberId));

            if (waitIterationAndCheckIfLeadershipShouldBeReturned()) {
              break;
            }
          }
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
        } finally {
          leaderRemovedCallback.run();
        }
      }

      @Override
      public void stateChanged(CuratorFramework client, ConnectionState newState) {
        if (newState == ConnectionState.SUSPENDED || newState == ConnectionState.LOST) {
          throw new CancelLeadershipException();
        }
      }
    });

    leaderSelector.autoRequeue();

    leaderSelector.start();
  }

  private Assignment readAssignment(String groupMemberId) {
    try {
      String assignmentPath = makeAssignmentPath(groupMemberId, subscriberId);
      byte[] binaryData = curatorFramework.getData().forPath(assignmentPath);
      return JSonMapper.fromJson(byteArrayToString(binaryData), Assignment.class);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private void saveAssignment(Assignment assignment, String groupMemberId) {
    try {
      String assignmentPath = makeAssignmentPath(groupMemberId, subscriberId);
      byte[] binaryData = stringToByteArray(JSonMapper.toJson(assignment));
      curatorFramework.setData().forPath(assignmentPath, binaryData);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private boolean waitIterationAndCheckIfLeadershipShouldBeReturned() {
    try {
      Thread.sleep(100);
      return false;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return true;
    }
  }

  private String makeAssignmentPath(String groupMemberId, String subscriberId) {
    return String.format("/eventuate-tram/rabbitmq/consumer-assignments/%s/%s", subscriberId, groupMemberId);
  }

  public void close() {
    running = false;
    leaderSelector.close();

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
