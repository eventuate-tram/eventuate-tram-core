package io.eventuate.tram.consumer.common;

import java.sql.SQLException;

public interface SubscriberIdAndMessageConsumer {
  void accept(SubscriberIdAndMessage subscriberIdAndMessage) throws SQLException;
}
