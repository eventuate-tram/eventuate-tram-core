package io.eventuate.tram.events.publisher;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.common.EventMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DomainEventPublisherImpl implements DomainEventPublisher {

  private MessageProducer messageProducer;

  public DomainEventPublisherImpl(MessageProducer messageProducer) {
    this.messageProducer = messageProducer;
  }

  @Override
  public void publish(String aggregateType, String aggregateId, List<DomainEvent> domainEvents) {
    publish(aggregateType, aggregateId, Collections.emptyMap(), domainEvents);
  }

  @Override
  public void publish(String aggregateType, String aggregateId, Map<String, String> headers, List<DomainEvent> domainEvents) {
    for (DomainEvent event : domainEvents) {
      messageProducer.send(aggregateType,
              makeMessageForDomainEvent(aggregateType, aggregateId, headers, event));

    }
  }

  public static Message makeMessageForDomainEvent(String aggregateType, String aggregateId, Map<String, String> headers, DomainEvent event) {
    return MessageBuilder
            .withPayload(JSonMapper.toJson(event))
            .withExtraHeaders("", headers)
            .withHeader(Message.PARTITION_ID, aggregateId)
            .withHeader(EventMessageHeaders.AGGREGATE_ID, aggregateId)
            .withHeader(EventMessageHeaders.AGGREGATE_TYPE, aggregateType)
            .withHeader(EventMessageHeaders.EVENT_TYPE, event.getClass().getName())
            .build();
  }
}
