package io.eventuate.tram.reactive.consumer.kafka;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.messaging.kafka.consumer.KafkaSubscription;
import io.eventuate.messaging.kafka.consumer.MessageConsumerKafkaImpl;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumerImplementation;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandler;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Set;

public class EventuateTramReactiveKafkaMessageConsumer implements ReactiveMessageConsumerImplementation {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private MessageConsumerKafkaImpl messageConsumerKafka;

  public EventuateTramReactiveKafkaMessageConsumer(MessageConsumerKafkaImpl messageConsumerKafka) {
    this.messageConsumerKafka = messageConsumerKafka;
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, ReactiveMessageHandler handler) {
    logger.info("Subscribing (reactive): subscriberId = {}, channels = {}", subscriberId, channels);

    KafkaSubscription subscription = messageConsumerKafka.subscribeWithReactiveHandler(subscriberId,
            channels, message -> Mono.from(handler.apply(JSonMapper.fromJson(message.getPayload(), MessageImpl.class))).then().toFuture());

    logger.info("Subscribed (reactive): subscriberId = {}, channels = {}", subscriberId, channels);

    return subscription::close;
  }

  @Override
  public String getId() {
    return messageConsumerKafka.getId();
  }

  @Override
  public void close() {
    logger.info("Closing reactive consumer");

    messageConsumerKafka.close();

    logger.info("Closed reactive consumer");
  }
}
