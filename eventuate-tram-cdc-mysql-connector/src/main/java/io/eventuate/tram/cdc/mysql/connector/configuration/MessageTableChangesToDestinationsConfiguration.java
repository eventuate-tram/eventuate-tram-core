package io.eventuate.tram.cdc.mysql.connector.configuration;

import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.mysql.binlog.DebeziumBinlogOffsetKafkaStore;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.OffsetStoreFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.DebeziumOffsetStoreFactory;
import io.eventuate.sql.dialect.SqlDialectConfiguration;
import io.eventuate.tram.cdc.mysql.connector.EventuateTramChannelProperties;
import io.eventuate.tram.cdc.mysql.connector.JdbcOffsetStore;
import io.eventuate.tram.cdc.mysql.connector.configuration.condition.ActiveMQOrRabbitMQOrRedisCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

@Configuration
@Import({SqlDialectConfiguration.class,
        CommonMessageTableChangesToDestinationsConfiguration.class,
        ZookeeperConfiguration.class,
        KafkaMessageTableChangesToDestinationsConfiguration.class,
        ActiveMQMessageTableChangesToDestinationsConfiguration.class,
        RabbitMQMessageTableChangesToDestinationsConfiguration.class,
        RedisMessageTableChangesToDestinationsConfiguration.class})
@EnableConfigurationProperties(EventuateTramChannelProperties.class)
public class MessageTableChangesToDestinationsConfiguration {

  @Bean
  @Conditional(ActiveMQOrRabbitMQOrRedisCondition.class)
  public DebeziumOffsetStoreFactory emptyDebeziumOffsetStoreFactory() {

    return () ->
            new DebeziumBinlogOffsetKafkaStore(null, null) {
      @Override
      public Optional<BinlogFileOffset> getLastBinlogFileOffset() {
        return Optional.empty();
      }
    };
  }

  @Bean
  @Conditional(ActiveMQOrRabbitMQOrRedisCondition.class)
  public OffsetStoreFactory postgresWalJdbcOffsetStoreFactory() {

    return (roperties, dataSource, eventuateSchema, clientName) ->
            new JdbcOffsetStore(clientName, new JdbcTemplate(dataSource), eventuateSchema);

  }
}
