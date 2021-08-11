package io.eventuate.tram.commands.common;

public interface CommandNameMapping {

  String commandToExternalCommandType(Command command);
  String externalCommandTypeToCommandClassName(String commandTypeHeader);

}
