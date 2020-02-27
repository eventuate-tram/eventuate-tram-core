package io.eventuate.tram.spring.cloudsleuthintegration;

import brave.propagation.Propagation;

import java.util.List;

public class MessageHeaderPropagation implements Propagation.Setter<MessageHeaderAccessor, String>, Propagation.Getter<MessageHeaderAccessor, String> {

  public static MessageHeaderPropagation INSTANCE = new MessageHeaderPropagation();

  public static void removeAnyTraceHeaders(MessageHeaderAccessor headers, List<String> keys) {
    keys.forEach(headers::remove);
  }

  @Override
  public String get(MessageHeaderAccessor carrier, String key) {
    return carrier.get(key);
  }

  @Override
  public void put(MessageHeaderAccessor carrier, String key, String value) {
    carrier.put(key, value);
  }
}
