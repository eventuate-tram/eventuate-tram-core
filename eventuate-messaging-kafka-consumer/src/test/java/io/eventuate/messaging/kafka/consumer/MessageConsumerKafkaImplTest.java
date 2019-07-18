package io.eventuate.messaging.kafka.consumer;

import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.messaging.kafka.basic.consumer.EventuateKafkaBasicConsumer;
import io.eventuate.messaging.kafka.basic.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.messaging.kafka.basic.consumer.EventuateKafkaConsumerMessageHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.eventuate.util.test.async.Eventually;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MessageConsumerKafkaImplTest.Config.class,
        properties = "eventuate.local.kafka.consumer.backPressure.high=3")
public class MessageConsumerKafkaImplTest {

  @Configuration
  @EnableAutoConfiguration
  @EnableConfigurationProperties({EventuateKafkaConsumerConfigurationProperties.class, EventuateKafkaProducerConfigurationProperties.class})
  @Import(MessageConsumerKafkaConfiguration.class)
  public static class Config {

    @Bean
    public EventuateKafkaProducer producer(EventuateKafkaConfigurationProperties kafkaProperties, EventuateKafkaProducerConfigurationProperties producerProperties) {
      return new EventuateKafkaProducer(kafkaProperties.getBootstrapServers(), producerProperties);
    }


  }

  @Autowired
  private EventuateKafkaConfigurationProperties kafkaProperties;

  @Autowired
  private EventuateKafkaConsumerConfigurationProperties consumerProperties;

  @Autowired
  private EventuateKafkaProducer producer;

  @Autowired
  private EventuateKafkaConsumer consumer;


  @Test
  public void shouldConsumeMessages() {
    String subscriberId = "subscriber-" + System.currentTimeMillis();
    String topic = "topic-" + System.currentTimeMillis();

    sendMessages(topic);

    KafkaMessageHandler handler = mock(KafkaMessageHandler.class);

    KafkaSubscription subscription = consumer.subscribe(subscriberId, Collections.singleton(topic), handler);

    Eventually.eventually(() -> {
      verify(handler, atLeastOnce()).accept(ArgumentMatchers.any());
    });

    subscription.close();

  }

  private EventuateKafkaBasicConsumer makeConsumer(String subscriberId, String topic, EventuateKafkaConsumerMessageHandler handler) {
    EventuateKafkaBasicConsumer consumer = new EventuateKafkaBasicConsumer(subscriberId, handler, Collections.singletonList(topic), kafkaProperties.getBootstrapServers(), consumerProperties);

    consumer.start();
    return consumer;
  }

  private void sendMessages(String topic) {
    producer.send(topic, null, "a");
    producer.send(topic, null, "b");
  }

  @Test
  public void shouldConsumeMessagesWithBackPressure() {
    String subscriberId = "subscriber-" + System.currentTimeMillis();
    String topic = "topic-" + System.currentTimeMillis();
    LinkedBlockingQueue<KafkaMessage> messages = new LinkedBlockingQueue<>();

    for (int i = 0 ; i < 100; i++)
      sendMessages(topic);

    KafkaMessageHandler handler = kafkaMessage -> {
      try {
        TimeUnit.MILLISECONDS.sleep(20);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      messages.add(kafkaMessage);
    };

    KafkaSubscription subscription = consumer.subscribe(subscriberId, Collections.singleton(topic), handler);

    Eventually.eventually(() -> {
      assertEquals(200, messages.size());
    });

    subscription.close();

  }

}