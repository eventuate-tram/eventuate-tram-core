package io.eventuate.tram.cdc.mysql.connector;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.db.log.common.OffsetStore;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

public class JdbcOffsetStore implements OffsetStore {
  private JdbcTemplate jdbcTemplate;
  private EventuateSchema eventuateSchema;
  private String clientName;
  private String tableName;

  public JdbcOffsetStore(String clientName, JdbcTemplate jdbcTemplate, EventuateSchema eventuateSchema) {
    this.clientName = clientName;
    this.jdbcTemplate = jdbcTemplate;
    this.eventuateSchema = eventuateSchema;

    init();
  }

  private void init() {
    tableName = eventuateSchema.qualifyTable("offset_store");

    String selectAllByClientNameQuery = String.format("select * from %s where client_name = ?", tableName);

    if (jdbcTemplate.queryForList(selectAllByClientNameQuery, clientName).isEmpty()) {

      String insertNullOffsetForClientNameQuery =
              String.format("insert into %s (client_name, serialized_offset) VALUES (?, NULL)",
                      tableName);

      jdbcTemplate.update(insertNullOffsetForClientNameQuery, clientName);
    }
  }

  @Override
  public Optional<BinlogFileOffset> getLastBinlogFileOffset() {
    String selectOffsetByClientNameQuery = String.format("select serialized_offset from %s where client_name = ?", tableName);

    String offset = jdbcTemplate.queryForObject(selectOffsetByClientNameQuery, String.class, clientName);
    return Optional.ofNullable(offset).map(o -> JSonMapper.fromJson(o, BinlogFileOffset.class));
  }

  @Override
  public void save(BinlogFileOffset binlogFileOffset) {
    String updateOffsetByClientNameQuery = String.format("update %s set serialized_offset = ?", tableName);
    jdbcTemplate.update(updateOffsetByClientNameQuery, JSonMapper.toJson(binlogFileOffset));
  }

  @Override
  public void stop() {
  }
}
