package io.eventuate.tram.spring.flyway;

import io.eventuate.common.flyway.TemplatedMessageTableCreator;
import io.eventuate.common.jdbc.OutboxPartitioningSpec;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.util.stream.Collectors;

public class V1005__MyMigration extends BaseJavaMigration {

  private final TemplatedMessageTableCreator templatedMessageTableCreator;
  private final OutboxPartitioningSpec outboxPartitioningSpec;

  public V1005__MyMigration(TemplatedMessageTableCreator templatedMessageTableCreator, OutboxPartitioningSpec outboxPartitioningSpec) {
    this.templatedMessageTableCreator = templatedMessageTableCreator;
    this.outboxPartitioningSpec = outboxPartitioningSpec;
  }

  @Override
  public void migrate(Context context) throws Exception {
    templatedMessageTableCreator.migrate(context,
        outboxPartitioningSpec.outboxTableSuffixes()
            .stream()
            .map(suffix -> suffix.suffixAsString).collect(Collectors.toList()));
  }

}
