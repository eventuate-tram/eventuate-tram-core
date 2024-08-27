package io.eventuate.tram.spring.testing.messaging.consumer;

import io.eventuate.tram.messaging.consumer.SubscriberMapping;

import java.util.UUID;

public class UniqueSubscriberIdMapping implements SubscriberMapping {

    @Override
    public String toExternal(String s) {
        return "ext-" + s + "-" + UUID.randomUUID();
    }
}