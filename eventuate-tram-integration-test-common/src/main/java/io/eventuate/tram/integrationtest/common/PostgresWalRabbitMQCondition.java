package io.eventuate.tram.integrationtest.common;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Profiles;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class PostgresWalRabbitMQCondition implements Condition {
  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    return context.getEnvironment().acceptsProfiles(Profiles.of("PostgresWal")) &&
            context.getEnvironment().acceptsProfiles(Profiles.of("RabbitMQ"));
  }
}
