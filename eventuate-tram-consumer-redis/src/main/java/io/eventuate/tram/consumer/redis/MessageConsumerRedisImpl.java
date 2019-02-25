package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.lettuce.core.api.StatefulRedisConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageConsumerRedisImpl implements MessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private DuplicateMessageDetector duplicateMessageDetector;

  private RedisTemplate<String, String> redisTemplate;

  private ExecutorService executorService = Executors.newCachedThreadPool();
  private List<ChannelProcessor> channelProcessors = new ArrayList<>();

  private Boolean acknowledgeFailedMessages;

  public MessageConsumerRedisImpl(RedisTemplate<String, String> redisTemplate) {
    this(redisTemplate, true);
  }

  public MessageConsumerRedisImpl(RedisTemplate<String, String> redisTemplate, Boolean acknowledgeFailedMessages) {
    this.redisTemplate = redisTemplate;
    this.acknowledgeFailedMessages = acknowledgeFailedMessages;
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
    for (String channel : channels) {
      subscribeToChannel(channel, subscriberId, handler);
    }
  }

  public void close() {
    channelProcessors.forEach(ChannelProcessor::stop);
  }

  private void subscribeToChannel(String channel, String subscriberId, MessageHandler messageHandler) {
    ChannelProcessor channelProcessor = new ChannelProcessor(redisTemplate,
            transactionTemplate,
            duplicateMessageDetector,
            subscriberId,
            channel,
            messageHandler,
            acknowledgeFailedMessages);

    executorService.submit(channelProcessor::process);

    channelProcessors.add(channelProcessor);
  }
}