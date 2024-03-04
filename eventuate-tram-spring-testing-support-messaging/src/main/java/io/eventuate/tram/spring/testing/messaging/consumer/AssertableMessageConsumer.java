package io.eventuate.tram.spring.testing.messaging.consumer;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;


public class AssertableMessageConsumer {

    private final Map<String, BlockingQueue<Message>> queues = new ConcurrentHashMap<>();

    private final MessageConsumer messageConsumer;

    public AssertableMessageConsumer(MessageConsumer messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    public void subscribe(String... channels) {
        messageConsumer.subscribe("TestMessageConsumer-" + UUID.randomUUID(), new HashSet<>(Arrays.asList(channels)), this::handleMessage);
    }

    private void handleMessage(Message message) {
        String destination = message.getRequiredHeader(Message.DESTINATION);
        BlockingQueue<Message> queue = getMessageBlockingQueue(destination);
        queue.add(message);
    }

    private BlockingQueue<Message> getMessageBlockingQueue(String destination) {
        queues.putIfAbsent(destination, new ArrayBlockingQueue<>(1));
        return queues.get(destination);
    }

    public AssertableMessage assertMessageReceived(String channel)  {
        BlockingQueue<Message> queue = getMessageBlockingQueue(channel);
        Message m;
        try {
            m = queue.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertNotNull(m);
        return new AssertableMessage(m);
    }
}
