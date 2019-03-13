package io.eventuate.tram.redis.common;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AdditionalRedissonClients {
  private List<RedissonClient> redissonClients;

  public AdditionalRedissonClients(String additionalRedisServers) {
    if (StringUtils.isEmpty(additionalRedisServers)) {
      redissonClients = Collections.emptyList();
    } else {

      redissonClients = Arrays
              .stream(additionalRedisServers.split(","))
              .map(url -> {
                Config config = new Config();
                config.useSingleServer().setAddress(url);
                return Redisson.create(config);
              })
              .collect(Collectors.toList());
    }
  }

  public List<RedissonClient> getRedissonClients() {
    return redissonClients;
  }
}
