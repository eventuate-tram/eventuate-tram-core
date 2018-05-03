package io.eventuate.tram.cdc.mysql.connector.configuration;

import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.EventuateConfigurationProperties;
import io.eventuate.local.db.log.common.DbLogBasedCdcProcessor;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.postgres.wal.PostgresWalClient;
import io.eventuate.local.postgres.wal.PostgresWalMessageParser;
import io.eventuate.tram.cdc.mysql.connector.MessageWithDestination;
import io.eventuate.tram.cdc.mysql.connector.PostgresWalJsonMessageParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PostgresWalMessageTableChangesToDestinationsConfiguration {
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
