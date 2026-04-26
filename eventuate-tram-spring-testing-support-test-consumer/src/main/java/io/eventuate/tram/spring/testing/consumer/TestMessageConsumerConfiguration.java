package io.eventuate.tram.spring.testing.consumer;

import io.eventuate.tram.testutil.TestMessageConsumerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestMessageConsumerConfiguration {

  @Bean
  public TestMessageConsumerFactory testMessageConsumerFactory() {
    return new TestMessageConsumerFactory();
  }
}
