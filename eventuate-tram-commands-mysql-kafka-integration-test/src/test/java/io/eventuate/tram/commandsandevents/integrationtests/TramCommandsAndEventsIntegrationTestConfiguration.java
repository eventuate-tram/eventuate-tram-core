package io.eventuate.tram.commandsandevents.integrationtests;

import io.eventuate.jdbckafka.TramJdbcKafkaConfiguration;
import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.commands.consumer.CommandDispatcherFactory;
import io.eventuate.tram.commands.consumer.TramCommandConsumerConfiguration;
import io.eventuate.tram.commands.producer.TramCommandProducerConfiguration;
import io.eventuate.tram.consumer.common.TramNoopDuplicateMessageDetectorConfiguration;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.spy;

@Configuration
@EnableAutoConfiguration
@Import({TramJdbcKafkaConfiguration.class,
        TramCommandProducerConfiguration.class,
        TramNoopDuplicateMessageDetectorConfiguration.class,
        TramCommandConsumerConfiguration.class
})
public class TramCommandsAndEventsIntegrationTestConfiguration {


  @Bean
  public TramCommandsAndEventsIntegrationData tramCommandsAndEventsIntegrationData() {
    return new TramCommandsAndEventsIntegrationData();
}


  @Bean
  public ChannelMapping channelMapping(TramCommandsAndEventsIntegrationData data) {
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
    return spy(new MyTestCommandHandler());
  }


  @Bean
  public MyReplyConsumer myReplyConsumer(MessageConsumer messageConsumer) {
    return new MyReplyConsumer(messageConsumer, "ReplyTo");
  }

//  @Bean
//  public DomainEventDispatcher domainEventDispatcher(TramCommandsAndEventsIntegrationData data, MyEventHandler myEventHandler, MessageConsumer messageConsumer) {
//    return new DomainEventDispatcher(data.getEventDispatcherId(),
//            Collections.singletonMap(data.getAggregateDestination(), singleton("*")),
//            myEventHandler,
//            messageConsumer);
//  }
//
//  @Bean
//  public MyEventHandler myEventHandler() {
//    return new MyEventHandler();
//  }
}
