package io.eventuate.tram.redis.common;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import java.util.List;
import java.util.stream.Collectors;

public class RedissonClients {
  private List<RedissonClient> redissonClients;

  public RedissonClients(RedisServers redisServers) {
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
    config.useSingleServer().setAddress(String.format("redis://%s:%s", hostAndPort.getHost(), hostAndPort.getPort()));
    return Redisson.create(config);
  }
}
