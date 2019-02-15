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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageConsumerRedisImpl implements MessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private static final String GROUP_NAME = "eventuate";
  private static final String CONSUMER_NAME = "eventuate";

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private DuplicateMessageDetector duplicateMessageDetector;

  private RedisTemplate<String, String> redisTemplate;
  private List<CompletableFuture<Object>> channelProcessingFutures = new ArrayList<>();
  private AtomicBoolean running = new AtomicBoolean(false);


  public MessageConsumerRedisImpl(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    running.set(true);

    ConcurrentHashMap<String, String> channelsWithGroup = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> channelsWithNoPendingOperations = new ConcurrentHashMap<>();

    for (String channel : channels) {
      channelProcessingFutures.add(createChannelProcessingFuture(channel,
              channelsWithGroup,
              channelsWithNoPendingOperations,
              subscriberId,
              handler));
    }
  }

  public void close() {
    running.set(false);

    channelProcessingFutures.forEach(f -> {
      try {
        f.get();
      } catch (InterruptedException | ExecutionException e) {
        logger.error(e.getMessage(), e);
      }
    });

    channelProcessingFutures.clear();
  }

  private CompletableFuture<Object> createChannelProcessingFuture(String channel,
                                                                  ConcurrentHashMap<String, String> channelsWithGroup,
                                                                  ConcurrentHashMap<String, String> channelsWithNoPendingOperations,
                                                                  String subscriberId,
                                                                  MessageHandler messageHandler) {
    return CompletableFuture.supplyAsync(() -> {
      while (running.get()) {
        if (!createRedisConsumerGroup(channel, channelsWithGroup)) {
          pause();
          continue;
        }

        getAndProcessRecordsIteration(channelsWithNoPendingOperations, channel, subscriberId, messageHandler);
      }

      return null;
    });
  }

  private boolean createRedisConsumerGroup(String channel, ConcurrentHashMap<String, String> channelsWithGroup) {
    if (channelsWithGroup.contains(channel)) {
      return true;
    }

    try {
      redisTemplate.opsForStream().createGroup(channel, ReadOffset.from("0"), GROUP_NAME);
      channelsWithGroup.put(channel, channel);
      return true;
    } catch (RedisSystemException e) {
      if (e.getCause() instanceof RedisCommandExecutionException) {
        String message = e.getCause().getMessage();

        if (message != null) {
          if (message.contains("ERR The XGROUP subcommand requires the key to exist")) {
            logger.info("Stream {} does not exist!", channel);
            pause();
            return false;
          } else if (message.contains("Consumer Group name already exists")) {
            logger.info("Group already exists!");
            channelsWithGroup.put(channel, channel);
            return true;
          } else throw e;
        } else throw e;
      } else throw e;
    }
  }

  private void getAndProcessRecordsIteration(ConcurrentHashMap<String, String> channelsWithNoPendingOperations,
                                             String channel,
                                             String subscriberId,
                                             MessageHandler messageHandler) {

    if (!channelsWithNoPendingOperations.contains(channel)) {
      List<MapRecord<String, Object, Object>> pendingRecords = getPendingRecords(channel);

      if (pendingRecords.isEmpty()) {
        channelsWithNoPendingOperations.put(channel, channel);
        return;
      }

      processRecords(pendingRecords, channel, subscriberId, messageHandler);
    } else {
      processRecords(getUnprocessedRecords(channel), channel, subscriberId, messageHandler);
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

  private void processMessage(String message, String channel, String subscriberId, RecordId recordId, MessageHandler messageHandler) {
    Message tramMessage = JSonMapper.fromJson(message, MessageImpl.class);

    transactionTemplate.execute(ts -> {
      if (duplicateMessageDetector.isDuplicate(subscriberId, tramMessage.getId())) {
        redisTemplate.opsForStream().acknowledge(channel, GROUP_NAME, recordId);
        return null;
      }

      messageHandler.accept(tramMessage);
      redisTemplate.opsForStream().acknowledge(channel, GROUP_NAME, recordId);

      return null;
    });
  }

  private List<MapRecord<String, Object, Object>> getPendingRecords(String channel) {
    return redisTemplate
            .opsForStream()
            .read(Consumer.from(GROUP_NAME, CONSUMER_NAME), StreamOffset.create(channel, ReadOffset.from("0")));
  }

  private List<MapRecord<String, Object, Object>> getUnprocessedRecords(String channel) {
    return redisTemplate
            .opsForStream()
            .read(Consumer.from(GROUP_NAME, CONSUMER_NAME), StreamOffset.create(channel, ReadOffset.lastConsumed()));
  }

  private void pause() {
    try {
      Thread.sleep(500);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}