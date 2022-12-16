package io.eventuate.tram.spring.logging;

import io.eventuate.tram.messaging.common.MessageInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingMessageInterceptorAutoConfiguration {
    @Bean
    public MessageInterceptor messageLoggingInterceptor() {
        return new LoggingMessageInterceptor();
    }
}
