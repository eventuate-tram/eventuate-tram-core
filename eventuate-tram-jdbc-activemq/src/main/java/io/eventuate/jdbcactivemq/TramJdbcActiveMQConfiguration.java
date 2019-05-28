package io.eventuate.jdbcactivemq;

import io.eventuate.messaging.activemq.consumer.MessageConsumerActiveMQImpl;
import io.eventuate.messaging.activemq.consumer.TramConsumerActiveMQConfiguration;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.consumer.wrappers.EventuateActiveMQMessageConsumerWrapper;
import io.eventuate.tram.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramConsumerActiveMQConfiguration.class,
        TramMessageProducerJdbcConfiguration.class,
        TramConsumerCommonConfiguration.class})
public class TramJdbcActiveMQConfiguration {
  @Bean
  public MessageConsumerImplementation messageConsumerImplementation(MessageConsumerActiveMQImpl messageConsumerActiveMQ) {
    return new EventuateActiveMQMessageConsumerWrapper(messageConsumerActiveMQ);
  }
}
