package io.eventuate.tram.cdc.mysql.connector.configuration;

import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.HealthCheck;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.mysql.binlog.DebeziumBinlogOffsetKafkaStore;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.DefaultSourceTableNameResolver;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.OffsetStoreFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.DebeziumOffsetStoreFactory;
import io.eventuate.tram.cdc.mysql.connector.EventuateTramChannelProperties;
import io.eventuate.tram.cdc.mysql.connector.JdbcOffsetStore;
import io.eventuate.tram.cdc.mysql.connector.configuration.condition.ActiveMQOrRabbitMQCondition;
import io.eventuate.tram.cdc.mysql.connector.configuration.condition.KafkaCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

@Configuration
@Import({CommonMessageTableChangesToDestinationsConfiguration.class,
        KafkaMessageTableChangesToDestinationsConfiguration.class,
        ActiveMQMessageTableChangesToDestinationsConfiguration.class,
        RabbitMQMessageTableChangesToDestinationsConfiguration.class})
@EnableConfigurationProperties(EventuateTramChannelProperties.class)
public class MessageTableChangesToDestinationsConfiguration {

  @Bean
  public HealthCheck healthCheck() {
    return new HealthCheck();
  }

  @Bean
  public DefaultSourceTableNameResolver defaultSourceTableNameResolver() {
    return pipelineType -> {
      if ("eventuate-tram".equals(pipelineType) || "default".equals(pipelineType)) return "message";
      if ("eventuate-local".equals(pipelineType)) return "events";

      throw new RuntimeException(String.format("Unknown pipeline type '%s'", pipelineType));
    };
  }

  @Bean
  public BinlogEntryReaderProvider dbClientProvider() {
    return new BinlogEntryReaderProvider();
  }

  @Bean
  @Conditional(KafkaCondition.class)
  public DebeziumOffsetStoreFactory debeziumOffsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                               EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return (oldDbHistoryTopicName) ->
            new DebeziumBinlogOffsetKafkaStore(oldDbHistoryTopicName,
              eventuateKafkaConfigurationProperties,
              eventuateKafkaConsumerConfigurationProperties);
  }

  @Bean
  @Conditional(ActiveMQOrRabbitMQCondition.class)
  public DebeziumOffsetStoreFactory emptyDebeziumOffsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return (oldDbHistoryTopicName) ->
            new DebeziumBinlogOffsetKafkaStore(oldDbHistoryTopicName,
              eventuateKafkaConfigurationProperties,
              eventuateKafkaConsumerConfigurationProperties) {
      @Override
      public Optional<BinlogFileOffset> getLastBinlogFileOffset() {
        return Optional.empty();
      }
    };
  }

  @Bean
  @Conditional(ActiveMQOrRabbitMQCondition.class)
  public OffsetStoreFactory postgresWalJdbcOffsetStoreFactory() {

    return (roperties, dataSource, eventuateSchema, clientName) ->
            new JdbcOffsetStore(clientName, new JdbcTemplate(dataSource), eventuateSchema);

  }

  @Bean
  @Conditional(KafkaCondition.class)
  public OffsetStoreFactory postgresWalKafkaOffsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                          EventuateKafkaProducer eventuateKafkaProducer,
                                                                          EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return (properties, dataSource, eventuateSchema, clientName) ->  new DatabaseOffsetKafkaStore(properties.getDbHistoryTopicName(),
            clientName,
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }
}
