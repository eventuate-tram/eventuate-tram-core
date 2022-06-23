package io.eventuate.tram.spring.reactive.optimisticlocking;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandlerDecorator;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandlerDecoratorChain;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class OptimisticLockingDecorator implements ReactiveMessageHandlerDecorator, Ordered {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Publisher<?> accept(SubscriberIdAndMessage subscriberIdAndMessage, ReactiveMessageHandlerDecoratorChain decoratorChain) {
        Mono<Object> m = Mono.defer(() -> Mono.from(decoratorChain
                .next(subscriberIdAndMessage)));
        return m.retryWhen(Retry.backoff(2, Duration.ofMillis(100))
                .filter(e -> {
                    boolean b = e instanceof DataIntegrityViolationException;
                    if (b)
                        logger.info("Retrying due to DataIntegrityViolationException");
                    return b;
                }));
    }

    @Override
    public int getOrder() {
        return 150;
    }
}
