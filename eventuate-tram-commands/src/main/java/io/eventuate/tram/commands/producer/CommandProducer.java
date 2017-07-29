package io.eventuate.tram.commands.producer;

import io.eventuate.tram.commands.common.Command;

import java.util.Map;

public interface CommandProducer {

  /**
   * Sends a command
   * @param channel
   * @param resource
   * @param command the command to send
   * @param headers additional headers
   * @return the id of the sent command
   */
  String send(String channel, String resource, Command command, Map<String, String> headers);
}
