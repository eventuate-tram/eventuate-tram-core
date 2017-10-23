package io.eventuate.tram.commands.consumer;

import io.eventuate.tram.commands.common.Command;
import org.apache.commons.lang.builder.ToStringBuilder;

// Todo - replace CommandToSendWithThis

public class CommandWithDestination {
  private final String destinationChannel;
  private final String resource;
  private final Command command;

  @Override
  public String toString() {
    return new ToStringBuilder(this)
            .append("destinationChannel", destinationChannel)
            .append("resource", resource)
            .append("command", command)
            .toString();
  }

  public CommandWithDestination(String destinationChannel, String resource, Command command) {
    this.destinationChannel = destinationChannel;
    this.resource = resource;
    this.command = command;
  }

  public String getDestinationChannel() {
    return destinationChannel;
  }

  public String getResource() {
    return resource;
  }

  public Command getCommand() {
    return command;
  }
}
