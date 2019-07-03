package io.eventuate.tram.events.spring.common;

import io.eventuate.tram.events.common.DefaultDomainEventNameMapping;
import io.eventuate.tram.events.common.DomainEventNameMapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(DomainEventNameMapping.class)
public class TramEventsCommonAutoConfiguration {

  @Bean
  public DomainEventNameMapping domainEventNameMapping() {
    return new DefaultDomainEventNameMapping();
  }

}
