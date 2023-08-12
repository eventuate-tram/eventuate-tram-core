package io.eventuate.tram.spring.commands.common;

import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.commands.common.DefaultCommandNameMapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;

@AutoConfiguration
@ConditionalOnMissingBean(CommandNameMapping.class)
public class TramCommandsCommonAutoConfiguration {

  @Bean
  public CommandNameMapping commandNameMapping() {
    return new DefaultCommandNameMapping();
  }

}
