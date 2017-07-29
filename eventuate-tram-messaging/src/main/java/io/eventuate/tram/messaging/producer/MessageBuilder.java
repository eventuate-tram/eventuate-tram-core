package io.eventuate.tram.messaging.producer;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageImpl;

import java.util.HashMap;
import java.util.Map;

public class MessageBuilder {

  private String body;
  private Map<String, String> headers = new HashMap<>();

  public MessageBuilder(String body) {
    this.body = body;
  }

  public static MessageBuilder withPayload(String payload) {
      return new MessageBuilder(payload);
  }

  public MessageBuilder withHeader(String name, String value) {
    this.headers.put(name, value);
    return this;
  }

  public MessageBuilder withExtraHeaders(String prefix, Map<String, String> headers) {

    for (Map.Entry<String,String> entry : headers.entrySet())
      this.headers.put(prefix + entry.getKey(), entry.getValue());

    return this;
  }

  public Message build() {
    return new MessageImpl(body, headers);
  }

}
