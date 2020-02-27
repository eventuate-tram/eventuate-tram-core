package io.eventuate.tram.testing;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.spring.commands.producer.TramCommandProducerConfiguration;
import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.spring.events.publisher.TramEventsPublisherConfiguration;
import io.eventuate.tram.spring.inmemory.TramInMemoryConfiguration;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=MessageTrackerTest.MessageTrackerTestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MessageTrackerTest {

  private static String channel = "x";
  private static Set<String> channels = Collections.singleton(channel);
  private String aggregateType = channel;
  private Object aggregateId = "aggregate-id";

  @Configuration
  @EnableAutoConfiguration
  @Import({TramInMemoryConfiguration.class, TramCommandProducerConfiguration.class, TramEventsPublisherConfiguration.class})
  public static  class MessageTrackerTestConfiguration {

    @Bean
    public MessageTracker messageTracker(MessageConsumer messageConsumer) {
      return new MessageTracker(channels, messageConsumer);
    }
  }

  @Autowired
  private CommandProducer commandProducer;

  @Autowired
  private DomainEventPublisher domainEventPublisher;

  @Autowired
  private MessageTracker messageTracker;

  class MyCommand implements Command {

  }


  @Test
  public void shouldAssertCommandMessageSent() {
    commandProducer.send(channel, new MyCommand(), "my-reply-to", Collections.emptyMap());
    messageTracker.assertCommandMessageSent(channel, MyCommand.class);
  }

  @Test
  public void shouldAssertDomainEventPublished() {
    domainEventPublisher.publish(aggregateType, aggregateId, Collections.singletonList(new MyDomainEvent()));
    messageTracker.assertDomainEventPublished(channel, MyDomainEvent.class);
  }

  private class MyDomainEvent implements DomainEvent {
  }
}