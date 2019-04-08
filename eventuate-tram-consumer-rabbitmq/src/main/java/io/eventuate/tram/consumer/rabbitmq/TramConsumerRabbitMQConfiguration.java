package io.eventuate.tram.consumer.rabbitmq;

import io.eventuate.coordination.leadership.LeaderSelectorFactory;
import io.eventuate.coordination.leadership.zookeeper.ZkLeaderSelector;
import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.consumer.common.coordinator.*;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramConsumerCommonConfiguration.class)
public class TramConsumerRabbitMQConfiguration {

  @Value("${eventuatelocal.zookeeper.connection.string}")
  private String zkUrl;

  @Value("${eventuate.rabbitmq.partition.count:#{2}}")
  private int partitionCount;

  @Value("${rabbitmq.url}")
  private String rabbitMQUrl;

  @Bean
  public MessageConsumer messageConsumer(CoordinatorFactory coordinatorFactory) {
    return new MessageConsumerRabbitMQImpl(coordinatorFactory, rabbitMQUrl, partitionCount);
  }

  @Bean
  public CoordinatorFactory coordinatorFactory(AssignmentManager assignmentManager,
                                               AssignmentListenerFactory assignmentListenerFactory,
                                               MemberGroupManagerFactory memberGroupManagerFactory,
                                               LeaderSelectorFactory leaderSelectorFactory,
                                               GroupMemberFactory groupMemberFactory) {
    return new CoordinatorFactoryImpl(assignmentManager,
            assignmentListenerFactory,
            memberGroupManagerFactory,
            leaderSelectorFactory,
            groupMemberFactory,
            partitionCount);
  }

  @Bean
  public GroupMemberFactory groupMemberFactory(CuratorFramework curatorFramework) {
    return (groupId, memberId) -> new ZkGroupMember(curatorFramework, groupId, memberId);
  }

  @Bean
  public LeaderSelectorFactory leaderSelectorFactory(CuratorFramework curatorFramework) {
    return (lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback) ->
            new ZkLeaderSelector(curatorFramework, lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback);
  }

  @Bean
  public MemberGroupManagerFactory memberGroupManagerFactory(CuratorFramework curatorFramework) {
    return (groupId, memberId, groupMembersUpdatedCallback) ->
            new ZkMemberGroupManager(curatorFramework, groupId, memberId, groupMembersUpdatedCallback);
  }

  @Bean
  public AssignmentListenerFactory assignmentListenerFactory(CuratorFramework curatorFramework) {
    return (groupId, memberId, assignmentUpdatedCallback) ->
            new ZkAssignmentListener(curatorFramework, groupId, memberId, assignmentUpdatedCallback);
  }

  @Bean
  public AssignmentManager assignmentManager(CuratorFramework curatorFramework) {
    return new ZkAssignmentManager(curatorFramework);
  }

  @Bean
  public CuratorFramework curatorFramework() {
    CuratorFramework framework = CuratorFrameworkFactory.newClient(zkUrl, new ExponentialBackoffRetry(1000, 5));
    framework.start();
    return framework;
  }
}
