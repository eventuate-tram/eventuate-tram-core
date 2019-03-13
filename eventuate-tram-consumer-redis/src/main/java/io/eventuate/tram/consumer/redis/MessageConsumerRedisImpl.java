package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class MessageConsumerRedisImpl implements MessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  public final String consumerId;

  private Supplier<String> subscriptionIdSupplier;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private DuplicateMessageDetector duplicateMessageDetector;

  private String zkUrl;

  private RedisTemplate<String, String> redisTemplate;
  private RedissonClient redissonClient;

  private int partitions;
  private long groupMemberTtlInMilliseconds;
  private long listenerIntervalInMilliseconds;
  private long assignmentTtlInMilliseconds;
  private long leadershipTtlInMilliseconds;
  private List<Subscription> subscriptions = new ArrayList<>();

  public MessageConsumerRedisImpl(RedisTemplate<String, String> redisTemplate,
                                  RedissonClient redissonClient,
                                  int partitions,
                                  long groupMemberTtlInMilliseconds,
                                  long listenerIntervalInMilliseconds,
                                  long assignmentTtlInMilliseconds,
                                  long leadershipTtlInMilliseconds) {
    this(() -> UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            redisTemplate,
            redissonClient,
            partitions,
            groupMemberTtlInMilliseconds,
            listenerIntervalInMilliseconds,
            assignmentTtlInMilliseconds,
            leadershipTtlInMilliseconds);
  }

  public MessageConsumerRedisImpl(Supplier<String> subscriptionIdSupplier,
                                  String consumerId,
                                  RedisTemplate<String, String> redisTemplate,
                                  RedissonClient redissonClient,
                                  int partitions,
                                  long groupMemberTtlInMilliseconds,
                                  long listenerIntervalInMilliseconds,
                                  long assignmentTtlInMilliseconds,
                                  long leadershipTtlInMilliseconds) {

    this.subscriptionIdSupplier = subscriptionIdSupplier;
    this.consumerId = consumerId;
    this.redisTemplate = redisTemplate;
    this.redissonClient = redissonClient;
    this.partitions = partitions;
    this.groupMemberTtlInMilliseconds = groupMemberTtlInMilliseconds;
    this.listenerIntervalInMilliseconds = listenerIntervalInMilliseconds;
    this.assignmentTtlInMilliseconds = assignmentTtlInMilliseconds;
    this.leadershipTtlInMilliseconds = leadershipTtlInMilliseconds;

    logger.info("Consumer created (consumer id = {})", consumerId);
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
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {

    logger.info("consumer subscribes to channels (consumer id = {}, subscriber id {}, channels = {})", consumerId, subscriberId, channels);

    Subscription subscription = new Subscription(subscriptionIdSupplier.get(),
            consumerId,
            redisTemplate,
            redissonClient,
            transactionTemplate,
            duplicateMessageDetector,
            subscriberId,
            channels,
            handler,
            partitions,
            groupMemberTtlInMilliseconds,
            listenerIntervalInMilliseconds,
            assignmentTtlInMilliseconds,
            leadershipTtlInMilliseconds);

    subscriptions.add(subscription);

    return subscription::close;
  }

  public void setSubscriptionLifecycleHook(SubscriptionLifecycleHook subscriptionLifecycleHook) {
    subscriptions.forEach(subscription -> subscription.setSubscriptionLifecycleHook(subscriptionLifecycleHook));
  }

  public void close() {
    subscriptions.forEach(Subscription::close);
    subscriptions.clear();
  }
}