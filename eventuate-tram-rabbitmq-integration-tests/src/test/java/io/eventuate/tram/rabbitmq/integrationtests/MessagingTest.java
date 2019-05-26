package io.eventuate.tram.rabbitmq.integrationtests;

import com.google.common.collect.ImmutableSet;
import io.eventuate.coordination.leadership.zookeeper.ZkLeaderSelector;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.common.integrationtests.AbstractMessagingTest;
import io.eventuate.tram.consumer.common.TramConsumerBaseCommonConfiguration;
import io.eventuate.tram.consumer.common.TramNoopDuplicateMessageDetectorConfiguration;
import io.eventuate.tram.consumer.common.coordinator.CoordinatorFactory;
import io.eventuate.tram.consumer.common.coordinator.CoordinatorFactoryImpl;
import io.eventuate.tram.consumer.rabbitmq.*;
import io.eventuate.tram.data.producer.rabbitmq.EventuateRabbitMQProducer;
import io.eventuate.tram.messaging.common.MessageImpl;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MessagingTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MessagingTest extends AbstractMessagingTest {

  @Configuration
  @EnableAutoConfiguration
  @Import({TramConsumerBaseCommonConfiguration.class, TramNoopDuplicateMessageDetectorConfiguration.class})
  public static class Config {
    @Bean
    public EventuateRabbitMQProducer rabbitMQMessageProducer(@Value("${rabbitmq.url}") String rabbitMQURL) {
      return new EventuateRabbitMQProducer(rabbitMQURL);
    }
  }

  @Value("${rabbitmq.url}")
  private String rabbitMQURL;

  @Value("${eventuatelocal.zookeeper.connection.string}")
  private String zkUrl;

  @Autowired
  private EventuateRabbitMQProducer eventuateRabbitMQProducer;

  @Override
  protected TestSubscription subscribe(int partitionCount) {
    ConcurrentLinkedQueue<Integer> messageQueue = new ConcurrentLinkedQueue<>();

    MessageConsumerRabbitMQImpl consumer = createConsumer(partitionCount);

    consumer.subscribe(subscriberId, ImmutableSet.of(destination), message ->
            messageQueue.add(Integer.parseInt(message.getPayload())));

    TestSubscription testSubscription = new TestSubscription(consumer, messageQueue);

    consumer.setSubscriptionLifecycleHook((channel, subscriptionId, currentPartitions) -> testSubscription.setCurrentPartitions(currentPartitions));
    consumer.setLeaderHook((leader, subscriptionId) -> testSubscription.setLeader(leader));

    return testSubscription;
  }

  private MessageConsumerRabbitMQImpl createConsumer(int partitionCount) {
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(zkUrl, new ExponentialBackoffRetry(1000, 5));
    curatorFramework.start();

    CoordinatorFactory coordinatorFactory = new CoordinatorFactoryImpl(new ZkAssignmentManager(curatorFramework),
            (groupId, memberId, assignmentUpdatedCallback) -> new ZkAssignmentListener(curatorFramework, groupId, memberId, assignmentUpdatedCallback),
            (groupId, memberId, groupMembersUpdatedCallback) -> new ZkMemberGroupManager(curatorFramework, groupId, memberId, groupMembersUpdatedCallback),
            (lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback) -> new ZkLeaderSelector(curatorFramework, lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback),
            (groupId, memberId) -> new ZkGroupMember(curatorFramework, groupId, memberId),
            partitionCount);

    MessageConsumerRabbitMQImpl messageConsumerRabbitMQ = new MessageConsumerRabbitMQImpl(subscriptionIdSupplier, consumerIdSupplier.get(), coordinatorFactory, rabbitMQURL, partitionCount);
    applicationContext.getAutowireCapableBeanFactory().autowireBean(messageConsumerRabbitMQ);
    return messageConsumerRabbitMQ;
  }

  @Override
  protected void sendMessages(int messageCount, int partitions) {
    for (int i = 0; i < messageCount; i++) {
      eventuateRabbitMQProducer.send(destination,
              String.valueOf(i),
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", messageIdSupplier.get()))));
    }
  }
}
