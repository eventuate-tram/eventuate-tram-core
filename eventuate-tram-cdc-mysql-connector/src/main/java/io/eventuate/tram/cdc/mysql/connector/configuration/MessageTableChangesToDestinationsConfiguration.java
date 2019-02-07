package io.eventuate.tram.cdc.mysql.connector.configuration;

import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.CdcDataPublisherFactory;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.java.common.broker.CdcDataPublisherTransactionTemplateFactory;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.common.broker.EmptyCdcDataPublisherTransactionTemplate;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.KafkaCdcDataPublisherTransactionTemplate;
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
import io.eventuate.tram.cdc.mysql.connector.configuration.condition.ActiveMQOrRabbitMQCondition;
import io.eventuate.tram.cdc.mysql.connector.configuration.condition.KafkaCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
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
  public CdcProcessingStatusController cdcProcessingStatusController(BinlogEntryReaderProvider binlogEntryReaderProvider) {
    return new CdcProcessingStatusController(binlogEntryReaderProvider);
  }

  @Bean
  public BinlogEntryReaderHealthCheck binlogEntryReaderHealthCheck(BinlogEntryReaderProvider binlogEntryReaderProvider) {
    return new BinlogEntryReaderHealthCheck(binlogEntryReaderProvider);
  }

  @Bean
  public CdcDataPublisherHealthCheck cdcDataPublisherHealthCheck(DataProducerFactory dataProducerFactory,
                                                                 CdcDataPublisherFactory cdcDataPublisherFactory) {
    return new CdcDataPublisherHealthCheck(cdcDataPublisherFactory.create(dataProducerFactory.create()));
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
  @Conditional(ActiveMQOrRabbitMQCondition.class)
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
  @Conditional(ActiveMQOrRabbitMQCondition.class)
  public OffsetStoreFactory postgresWalJdbcOffsetStoreFactory() {

    return (roperties, dataSource, eventuateSchema, clientName, dataProducer) ->
            new JdbcOffsetStore(clientName, new JdbcTemplate(dataSource), eventuateSchema);

  }

  @Bean
  @Conditional(KafkaCondition.class)
  public OffsetStoreFactory postgresWalKafkaOffsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                          EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return (properties, dataSource, eventuateSchema, clientName, dataProducer) ->  {
      if (!(dataProducer instanceof EventuateKafkaProducer)) {
        throw new IllegalArgumentException(String.format("Expected %s", EventuateKafkaProducer.class));
      }

      return new DatabaseOffsetKafkaStore(properties.getOffsetStorageTopicName(),
              clientName,
              (EventuateKafkaProducer) dataProducer,
              eventuateKafkaConfigurationProperties,
              eventuateKafkaConsumerConfigurationProperties);
    };
  }

  @Bean
  @Conditional(KafkaCondition.class)
  public CdcDataPublisherTransactionTemplateFactory kafkaCdcDataPublisherTransactionTemplateFactory() {
    return (dataProducer) -> {
      if (!(dataProducer instanceof EventuateKafkaProducer)) {
        throw new IllegalArgumentException(String.format("Expected %s", EventuateKafkaProducer.class));
      }

      return new KafkaCdcDataPublisherTransactionTemplate((EventuateKafkaProducer)dataProducer);
    };
  }

  @Bean
  @Conditional(ActiveMQOrRabbitMQCondition.class)
  public CdcDataPublisherTransactionTemplateFactory emptyCdcDataPublisherTransactionTemplateFactory() {
    return (dataProducer) -> new EmptyCdcDataPublisherTransactionTemplate();
  }
}
