package io.eventuate.tram.cdc.mysql.connector;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.unified.cdc.pipeline.dblog.postgreswal.properties.PostgresWalCdcPipelineProperties;
import org.springframework.jdbc.core.JdbcTemplate;

public interface PostgresWalOffsetStoreFactory {
  OffsetStore create(PostgresWalCdcPipelineProperties postgresWalCdcPipelineProperties,
                     JdbcTemplate jdbcTemplate,
                     EventuateSchema eventuateSchema);
}
