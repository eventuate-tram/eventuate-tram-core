import com.google.common.collect.ImmutableSet;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.rabbitmq.MessageConsumerRabbitMQImpl;
import io.eventuate.tram.data.producer.rabbitmq.EventuateRabbitMQProducer;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.util.test.async.Eventually;
import org.junit.Assert;
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

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MessagingTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MessagingTest {

  @Configuration
  @EnableAutoConfiguration
  public static class Config {
    @Bean
    public EventuateRabbitMQProducer rabbitMQMessageProducer(@Value("${rabbitmq.url}") String rabbitMQURL) {
      return new EventuateRabbitMQProducer(rabbitMQURL,
              Collections.emptyMap());
    }

    @Bean
    public DuplicateMessageDetector duplicateMessageDetector() {
      return (consumerId, messageId) -> false;
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

  @Test
  public void testOneConsumerTwoQueues() throws Exception {
    int messages = 100;
    String destination = "destination" + UUID.randomUUID();

    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    createConsumer().subscribe("subscriber", ImmutableSet.of(destination), message ->
            concurrentLinkedQueue.add(Integer.parseInt(message.getPayload())));

    Thread.sleep(3000);

    for (int i = 0; i < messages; i++) {
      eventuateRabbitMQProducer.send(destination,
              String.valueOf(Math.random()),
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", UUID.randomUUID().toString()))));
    }

    Eventually.eventually(() -> Assert.assertEquals(messages, concurrentLinkedQueue.size()));
  }

  @Test
  public void testTwoSubscribersTwoQueues() throws Exception {
    int messages = 100;
    String destination = "destination" + UUID.randomUUID();

    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue1 = new ConcurrentLinkedQueue<>();
    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue2 = new ConcurrentLinkedQueue<>();

    createConsumer().subscribe("subscriber", ImmutableSet.of(destination), message ->
            concurrentLinkedQueue1.add(Integer.parseInt(message.getPayload())));

    createConsumer().subscribe("subscriber", ImmutableSet.of(destination), message ->
            concurrentLinkedQueue2.add(Integer.parseInt(message.getPayload())));

    Thread.sleep(3000);

    for (int i = 0; i < messages; i++) {
      eventuateRabbitMQProducer.send(destination,
              String.valueOf(Math.random()),
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", UUID.randomUUID().toString()))));
    }

    Eventually.eventually(() -> {
      Assert.assertFalse(concurrentLinkedQueue1.isEmpty());
      Assert.assertFalse(concurrentLinkedQueue2.isEmpty());
      Assert.assertEquals(messages, concurrentLinkedQueue1.size() + concurrentLinkedQueue2.size());
    });
 }

  @Test
  public void testOneConsumerTwoQueuesThenTwoConsumersTwoQueues() throws Exception {
    int messages = 100;
    String destination = "destination" + UUID.randomUUID();

    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue1 = new ConcurrentLinkedQueue<>();

    createConsumer().subscribe("subscriber", ImmutableSet.of(destination), message ->
            concurrentLinkedQueue1.add(Integer.parseInt(message.getPayload())));

    Thread.sleep(3000);

    for (int i = 0; i < messages; i++) {
      eventuateRabbitMQProducer.send(destination,
              String.valueOf(Math.random()),
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", UUID.randomUUID().toString()))));
    }

    Eventually.eventually(() -> Assert.assertEquals(messages, concurrentLinkedQueue1.size()));
    concurrentLinkedQueue1.clear();

    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue2 = new ConcurrentLinkedQueue<>();

    createConsumer().subscribe("subscriber", ImmutableSet.of(destination), message ->
            concurrentLinkedQueue2.add(Integer.parseInt(message.getPayload())));

    Thread.sleep(3000);

    for (int i = 0; i < messages; i++) {
      eventuateRabbitMQProducer.send(destination,
              String.valueOf(Math.random()),
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", UUID.randomUUID().toString()))));
    }

    Eventually.eventually(() -> {
      Assert.assertFalse(concurrentLinkedQueue1.isEmpty());
      Assert.assertFalse(concurrentLinkedQueue2.isEmpty());
      Assert.assertEquals(messages, concurrentLinkedQueue1.size() + concurrentLinkedQueue2.size());
    });
  }

  @Test
  public void testTwoSubscribersTwoQueuesThenOneSubscriberTwoQueues() throws Exception {
    int messages = 100;
    String destination = "destination" + UUID.randomUUID();

    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue1 = new ConcurrentLinkedQueue<>();
    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue2 = new ConcurrentLinkedQueue<>();

    MessageConsumerRabbitMQImpl consumer1 = createConsumer();
    consumer1.subscribe("subscriber", ImmutableSet.of(destination), message ->
            concurrentLinkedQueue1.add(Integer.parseInt(message.getPayload())));

    MessageConsumerRabbitMQImpl consumer2 = createConsumer();
    consumer2.subscribe("subscriber", ImmutableSet.of(destination), message ->
            concurrentLinkedQueue2.add(Integer.parseInt(message.getPayload())));

    Thread.sleep(3000);

    for (int i = 0; i < messages; i++) {
      eventuateRabbitMQProducer.send(destination,
              String.valueOf(Math.random()),
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", UUID.randomUUID().toString()))));
    }

    Eventually.eventually(() -> {
      Assert.assertFalse(concurrentLinkedQueue1.isEmpty());
      Assert.assertFalse(concurrentLinkedQueue2.isEmpty());
      Assert.assertEquals(messages, concurrentLinkedQueue1.size() + concurrentLinkedQueue2.size());
    });

    concurrentLinkedQueue1.clear();
    concurrentLinkedQueue2.clear();

    consumer2.close();

    Thread.sleep(5000);

    for (int i = 0; i < messages; i++) {
      eventuateRabbitMQProducer.send(destination,
              String.valueOf(Math.random()),
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", UUID.randomUUID().toString()))));
    }

    Eventually.eventually(() -> {
      Assert.assertTrue(concurrentLinkedQueue2.isEmpty());
      Assert.assertEquals(messages, concurrentLinkedQueue1.size());
    });
  }

  private MessageConsumerRabbitMQImpl createConsumer() {
    MessageConsumerRabbitMQImpl messageConsumerRabbitMQ = new MessageConsumerRabbitMQImpl(rabbitMQURL,zkUrl, 2);
    applicationContext.getAutowireCapableBeanFactory().autowireBean(messageConsumerRabbitMQ);
    return messageConsumerRabbitMQ;
  }
}
