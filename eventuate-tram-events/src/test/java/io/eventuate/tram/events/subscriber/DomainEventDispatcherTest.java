package io.eventuate.tram.events.subscriber;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.publisher.DomainEventPublisherImpl;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class DomainEventDispatcherTest {

  private String eventDispatcherId;
  private String aggregateType =  "AggregateType";

  private String aggregateId = "xyz";
  private String messageId = "message-" + System.currentTimeMillis();

  class MyTarget {

    public BlockingQueue<DomainEventEnvelope<?>> queue = new LinkedBlockingDeque<>();

    @DomainEventHandler
    public void handleAccountDebited(DomainEventEnvelope<MyDomainEvent> event) {
      queue.add(event);
    }

  }


  static class MyDomainEvent implements DomainEvent {

  }

  private Map<String, Set<String>> aggregateTypesAndEvents = Collections.singletonMap(aggregateType,
          Collections.singleton(MyDomainEvent.class.getName()));

  @Test
  public void shouldDispatchMessage() {
    MyTarget target = new MyTarget();

    MessageConsumer messageConsumer = mock(MessageConsumer.class);

    DomainEventDispatcher dispatcher =
            new DomainEventDispatcher(eventDispatcherId, aggregateTypesAndEvents, target, messageConsumer);

    dispatcher.initialize();


    dispatcher.messageHandler(DomainEventPublisherImpl.makeMessageForDomainEvent(aggregateType,
            aggregateId,
            Collections.singletonMap(Message.ID, messageId),
            new MyDomainEvent()));

    DomainEventEnvelope<?> dee = target.queue.peek();

    assertNotNull(dee);

    assertEquals(aggregateId, dee.getAggregateId());
    assertEquals(aggregateType, dee.getAggregateType());
    assertEquals(messageId, dee.getEventId());

  }
}