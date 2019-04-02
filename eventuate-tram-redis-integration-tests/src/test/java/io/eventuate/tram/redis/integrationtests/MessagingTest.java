package io.eventuate.tram.redis.integrationtests;

import com.google.common.collect.ImmutableSet;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.consumer.common.TramNoopDuplicateMessageDetectorConfiguration;
import io.eventuate.tram.consumer.common.coordinator.CoordinatorFactoryImpl;
import io.eventuate.tram.consumer.redis.*;
import io.eventuate.tram.consumer.common.coordinator.CoordinatorFactory;
import io.eventuate.tram.data.producer.redis.EventuateRedisProducer;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.redis.common.CommonRedisConfiguration;
import io.eventuate.tram.redis.common.RedissonClients;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MessagingTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MessagingTest extends AbstractMessagingTest {

  @Configuration
  @EnableAutoConfiguration
  @Import({CommonRedisConfiguration.class, TramConsumerCommonConfiguration.class, TramNoopDuplicateMessageDetectorConfiguration.class})
  public static class Config {
  }

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Autowired
  private RedissonClients redissonClients;

  @Override
  protected TestSubscription subscribe(int partitionCount) {
    ConcurrentLinkedQueue<Integer> messageQueue = new ConcurrentLinkedQueue<>();

    MessageConsumerRedisImpl consumer = createConsumer(partitionCount);

    consumer.subscribe(subscriberId, ImmutableSet.of(destination), message ->
            messageQueue.add(Integer.parseInt(message.getPayload())));

    TestSubscription testSubscription = new TestSubscription(consumer, messageQueue);

    consumer.setSubscriptionLifecycleHook((channel, subscriptionId, currentPartitions) -> {
      testSubscription.setCurrentPartitions(currentPartitions);
    });
    consumer.setLeaderHook((leader, subscriptionId) -> testSubscription.setLeader(leader));

    return testSubscription;
  }

  private MessageConsumerRedisImpl createConsumer(int partitionCount) {

    CoordinatorFactory coordinatorFactory = new CoordinatorFactoryImpl(new RedisAssignmentManager(redisTemplate, 3600000),
            (groupId, memberId, assignmentUpdatedCallback) -> new RedisAssignmentListener(redisTemplate, groupId, memberId, 50, assignmentUpdatedCallback),
            (groupId, memberId, groupMembersUpdatedCallback) -> new RedisMemberGroupManager(redisTemplate, groupId, memberId,50, groupMembersUpdatedCallback),
            (groupId, memberId, leaderSelectedCallback, leaderRemovedCallback) -> new RedisLeaderSelector(redissonClients, groupId, memberId,10000, leaderSelectedCallback, leaderRemovedCallback),
            (groupId, memberId) -> new RedisGroupMember(redisTemplate, groupId, memberId, 1000),
            partitionCount);

    MessageConsumerRedisImpl messageConsumerRedis = new MessageConsumerRedisImpl(subscriptionIdSupplier,
            consumerIdSupplier.get(),
            redisTemplate,
            coordinatorFactory,
            100,
            100);

    applicationContext.getAutowireCapableBeanFactory().autowireBean(messageConsumerRedis);

    return messageConsumerRedis;
  }

  @Override
  protected void sendMessages(int messageCount, int partitions) {
    EventuateRedisProducer eventuateRedisProducer = new EventuateRedisProducer(redisTemplate, partitions);

    for (int i = 0; i < messageCount; i++) {
      eventuateRedisProducer.send(destination,
              String.valueOf(i),
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", messageIdSupplier.get()))));
    }
  }
}
