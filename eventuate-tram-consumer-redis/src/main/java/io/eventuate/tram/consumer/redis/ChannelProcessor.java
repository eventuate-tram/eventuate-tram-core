package io.eventuate.tram.consumer.redis;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.lettuce.core.RedisCommandExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChannelProcessor {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private CountDownLatch stopCountDownLatch = new CountDownLatch(1);
  private AtomicBoolean running = new AtomicBoolean(false);

  private TransactionTemplate transactionTemplate;
  private DuplicateMessageDetector duplicateMessageDetector;
  private RedisTemplate<String, String> redisTemplate;
  private String subscriberId;
  private String channel;
  private MessageHandler messageHandler;

  public ChannelProcessor(TransactionTemplate transactionTemplate,
                          DuplicateMessageDetector duplicateMessageDetector,
                          RedisTemplate<String, String> redisTemplate,
                          String subscriberId,
                          String channel,
                          MessageHandler messageHandler) {
    this.transactionTemplate = transactionTemplate;
    this.duplicateMessageDetector = duplicateMessageDetector;
    this.subscriberId = subscriberId;
    this.channel = channel;
    this.messageHandler = messageHandler;
    this.redisTemplate = redisTemplate;
  }

  public void process() {
    running.set(true);
    makeSureConsumerGroupExists();
    processPendingRecords();
    processRegularRecords();
    stopCountDownLatch.countDown();
  }

  public void stop() {
    running.set(false);
    try {
      stopCountDownLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void makeSureConsumerGroupExists() {
    while (running.get()) {
      try {
        redisTemplate.opsForStream().createGroup(channel, ReadOffset.from("0"), subscriberId);
        return;
      } catch (RedisSystemException e) {
        if (isKeyDoesNotExist(e)) {
          logger.info("Stream {} does not exist!", channel);
          sleep();
          continue;
        } else if (isGroupExistsAlready(e)) {
          return;
        }
        throw e;
      }
    }
  }

  private boolean isKeyDoesNotExist(RedisSystemException e) {
    String message = e.getCause().getMessage();

    return e.getCause() instanceof RedisCommandExecutionException &&
            message != null &&
            message.contains("ERR The XGROUP subcommand requires the key to exist");
  }

  private boolean isGroupExistsAlready(RedisSystemException e) {
    String message = e.getCause().getMessage();

    return e.getCause() instanceof RedisCommandExecutionException &&
            message != null &&
            message.contains("Consumer Group name already exists");
  }

  private void processPendingRecords() {
    while (running.get()) {
      List<MapRecord<String, Object, Object>> pendingRecords = getPendingRecords();

      if (pendingRecords.isEmpty()) {
        return;
      }

      processRecords(pendingRecords);
    }
  }

  private void processRegularRecords() {

    while (running.get()) {
      processRecords(getUnprocessedRecords());
    }
  }

  private void processRecords(List<MapRecord<String, Object, Object>> records) {

    records.forEach(entries ->
            entries
                    .getValue()
                    .values()
                    .forEach(v -> processMessage(v.toString(), entries.getId())));
  }

  private void processMessage(String message, RecordId recordId) {

    Message tramMessage = JSonMapper.fromJson(message, MessageImpl.class);

    transactionTemplate.execute(ts -> {
      if (!duplicateMessageDetector.isDuplicate(subscriberId, tramMessage.getId())) {
        messageHandler.accept(tramMessage);
      }

      redisTemplate.opsForStream().acknowledge(channel, subscriberId, recordId);

      return null;
    });
  }

  private List<MapRecord<String, Object, Object>> getPendingRecords() {
    return getRecords(ReadOffset.from("0"));
  }

  private List<MapRecord<String, Object, Object>> getUnprocessedRecords() {
    return getRecords(ReadOffset.lastConsumed());
  }

  private List<MapRecord<String, Object, Object>> getRecords(ReadOffset readOffset) {
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
