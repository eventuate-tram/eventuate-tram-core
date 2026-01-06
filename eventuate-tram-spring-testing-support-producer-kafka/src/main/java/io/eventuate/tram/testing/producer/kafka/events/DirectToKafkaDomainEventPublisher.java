package io.eventuate.tram.testing.producer.kafka.events;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.messaging.kafka.producer.EventuateKafkaProducer;
import io.eventuate.messaging.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.common.EventUtil;
import io.eventuate.tram.messaging.producer.MessageHeaderUtils;
import io.eventuate.tram.messaging.producer.MessageProducer;

import java.util.Map;

public class DirectToKafkaDomainEventPublisher {

  private final MessageProducer messageProducer;

  public DirectToKafkaDomainEventPublisher(String bootstrapServer) {
    var eventuateKafkaProducer = new EventuateKafkaProducer(bootstrapServer, EventuateKafkaProducerConfigurationProperties.empty());
    messageProducer = (destination, message) -> {
      MessageHeaderUtils.prepareMessageHeaders(destination, message);
      eventuateKafkaProducer.send(destination, "1", JSonMapper.toJson(message));
    };
  }

  public void publish(String aggregateType,
                      Object aggregateId,
                      DomainEvent event) {
    messageProducer.send(aggregateType,
        EventUtil.makeMessageForDomainEvent(aggregateType, aggregateId, Map.of(), event, event.getClass().getName()));
  }
}
