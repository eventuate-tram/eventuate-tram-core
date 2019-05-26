package io.eventuate.tram.events.publisher;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.events.common.EventMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DomainEventPublisherImpl implements DomainEventPublisher {

  private MessageProducer messageProducer;

  private DomainEventNameMapping domainEventNameMapping;

  public DomainEventPublisherImpl(MessageProducer messageProducer, DomainEventNameMapping domainEventNameMapping) {
    this.messageProducer = messageProducer;
    this.domainEventNameMapping = domainEventNameMapping;
  }

  @Override
  public void publish(String aggregateType, Object aggregateId, List<DomainEvent> domainEvents) {
    publish(aggregateType, aggregateId, Collections.emptyMap(), domainEvents);
  }

  @Override
  public void publish(String aggregateType, Object aggregateId, Map<String, String> headers, List<DomainEvent> domainEvents) {
    for (DomainEvent event : domainEvents) {
      messageProducer.send(aggregateType,
              makeMessageForDomainEvent(aggregateType, aggregateId, headers, event,
                      domainEventNameMapping.eventToExternalEventType(aggregateType, event)));

    }
  }

  public static Message makeMessageForDomainEvent(String aggregateType, Object aggregateId, Map<String, String> headers, DomainEvent event, String eventType) {
    String aggregateIdAsString = aggregateId.toString();
    return MessageBuilder
            .withPayload(JSonMapper.toJson(event))
            .withExtraHeaders("", headers)
            .withHeader(Message.PARTITION_ID, aggregateIdAsString)
            .withHeader(EventMessageHeaders.AGGREGATE_ID, aggregateIdAsString)
            .withHeader(EventMessageHeaders.AGGREGATE_TYPE, aggregateType)
            .withHeader(EventMessageHeaders.EVENT_TYPE, eventType)
            .build();
  }

}
