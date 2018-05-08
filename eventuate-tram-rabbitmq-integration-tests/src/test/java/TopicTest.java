import com.google.common.collect.ImmutableSet;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.rabbitmq.MessageConsumerRabbitMQImpl;
import io.eventuate.tram.data.producer.rabbitmq.EventuateRabbitMQProducer;
import io.eventuate.tram.messaging.common.ChannelType;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.util.test.async.Eventually;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TopicTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD) //to generate unique topic name for each test
public class TopicTest {

  @Configuration
  @Import(CommonQueueTopicTestConfiguration.class)
  public static class Config {
    @Qualifier("testChannelType")
    @Bean
    public ChannelType testChannelType() {
      return ChannelType.TOPIC;
    }
  }

  @Autowired
  private MessageConsumerRabbitMQImpl messageConsumerRabbitMQ;

  @Autowired
  private EventuateRabbitMQProducer eventuateRabbitMQProducer;

  @Autowired
  private String uniquePostfix;

  @Test
  public void testSeveralSubscribersForDefinedTopic() {
    String topic = "destination" + uniquePostfix;
    testSeveralSubscribers(topic);
  }

  @Test
  public void testSeveralSubsribersDefaultMode() {
    String topic = "not_specfied_destination" + uniquePostfix;
    testSeveralSubscribers(topic);
  }

//  @Test
  public void testJMSGroupIdOrdering() {
    int messages = 100;
    int consumers = 5;

    String destination = "destination" + uniquePostfix;
    String key = "key";

    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    for (int i = 0; i < consumers; i ++) {
      messageConsumerRabbitMQ.subscribe("subscriber", ImmutableSet.of(destination), message ->
              concurrentLinkedQueue.add(Integer.parseInt(message.getPayload())));
    }

    for (int i = 0; i < messages; i++) {
      eventuateRabbitMQProducer.send(destination,
              key,
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", UUID.randomUUID().toString()))));
    }

    Eventually.eventually(() -> Assert.assertEquals(messages, concurrentLinkedQueue.size()));

    for (int i = 0; i < messages; i++) {
      Assert.assertEquals(i, (int)concurrentLinkedQueue.poll());
    }
  }

  private void testSeveralSubscribers(String destination) {
    int messages = 10;
    int consumers = 2;

    String key = "key";

    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    for (int i = 0; i < consumers; i ++) {
      messageConsumerRabbitMQ.subscribe("subscriber" + i, ImmutableSet.of(destination), message ->
              concurrentLinkedQueue.add(Integer.parseInt(message.getPayload())));
    }

    for (int i = 0; i < messages; i++) {
      eventuateRabbitMQProducer.send(destination,
              key,
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", UUID.randomUUID().toString()))));
    }

    Eventually.eventually(() -> Assert.assertEquals(messages * consumers, concurrentLinkedQueue.size()));
  }
}
