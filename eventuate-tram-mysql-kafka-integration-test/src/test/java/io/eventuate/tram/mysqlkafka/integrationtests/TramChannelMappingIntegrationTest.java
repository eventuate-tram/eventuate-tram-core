package io.eventuate.tram.mysqlkafka.integrationtests;

import io.eventuate.tram.messaging.common.ChannelMapping;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TramChannelMappingIntegrationTest.TramChannelMappingIntegrationTestConfiguration.class)
public class TramChannelMappingIntegrationTest extends AbstractTramIntegrationTest {

  private static String channelName = "mapped-channel-name" + System.currentTimeMillis();

  @Configuration
  @Import({TramIntegrationTestConfiguration.class})
  static public class TramChannelMappingIntegrationTestConfiguration {

    @Bean
    public ChannelMapping channelMapping() {
      return channel -> channelName;
    }
  }

}
