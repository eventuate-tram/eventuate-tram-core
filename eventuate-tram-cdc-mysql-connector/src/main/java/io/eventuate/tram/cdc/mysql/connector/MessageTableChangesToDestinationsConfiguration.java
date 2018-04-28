package io.eventuate.tram.cdc.mysql.connector;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.*;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.mysql.binlog.*;
import io.eventuate.local.polling.PollingCdcDataPublisher;
import io.eventuate.local.polling.PollingCdcProcessor;
import io.eventuate.local.polling.PollingDao;
import io.eventuate.local.polling.PollingDataProvider;
import io.eventuate.local.postgres.wal.PostgresWalClient;
import io.eventuate.local.postgres.wal.PostgresWalMessageParser;
import io.eventuate.tram.data.producer.activemq.EventuateActiveMQProducer;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;
import java.util.Optional;

@Configuration
@Import(EventuateDriverConfiguration.class)
@EnableConfigurationProperties(EventuateTramChannelProperties.class)
public class MessageTableChangesToDestinationsConfiguration {

  @Bean
  @Profile("!ActiveMQ")
  public PublishingFilter kafkaDuplicatePublishingDetector(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {
    return new DuplicatePublishingDetector(eventuateKafkaConfigurationProperties.getBootstrapServers());
  }

  @Bean
  @Profile("ActiveMQ")
  public PublishingFilter activeMQDuplicatePublishingDetector() {
    return (fileOffset, topic) -> true;
  }

  @Bean
  public EventuateSchema eventuateSchema(@Value("${eventuate.database.schema:#{null}}") String eventuateDatabaseSchema) {
    return new EventuateSchema(eventuateDatabaseSchema);
  }

  @Bean
  public EventuateConfigurationProperties eventuateConfigurationProperties() {
    return new EventuateConfigurationProperties();
  }

  @Bean
  public EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties() {
    return new EventuateLocalZookeperConfigurationProperties();
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public SourceTableNameSupplier sourceTableNameSupplier(EventuateConfigurationProperties eventuateConfigurationProperties) {
    return new SourceTableNameSupplier(eventuateConfigurationProperties.getSourceTableName(), MySQLTableConfig.EVENTS_TABLE_NAME);
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public IWriteRowsEventDataParser eventDataParser(EventuateSchema eventuateSchema,
          DataSource dataSource) {
    return new WriteRowsEventDataParser(dataSource, eventuateSchema);
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public DbLogClient<MessageWithDestination> mySqlBinaryLogClient(@Value("${spring.datasource.url}") String dataSourceURL,
                                                                  EventuateConfigurationProperties eventuateConfigurationProperties,
                                                                  SourceTableNameSupplier sourceTableNameSupplier,
                                                                  IWriteRowsEventDataParser<MessageWithDestination> eventDataParser) {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);
    return new MySqlBinaryLogClient<>(eventDataParser,
            eventuateConfigurationProperties.getDbUserName(),
            eventuateConfigurationProperties.getDbPassword(),
            jdbcUrl.getHost(),
            jdbcUrl.getPort(),
            eventuateConfigurationProperties.getBinlogClientId(),
            sourceTableNameSupplier.getSourceTableName(),
            eventuateConfigurationProperties.getMySqlBinLogClientName(),
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection());
  }

  @Bean
  public EventuateKafkaProducer eventuateKafkaProducer(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {
    return new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers());
  }

  @Bean
  @Profile("ActiveMQ")
  public DataProducerFactory activeMQDataProducerFactory(@Value("${activemq.url}") String activeMQURL) {
    return () -> new EventuateActiveMQProducer(activeMQURL);
  }

  @Bean
  @Profile("!ActiveMQ")
  public DataProducerFactory kafkaDataProducerFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {
    return () -> new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers());
  }

  @Bean
  public PublishingStrategy<MessageWithDestination> publishingStrategy() {
    return new MessageWithDestinationPublishingStrategy();
  }

  @Bean
  @Conditional(MysqlBinlogKafkaCondition.class)
  public DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
                                                                       EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {

    return new DebeziumBinlogOffsetKafkaStore(eventuateConfigurationProperties.getOldDbHistoryTopicName(), eventuateKafkaConfigurationProperties);
  }

  @Bean
  @Conditional(MysqlBinlogActiveMQCondition.class)
  public DebeziumBinlogOffsetKafkaStore emptyDebeziumBinlogOffsetKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
                                                                            EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {

    return new DebeziumBinlogOffsetKafkaStore(eventuateConfigurationProperties.getOldDbHistoryTopicName(), eventuateKafkaConfigurationProperties) {
      @Override
      public Optional<BinlogFileOffset> getLastBinlogFileOffset() {
        return Optional.empty();
      }
    };
  }


  @Bean
  public EventTableChangesToAggregateTopicTranslator<MessageWithDestination> eventTableChangesToAggregateTopicTranslator(EventuateConfigurationProperties eventuateConfigurationProperties,
                                                                                                                         CdcDataPublisher<MessageWithDestination> cdcDataPublisher,
                                                                                                                         CdcProcessor<MessageWithDestination> cdcProcessor,
                                                                                                                         CuratorFramework curatorFramework) {
    return new EventTableChangesToAggregateTopicTranslator<>(cdcDataPublisher, cdcProcessor, curatorFramework, eventuateConfigurationProperties.getLeadershipLockPath());
  }

  @Bean(destroyMethod = "close")
  public CuratorFramework curatorFramework(EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties) {
    String connectionString = eventuateLocalZookeperConfigurationProperties.getConnectionString();
    return makeStartedCuratorClient(connectionString);
  }

  @Bean
  @Profile("!EventuatePolling")
  public CdcDataPublisher<MessageWithDestination> dbLogBasedCdcDataPublisher(DataProducerFactory dataProducerFactory,
                                                                             PublishingFilter publishingFilter,
                                                                             OffsetStore offsetStore,
                                                                             PublishingStrategy<MessageWithDestination> publishingStrategy) {

    return new DbLogBasedCdcDataPublisher<>(dataProducerFactory,
            offsetStore,
            publishingFilter,
            publishingStrategy);
  }

  @Bean
  @Conditional(MySqlBinlogCondition.class)
  public CdcProcessor<MessageWithDestination> mysqlBinLogCdcProcessor(DbLogClient<MessageWithDestination> dbLogClient,
                                                                      OffsetStore offsetStore,
                                                                      DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore) {

    return new MySQLCdcProcessor<>(dbLogClient, offsetStore, debeziumBinlogOffsetKafkaStore);
  }

  @Bean
  @Conditional(DbLogKafkaCondition.class)
  @Primary
  public OffsetStore databaseOffsetKafkaStore(EventuateConfigurationProperties eventuateConfigurationProperties,
                                                           EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                           EventuateKafkaProducer eventuateKafkaProducer) {

    return new DatabaseOffsetKafkaStore(eventuateConfigurationProperties.getDbHistoryTopicName(),
            eventuateConfigurationProperties.getMySqlBinLogClientName(),
            eventuateKafkaProducer,
            eventuateKafkaConfigurationProperties);
  }

  @Bean
  @Conditional(DbLogAvtiveMQCondition.class)
  @Primary
  public OffsetStore databaseOffsetJdbcStore(EventuateConfigurationProperties eventuateConfigurationProperties) {
    return new JdbcOffsetStore(eventuateConfigurationProperties.getMySqlBinLogClientName());
  }

  @Bean
  @Profile("EventuatePolling")
  public CdcDataPublisher<MessageWithDestination> pollingCdcDataPublisher(DataProducerFactory dataProducerFactory,
                                                                            PublishingStrategy<MessageWithDestination> publishingStrategy) {

    return new PollingCdcDataPublisher<>(dataProducerFactory, publishingStrategy);
  }

  @Bean
  @Profile("EventuatePolling")
  public CdcProcessor<MessageWithDestination> pollingCdcProcessor(EventuateConfigurationProperties eventuateConfigurationProperties,
    PollingDao<PollingMessageBean, MessageWithDestination, String> pollingDao) {

    return new PollingCdcProcessor<>(pollingDao, eventuateConfigurationProperties.getPollingIntervalInMilliseconds());
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingDao<PollingMessageBean, MessageWithDestination, String> pollingDao(PollingDataProvider<PollingMessageBean, MessageWithDestination, String> pollingDataProvider,
                                                                                   DataSource dataSource,
                                                                                   EventuateConfigurationProperties eventuateConfigurationProperties) {

    return new PollingDao<>(pollingDataProvider,
      dataSource,
      eventuateConfigurationProperties.getMaxEventsPerPolling(),
      eventuateConfigurationProperties.getMaxAttemptsForPolling(),
      eventuateConfigurationProperties.getPollingRetryIntervalInMilliseconds());
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingDataProvider<PollingMessageBean, MessageWithDestination, String> pollingDataProvider(EventuateSchema eventuateSchema) {
    return new PollingMessageDataProvider(eventuateSchema);
  }

  static CuratorFramework makeStartedCuratorClient(String connectionString) {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client = CuratorFrameworkFactory.
            builder().retryPolicy(retryPolicy)
            .connectString(connectionString)
            .build();
    client.start();
    return client;
  }

  @Bean
  @Profile("PostgresWal")
  public DbLogClient<MessageWithDestination> postgresWalDbLogClient(@Value("${spring.datasource.url}") String dbUrl,
                                                 @Value("${spring.datasource.username}") String dbUserName,
                                                 @Value("${spring.datasource.password}") String dbPassword,
                                                 EventuateConfigurationProperties eventuateConfigurationProperties,
                                                 PostgresWalMessageParser<MessageWithDestination> postgresWalMessageParser) {

    return new PostgresWalClient<>(postgresWalMessageParser,
            dbUrl,
            dbUserName,
            dbPassword,
            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection(),
            eventuateConfigurationProperties.getPostgresWalIntervalInMilliseconds(),
            eventuateConfigurationProperties.getPostgresReplicationStatusIntervalInMilliseconds(),
            eventuateConfigurationProperties.getPostgresReplicationSlotName());
  }

  @Bean
  @Profile("PostgresWal")
  public PostgresWalMessageParser<MessageWithDestination> postgresReplicationMessageParser() {
    return new PostgresWalJsonMessageParser();
  }

  @Bean
  @Profile("PostgresWal")
  public CdcProcessor<MessageWithDestination> postgresWalCdcProcessor(DbLogClient<MessageWithDestination> dbLogClient,
                                                                      OffsetStore offsetStore) {

    return new DbLogBasedCdcProcessor<>(dbLogClient, offsetStore);
  }

}
