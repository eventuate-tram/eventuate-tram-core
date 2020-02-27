package io.eventuate.tram.spring.cloudsleuthintegration;

import brave.Tracing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramSpringCloudSleuthIntegrationConfiguration {

  @Bean
  public TracingMessagingInterceptor tracingMessagingInterceptor(Tracing tracing) {
    return new TracingMessagingInterceptor(tracing, MessageHeaderPropagation.INSTANCE, MessageHeaderPropagation.INSTANCE);
  }
}
