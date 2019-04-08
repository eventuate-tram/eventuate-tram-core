package io.eventuate.tram.data.producer.redis;

import io.eventuate.local.java.common.broker.DataProducer;
import io.eventuate.tram.redis.common.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class EventuateRedisProducer implements DataProducer {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private RedisTemplate<String, String> redisTemplate;
  private int partitions;

  public EventuateRedisProducer(RedisTemplate<String, String> redisTemplate, int partitions) {
    this.redisTemplate = redisTemplate;
    this.partitions = partitions;
  }

  @Override
  public CompletableFuture<?> send(String topic, String key, String body) {
    int partition = Math.abs(key.hashCode()) % partitions;

    logger.info("Sending message = {} with key = {} for topic = {}, partition = {}", body, key, topic, partition);

    redisTemplate
            .opsForStream()
            .add(StreamRecords
                    .string(Collections.singletonMap(key, body))
                    .withStreamKey(RedisUtil.channelToRedisStream(topic, partition)));

    logger.info("message sent = {} with key = {} for topic = {}, partition = {}", body, key, topic, partition);

    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void close() {
  }
}
