package io.eventuate.tram.redis.common;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.List;
import java.util.stream.Collectors;

public class RedissonClients {
  private RedisServers redisServers;
  private List<RedissonClient> redissonClients;

  public RedissonClients(RedisServers redisServers) {
    this.redisServers = redisServers;

    redissonClients = redisServers
            .getHostsAndPorts()
            .stream()
            .map(this::createRedissonClient)
            .collect(Collectors.toList());
  }

  public List<RedissonClient> getRedissonClients() {
    return redissonClients;
  }

  private RedissonClient createRedissonClient(RedisServers.HostAndPort hostAndPort) {
    Config config = new Config();
    config.useSingleServer().setRetryAttempts(Integer.MAX_VALUE);
    config.useSingleServer().setRetryInterval(100);
    config.useSingleServer().setAddress(String.format("redis://%s:%s", hostAndPort.getHost(), hostAndPort.getPort()));
    return Redisson.create(config);
  }
}
