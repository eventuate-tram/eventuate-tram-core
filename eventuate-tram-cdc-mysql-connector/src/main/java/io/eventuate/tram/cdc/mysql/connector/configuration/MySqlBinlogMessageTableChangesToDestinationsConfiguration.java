package io.eventuate.tram.cdc.mysql.connector.configuration;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.*;
import io.eventuate.local.db.log.common.DbLogClient;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.mysql.binlog.*;
import io.eventuate.tram.cdc.mysql.connector.MessageWithDestination;
import io.eventuate.tram.cdc.mysql.connector.MySQLTableConfig;
import io.eventuate.tram.cdc.mysql.connector.WriteRowsEventDataParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MySqlBinlogMessageTableChangesToDestinationsConfiguration {
//  @Bean
//  @Conditional(MySqlBinlogCondition.class)
//  public CdcProcessor<MessageWithDestination> mysqlBinLogCdcProcessor(DbLogClient<MessageWithDestination> dbLogClient,
//                                                                      OffsetStore offsetStore,
//                                                                      DebeziumBinlogOffsetKafkaStore debeziumBinlogOffsetKafkaStore) {
//
//    return new MySQLCdcProcessor<>(dbLogClient, offsetStore, debeziumBinlogOffsetKafkaStore);
//  }
//
//  @Bean
//  @Conditional(MySqlBinlogCondition.class)
//  public SourceTableNameSupplier sourceTableNameSupplier(EventuateConfigurationProperties eventuateConfigurationProperties) {
//    return new SourceTableNameSupplier(eventuateConfigurationProperties.getSourceTableName(), MySQLTableConfig.EVENTS_TABLE_NAME);
//  }
//
//  @Bean
//  @Conditional(MySqlBinlogCondition.class)
//  public IWriteRowsEventDataParser eventDataParser(EventuateSchema eventuateSchema,
//                                                   DataSource dataSource) {
//    return new WriteRowsEventDataParser(dataSource, eventuateSchema);
//  }
//
//  @Bean
//  @Conditional(MySqlBinlogCondition.class)
//  public DbLogClient<MessageWithDestination> mySqlBinaryLogClient(@Value("${spring.datasource.url}") String dataSourceURL,
//                                                                  EventuateConfigurationProperties eventuateConfigurationProperties,
//                                                                  SourceTableNameSupplier sourceTableNameSupplier,
//                                                                  IWriteRowsEventDataParser<MessageWithDestination> eventDataParser) {
//    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);
//    return new MySqlBinaryLogClient<>(eventDataParser,
//            eventuateConfigurationProperties.getDbUserName(),
//            eventuateConfigurationProperties.getDbPassword(),
//            jdbcUrl.getHost(),
//            jdbcUrl.getPort(),
//            eventuateConfigurationProperties.getBinlogClientId(),
//            sourceTableNameSupplier.getSourceTableName(),
//            eventuateConfigurationProperties.getMySqlBinLogClientName(),
//            eventuateConfigurationProperties.getBinlogConnectionTimeoutInMilliseconds(),
//            eventuateConfigurationProperties.getMaxAttemptsForBinlogConnection());
//  }
}
