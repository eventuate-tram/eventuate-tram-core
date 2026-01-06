package io.eventuate.tram.spring.testing.outbox.events;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.common.EventMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.spring.testing.outbox.messaging.MessageOutboxTestSupport;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.eventuate.util.test.async.Eventually.eventually;
import static org.assertj.core.api.Assertions.assertThat;

public class DomainEventOutboxTestSupport {

  private final MessageOutboxTestSupport messageOutboxTestSupport;

  public DomainEventOutboxTestSupport(MessageOutboxTestSupport messageOutboxTestSupport) {
    this.messageOutboxTestSupport = messageOutboxTestSupport;
  }

  public void assertDomainEventInOutbox(String aggregateType, String aggregateId, String eventType) {
    eventually(10, 500, TimeUnit.MILLISECONDS, () -> {
      List<Message> messages = messageOutboxTestSupport.findMessagesSentToChannel(aggregateType);
      boolean found = messages.stream().anyMatch(msg ->
          aggregateId.equals(msg.getHeader(EventMessageHeaders.AGGREGATE_ID).orElse(null)) &&
          eventType.equals(msg.getHeader(EventMessageHeaders.EVENT_TYPE).orElse(null)));
      assertThat(found)
          .withFailMessage("Expected to find event %s for aggregate %s in channel %s", eventType, aggregateId, aggregateType)
          .isTrue();
    });
  }

  public <T extends DomainEvent> List<T> findEventsOfType(String aggregateType, Class<T> eventType) {
    return findEventMessagesOfType(aggregateType, eventType).stream()
        .map(msg -> JSonMapper.fromJson(msg.getPayload(), eventType))
        .toList();
  }

  public <T extends DomainEvent> List<Message> findEventMessagesOfType(String aggregateType, Class<T> eventType) {
    return messageOutboxTestSupport.findMessagesSentToChannel(aggregateType).stream()
        .filter(msg -> eventType.getName().equals(msg.getHeader(EventMessageHeaders.EVENT_TYPE).orElse(null)))
        .toList();
  }
}
