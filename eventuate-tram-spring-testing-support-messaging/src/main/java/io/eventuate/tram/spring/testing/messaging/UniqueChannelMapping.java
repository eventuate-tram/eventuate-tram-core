package io.eventuate.tram.spring.testing.messaging;

import io.eventuate.tram.messaging.common.ChannelMapping;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UniqueChannelMapping implements ChannelMapping {
    private ConcurrentMap<String, String> externalToInternal = new ConcurrentHashMap<>();
    private ConcurrentMap<String, String> internalToExternal = new ConcurrentHashMap<>();

    @Override
    public String transform(String channel) {
        return channel.startsWith("ext-") ? transformToInternal(channel) : transformToExternal(channel);
    }

    private String transformToInternal(String channel) {
        return externalToInternal.getOrDefault(channel, channel);
    }

    private String transformToExternal(String channel) {
        if (!internalToExternal.containsKey(channel))
            internalToExternal.putIfAbsent(channel, "ext-" + channel + "-" + UUID.randomUUID());
        String external = internalToExternal.get(channel);
        externalToInternal.putIfAbsent(external, channel);
        return external;
    }
}
