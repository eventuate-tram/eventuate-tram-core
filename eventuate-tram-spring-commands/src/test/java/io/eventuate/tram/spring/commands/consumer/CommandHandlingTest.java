package io.eventuate.tram.spring.commands.consumer;

import io.eventuate.examples.common.money.Money;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.spring.commands.consumer.customersandorders.CustomerService;
import io.eventuate.tram.spring.commands.consumer.customersandorders.CustomersAndOrdersConfiguration;
import io.eventuate.tram.spring.commands.consumer.customersandorders.commands.ReserveCreditCommand;
import io.eventuate.tram.spring.inmemory.TramInMemoryConfiguration;
import io.eventuate.tram.testutil.TestMessageConsumer;
import io.eventuate.tram.testutil.TestMessageConsumerFactory;
import io.eventuate.util.test.async.Eventually;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CommandHandlingTest {

  @Configuration
  @EnableAutoConfiguration
  @Import({TramInMemoryConfiguration.class, AnnotationBasedCommandHandlerConfiguration.class, CustomersAndOrdersConfiguration.class})
  public static class Config {

    @Bean
    TestMessageConsumerFactory testMessageConsumerFactory() {
      return new TestMessageConsumerFactory();
    }

    @Bean
    TestMessageConsumer testConsumer(TestMessageConsumerFactory testMessageConsumerFactory) {
      return testMessageConsumerFactory.make();
    }

  }

  @Autowired
  private CommandProducer commandProducer;

  @MockBean
  private CustomerService customerService;

  @Autowired
  private TestMessageConsumer testConsumer;

  @Test
  public void shouldHandleCommand() {
    String commandId = commandProducer.send("customerService", new ReserveCreditCommand(101L, 100L, new Money("12.34")),
        testConsumer.getReplyChannel(), Collections.emptyMap());

    Eventually.eventually(() -> {
      verify(customerService).reserveCredit(101L, 100L, new Money("12.34"));
    });

    testConsumer.assertHasReplyTo(commandId);

  }

}
