package io.eventuate.tram.consumer.redis;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.lettuce.core.RedisCommandExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageConsumerRedisImpl implements MessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private DuplicateMessageDetector duplicateMessageDetector;

  private RedisTemplate<String, String> redisTemplate;

  private AtomicBoolean running = new AtomicBoolean(false);
  private ExecutorService executorService = Executors.newCachedThreadPool();
  private CountDownLatch stopCountDownLatch;


  public MessageConsumerRedisImpl(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
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
    running.set(true);

    stopCountDownLatch = new CountDownLatch(channels.size());

    for (String channel : channels) {
      subscribeToChannel(channel, subscriberId, handler);
    }
  }

  public void close() {
    running.set(false);

    try {
      stopCountDownLatch.await();
    } catch (InterruptedException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private void subscribeToChannel(String channel, String subscriberId, MessageHandler messageHandler) {
    executorService.submit(() -> {
      makeSureConsumerGroupExists(subscriberId, channel);
      processPendingRecords(channel, subscriberId, messageHandler);
      processRegularRecords(channel, subscriberId, messageHandler);
      stopCountDownLatch.countDown();
    });
  }

  private void makeSureConsumerGroupExists(String subscriberId, String channel) {
    while (running.get()) {
      try {
        redisTemplate.opsForStream().createGroup(channel, ReadOffset.from("0"), subscriberId);
        return;
      } catch (RedisSystemException e) {
        if (e.getCause() instanceof RedisCommandExecutionException) {
          String message = e.getCause().getMessage();

          if (message != null) {
            if (message.contains("ERR The XGROUP subcommand requires the key to exist")) {
              logger.info("Stream {} does not exist!", channel);
              sleep();
              continue;
            } else if (message.contains("Consumer Group name already exists")) {
              logger.info("Group already exists!");
              return;
            }
            throw e;
          }
          throw e;
        }
        throw e;
      }
    }
  }

  private void processPendingRecords(String channel, String subscriberId, MessageHandler messageHandler) {
    while (running.get()) {
      List<MapRecord<String, Object, Object>> pendingRecords = getPendingRecords(subscriberId, channel);

      if (pendingRecords.isEmpty()) {
        return;
      }

      processRecords(pendingRecords, channel, subscriberId, messageHandler);
    }
  }

  private void processRegularRecords(String channel, String subscriberId, MessageHandler messageHandler) {

    while (running.get()) {
      processRecords(getUnprocessedRecords(subscriberId, channel), channel, subscriberId, messageHandler);
    }
  }

  private void processRecords(List<MapRecord<String, Object, Object>> records,
                              String channel,
                              String subscriberId,
                              MessageHandler messageHandler) {

    records.forEach(entries ->
            entries
                    .getValue()
                    .values()
                    .forEach(v -> processMessage(v.toString(), channel, subscriberId, entries.getId(), messageHandler)));
  }

  private void processMessage(String message,
                              String channel,
                              String subscriberId,
                              RecordId recordId,
                              MessageHandler messageHandler) {

    Message tramMessage = JSonMapper.fromJson(message, MessageImpl.class);

    transactionTemplate.execute(ts -> {
      if (duplicateMessageDetector.isDuplicate(subscriberId, tramMessage.getId())) {
        redisTemplate.opsForStream().acknowledge(channel, subscriberId, recordId);
        return null;
      }

      messageHandler.accept(tramMessage);
      redisTemplate.opsForStream().acknowledge(channel, subscriberId, recordId);

      return null;
    });
  }

  private List<MapRecord<String, Object, Object>> getPendingRecords(String subscriberId, String channel) {
    return getRecords(subscriberId, channel, ReadOffset.from("0"));
  }

  private List<MapRecord<String, Object, Object>> getUnprocessedRecords(String subscriberId, String channel) {
    return getRecords(subscriberId, channel, ReadOffset.lastConsumed());
  }

  private List<MapRecord<String, Object, Object>> getRecords(String subscriberId,
                                                             String channel,
                                                             ReadOffset readOffset) {
    return redisTemplate
            .opsForStream()
            .read(Consumer.from(subscriberId, subscriberId),
                    StreamReadOptions.empty().block(Duration.ofMillis(100)),
                    StreamOffset.create(channel, readOffset));
  }

  private void sleep() {
    try {
      Thread.sleep(500);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}