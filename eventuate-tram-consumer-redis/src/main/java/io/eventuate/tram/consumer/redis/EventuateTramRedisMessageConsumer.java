package io.eventuate.tram.consumer.redis;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.messaging.redis.spring.consumer.MessageConsumerRedisImpl;
import io.eventuate.messaging.redis.spring.consumer.Subscription;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class EventuateTramRedisMessageConsumer implements MessageConsumerImplementation {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private MessageConsumerRedisImpl messageConsumerRedis;

  public EventuateTramRedisMessageConsumer(MessageConsumerRedisImpl messageConsumerRedis) {
    this.messageConsumerRedis = messageConsumerRedis;
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    logger.info("Subscribing: subscriberId = {}, channels = {}", subscriberId, channels);

    Subscription subscription = messageConsumerRedis.subscribe(subscriberId,
            channels, message -> handler.accept(JSonMapper.fromJson(message.getPayload(), MessageImpl.class)));

    logger.info("Subscribed: subscriberId = {}, channels = {}", subscriberId, channels);

    return subscription::close;
  }

  @Override
  public String getId() {
    return messageConsumerRedis.getId();
  }

  @Override
  public void close() {
    logger.info("Closing consumer");

    messageConsumerRedis.close();

    logger.info("Closed consumer");
  }
}
