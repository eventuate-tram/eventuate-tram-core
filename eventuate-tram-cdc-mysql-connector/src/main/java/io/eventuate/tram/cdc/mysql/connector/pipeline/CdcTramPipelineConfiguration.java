package io.eventuate.tram.cdc.mysql.connector.pipeline;

import io.eventuate.local.unified.cdc.pipeline.CdcPipelineConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.MySqlBinlogCdcDefaultPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.mysqlbinlog.configuration.MySqlBinlogCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.PostgresWalCdcDefaultPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.configuration.PostgresWalCdcPipelineFactoryConfiguration;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.PollingCdcDefaultPipelinePropertiesConfiguration;
import io.eventuate.local.unified.cdc.pipeline.polling.configuration.PollingCdcPipelineFactoryConfiguration;
import io.eventuate.tram.cdc.mysql.connector.configuration.MessageTableChangesToDestinationsConfiguration;
import io.eventuate.tram.cdc.mysql.connector.pipeline.mysqlbinlog.configuration.MySqlBinlogCdcTramPipelineFactoryConfiguration;
import io.eventuate.tram.cdc.mysql.connector.pipeline.polling.configuration.PollingCdcTramPipelineFactoryConfiguration;
import io.eventuate.tram.cdc.mysql.connector.pipeline.postgreswal.configuration.PostgresWalCdcTramPipelineFactoryConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MessageTableChangesToDestinationsConfiguration.class,

        PollingCdcDefaultPipelinePropertiesConfiguration.class,
        PollingCdcPipelineFactoryConfiguration.class,
        PollingCdcTramPipelineFactoryConfiguration.class,

        MySqlBinlogCdcDefaultPipelinePropertiesConfiguration.class,
        MySqlBinlogCdcPipelineFactoryConfiguration.class,
        MySqlBinlogCdcTramPipelineFactoryConfiguration.class,

        PostgresWalCdcDefaultPipelinePropertiesConfiguration.class,
        PostgresWalCdcPipelineFactoryConfiguration.class,
        PostgresWalCdcTramPipelineFactoryConfiguration.class,

        CdcPipelineConfiguration.class})
public class CdcTramPipelineConfiguration {
}
