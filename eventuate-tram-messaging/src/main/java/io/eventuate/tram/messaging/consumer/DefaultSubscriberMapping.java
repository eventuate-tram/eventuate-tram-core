package io.eventuate.tram.messaging.consumer;

public class DefaultSubscriberMapping implements SubscriberMapping {

    @Override
    public String toExternal(String subscriberId) {
        return subscriberId;
    }

}
