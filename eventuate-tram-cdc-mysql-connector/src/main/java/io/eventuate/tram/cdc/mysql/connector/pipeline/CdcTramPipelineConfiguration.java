package io.eventuate.tram.cdc.mysql.connector.pipeline;

import io.eventuate.local.unified.cdc.pipeline.CdcPipelineConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.MySqlBinlogCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.MySqlBinlogDefaultCdcPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.PostgresWalCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.PostgresWalDefaultCdcPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.PollingCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.PollingDefaultCdcPipelinePropertiesConfiguration;
import io.eventuate.tram.cdc.mysql.connector.configuration.MessageTableChangesToDestinationsConfiguration;
import io.eventuate.tram.cdc.mysql.connector.pipeline.mysqlbinlog.configuration.MySqlBinlogCdcTramPipelineFactoryConfiguration;
import io.eventuate.tram.cdc.mysql.connector.pipeline.mysqlbinlog.configuration.MySqlBinlogDefaultCdcTramPipelineFactoryConfiguration;
import io.eventuate.tram.cdc.mysql.connector.pipeline.polling.configuration.PollingCdcTramPipelineFactoryConfiguration;
import io.eventuate.tram.cdc.mysql.connector.pipeline.polling.configuration.PollingDefaultCdcTramPipelineFactoryConfiguration;
import io.eventuate.tram.cdc.mysql.connector.pipeline.postgreswal.configuration.PostgresWalCdcTramPipelineFactoryConfiguration;
import io.eventuate.tram.cdc.mysql.connector.pipeline.postgreswal.configuration.PostgresWalDefaultCdcTramPipelineFactoryConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MessageTableChangesToDestinationsConfiguration.class,

        PollingDefaultCdcPipelinePropertiesConfiguration.class,
        PollingCdcPipelineFactoryConfiguration.class,
        PollingCdcTramPipelineFactoryConfiguration.class,
        PollingDefaultCdcTramPipelineFactoryConfiguration.class,

        MySqlBinlogDefaultCdcPipelinePropertiesConfiguration.class,
        MySqlBinlogCdcPipelineFactoryConfiguration.class,
        MySqlBinlogCdcTramPipelineFactoryConfiguration.class,
        MySqlBinlogDefaultCdcTramPipelineFactoryConfiguration.class,

        PostgresWalDefaultCdcPipelinePropertiesConfiguration.class,
        PostgresWalCdcPipelineFactoryConfiguration.class,
        PostgresWalCdcTramPipelineFactoryConfiguration.class,
        PostgresWalDefaultCdcTramPipelineFactoryConfiguration.class,

        CdcPipelineConfiguration.class})
public class CdcTramPipelineConfiguration {
}
