package io.eventuate.tram.spring.reactive.consumer.common;

import io.eventuate.tram.consumer.common.reactive.ReactiveDuplicateMessageDetector;
import io.eventuate.tram.consumer.common.reactive.ReactiveNoopDuplicateMessageDetector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactiveTramNoopDuplicateMessageDetectorConfiguration {

  @Bean
  public ReactiveDuplicateMessageDetector duplicateMessageDetector() {
    return new ReactiveNoopDuplicateMessageDetector();
  }
}
