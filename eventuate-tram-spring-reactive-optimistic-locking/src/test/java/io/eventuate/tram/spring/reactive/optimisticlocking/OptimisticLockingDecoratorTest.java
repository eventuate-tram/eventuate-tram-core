package io.eventuate.tram.spring.reactive.optimisticlocking;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandlerDecoratorChain;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import junit.framework.TestCase;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.springframework.dao.DataIntegrityViolationException;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class OptimisticLockingDecoratorTest {

    @Test
    public void shouldRetry() {
        OptimisticLockingDecorator decorator = new OptimisticLockingDecorator();
        SubscriberIdAndMessage sm = new SubscriberIdAndMessage("subscriberID", null);
        ReactiveMessageHandlerDecoratorChain chain = mock(ReactiveMessageHandlerDecoratorChain.class);
        Object chainResult = "chainResult";
        when(chain.next(sm))
                .thenThrow(new DataIntegrityViolationException("failed"))
                .thenReturn((Publisher)Mono.just(chainResult));
        Object result = Mono.from(decorator.accept(sm, chain)).block();
        assertEquals(chainResult, result);
        verify(chain,times(2)).next(sm);
    }
}