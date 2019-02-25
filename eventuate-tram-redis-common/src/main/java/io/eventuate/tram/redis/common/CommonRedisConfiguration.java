package io.eventuate.tram.redis.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class CommonRedisConfiguration {

  @Bean
  public LettuceConnectionFactory lettuceConnectionFactory(@Value("${redis.host}") String redisHost,
                                                           @Value("${redis.port}") Integer redisPort) {

    return new LettuceConnectionFactory(redisHost, redisPort);
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
}
