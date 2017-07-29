package io.eventuate.tram.commands.common;

public interface ChannelMapping {

  String transform(String channel);

}
