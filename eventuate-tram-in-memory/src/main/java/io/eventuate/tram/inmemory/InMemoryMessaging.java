package io.eventuate.tram.inmemory;

import io.eventuate.tram.consumer.common.DecoratedMessageHandlerFactory;
import io.eventuate.tram.consumer.common.MessageConsumerImpl;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.consumer.DefaultSubscriberMapping;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.messaging.producer.common.MessageProducerImpl;

import java.util.Collections;

public class InMemoryMessaging {
    public final MessageConsumer messageConsumer;
    public final MessageProducer messageProducer;

    public InMemoryMessaging(MessageProducer messageProducer, MessageConsumer messageConsumer) {
        this.messageConsumer = messageConsumer;
        this.messageProducer = messageProducer;
    }

    public static InMemoryMessaging make() {

        InMemoryMessageConsumer inMemoryMessageConsumer = new InMemoryMessageConsumer();
        EventuateTransactionSynchronizationManager eventuateTransactionSynchronizationManager = new EventuateTransactionSynchronizationManager() {
            @Override
            public boolean isTransactionActive() {
                return false;
            }

            @Override
            public void executeAfterTransaction(Runnable runnable) {
                throw new UnsupportedOperationException();
            }
        };

        ChannelMapping channelMapping = new DefaultChannelMapping.DefaultChannelMappingBuilder().build();
        MessageProducer messageProducer = new MessageProducerImpl(new MessageInterceptor[0], channelMapping,
                new InMemoryMessageProducer(inMemoryMessageConsumer, eventuateTransactionSynchronizationManager));

        MessageConsumer messageConsumer = new MessageConsumerImpl(channelMapping, inMemoryMessageConsumer,
                        new DecoratedMessageHandlerFactory(Collections.emptyList()),
                        new DefaultSubscriberMapping());

        return new InMemoryMessaging(messageProducer, messageConsumer);
    }
}