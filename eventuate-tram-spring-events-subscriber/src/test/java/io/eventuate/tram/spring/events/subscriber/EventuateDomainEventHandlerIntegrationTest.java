package io.eventuate.tram.spring.events.subscriber;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;
import io.eventuate.tram.spring.events.common.TramEventsCommonAutoConfiguration;
import io.eventuate.tram.spring.events.publisher.TramEventsPublisherConfiguration;
import io.eventuate.tram.spring.inmemory.TramInMemoryConfiguration;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EventuateDomainEventHandlerIntegrationTest.TestConfiguration.class)
public class EventuateDomainEventHandlerIntegrationTest {

  @Configuration
  @EnableAutoConfiguration
  @Import({
      TramEventsCommonAutoConfiguration.class,
      TramEventsPublisherConfiguration.class,
      TramEventSubscriberConfiguration.class,
      TramInMemoryConfiguration.class
  })
  public static class TestConfiguration {

    @Bean
    public TestEventHandler testEventHandler() {
      return new TestEventHandler();
    }
  }

  public static class TestEvent implements DomainEvent {
    private String message;

    public TestEvent() {}

    public TestEvent(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }

  public static class TestEventHandler {
    private final BlockingQueue<TestEvent> events = new LinkedBlockingQueue<>();

    @EventuateDomainEventHandler(subscriberId = "testSubscriber", channel = "testChannel")
    public void handleTestEvent(DomainEventEnvelope<TestEvent> envelope) {
      events.add(envelope.getEvent());
    }

    public TestEvent getEvent() throws InterruptedException {
      return events.poll(10, TimeUnit.SECONDS);
    }
  }

  @Autowired
  private DomainEventPublisher eventPublisher;

  @Autowired
  private TestEventHandler eventHandler;

  @Test
  public void shouldHandleEvent() throws InterruptedException {
    // Given
    String testMessage = "Test Message";
    TestEvent event = new TestEvent(testMessage);

    // When
    eventPublisher.publish("testChannel", "123", Collections.singletonList(event));

    // Then
    TestEvent receivedEvent = eventHandler.getEvent();
    assertNotNull(receivedEvent);
    assertEquals(testMessage, receivedEvent.getMessage());
  }
}
