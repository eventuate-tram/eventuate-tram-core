package io.eventuate.tram.consumer.rabbitmq;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.messaging.rabbitmq.spring.consumer.MessageConsumerRabbitMQImpl;
import io.eventuate.messaging.rabbitmq.spring.consumer.Subscription;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class EventuateTramRabbitMQMessageConsumer implements MessageConsumerImplementation {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private MessageConsumerRabbitMQImpl messageConsumerRabbitMQ;

  public EventuateTramRabbitMQMessageConsumer(MessageConsumerRabbitMQImpl messageConsumerRabbitMQ) {
    this.messageConsumerRabbitMQ = messageConsumerRabbitMQ;
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    logger.info("Subscribing: subscriberId = {}, channels = {}", subscriberId, channels);

    Subscription subscription = messageConsumerRabbitMQ.subscribe(subscriberId,
            channels, message -> handler.accept(JSonMapper.fromJson(message.getPayload(), MessageImpl.class)));

    logger.info("Subscribed: subscriberId = {}, channels = {}", subscriberId, channels);

    return subscription::close;
  }

  @Override
  public String getId() {
    return messageConsumerRabbitMQ.getId();
  }

  @Override
  public void close() {
    logger.info("Closing consumer");
    messageConsumerRabbitMQ.close();
    logger.info("Closed consumer");
  }
}
