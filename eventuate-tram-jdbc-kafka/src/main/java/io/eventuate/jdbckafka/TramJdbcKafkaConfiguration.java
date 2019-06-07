package io.eventuate.jdbckafka;

import io.eventuate.messaging.kafka.consumer.MessageConsumerKafkaConfiguration;
import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.consumer.kafka.EventuateTramKafkaMessageConsumerConfiguration;
import io.eventuate.tram.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MessageConsumerKafkaConfiguration.class,
        TramMessageProducerJdbcConfiguration.class,
        TramConsumerCommonConfiguration.class,
        EventuateTramKafkaMessageConsumerConfiguration.class})
public class TramJdbcKafkaConfiguration {
}
