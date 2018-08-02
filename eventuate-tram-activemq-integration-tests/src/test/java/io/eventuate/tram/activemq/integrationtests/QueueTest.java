package io.eventuate.tram.activemq.integrationtests;

import com.google.common.collect.ImmutableSet;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.activemq.MessageConsumerActiveMQImpl;
import io.eventuate.tram.data.producer.activemq.EventuateActiveMQProducer;
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
@SpringBootTest(classes = QueueTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD) //to generate unique topic name for each test
public class QueueTest {

  @Configuration
  @Import(CommonQueueTopicTestConfiguration.class)
  public static class Config {
    @Qualifier("testChannelType")
    @Bean
    public ChannelType testChannelType() {
      return ChannelType.QUEUE;
    }
  }

  @Autowired
  private MessageConsumerActiveMQImpl messageConsumerActiveMQ;

  @Autowired
  private EventuateActiveMQProducer eventuateActiveMQProducer;

  @Autowired
  private String uniquePostfix;

  @Test
  public void testSeveralSubscribersWithPersistence() {
    int messages = 10;
    int consumers = 2;
    String destination = "destination" + uniquePostfix;

    String key = "key";

    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    for (int i = 0; i < messages; i++) {
      eventuateActiveMQProducer.send(destination,
              key,
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", UUID.randomUUID().toString()))));
    }

    for (int i = 0; i < consumers; i ++) {
      messageConsumerActiveMQ.subscribe("subscriber" + i, ImmutableSet.of(destination), message ->
              concurrentLinkedQueue.add(Integer.parseInt(message.getPayload())));
    }

    Eventually.eventually(() -> Assert.assertEquals(messages, concurrentLinkedQueue.size()));
  }

  @Test
  public void testJMSXGroupIdOrdering() {
    int messages = 100;
    int consumers = 5;
    String destination = "destination" + uniquePostfix;

    String key = "key";

    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    for (int i = 0; i < consumers; i ++) {
      messageConsumerActiveMQ.subscribe("subscriber", ImmutableSet.of(destination), message ->
              concurrentLinkedQueue.add(Integer.parseInt(message.getPayload())));
    }

    for (int i = 0; i < messages; i++) {
      eventuateActiveMQProducer.send(destination,
              key,
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", UUID.randomUUID().toString()))));
    }

    Eventually.eventually(() -> Assert.assertEquals(messages, concurrentLinkedQueue.size()));

    for (int i = 0; i < messages; i++) {
      Assert.assertEquals(i, (int)concurrentLinkedQueue.poll());
    }
  }
}
