package io.eventuate.tram.spring.cloudsleuthintegration;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.Propagation;
import brave.propagation.ThreadLocalSpan;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageInterceptor;

import java.util.HashMap;
import java.util.Map;

public class TracingMessagingInterceptor implements MessageInterceptor {

  private final Tracing tracing;
  private final Tracer tracer;
  private final ThreadLocalSpan threadLocalSpan;
  private final TraceContext.Injector<MessageHeaderAccessor> injector;
  private final TraceContext.Extractor<MessageHeaderAccessor> extractor;

  TracingMessagingInterceptor(Tracing tracing, Propagation.Setter<MessageHeaderAccessor, String> setter,
                              Propagation.Getter<MessageHeaderAccessor, String> getter) {

    this.tracing = tracing;
    this.tracer = tracing.tracer();
    this.threadLocalSpan = ThreadLocalSpan.create(this.tracer);
    this.injector = tracing.propagation().injector(setter);
    this.extractor = tracing.propagation().extractor(getter);
  }

  @Override
  public void preSend(Message message) {
    MessageHeaderAccessor headers = makeMessageHeaderAccessor(message);
    TraceContextOrSamplingFlags extracted = this.extractor.extract(headers);
    Span span = this.threadLocalSpan.next(extracted);
    MessageHeaderPropagation.removeAnyTraceHeaders(headers, this.tracing.propagation().keys());
    this.injector.inject(span.context(), headers);
    if (!span.isNoop()) {
      span.kind(Span.Kind.PRODUCER).name("doSend " + message.getRequiredHeader(Message.DESTINATION)).start();
      addMessageTags(span, message);
    }
  }

  private void addMessageTags(Span span, Message message) {
    Map<String, String> copy = new HashMap<>(message.getHeaders());
    MessageHeaderPropagation.removeAnyTraceHeaders(new MessageHeaderMapAccessor(copy), this.tracing.propagation().keys());
    copy.forEach((key, value) -> span.tag("message." + key, value));
  }

  private MessageHeaderAccessor makeMessageHeaderAccessor(Message message) {
    return new MessageHeaderAccessor() {
        @Override
        public void put(String key, String value) {
          message.setHeader(key, value);
        }

        @Override
        public String get(String key) {
          return message.getHeader(key).orElse(null);
        }

        @Override
        public void remove(String key) {
          message.removeHeader(key);
        }
      };
  }


  @Override
  public void postSend(Message message, Exception e) {
    finishSpan(e);
  }

  void finishSpan(Throwable error) {
    Span span = this.threadLocalSpan.remove();
    if (span == null || span.isNoop())
      return;
    if (error != null) { // an error occurred, adding error to span
      String message = error.getMessage();
      if (message == null)
        message = error.getClass().getSimpleName();
      span.tag("error", message);
    }
    span.finish();
  }

  @Override
  public void preHandle(String subscriberId, Message message) {
    MessageHeaderAccessor headers = makeMessageHeaderAccessor(message);
    TraceContextOrSamplingFlags extracted = this.extractor.extract(headers);
    Span span = this.threadLocalSpan.next(extracted);
    MessageHeaderPropagation.removeAnyTraceHeaders(headers, this.tracing.propagation().keys());
    this.injector.inject(span.context(), headers);
    if (!span.isNoop()) {
      span.kind(Span.Kind.CONSUMER).name("receive " + message.getRequiredHeader(Message.DESTINATION)).start();
      span.tag("subscriberId", subscriberId);
      addMessageTags(span, message);
    }

  }

  @Override
  public void postHandle(String subscriberId, Message message, Throwable throwable) {
    finishSpan(throwable);
  }

  private static class MessageHeaderMapAccessor implements MessageHeaderAccessor {
    private final Map<String, String> headers;

    public MessageHeaderMapAccessor(Map<String, String> headers) {
      this.headers = headers;
    }

    @Override
    public void put(String key, String value) {
      headers.put(key, value);
    }

    @Override
    public String get(String key) {
      return headers.get(key);
    }

    @Override
    public void remove(String key) {
      headers.remove(key);
    }
  }
}
