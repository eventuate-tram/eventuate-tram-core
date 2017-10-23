package io.eventuate.tram.events.subscriber;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.common.EventMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Optional;

public class DomainEventDispatcher {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final String eventDispatcherId;
  private DomainEventHandlers domainEventHandlers;
  private MessageConsumer messageConsumer;

  public DomainEventDispatcher(String eventDispatcherId, DomainEventHandlers domainEventHandlers, MessageConsumer messageConsumer) {
    this.eventDispatcherId = eventDispatcherId;
    this.domainEventHandlers = domainEventHandlers;
    this.messageConsumer = messageConsumer;
  }

  @PostConstruct
  public void initialize() {
    messageConsumer.subscribe(eventDispatcherId, domainEventHandlers.getAggregateTypesAndEvents(), this::messageHandler);
  }

  public void messageHandler(Message message) {
    String aggregateType = message.getRequiredHeader(EventMessageHeaders.AGGREGATE_TYPE);

    Optional<DomainEventHandler> handler = domainEventHandlers.findTargetMethod(message);

    if (!handler.isPresent()) {
      return;
    }

    DomainEvent param = JSonMapper.fromJson(message.getPayload(), handler.get().getEventClass());

    handler.get().invoke(new DomainEventEnvelopeImpl<>(message,
            aggregateType,
            message.getRequiredHeader(EventMessageHeaders.AGGREGATE_ID),
            message.getRequiredHeader(Message.ID),
            param));

  }


}
