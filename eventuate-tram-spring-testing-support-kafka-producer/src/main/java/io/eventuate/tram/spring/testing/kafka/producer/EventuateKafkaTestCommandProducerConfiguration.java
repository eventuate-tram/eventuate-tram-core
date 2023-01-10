package io.eventuate.tram.spring.testing.kafka.producer;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.messaging.kafka.producer.EventuateKafkaProducer;
import io.eventuate.messaging.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.messaging.kafka.spring.producer.EventuateKafkaProducerSpringConfigurationPropertiesConfiguration;
import io.eventuate.tram.commands.common.DefaultCommandNameMapping;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.messaging.common.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.UUID;

@Configuration
@Import(EventuateKafkaProducerSpringConfigurationPropertiesConfiguration.class)
public class EventuateKafkaTestCommandProducerConfiguration {

  @Bean
  public EventuateKafkaProducer eventuateKafkaProducer(@Value("${eventuatelocal.kafka.bootstrap.servers}") String bootstrapServer, EventuateKafkaProducerConfigurationProperties eventuateKafkaProducerConfigurationProperties) {
    return new EventuateKafkaProducer(bootstrapServer, eventuateKafkaProducerConfigurationProperties);
  }

  @Bean
  public CommandProducerImpl commandProducer(EventuateKafkaProducer eventuateKafkaProducer) {
    return new CommandProducerImpl((destination, message) -> {
      message.getHeaders().put(Message.ID, UUID.randomUUID().toString());
      eventuateKafkaProducer.send(destination, "1", JSonMapper.toJson(message));
    }, new DefaultCommandNameMapping());
  }
}
