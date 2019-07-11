package io.eventuate.tram.consumer.jdbc;

import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;

public abstract class AbstractSqlTableBasedDuplicateMessageDetector implements DuplicateMessageDetector {
  private EventuateSchema eventuateSchema;
  private String currentTimeInMillisecondsSql;
  private EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor;

  public AbstractSqlTableBasedDuplicateMessageDetector(EventuateSchema eventuateSchema,
                                                       String currentTimeInMillisecondsSql,
                                                       EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor) {
    this.eventuateSchema = eventuateSchema;
    this.currentTimeInMillisecondsSql = currentTimeInMillisecondsSql;
    this.eventuateJdbcStatementExecutor = eventuateJdbcStatementExecutor;
  }

  protected void tryInsertMessage(String consumerId, String messageId) {
    String table = eventuateSchema.qualifyTable("received_messages");

    eventuateJdbcStatementExecutor.update(String.format("insert into %s(consumer_id, message_id, creation_time) values(?, ?, %s)",
            table,
            currentTimeInMillisecondsSql),
            consumerId, messageId);
  }
}
