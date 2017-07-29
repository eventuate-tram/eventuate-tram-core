package io.eventuate.tram.commands.consumer;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ReplyDestination {

  public final String destination;
  public final String partitionKey;

  public ReplyDestination(String destination, String partitionKey) {
    this.partitionKey = partitionKey;
    this.destination = destination;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
