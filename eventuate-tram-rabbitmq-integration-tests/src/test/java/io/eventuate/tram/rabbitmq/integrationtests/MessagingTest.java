package io.eventuate.tram.rabbitmq.integrationtests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.rabbitmq.MessageConsumerRabbitMQImpl;
import io.eventuate.tram.data.producer.rabbitmq.EventuateRabbitMQProducer;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.util.test.async.Eventually;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MessagingTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MessagingTest {

  @Configuration
  @EnableAutoConfiguration
  public static class Config {
    @Bean
    public EventuateRabbitMQProducer rabbitMQMessageProducer(@Value("${rabbitmq.url}") String rabbitMQURL) {
      return new EventuateRabbitMQProducer(rabbitMQURL);
    }

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

  @Value("${rabbitmq.url}")
  private String rabbitMQURL;

  @Value("${eventuatelocal.zookeeper.connection.string}")
  private String zkUrl;

  @Autowired
  private EventuateRabbitMQProducer eventuateRabbitMQProducer;

  @Autowired
  private ApplicationContext applicationContext;

  private static final int MESSAGE_COUNT = 100;
  private static final int REBALANCE_TIMEOUT_IN_MILLIS = 3000;
  private static final EventuallyConfig EVENTUALLY_CONFIG = new EventuallyConfig(30, 1, TimeUnit.SECONDS);


  private String destination;
  private String subscriberId;

  @Before
  public void init() {
    destination = "destination" + UUID.randomUUID();
    subscriberId = "subscriber" + UUID.randomUUID();
  }

  @Test
  public void test1Consumer2Partitions() throws Exception {
    ConcurrentLinkedQueue<Integer> messageQueue = new ConcurrentLinkedQueue<>();

    createConsumerAndSubscribe(2, messageQueue);

    waitForRebalance();

    sendMessages();

    assertMessagesConsumed(messageQueue);
  }

  @Test
  public void test2Consumers2Partitions() throws Exception {
    ConcurrentLinkedQueue<Integer> messageQueue1 = new ConcurrentLinkedQueue<>();
    ConcurrentLinkedQueue<Integer> messageQueue2 = new ConcurrentLinkedQueue<>();

    createConsumerAndSubscribe(2, messageQueue1);
    createConsumerAndSubscribe(2, messageQueue2);

    waitForRebalance();

    sendMessages();

    assertMessagesConsumed(ImmutableList.of(messageQueue1, messageQueue2));
  }

  @Test
  public void test1Consumer2PartitionsThenAddedConsumer() throws Exception {
    ConcurrentLinkedQueue<Integer> messageQueue1 = new ConcurrentLinkedQueue<>();

    createConsumerAndSubscribe(2, messageQueue1);

    waitForRebalance();

    sendMessages();

    assertMessagesConsumed(messageQueue1);

    messageQueue1.clear();
    ConcurrentLinkedQueue<Integer> messageQueue2 = new ConcurrentLinkedQueue<>();

    createConsumerAndSubscribe(2, messageQueue2);

    waitForRebalance();

    sendMessages();

    assertMessagesConsumed(ImmutableList.of(messageQueue1, messageQueue2));
  }

  @Test
  public void test2Consumers2PartitionsThenRemovedConsumer() throws Exception {
    ConcurrentLinkedQueue<Integer> messageQueue1 = new ConcurrentLinkedQueue<>();
    ConcurrentLinkedQueue<Integer> messageQueue2 = new ConcurrentLinkedQueue<>();

    createConsumerAndSubscribe(2, messageQueue1);
    MessageConsumerRabbitMQImpl consumer2 =  createConsumerAndSubscribe(2, messageQueue2);

    waitForRebalance();

    sendMessages();

    assertMessagesConsumed(ImmutableList.of(messageQueue1, messageQueue2));

    messageQueue1.clear();
    closeConsumer(consumer2, messageQueue2);

    waitForRebalance();

    sendMessages();

    assertMessagesConsumed(messageQueue1);
  }

  @Test
  public void test5Consumers9PartitionsThenRemoved2ConsumersAndAdded3Consumers() throws Exception {
    LinkedList<ConcurrentLinkedQueue<Integer>> messageQueues = new LinkedList<>();
    LinkedList<MessageConsumerRabbitMQImpl> consumers = new LinkedList<>();

    createConsumersAndSubscribe(5, 9, consumers, messageQueues);

    waitForRebalance();

    sendMessages();

    assertMessagesConsumed(messageQueues);

    closeConsumers(2, consumers, messageQueues);
    messageQueues.forEach(AbstractQueue::clear);
    createConsumersAndSubscribe(3, 9, consumers, messageQueues);

    waitForRebalance();

    sendMessages();

    assertMessagesConsumed(messageQueues);
  }

  private void assertMessagesConsumed(ConcurrentLinkedQueue<Integer> messageQueue) {
    Eventually.eventually(EVENTUALLY_CONFIG.iterations,
            EVENTUALLY_CONFIG.timeout,
            EVENTUALLY_CONFIG.timeUnit,
            () -> Assert.assertEquals(MESSAGE_COUNT, messageQueue.size()));
  }

  private void assertMessagesConsumed(List<ConcurrentLinkedQueue<Integer>> messageQueues) {
    Eventually.eventually(EVENTUALLY_CONFIG.iterations,
            EVENTUALLY_CONFIG.timeout,
            EVENTUALLY_CONFIG.timeUnit,
            () -> {
      Assert.assertTrue(messageQueues.stream().noneMatch(ConcurrentLinkedQueue::isEmpty));
      Assert.assertEquals((long) MESSAGE_COUNT,
              (long) messageQueues.stream().map(ConcurrentLinkedQueue::size).reduce((a, b) -> a + b).orElse(0));
    });
  }

  private void waitForRebalance() throws Exception {
    Thread.sleep(REBALANCE_TIMEOUT_IN_MILLIS);
  }

  private void createConsumersAndSubscribe(int consumerCount,
                                           int partitionCount,
                                           LinkedList<MessageConsumerRabbitMQImpl> consumers,
                                           LinkedList<ConcurrentLinkedQueue<Integer>> messageQueues) {

    for (int i = 0; i < consumerCount; i++) {
      ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
      messageQueues.add(concurrentLinkedQueue);
      consumers.add(createConsumerAndSubscribe(partitionCount, concurrentLinkedQueue));
    }
  }

  private MessageConsumerRabbitMQImpl createConsumerAndSubscribe(int partitionCount, ConcurrentLinkedQueue<Integer> messageQueue) {
    MessageConsumerRabbitMQImpl consumer = createConsumer(partitionCount);

    consumer.subscribe(subscriberId, ImmutableSet.of(destination), message ->
            messageQueue.add(Integer.parseInt(message.getPayload())));

    return consumer;
  }

  private MessageConsumerRabbitMQImpl createConsumer(int partitionCount) {
    MessageConsumerRabbitMQImpl messageConsumerRabbitMQ = new MessageConsumerRabbitMQImpl(rabbitMQURL,zkUrl, partitionCount);
    applicationContext.getAutowireCapableBeanFactory().autowireBean(messageConsumerRabbitMQ);
    return messageConsumerRabbitMQ;
  }

  private void sendMessages() {
    for (int i = 0; i < MESSAGE_COUNT; i++) {
      eventuateRabbitMQProducer.send(destination,
              String.valueOf(Math.random()),
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", UUID.randomUUID().toString()))));
    }
  }

  private void closeConsumers(int count,
                              LinkedList<MessageConsumerRabbitMQImpl> consumers,
                              LinkedList<ConcurrentLinkedQueue<Integer>> messageQueues) {

    for (int i = 0; i < count; i++) {
      Assert.assertFalse(consumers.isEmpty());
      Assert.assertFalse(messageQueues.isEmpty());
      closeConsumer(consumers.poll(), messageQueues.poll());
    }
  }

  private void closeConsumer(MessageConsumerRabbitMQImpl consumer, ConcurrentLinkedQueue<Integer> messageQueue) {
    consumer.close();
    messageQueue.clear();
  }
}
