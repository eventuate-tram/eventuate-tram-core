package io.eventuate.tram.redis.integrationtests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.redis.MessageConsumerRedisImpl;
import io.eventuate.tram.data.producer.redis.EventuateRedisProducer;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.redis.common.CommonRedisConfiguration;
import io.eventuate.util.test.async.Eventually;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MessagingTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MessagingTest {

  @Configuration
  @EnableAutoConfiguration
  @Import(CommonRedisConfiguration.class)
  public static class Config {
    @Bean
    public DuplicateMessageDetector duplicateMessageDetector() {
      return (consumerId, messageId) -> false;
    }
  }

  private static class EventuallyConfig {
    public final int iterations;
    public final int timeout;
    public final TimeUnit timeUnit;

    public EventuallyConfig(int iterations, int timeout, TimeUnit timeUnit) {
      this.iterations = iterations;
      this.timeout = timeout;
      this.timeUnit = timeUnit;
    }
  }

  private static class TestSubscription {
    private MessageConsumerRedisImpl consumer;
    private ConcurrentLinkedQueue<Integer> messageQueue;
    private Set<Integer> currentPartitions;

    public TestSubscription(MessageConsumerRedisImpl consumer, ConcurrentLinkedQueue<Integer> messageQueue) {
      this.consumer = consumer;
      this.messageQueue = messageQueue;
    }

    public MessageConsumerRedisImpl getConsumer() {
      return consumer;
    }

    public ConcurrentLinkedQueue<Integer> getMessageQueue() {
      return messageQueue;
    }

    public Set<Integer> getCurrentPartitions() {
      return currentPartitions;
    }

    public void setCurrentPartitions(Set<Integer> currentPartitions) {
      this.currentPartitions = currentPartitions;
    }

    public void clearMessages() {
      messageQueue.clear();
    }

    public void close() {
      consumer.close();
      messageQueue.clear();
    }
  }

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  private static final int DEFAULT_MESSAGE_COUNT = 100;
  private static final EventuallyConfig EVENTUALLY_CONFIG = new EventuallyConfig(100, 1, TimeUnit.SECONDS);


  private String destination;
  private String subscriberId;

  @Before
  public void init() {
    destination = "destination" + UUID.randomUUID();
    subscriberId = "subscriber" + UUID.randomUUID();
  }

  @Test
  public void test1Consumer2Partitions() throws Exception {
    TestSubscription subscription = subscribe(2);

    waitForRebalance(ImmutableList.of(subscription), 2);

    sendMessages(2);

    assertMessagesConsumed(subscription);
  }

  @Test
  public void test2Consumers2Partitions() throws Exception {
    TestSubscription subscription1 = subscribe(2);
    TestSubscription subscription2 = subscribe(2);

    waitForRebalance(ImmutableList.of(subscription1, subscription2), 2);

    sendMessages(2);

    assertMessagesConsumed(ImmutableList.of(subscription1, subscription2));
  }

  @Test
  public void test1Consumer2PartitionsThenAddedConsumer() throws Exception {
    TestSubscription testSubscription1 = subscribe(2);

    waitForRebalance(ImmutableList.of(testSubscription1), 2);

    sendMessages(2);

    assertMessagesConsumed(testSubscription1);

    testSubscription1.clearMessages();
    TestSubscription testSubscription2 = subscribe(2);

    waitForRebalance(ImmutableList.of(testSubscription1, testSubscription2), 2);

    sendMessages(2);

    assertMessagesConsumed(ImmutableList.of(testSubscription1, testSubscription2));
  }

  @Test
  public void test2Consumers2PartitionsThenRemovedConsumer() throws Exception {

    TestSubscription testSubscription1 = subscribe(2);
    TestSubscription testSubscription2 = subscribe(2);

    waitForRebalance(ImmutableList.of(testSubscription1, testSubscription2), 2);

    sendMessages(2);

    assertMessagesConsumed(ImmutableList.of(testSubscription1, testSubscription2));

    testSubscription1.clearMessages();
    testSubscription2.close();

    waitForRebalance(ImmutableList.of(testSubscription1), 2);

    sendMessages(2);

    assertMessagesConsumed(testSubscription1);
  }

  @Test
  public void test5Consumers9PartitionsThenRemoved2ConsumersAndAdded3Consumers() throws Exception {

    LinkedList<TestSubscription> testSubscriptions = createConsumersAndSubscribe(5, 9);

    waitForRebalance(testSubscriptions, 9);

    sendMessages(9);

    assertMessagesConsumed(testSubscriptions);

    for (int i = 0; i < 2; i++) {
      testSubscriptions.poll().close();
    }

    testSubscriptions.forEach(TestSubscription::clearMessages);

    testSubscriptions.addAll(createConsumersAndSubscribe(3, 9));

    waitForRebalance(testSubscriptions, 9);

    sendMessages(9);

    assertMessagesConsumed(testSubscriptions);
  }

  @Test
  public void testReassignment() throws Exception {
    for (int i = 0; i < 10; i++) {
      logger.info("testReassignment iteration {}", i);
      
      destination = "destination1";
      subscriberId = "subscriber1";

      TestSubscription testSubscription1 = subscribe(2);

      waitForRebalance(ImmutableList.of(testSubscription1), 2);

      sendMessages(2);

      try {
        assertMessagesConsumed(testSubscription1);
      } catch (Throwable t){
        testSubscription1.close();
        throw t;
      }

      testSubscription1.clearMessages();
      TestSubscription testSubscription2 = subscribe(2);

      waitForRebalance(ImmutableList.of(testSubscription1, testSubscription2), 2);

      sendMessages(2);

      try {
        assertMessagesConsumed(ImmutableList.of(testSubscription1, testSubscription2));
      } finally {
        testSubscription1.close();
        testSubscription2.close();
      }
    }
  }

  private void assertMessagesConsumed(TestSubscription testSubscription) {
    assertMessagesConsumed(testSubscription, DEFAULT_MESSAGE_COUNT);
  }

  private void assertMessagesConsumed(TestSubscription testSubscription, int messageCount) {
    Eventually.eventually(EVENTUALLY_CONFIG.iterations,
            EVENTUALLY_CONFIG.timeout,
            EVENTUALLY_CONFIG.timeUnit,
            () -> Assert.assertEquals(String.format("consumer %s did not receive expected messages", testSubscription.getConsumer().consumerId),
                    messageCount,
                    testSubscription.messageQueue.size()));
  }

  private void assertMessagesConsumed(List<TestSubscription> testSubscriptions) {
    assertMessagesConsumed(testSubscriptions, DEFAULT_MESSAGE_COUNT);
  }

  private void assertMessagesConsumed(List<TestSubscription> testSubscriptions, int messageCount) {
    Eventually.eventually(EVENTUALLY_CONFIG.iterations,
            EVENTUALLY_CONFIG.timeout,
            EVENTUALLY_CONFIG.timeUnit,
            () -> {

      List<TestSubscription> emptySubscriptions = testSubscriptions
              .stream()
              .filter(testSubscription -> testSubscription.messageQueue.isEmpty())
              .collect(Collectors.toList());

      emptySubscriptions.forEach(testSubscription -> logger.info("[{}] consumer is empty", testSubscription.getConsumer().consumerId));

      Assert.assertTrue(emptySubscriptions.isEmpty());

      Assert.assertEquals((long) messageCount,
              (long) testSubscriptions
                      .stream()
                      .map(testSubscription -> testSubscription.getMessageQueue().size())
                      .reduce((a, b) -> a + b)
                      .orElse(0));
    });
  }

  private void waitForRebalance(List<TestSubscription> subscriptions, int totalPartitions) throws Exception {
    Eventually.eventually(EVENTUALLY_CONFIG.iterations,
            EVENTUALLY_CONFIG.timeout,
            EVENTUALLY_CONFIG.timeUnit,
            () -> {
              Assert.assertTrue(subscriptions
                      .stream()
                      .noneMatch(testSubscription ->
                              testSubscription.getCurrentPartitions() == null ||
                                      testSubscription.getCurrentPartitions().isEmpty()));

              List<Integer> allPartitions = subscriptions
                      .stream()
                      .map(TestSubscription::getCurrentPartitions)
                      .flatMap(Collection::stream)
                      .collect(Collectors.toList());

              Set<Integer> uniquePartitions = new HashSet<>(allPartitions);

              Assert.assertEquals(allPartitions.size(), uniquePartitions.size());
              Assert.assertEquals(totalPartitions, uniquePartitions.size());
            });
  }

  private LinkedList<TestSubscription> createConsumersAndSubscribe(int consumerCount, int partitionCount) {

    LinkedList<TestSubscription> subscriptions = new LinkedList<>();

    for (int i = 0; i < consumerCount; i++) {
      subscriptions.add(subscribe(partitionCount));
    }

    return subscriptions;
  }

  private TestSubscription subscribe(int partitionCount) {
    ConcurrentLinkedQueue<Integer> messageQueue = new ConcurrentLinkedQueue<>();

    MessageConsumerRedisImpl consumer = createConsumer(partitionCount);

    consumer.subscribe(subscriberId, ImmutableSet.of(destination), message ->
            messageQueue.add(Integer.parseInt(message.getPayload())));

    TestSubscription testSubscription = new TestSubscription(consumer, messageQueue);

    consumer.setSubscriptionLifecycleHook((channel, subscriptionId, currentPartitions) -> {
      System.out.println(channel + " ---$--- " + subscriptionId + " ---$--- " + currentPartitions);
      testSubscription.setCurrentPartitions(currentPartitions);
    });

    return testSubscription;
  }

  private MessageConsumerRedisImpl createConsumer(int partitionCount) {
    MessageConsumerRedisImpl messageConsumerRedis = new MessageConsumerRedisImpl(redisTemplate, partitionCount);
    applicationContext.getAutowireCapableBeanFactory().autowireBean(messageConsumerRedis);
    return messageConsumerRedis;
  }

  private void sendMessages(int partitions) {
    sendMessages(DEFAULT_MESSAGE_COUNT, partitions);
  }

  private void sendMessages(int messageCount, int partitions) {
    EventuateRedisProducer eventuateRedisProducer = new EventuateRedisProducer(redisTemplate, partitions);

    for (int i = 0; i < messageCount; i++) {
      eventuateRedisProducer.send(destination,
              String.valueOf(i),
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", UUID.randomUUID().toString()))));
    }
  }
}
