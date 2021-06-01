package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ReactiveMessageConsumerImpl implements ReactiveMessageConsumer {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private ChannelMapping channelMapping;
  private ReactiveMessageConsumerImplementation target;
  private DecoratedReactiveMessageHandlerFactory decoratedMessageHandlerFactory;

  public ReactiveMessageConsumerImpl(ChannelMapping channelMapping,
                                     ReactiveMessageConsumerImplementation target,
                                     DecoratedReactiveMessageHandlerFactory decoratedMessageHandlerFactory) {
    this.channelMapping = channelMapping;
    this.target = target;
    this.decoratedMessageHandlerFactory = decoratedMessageHandlerFactory;
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, ReactiveMessageHandler handler) {
    logger.info("Subscribing (reactive): subscriberId = {}, channels = {}", subscriberId, channels);

    Function<SubscriberIdAndMessage, Mono<Void>> decoratedHandler =
            decoratedMessageHandlerFactory.decorate(handler);

    MessageSubscription messageSubscription =
            target.subscribe(
                    subscriberId,
                    channels.stream().map(channelMapping::transform).collect(Collectors.toSet()),
                    message -> decoratedHandler
                            .apply(new SubscriberIdAndMessage(subscriberId, message)));

    logger.info("Subscribed (reactive): subscriberId = {}, channels = {}", subscriberId, channels);

    return messageSubscription;
  }

  @Override
  public String getId() {
    return target.getId();
  }

  @Override
  public void close() {
    logger.info("Closing reactive consumer");

    target.close();

    logger.info("Closed reactive consumer");
  }

}
