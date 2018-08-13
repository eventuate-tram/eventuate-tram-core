package io.eventuate.tram.cdc.mysql.connector.configuration;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.EventuateConfigurationProperties;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.db.log.common.DbLogBasedCdcDataPublisher;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.mysql.binlog.DebeziumBinlogOffsetKafkaStore;
import io.eventuate.tram.cdc.mysql.connector.*;
import io.eventuate.tram.cdc.mysql.connector.configuration.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import java.util.Optional;

@Configuration
@Import({CommonMessageTableChangesToDestinationsConfiguration.class,
        PostgresWalMessageTableChangesToDestinationsConfiguration.class,
        EventuatePollingMessageTableChangesToDestinationsConfiguration.class,
        MySqlBinlogMessageTableChangesToDestinationsConfiguration.class,
        KafkaMessageTableChangesToDestinationsConfiguration.class,
        ActiveMQMessageTableChangesToDestinationsConfiguration.class,
        RabbitMQMessageTableChangesToDestinationsConfiguration.class})
@EnableConfigurationProperties(EventuateTramChannelProperties.class)
public class MessageTableChangesToDestinationsConfiguration {

  @Bean
  @Conditional(KafkaCondition.class)
  public MysqlBinLogOffsetStoreFactory debeziumOffsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                  EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return (mySqlBinlogCdcPipelineProperties) -> new DebeziumBinlogOffsetKafkaStore(mySqlBinlogCdcPipelineProperties.getOldDbHistoryTopicName(),
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }

  @Bean
  @Conditional(ActiveMQOrRabbitMQCondition.class)
  public MysqlBinLogOffsetStoreFactory emptyDebeziumOffsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return (mySqlBinlogCdcPipelineProperties) -> new DebeziumBinlogOffsetKafkaStore(mySqlBinlogCdcPipelineProperties.getOldDbHistoryTopicName(),
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties) {
      @Override
      public Optional<BinlogFileOffset> getLastBinlogFileOffset() {
        return Optional.empty();
      }
    };
  }

//  @Bean
//  @Conditional(MysqlBinlogKafkaCondition.class)
//  public DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
//                                                                       EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
//                                                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {
//
//    return new DebeziumBinlogOffsetKafkaStore(eventuateConfigurationProperties.getOldDbHistoryTopicName(),
//            eventuateKafkaConfigurationProperties,
//            eventuateKafkaConsumerConfigurationProperties);
//  }
//
//  @Bean
//  @Conditional(MysqlBinlogActiveMQOrRabbitMQCondition.class)
//  public DebeziumBinlogOffsetKafkaStore emptyDebeziumBinlogOffsetKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
//                                                                            EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
//                                                                            EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {
//
//    return new DebeziumBinlogOffsetKafkaStore(eventuateConfigurationProperties.getOldDbHistoryTopicName(),
//            eventuateKafkaConfigurationProperties,
//            eventuateKafkaConsumerConfigurationProperties) {
//      @Override
//      public Optional<BinlogFileOffset> getLastBinlogFileOffset() {
//        return Optional.empty();
//      }
//    };
//  }

//  @Bean
//  @Profile("!EventuatePolling")
//  public CdcDataPublisher<MessageWithDestination> dbLogBasedCdcDataPublisher(DataProducerFactory dataProducerFactory,
//                                                                             PublishingFilter publishingFilter,
//                                                                             OffsetStore offsetStore,
//                                                                             PublishingStrategy<MessageWithDestination> publishingStrategy) {
//
//    return new DbLogBasedCdcDataPublisher<>(dataProducerFactory,
//            offsetStore,
//            publishingFilter,
//            publishingStrategy);
//  }

//  @Bean
//  @Conditional(DbLogKafkaCondition.class)
//  @Primary
//  public OffsetStore databaseOffsetKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
//                                                           EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
//                                                           EventuateKafkaProducer eventuateKafkaProducer,
//                                                           EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {
//
//    return new DatabaseOffsetKafkaStore(eventuateConfigurationProperties.getDbHistoryTopicName(),
//            eventuateConfigurationProperties.getMySqlBinLogClientName(),
//            eventuateKafkaProducer,
//            eventuateKafkaConfigurationProperties,
//            eventuateKafkaConsumerConfigurationProperties);
//  }
//
//  @Bean
//  @Conditional(DbLogActiveMQOrRabbitMQCondition.class)
//  @Primary
//  public OffsetStore databaseOffsetJdbcStore(EventuateConfigurationProperties eventuateConfigurationProperties) {
//    return new JdbcOffsetStore(eventuateConfigurationProperties.getMySqlBinLogClientName());
//  }

  @Bean
  @Conditional(ActiveMQOrRabbitMQCondition.class)
  public PostgresWalOffsetStoreFactory postgresWalJdbcOffsetStoreFactory() {

    return (postgresWalCdcPipelineProperties) ->
            new JdbcOffsetStore(postgresWalCdcPipelineProperties.getMySqlBinLogClientName());

  }

  @Bean
  @Conditional(KafkaCondition.class)
  public PostgresWalOffsetStoreFactory postgresWalKafkaOffsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                          EventuateKafkaProducer eventuateKafkaProducer,
                                                                          EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return (postgresWalCdcPipelineProperties) ->  new DatabaseOffsetKafkaStore(postgresWalCdcPipelineProperties.getDbHistoryTopicName(),
            postgresWalCdcPipelineProperties.getMySqlBinLogClientName(),
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }
}
