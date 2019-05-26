package io.eventuate.e2e.tests.basic.commands;

import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.commands.consumer.CommandDispatcherFactory;
import io.eventuate.tram.commands.consumer.TramCommandConsumerConfiguration;
import io.eventuate.tram.commands.producer.TramCommandProducerConfiguration;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collections;

@Configuration
@Import({TramCommandProducerConfiguration.class, TramCommandConsumerConfiguration.class})
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
  public CommandDispatcher commandDispatcher(AbstractTramCommandTestConfig config, AbstractTramCommandTestCommandHandler target, CommandDispatcherFactory commandDispatcherFactory) {
    return commandDispatcherFactory.make(config.getCommandDispatcheId(), target.getCommandHandlers());
  }

  @Bean
  public ChannelMapping channelMapping(AbstractTramCommandTestConfig config) {
    return new DefaultChannelMapping(Collections.singletonMap("CustomerAggregate", config.getCustomerChannel()));
  }
}
