package io.eventuate.local.mysql.binlog.main;

import io.eventuate.local.unified.cdc.CdcPipelineConfiguration;
import io.eventuate.tram.cdc.mysql.connector.configuration.MessageTableChangesToDestinationsConfiguration;
import io.eventuate.tram.cdc.mysql.connector.configuration.pipeline.CdcTramPipelineConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CdcTramPipelineConfiguration.class)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class EventuateLocalCdcMysqlBinlogMain {

  public static void main(String[] args) {
    SpringApplication.run(EventuateLocalCdcMysqlBinlogMain.class, args);
  }
}
