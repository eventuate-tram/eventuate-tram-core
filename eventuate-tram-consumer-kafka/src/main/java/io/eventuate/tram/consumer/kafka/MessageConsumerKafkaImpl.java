package io.eventuate.tram.consumer.kafka;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.messaging.kafka.consumer.EventuateKafkaConsumer;
import io.eventuate.messaging.kafka.consumer.KafkaMessage;
import io.eventuate.messaging.kafka.consumer.KafkaSubscription;
import io.eventuate.tram.consumer.common.DecoratedMessageHandlerFactory;
import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class MessageConsumerKafkaImpl implements MessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final String id = UUID.randomUUID().toString();

  @Autowired
  private DecoratedMessageHandlerFactory decoratedMessageHandlerFactory;


  @Autowired
  private EventuateKafkaConsumer messageConsumerKafka;

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {

    Consumer<SubscriberIdAndMessage> decoratedHandler = decoratedMessageHandlerFactory.decorate(handler);

    KafkaSubscription ks = messageConsumerKafka.subscribe(subscriberId, channels, message -> handle(toMessage(message), subscriberId, decoratedHandler));
    return ks::close;

  }

  private Message toMessage(KafkaMessage message) {
    return JSonMapper.fromJson(message.getPayload(), MessageImpl.class);
  }

  public void handle(Message message, String subscriberId, Consumer<SubscriberIdAndMessage> decoratedHandler) {
    decoratedHandler.accept(new SubscriberIdAndMessage(subscriberId, message));
  }



  @Override
  public String getId() {
    return id;
  }

  @Override
  public void close() {
    // Do nothing??
  }
}
