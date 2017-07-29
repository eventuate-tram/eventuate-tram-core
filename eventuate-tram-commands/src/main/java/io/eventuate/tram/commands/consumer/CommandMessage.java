package io.eventuate.tram.commands.consumer;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Map;

public class CommandMessage<T> {

  private String messageId;
  private T command;
  private Map<String, String> correlationHeaders;

  public CommandMessage(String messageId, T command, Map<String, String> correlationHeaders) {
    this.messageId = messageId;
    this.command = command;
    this.correlationHeaders = correlationHeaders;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public String getMessageId() {
    return messageId;
  }

  public T getCommand() {
    return command;
  }

  public Map<String, String> getCorrelationHeaders() {
    return correlationHeaders;
  }
}
