package io.eventuate.tram.messaging.consumer;

import io.eventuate.tram.messaging.common.Message;

import java.util.function.Consumer;

public interface MessageHandler extends Consumer<Message> {
}
