package io.eventuate.tram.cdc.mysql.connector;

import io.eventuate.tram.messaging.common.ChannelType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChannelConfigurationTest.Config.class)
public class ChannelConfigurationTest {
  @EnableConfigurationProperties(EventuateTramChannelProperties.class)
  public static class Config {
  }

  @Autowired
  private EventuateTramChannelProperties eventuateTramChannelProperties;

  @Test
  public void testPropertyParsing() {
    Assert.assertEquals(2, eventuateTramChannelProperties.getChannelTypes().size());

    Assert.assertEquals(ChannelType.QUEUE,
            eventuateTramChannelProperties.getChannelTypes().get("channel1"));

    Assert.assertEquals(ChannelType.TOPIC,
            eventuateTramChannelProperties.getChannelTypes().get("channel2"));
  }
}
