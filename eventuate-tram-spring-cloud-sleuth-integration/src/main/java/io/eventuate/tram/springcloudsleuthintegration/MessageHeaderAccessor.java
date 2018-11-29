package io.eventuate.tram.springcloudsleuthintegration;

public interface MessageHeaderAccessor {
  void put(String key, String value);
  String get(String key);
  void remove(String key);
}
