package io.eventuate.tram.spring.flyway;

import io.eventuate.common.jdbc.OutboxPartitioningSpec;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.boot.jdbc.DatabaseDriver;

import java.sql.Connection;
import java.util.Collections;

public class V1005__MyMigration extends BaseJavaMigration {

  private OutboxPartitioningSpec outboxPartitioningSpec;
  private ScriptExecutor scriptExecutor;

  public V1005__MyMigration(OutboxPartitioningSpec outboxPartitioningSpec) {
    this.outboxPartitioningSpec = outboxPartitioningSpec;
    this.scriptExecutor = new ScriptExecutor();
  }

  @Override
  public void migrate(Context context) throws Exception {
    System.out.println("Hi-flyway");

    Connection connection = context.getConnection();
    DatabaseDriver driver = DatabaseDriver.fromJdbcUrl(connection.getMetaData().getURL());
    String driverId = driver.getId();

    SqlExecutor sqlExecutor = statement -> connection.prepareStatement(statement).execute();

    outboxPartitioningSpec.outboxTableSuffixes().forEach(suffix ->
            scriptExecutor.executeScript(Collections.singletonMap("EVENTUATE_OUTBOX_SUFFIX", suffix.suffixAsString),
                    "flyway-templates/" + driverId + "/3.create-message-table.sql", sqlExecutor));
  }

}
