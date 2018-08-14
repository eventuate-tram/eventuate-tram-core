package io.eventuate.tram.cdc.mysql.connector;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.unified.cdc.properties.MySqlBinlogCdcPipelineProperties;
import org.springframework.jdbc.core.JdbcTemplate;

public interface MysqlBinLogOffsetStoreFactory {
  OffsetStore create(MySqlBinlogCdcPipelineProperties mySqlBinlogCdcPipelineProperties,
                     JdbcTemplate jdbcTemplate,
                     EventuateSchema eventuateSchema);
}
