package io.eventuate.tram.commands.db.broker.integrationtests;

import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.commands.consumer.CommandDispatcherFactory;
import io.eventuate.tram.commands.spring.consumer.TramCommandConsumerConfiguration;
import io.eventuate.tram.commands.spring.producer.TramCommandProducerConfiguration;
import io.eventuate.tram.consumer.common.spring.TramNoopDuplicateMessageDetectorConfiguration;
import io.eventuate.tram.integrationtest.common.TramIntegrationTestMySqlBinlogKafkaConfiguration;
import io.eventuate.tram.integrationtest.common.TramIntegrationTestMySqlBinlogRedisConfiguration;
import io.eventuate.tram.integrationtest.common.TramIntegrationTestPollingActiveMQConfiguration;
import io.eventuate.tram.integrationtest.common.TramIntegrationTestPostgresWalRabbitMQConfiguration;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({TramIntegrationTestMySqlBinlogKafkaConfiguration.class,
        TramIntegrationTestPostgresWalRabbitMQConfiguration.class,
        TramIntegrationTestPollingActiveMQConfiguration.class,
        TramIntegrationTestMySqlBinlogRedisConfiguration.class,
        TramCommandProducerConfiguration.class,
        TramNoopDuplicateMessageDetectorConfiguration.class,
        TramCommandConsumerConfiguration.class
})
public class TramCommandsDBBrokerIntegrationTestConfiguration {


  @Bean
  public TramCommandsDBBrokerIntegrationData tramCommandsAndEventsIntegrationData() {
    return new TramCommandsDBBrokerIntegrationData();
}


  @Bean
  public ChannelMapping channelMapping(TramCommandsDBBrokerIntegrationData data) {
    return DefaultChannelMapping.builder()
            .with("ReplyTo", data.getAggregateDestination())
            .with("customerService", data.getCommandChannel())
            .build();
  }

  @Bean
  public CommandDispatcher consumerCommandDispatcher(MyTestCommandHandler target, CommandDispatcherFactory commandDispatcherFactory) {

    return commandDispatcherFactory.make("customerCommandDispatcher", target.defineCommandHandlers());
  }


  @Bean
  public MyTestCommandHandler myTestCommandHandler() {
    return Mockito.spy(new MyTestCommandHandler());
  }


  @Bean
  public MyReplyConsumer myReplyConsumer(MessageConsumer messageConsumer) {
    return new MyReplyConsumer(messageConsumer, "ReplyTo");
  }
}
