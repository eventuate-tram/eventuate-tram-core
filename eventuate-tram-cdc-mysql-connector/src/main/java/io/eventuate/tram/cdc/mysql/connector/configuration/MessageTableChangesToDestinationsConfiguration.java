package io.eventuate.tram.cdc.mysql.connector.configuration;

import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.mysql.binlog.DebeziumBinlogOffsetKafkaStore;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.DefaultSourceTableNameResolver;
import io.eventuate.local.unified.cdc.pipeline.common.health.BinlogEntryReaderHealthCheck;
import io.eventuate.local.unified.cdc.pipeline.common.health.CdcDataPublisherHealthCheck;
import io.eventuate.local.unified.cdc.pipeline.common.health.KafkaHealthCheck;
import io.eventuate.local.unified.cdc.pipeline.common.health.ZookeeperHealthCheck;
import io.eventuate.local.unified.cdc.pipeline.dblog.common.factory.OffsetStoreFactory;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.factory.DebeziumOffsetStoreFactory;
import io.eventuate.tram.cdc.mysql.connector.CdcProcessingStatusController;
import io.eventuate.tram.cdc.mysql.connector.EventuateTramChannelProperties;
import io.eventuate.tram.cdc.mysql.connector.JdbcOffsetStore;
import io.eventuate.tram.cdc.mysql.connector.configuration.condition.ActiveMQOrRabbitMQOrRedisCondition;
import io.eventuate.tram.cdc.mysql.connector.configuration.condition.KafkaCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

@Configuration
@Import({CommonMessageTableChangesToDestinationsConfiguration.class,
        KafkaMessageTableChangesToDestinationsConfiguration.class,
        ActiveMQMessageTableChangesToDestinationsConfiguration.class,
        RabbitMQMessageTableChangesToDestinationsConfiguration.class,
        RedisMessageTableChangesToDestinationsConfiguration.class})
@EnableConfigurationProperties(EventuateTramChannelProperties.class)
public class MessageTableChangesToDestinationsConfiguration {

  @Bean
  public CdcProcessingStatusController cdcProcessingStatusController(BinlogEntryReaderProvider binlogEntryReaderProvider) {
    return new CdcProcessingStatusController(binlogEntryReaderProvider);
  }

  @Bean
  public BinlogEntryReaderHealthCheck binlogEntryReaderHealthCheck(BinlogEntryReaderProvider binlogEntryReaderProvider) {
    return new BinlogEntryReaderHealthCheck(binlogEntryReaderProvider);
  }

  @Bean
  public CdcDataPublisherHealthCheck cdcDataPublisherHealthCheck(CdcDataPublisher cdcDataPublisher) {
    return new CdcDataPublisherHealthCheck(cdcDataPublisher);
  }

  @Bean
  public ZookeeperHealthCheck zookeeperHealthCheck() {
    return new ZookeeperHealthCheck();
  }

  @Bean
  public KafkaHealthCheck kafkaHealthCheck() {
    return new KafkaHealthCheck();
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

    return () ->
            new DebeziumBinlogOffsetKafkaStore(eventuateKafkaConfigurationProperties,
              eventuateKafkaConsumerConfigurationProperties);
  }

  @Bean
  @Conditional(ActiveMQOrRabbitMQOrRedisCondition.class)
  public DebeziumOffsetStoreFactory emptyDebeziumOffsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return () ->
            new DebeziumBinlogOffsetKafkaStore(eventuateKafkaConfigurationProperties,
              eventuateKafkaConsumerConfigurationProperties) {
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

  @Bean
  @Conditional(KafkaCondition.class)
  public OffsetStoreFactory postgresWalKafkaOffsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                          EventuateKafkaProducer eventuateKafkaProducer,
                                                                          EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return (properties, dataSource, eventuateSchema, clientName) ->  new DatabaseOffsetKafkaStore(properties.getOffsetStorageTopicName(),
            clientName,
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }
}
