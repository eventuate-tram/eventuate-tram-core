package io.eventuate.tram.consumer.wrappers;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.messaging.rabbitmq.consumer.MessageConsumerRabbitMQImpl;
import io.eventuate.messaging.rabbitmq.consumer.Subscription;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;

import java.util.Set;

public class EventuateRabbitMQMessageConsumerWrapper implements MessageConsumerImplementation {

  private MessageConsumerRabbitMQImpl messageConsumerRabbitMQ;

  public EventuateRabbitMQMessageConsumerWrapper(MessageConsumerRabbitMQImpl messageConsumerRabbitMQ) {
    this.messageConsumerRabbitMQ = messageConsumerRabbitMQ;
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    Subscription subscription = messageConsumerRabbitMQ.subscribe(subscriberId,
            channels, message -> handler.accept(JSonMapper.fromJson(message.getPayload(), MessageImpl.class)));

    return subscription::close;
  }

  @Override
  public String getId() {
    return messageConsumerRabbitMQ.getId();
  }

  @Override
  public void close() {
    messageConsumerRabbitMQ.close();
  }
}
