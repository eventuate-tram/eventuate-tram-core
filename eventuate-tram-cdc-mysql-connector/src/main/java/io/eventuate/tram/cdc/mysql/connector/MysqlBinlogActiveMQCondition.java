package io.eventuate.tram.cdc.mysql.connector;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class MysqlBinlogActiveMQCondition implements Condition {
  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    return !context.getEnvironment().acceptsProfiles("EventuatePolling") &&
            !context.getEnvironment().acceptsProfiles("PostgresWal") &&
            context.getEnvironment().acceptsProfiles("ActiveMQ");
  }
}
