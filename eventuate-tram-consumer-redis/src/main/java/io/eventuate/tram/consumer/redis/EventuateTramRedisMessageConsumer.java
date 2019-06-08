package io.eventuate.tram.consumer.redis;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.messaging.redis.consumer.MessageConsumerRedisImpl;
import io.eventuate.messaging.redis.consumer.Subscription;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;

import java.util.Set;

public class EventuateTramRedisMessageConsumer implements MessageConsumerImplementation {

  private MessageConsumerRedisImpl messageConsumerRedis;

  public EventuateTramRedisMessageConsumer(MessageConsumerRedisImpl messageConsumerRedis) {
    this.messageConsumerRedis = messageConsumerRedis;
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    Subscription subscription = messageConsumerRedis.subscribe(subscriberId,
            channels, message -> handler.accept(JSonMapper.fromJson(message.getPayload(), MessageImpl.class)));

    return subscription::close;
  }

  @Override
  public String getId() {
    return messageConsumerRedis.getId();
  }

  @Override
  public void close() {
    messageConsumerRedis.close();
  }
}
