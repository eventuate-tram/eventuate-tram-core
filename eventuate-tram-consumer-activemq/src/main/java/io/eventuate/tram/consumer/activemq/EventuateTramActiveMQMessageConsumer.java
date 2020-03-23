package io.eventuate.tram.consumer.activemq;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.messaging.activemq.spring.consumer.MessageConsumerActiveMQImpl;
import io.eventuate.messaging.activemq.spring.consumer.Subscription;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class EventuateTramActiveMQMessageConsumer implements MessageConsumerImplementation {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private MessageConsumerActiveMQImpl messageConsumerActiveMQ;

  public EventuateTramActiveMQMessageConsumer(MessageConsumerActiveMQImpl messageConsumerActiveMQ) {
    this.messageConsumerActiveMQ = messageConsumerActiveMQ;
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    logger.info("Subscribing: subscriberId = {}, channels = {}", subscriberId, channels);

    Subscription subscription = messageConsumerActiveMQ.subscribe(subscriberId,
            channels, message -> handler.accept(JSonMapper.fromJson(message.getPayload(), MessageImpl.class)));

    logger.info("Subscribed: subscriberId = {}, channels = {}", subscriberId, channels);

    return subscription::close;
  }

  @Override
  public String getId() {
    return messageConsumerActiveMQ.getId();
  }

  @Override
  public void close() {
    logger.info("Closing consumer");
    messageConsumerActiveMQ.close();
    logger.info("Closed consumer");
  }
}
