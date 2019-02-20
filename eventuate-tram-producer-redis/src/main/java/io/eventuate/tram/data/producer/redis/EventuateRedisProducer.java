package io.eventuate.tram.data.producer.redis;

import io.eventuate.local.java.common.broker.DataProducer;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class EventuateRedisProducer implements DataProducer {
  private RedisTemplate<String, String> redisTemplate;

  public EventuateRedisProducer(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public CompletableFuture<?> send(String topic, String key, String body) {
    redisTemplate
            .opsForStream()
            .add(StreamRecords.string(Collections.singletonMap(key, body)).withStreamKey(topic));

    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void close() {
  }
}
