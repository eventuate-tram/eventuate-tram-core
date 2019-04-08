package io.eventuate.tram.cdc.mysql.connector.configuration.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class KafkaCondition implements Condition {
  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    return !context.getEnvironment().acceptsProfiles("ActiveMQ") &&
            !context.getEnvironment().acceptsProfiles("RabbitMQ") &&
            !context.getEnvironment().acceptsProfiles("Redis");
  }
}
