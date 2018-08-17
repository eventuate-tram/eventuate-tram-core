package io.eventuate.tram.cdc.mysql.connector.configuration;

import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.db.log.common.DatabaseOffsetKafkaStore;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.mysql.binlog.DebeziumBinlogOffsetKafkaStore;
import io.eventuate.local.unified.cdc.DefaultCdcPipelineTypes;
import io.eventuate.tram.cdc.mysql.connector.*;
import io.eventuate.tram.cdc.mysql.connector.configuration.condition.ActiveMQOrRabbitMQCondition;
import io.eventuate.tram.cdc.mysql.connector.configuration.condition.KafkaCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Optional;

@Configuration
@Import({CommonMessageTableChangesToDestinationsConfiguration.class,
        KafkaMessageTableChangesToDestinationsConfiguration.class,
        ActiveMQMessageTableChangesToDestinationsConfiguration.class,
        RabbitMQMessageTableChangesToDestinationsConfiguration.class})
@EnableConfigurationProperties(EventuateTramChannelProperties.class)
public class MessageTableChangesToDestinationsConfiguration {

  @Bean
  public DefaultCdcPipelineTypes defaultCdcPipelineTypes() {
    return new DefaultCdcPipelineTypes() {
      @Override
      public String mySqlBinlogPipelineType() {
        return TramCdcPipelineType.MYSQL_BINLOG.stringRepresentation;
      }

      @Override
      public String eventPollingPipelineType() {
        return TramCdcPipelineType.EVENT_POLLING.stringRepresentation;
      }

      @Override
      public String postgresWalPipelineType() {
        return TramCdcPipelineType.POSTGRES_WAL.stringRepresentation;
      }
    };
  }

  @Bean
  @Conditional(KafkaCondition.class)
  public MysqlBinLogOffsetStoreFactory debeziumOffsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                  EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return (mySqlBinlogCdcPipelineProperties, jdbcTemplate, eventuateSchema) ->
            new DebeziumBinlogOffsetKafkaStore(mySqlBinlogCdcPipelineProperties.getOldDbHistoryTopicName(),
              eventuateKafkaConfigurationProperties,
              eventuateKafkaConsumerConfigurationProperties);
  }

  @Bean
  @Conditional(ActiveMQOrRabbitMQCondition.class)
  public MysqlBinLogOffsetStoreFactory emptyDebeziumOffsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return (mySqlBinlogCdcPipelineProperties, jdbcTemplate, eventuateSchema) ->
            new DebeziumBinlogOffsetKafkaStore(mySqlBinlogCdcPipelineProperties.getOldDbHistoryTopicName(),
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
  public PostgresWalOffsetStoreFactory postgresWalJdbcOffsetStoreFactory() {

    return (postgresWalCdcPipelineProperties, jdbcTemplate, eventuateSchema) ->
            new JdbcOffsetStore(postgresWalCdcPipelineProperties.getMySqlBinLogClientName(), jdbcTemplate, eventuateSchema);

  }

  @Bean
  @Conditional(KafkaCondition.class)
  public PostgresWalOffsetStoreFactory postgresWalKafkaOffsetStoreFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                          EventuateKafkaProducer eventuateKafkaProducer,
                                                                          EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {

    return (postgresWalCdcPipelineProperties, jdbcTemplate, eventuateSchema) ->  new DatabaseOffsetKafkaStore(postgresWalCdcPipelineProperties.getDbHistoryTopicName(),
            postgresWalCdcPipelineProperties.getMySqlBinLogClientName(),
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties);
  }
}
