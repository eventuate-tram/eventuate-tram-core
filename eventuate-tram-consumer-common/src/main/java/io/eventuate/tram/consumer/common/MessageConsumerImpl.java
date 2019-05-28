package io.eventuate.tram.consumer.common;

import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;

import java.util.Set;
import java.util.stream.Collectors;

public final class MessageConsumerImpl implements MessageConsumer {

  // This could be implemented as Around advice

  private ChannelMapping channelMapping;
  private MessageConsumerImplementation target;

  protected MessageConsumerImpl(ChannelMapping channelMapping, MessageConsumerImplementation target) {
    this.channelMapping = channelMapping;
    this.target = target;
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    return target.subscribe(subscriberId,
            channels.stream().map(channelMapping::transform).collect(Collectors.toSet()),
            handler);
  }

  @Override
  public String getId() {
    return target.getId();
  }

  @Override
  public void close() {
    target.close();
  }

}
