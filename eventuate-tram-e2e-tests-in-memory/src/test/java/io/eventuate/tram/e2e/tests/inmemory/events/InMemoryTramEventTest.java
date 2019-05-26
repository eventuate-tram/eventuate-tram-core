package io.eventuate.tram.e2e.tests.inmemory.events;

import io.eventuate.e2e.tests.basic.events.AbstractTramEventTest;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = InMemoryTramEventTestConfiguration.class)
public class InMemoryTramEventTest extends AbstractTramEventTest {
}
