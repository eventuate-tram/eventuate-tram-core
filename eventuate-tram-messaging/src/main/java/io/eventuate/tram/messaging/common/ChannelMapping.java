package io.eventuate.tram.messaging.common;

public interface ChannelMapping {

  String transform(String channel);

}
