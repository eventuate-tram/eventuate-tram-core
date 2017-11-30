package io.eventuate.tram.mysqlkafka.integrationtests;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TramIntegrationTestConfiguration.class)
public class TramIntegrationTest extends AbstractTramIntegrationTest{
}
