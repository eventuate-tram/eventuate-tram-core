import io.eventuate.tram.cdc.mysql.connector.EventuateTramChannelProperties;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.rabbitmq.MessageConsumerRabbitMQImpl;
import io.eventuate.tram.data.producer.rabbitmq.EventuateRabbitMQProducer;
import io.eventuate.tram.messaging.common.ChannelType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.UUID;

@Configuration
@EnableConfigurationProperties(EventuateTramChannelProperties.class)
@EnableAutoConfiguration
public class CommonQueueTopicTestConfiguration {

  @Bean
  @Qualifier("uniquePostfix")
  public String uniquePostfix() {
    return UUID.randomUUID().toString();
  }

  @Bean
  public MessageConsumerRabbitMQImpl messageConsumerRabbitMQ(@Value("${rabbitmq.url}") String rabbitMQURL,
                                                             @Qualifier("uniquePostfix") String uniquePostfix,
                                                             @Qualifier("testChannelType") ChannelType channelType) {
    return new MessageConsumerRabbitMQImpl(rabbitMQURL,
            Collections.singletonMap("destination" + uniquePostfix, channelType));
  }

  @Bean
  public EventuateRabbitMQProducer rabbitMQMessageProducer(@Value("${rabbitmq.url}") String rabbitMQURL,
                                                           @Qualifier("uniquePostfix") String uniquePostfix,
                                                           @Qualifier("testChannelType") ChannelType channelType) {
    return new EventuateRabbitMQProducer(rabbitMQURL,
            Collections.singletonMap("destination" + uniquePostfix, channelType));
  }

  @Bean
  public DuplicateMessageDetector duplicateMessageDetector() {
    return (consumerId, messageId) -> false;
  }
}
