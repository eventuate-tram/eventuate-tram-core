package io.eventuate.tram.cdc.mysql.connector;

import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.unified.cdc.properties.MySqlBinlogCdcPipelineProperties;
import io.eventuate.local.unified.cdc.properties.PostgresWalCdcPipelineProperties;

public interface PostgresWalOffsetStoreFactory {
  OffsetStore create(PostgresWalCdcPipelineProperties postgresWalCdcPipelineProperties);
}
