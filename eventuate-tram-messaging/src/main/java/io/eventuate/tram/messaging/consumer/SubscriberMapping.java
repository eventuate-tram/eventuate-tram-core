package io.eventuate.tram.messaging.consumer;

public interface SubscriberMapping {

    String toExternal(String subscriberId);
}
