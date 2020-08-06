package io.eventuate.tram.testing;

import io.eventuate.tram.events.common.DefaultDomainEventNameMapping;
import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.events.publisher.DomainEventPublisherImpl;
import io.eventuate.tram.events.subscriber.DomainEventDispatcher;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.DomainEventHandlers;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.util.SimpleIdGenerator;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.mockito.Mockito.verify;

/**
 * Provides a DSL for writing unit tests for domain event handlers
 */
public class DomainEventHandlerUnitTestSupport {
  private MessageHandler handler;
  private String aggregateType;
  private Object aggregateId;
  private DomainEventDispatcher dispatcher;
  private SimpleIdGenerator idGenerator = new SimpleIdGenerator();

  public static DomainEventHandlerUnitTestSupport given() {
    return new DomainEventHandlerUnitTestSupport();
  }


  public DomainEventHandlerUnitTestSupport eventHandlers(DomainEventHandlers domainEventHandlers) {

    this.dispatcher = new DomainEventDispatcher("MockId", domainEventHandlers, new MessageConsumer() {
      @Override
      public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
        DomainEventHandlerUnitTestSupport.this.handler = handler;
        return () -> {};
      }

      @Override
      public String getId() {
        return null;
      }

      @Override
      public void close() {

      }
    }, new DefaultDomainEventNameMapping());

    dispatcher.initialize();
    return this;
  }

  public DomainEventHandlerUnitTestSupport when() {
    return this;
  }

  public DomainEventHandlerUnitTestSupport then() {
    return this;
  }

  public DomainEventHandlerUnitTestSupport aggregate(String aggregateType, Object aggregateId) {
    this.aggregateType = aggregateType;
    this.aggregateId = aggregateId;
    return this;
  }


  public DomainEventHandlerUnitTestSupport publishes(DomainEvent event) {
    DomainEventPublisher publisher = new DomainEventPublisherImpl((destination, message) -> {
      String id = idGenerator.generateId().toString();
      message.getHeaders().put(Message.ID, id);
      handler.accept(message);
    }, new DefaultDomainEventNameMapping());

    publisher.publish(aggregateType, aggregateId, Collections.singletonList(event));
    return this;
  }

  public DomainEventHandlerUnitTestSupport verify(Runnable r) {
    r.run();
    return this;
  }

  public <EH, EV extends DomainEvent> DomainEventHandlerUnitTestSupport expectEventHandlerInvoked(EH eventHandlers, BiConsumer<EH, DomainEventEnvelope<EV>> c, Consumer<DomainEventEnvelope<EV>> consumer) {
    ArgumentCaptor<DomainEventEnvelope<EV>> arg = ArgumentCaptor.forClass(DomainEventEnvelope.class);
    c.accept(Mockito.verify(eventHandlers), arg.capture());
    consumer.accept(arg.getValue());
    return this;
  }

}
