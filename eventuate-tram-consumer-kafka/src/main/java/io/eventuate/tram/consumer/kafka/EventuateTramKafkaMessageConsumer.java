package io.eventuate.tram.consumer.kafka;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.messaging.kafka.consumer.KafkaSubscription;
import io.eventuate.messaging.kafka.consumer.MessageConsumerKafkaImpl;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;

import java.util.Set;

public class EventuateTramKafkaMessageConsumer implements MessageConsumerImplementation {

  private MessageConsumerKafkaImpl messageConsumerKafka;

  public EventuateTramKafkaMessageConsumer(MessageConsumerKafkaImpl messageConsumerKafka) {
    this.messageConsumerKafka = messageConsumerKafka;
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    KafkaSubscription subscription = messageConsumerKafka.subscribe(subscriberId,
            channels, message -> handler.accept(JSonMapper.fromJson(message.getPayload(), MessageImpl.class)));

    return subscription::close;
  }

  @Override
  public String getId() {
    return messageConsumerKafka.getId();
  }

  @Override
  public void close() {
    messageConsumerKafka.close();
  }
}
