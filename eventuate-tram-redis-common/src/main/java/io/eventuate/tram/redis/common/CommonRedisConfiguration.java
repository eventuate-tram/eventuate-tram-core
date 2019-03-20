package io.eventuate.tram.redis.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class CommonRedisConfiguration {

  @Value("${redis.servers:#{\"\"}}")
  private String redisServersProperty;

  @Bean
  public RedisServers redisServers() {
    return new RedisServers(redisServersProperty);
  }

  @Bean
  public LettuceConnectionFactory lettuceConnectionFactory(RedisServers redisServers) {
    RedisServers.HostAndPort mainServer = redisServers.getHostsAndPorts().get(0);
    return new LettuceConnectionFactory(mainServer.getHost(), mainServer.getPort());
  }

  @Bean
  public RedisTemplate<String, String> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(lettuceConnectionFactory);
    template.setDefaultSerializer(stringRedisSerializer);
    template.setKeySerializer(stringRedisSerializer);
    template.setValueSerializer(stringRedisSerializer);
    template.setHashKeySerializer(stringRedisSerializer);
    return template;
  }

  @Bean
  public RedissonClients redissonClients(RedisServers redisServers) {
    return new RedissonClients(redisServers);
  }
}
