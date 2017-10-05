package io.eventuate.tram.cdc.mysql.connector;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.mysql.binlog.PollingDataProvider;
import io.eventuate.tram.messaging.common.MessageImpl;

import java.util.Map;

public class PollingMessageDataProvider implements PollingDataProvider<PollingMessageBean, MessageWithDestination, String> {

  @Override
  public String table() {
    return "message";
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
