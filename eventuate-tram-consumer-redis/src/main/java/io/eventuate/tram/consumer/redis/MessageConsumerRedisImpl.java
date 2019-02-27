package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

public class MessageConsumerRedisImpl implements MessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  public final String consumerId = UUID.randomUUID().toString();

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private DuplicateMessageDetector duplicateMessageDetector;

  private RedisTemplate<String, String> redisTemplate;

  private Boolean acknowledgeFailedMessages;
  private int partitions;
  private List<Subscription> subscriptions = new ArrayList<>();

  public MessageConsumerRedisImpl(RedisTemplate<String, String> redisTemplate, int partitions) {
    this(redisTemplate, true, partitions);
  }


  public MessageConsumerRedisImpl(RedisTemplate<String, String> redisTemplate,
                                  Boolean acknowledgeFailedMessages,
                                  int partitions) {
    this.redisTemplate = redisTemplate;
    this.acknowledgeFailedMessages = acknowledgeFailedMessages;
    this.partitions = partitions;
  }

  public TransactionTemplate getTransactionTemplate() {
    return transactionTemplate;
  }

  public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
    this.transactionTemplate = transactionTemplate;
  }

  public DuplicateMessageDetector getDuplicateMessageDetector() {
    return duplicateMessageDetector;
  }

  public void setDuplicateMessageDetector(DuplicateMessageDetector duplicateMessageDetector) {
    this.duplicateMessageDetector = duplicateMessageDetector;
  }

  @Override
  public void subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {

    Subscription subscription = new Subscription(consumerId,
            redisTemplate,
            transactionTemplate,
            duplicateMessageDetector,
            subscriberId,
            channels,
            handler,
            acknowledgeFailedMessages,
            partitions);

    subscriptions.add(subscription);
  }

  public void setSubscriptionLifecycleHook(SubscriptionLifecycleHook subscriptionLifecycleHook) {
    subscriptions.forEach(subscription -> subscription.setSubscriptionLifecycleHook(subscriptionLifecycleHook));
  }

  public void close() {
    subscriptions.forEach(Subscription::close);
    subscriptions.clear();
  }
}