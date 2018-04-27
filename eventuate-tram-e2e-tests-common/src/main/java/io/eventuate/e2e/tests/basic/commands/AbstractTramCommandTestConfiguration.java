package io.eventuate.e2e.tests.basic.commands;

import io.eventuate.tram.commands.common.ChannelMapping;
import io.eventuate.tram.commands.common.DefaultChannelMapping;
import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.commands.producer.TramCommandProducerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collections;

@Configuration
@Import(TramCommandProducerConfiguration.class)
public class AbstractTramCommandTestConfiguration {

  @Bean
  public AbstractTramCommandTestConfig abstractTramCommandTestConfig() {
    return  new AbstractTramCommandTestConfig();

  }

  @Bean
  public AbstractTramCommandTestCommandHandler abstractTramCommandTestTarget(AbstractTramCommandTestConfig config) {
    return new AbstractTramCommandTestCommandHandler(config.getCommandChannel());
  }

  @Bean
  public CommandDispatcher commandDispatcher(AbstractTramCommandTestConfig config, AbstractTramCommandTestCommandHandler target) {
    return new CommandDispatcher(config.getCommandDispatcheId(), target.getCommandHandlers());
  }

  @Bean
  public ChannelMapping channelMapping(AbstractTramCommandTestConfig config) {
    return new DefaultChannelMapping(Collections.singletonMap("CustomerAggregate", config.getCustomerChannel()));
  }
}
