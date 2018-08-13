package io.eventuate.local.mysql.binlog.main;

import io.eventuate.local.unified.cdc.CdcPipelineConfiguration;
import io.eventuate.tram.cdc.mysql.connector.configuration.MessageTableChangesToDestinationsConfiguration;
import io.eventuate.tram.cdc.mysql.connector.configuration.pipeline.CdcTramPipelineConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CdcTramPipelineConfiguration.class)
public class EventuateLocalCdcMysqlBinlogMain {

  public static void main(String[] args) {
    SpringApplication.run(EventuateLocalCdcMysqlBinlogMain.class, args);
  }
}
