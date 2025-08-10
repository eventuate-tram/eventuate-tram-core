package io.eventuate.tram.reactive.messaging.producer.common;

import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.producer.HttpDateHeaderFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Arrays;

public class ReactiveMessageProducer {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private final MessageInterceptor[] messageInterceptors;
  private final ChannelMapping channelMapping;
  private final ReactiveMessageProducerImplementation implementation;

  public ReactiveMessageProducer(MessageInterceptor[] messageInterceptors,
                                 ChannelMapping channelMapping,
                                 ReactiveMessageProducerImplementation implementation) {
    this.messageInterceptors = messageInterceptors;
    this.channelMapping = channelMapping;
    this.implementation = implementation;
    
  }

  private void preSend(Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.preSend(message));
  }

  private void postSend(Message message, RuntimeException e) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.postSend(message, e));
  }

  public Mono<Message> send(String destination, Message message) {
    prepareMessageHeaders(destination, message);
    return send(message);
  }

  protected void prepareMessageHeaders(String destination, Message message) {
    message.getHeaders().put(Message.DESTINATION, channelMapping.transform(destination));
    message.getHeaders().put(Message.DATE, HttpDateHeaderFormatUtil.nowAsHttpDateString());
  }

  protected Mono<Message> send(Message message) {
    preSend(message);

    return implementation
            .send(message)
            .doOnError(throwable -> {
              logger.error("Sending failed", throwable);
              if (throwable instanceof RuntimeException exception) {
                postSend(message, exception);
              }
              throw new RuntimeException(throwable);
            })
            .doOnSuccess(msg -> postSend(message, null));
  }
}
