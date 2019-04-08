package io.eventuate.tram.redis.common;

public class RedisUtil {
  public static String channelToRedisStream(String topic, int partition) {
    return String.format("eventuate-tram:channel:%s-%s", topic, partition);
  }
}
