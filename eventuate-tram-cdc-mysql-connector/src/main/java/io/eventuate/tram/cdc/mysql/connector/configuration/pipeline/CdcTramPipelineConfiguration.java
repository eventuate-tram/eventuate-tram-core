package io.eventuate.tram.cdc.mysql.connector.configuration.pipeline;

import io.eventuate.local.unified.cdc.CdcPipelineConfiguration;
import io.eventuate.local.unified.cdc.CdcPipelineFactoryConfiguration;
import io.eventuate.tram.cdc.mysql.connector.configuration.MessageTableChangesToDestinationsConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CdcPipelineConfiguration.class, CdcPipelineFactoryConfiguration.class, CdcTramPipelineFactoryConfiguration.class, MessageTableChangesToDestinationsConfiguration.class})
public class CdcTramPipelineConfiguration {
}
