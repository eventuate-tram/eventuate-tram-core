package io.eventuate.tram.events.subscriber;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.common.EventMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DomainEventDispatcher {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final String eventDispatcherId;
  private final Map<String, Set<String>> aggregateTypesAndEvents;
  private final Object target;
  private MessageConsumer messageConsumer;

  public DomainEventDispatcher(String eventDispatcherId, Map<String, Set<String>> aggregateTypesAndEvents, Object target, MessageConsumer messageConsumer) {
    this.eventDispatcherId = eventDispatcherId;
    this.aggregateTypesAndEvents = aggregateTypesAndEvents;
    this.target = target;
    this.messageConsumer = messageConsumer;
  }

  @PostConstruct
  public void initialize() {
    messageConsumer.subscribe(eventDispatcherId, aggregateTypesAndEvents.keySet(), this::messageHandler);
  }

  // TODO I think this needs to be transactional
  // TODO It should handle deduping as well.

  public void messageHandler(Message message) {
    String aggregateType = message.getRequiredHeader(EventMessageHeaders.AGGREGATE_TYPE);

    Optional<Method> m = findTargetMethod(message);

    if (!m.isPresent()) {
      logger.error("no event handler on class {} for message {}", this, message);
      throw new RuntimeException("No method for " + message);
    }

    DomainEvent param = (DomainEvent) JSonMapper.fromJson(message.getPayload(), eventParameterClass(m.get()));

    try {
      m.get().invoke(target, new DomainEventEnvelopeImpl<>(message,
              aggregateType,
              message.getRequiredHeader(EventMessageHeaders.AGGREGATE_ID),
              message.getRequiredHeader(Message.ID),
              param));
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }

  }

  private Optional<Method> findTargetMethod(Message message) {
    for (Method m : target.getClass().getMethods()) {
      DomainEventHandler a = m.getDeclaredAnnotation(DomainEventHandler.class);
      if (a != null) {
        if (eventParameterClass(m).getName().equals(message.getRequiredHeader(EventMessageHeaders.EVENT_TYPE)))
          return Optional.of(m);
      }
    }
    return Optional.empty();
  }

  private Class<?> eventParameterClass(Method m) {
    if (!DomainEventEnvelope.class.isAssignableFrom(m.getParameterTypes()[0]))
      throw new IllegalArgumentException("Event handler parameter type should be DomainEventEnvelope: " + m.getParameterTypes()[0]);
    else
      return ((Class<?>) ((ParameterizedType) m.getGenericParameterTypes()[0]).getActualTypeArguments()[0]);
  }

}
