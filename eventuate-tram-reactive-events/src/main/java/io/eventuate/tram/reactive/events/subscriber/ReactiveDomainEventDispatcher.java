package io.eventuate.tram.reactive.events.subscriber;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.events.common.EventMessageHeaders;
import io.eventuate.tram.events.subscriber.DomainEventEnvelopeImpl;
import io.eventuate.tram.messaging.common.Message;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class ReactiveDomainEventDispatcher {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final String eventDispatcherId;
  private ReactiveDomainEventHandlers domainEventHandlers;
  private ReactiveMessageConsumer messageConsumer;

  private DomainEventNameMapping domainEventNameMapping;

  public ReactiveDomainEventDispatcher(String eventDispatcherId,
                                       ReactiveDomainEventHandlers domainEventHandlers,
                                       ReactiveMessageConsumer messageConsumer,
                                       DomainEventNameMapping domainEventNameMapping) {

    this.eventDispatcherId = eventDispatcherId;
    this.domainEventHandlers = domainEventHandlers;
    this.messageConsumer = messageConsumer;
    this.domainEventNameMapping = domainEventNameMapping;
  }

  public void initialize() {
    logger.info("Initializing reactive domain event dispatcher");
    messageConsumer.subscribe(eventDispatcherId, domainEventHandlers.getAggregateTypesAndEvents(), this::messageHandler);
    logger.info("Initialized reactive domain event dispatcher");
  }

  public Publisher<?> messageHandler(Message message) {
    String aggregateType = message.getRequiredHeader(EventMessageHeaders.AGGREGATE_TYPE);

    message.setHeader(EventMessageHeaders.EVENT_TYPE,
            domainEventNameMapping.externalEventTypeToEventClassName(aggregateType, message.getRequiredHeader(EventMessageHeaders.EVENT_TYPE)));

    Optional<ReactiveDomainEventHandler> handler = domainEventHandlers.findTargetMethod(message);

    if (!handler.isPresent()) {
      return Mono.empty();
    }

    DomainEvent param = JSonMapper.fromJson(message.getPayload(), handler.get().getEventClass());

    return handler.get().invoke(new DomainEventEnvelopeImpl<>(message,
            aggregateType,
            message.getRequiredHeader(EventMessageHeaders.AGGREGATE_ID),
            message.getRequiredHeader(Message.ID),
            param));
  }


}
