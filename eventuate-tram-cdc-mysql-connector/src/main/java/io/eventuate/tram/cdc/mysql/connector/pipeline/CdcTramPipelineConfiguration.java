package io.eventuate.tram.cdc.mysql.connector.pipeline;

import io.eventuate.local.unified.cdc.pipeline.CdcPipelineConfigurator;
import io.eventuate.local.unified.cdc.pipeline.common.configuration.CdcDataPublisherConfiguration;
import io.eventuate.local.unified.cdc.pipeline.common.configuration.CdcDefaultPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.common.configuration.CdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.common.properties.RawUnifiedCdcProperties;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.MySqlBinlogCdcPipelineReaderConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.PostgresWalCdcPipelineReaderConfiguration;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.PollingCdcPipelineReaderConfiguration;
import io.eventuate.tram.cdc.mysql.connector.configuration.MessageTableChangesToDestinationsConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MessageTableChangesToDestinationsConfiguration.class,

        CdcDataPublisherConfiguration.class,

        CdcDefaultPipelinePropertiesConfiguration.class,
        CdcPipelineFactoryConfiguration.class,
        DefaultCdcTramPipelineFactoryConfiguration.class,
        CdcTramPipelineFactoryConfiguration.class,

        MySqlBinlogCdcPipelineReaderConfiguration.class,
        PollingCdcPipelineReaderConfiguration.class,
        PostgresWalCdcPipelineReaderConfiguration.class})
@EnableConfigurationProperties(RawUnifiedCdcProperties.class)
public class CdcTramPipelineConfiguration {
  @Bean
  public CdcPipelineConfigurator cdcPipelineConfigurator() {
    return new CdcPipelineConfigurator();
  }
}
