package io.eventuate.tram.commands.common;

public class DefaultCommandNameMapping implements CommandNameMapping {

  @Override
  public String commandToExternalCommandType(Command command) {
    return command.getClass().getName();
  }

  @Override
  public String externalCommandTypeToCommandClassName(String commandTypeHeader) {
    return commandTypeHeader;
  }
}
