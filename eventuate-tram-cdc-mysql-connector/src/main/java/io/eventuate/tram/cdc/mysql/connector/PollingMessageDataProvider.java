package io.eventuate.tram.cdc.mysql.connector;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.polling.PollingDataProvider;
import io.eventuate.tram.messaging.common.MessageImpl;

import java.util.Map;
import java.util.Optional;

public class PollingMessageDataProvider implements PollingDataProvider<PollingMessageBean, MessageWithDestination, String> {
  private Optional<String> database;

  public PollingMessageDataProvider() {
    this(Optional.empty());
  }

  public PollingMessageDataProvider(Optional<String> database) {
    this.database = database;
  }

   @Override
   public String table() {
    return database.map(db -> db + ".").orElse("") + "message";
   }

  @Override
  public Class<PollingMessageBean> eventBeanClass() {
    return PollingMessageBean.class;
  }

  @Override
  public String getId(MessageWithDestination messageWithDestination) {
    return messageWithDestination.getMessage().getId();
  }

  @Override
  public String publishedField() {
    return "published";
  }

  @Override
  public String idField() {
    return "id";
  }

  @Override
  public MessageWithDestination transformEventBeanToEvent(PollingMessageBean pollingMessageBean) {
    Map<String, String> headers = JSonMapper.fromJson(pollingMessageBean.getHeaders(), Map.class);

    return new MessageWithDestination(pollingMessageBean.getDestination(),
      new MessageImpl(pollingMessageBean.getPayload(), headers), null);
  }
}
