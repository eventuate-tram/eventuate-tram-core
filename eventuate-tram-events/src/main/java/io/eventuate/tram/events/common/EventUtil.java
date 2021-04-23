package io.eventuate.tram.events.common;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;

import java.util.Map;

public class EventUtil {

  public static Message makeMessageForDomainEvent(String aggregateType,
                                                  Object aggregateId,
                                                  Map<String, String> headers,
                                                  DomainEvent event,
                                                  String eventType) {
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
