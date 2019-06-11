package io.eventuate.tram.integrationtest.common;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class PostgresWalRabbitMQCondition implements Condition {
  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    return context.getEnvironment().acceptsProfiles("PostgresWal") &&
            context.getEnvironment().acceptsProfiles("RabbitMQ");
  }
}
