package io.eventuate.tram.spring.cloudsleuthintegration.test;

import io.eventuate.common.spring.jdbc.EventuateTransactionTemplateConfiguration;
import io.eventuate.tram.spring.inmemory.TramInMemoryConfiguration;
import io.eventuate.util.test.async.Eventually;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=SpringCloudSleuthIntegrationTest.TestConfiguration.class, webEnvironment = SpringBootTest
        .WebEnvironment.RANDOM_PORT)
public class SpringCloudSleuthIntegrationTest {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Configuration
  @SpringBootApplication
  @Import({TramInMemoryConfiguration.class, EventuateTransactionTemplateConfiguration.class})
  static class TestConfiguration {

      @Bean
      public RestTemplate restTemplate() {
        return new RestTemplate();
      }
  }

  @Value("${spring.zipkin.baseUrl}")
  private String zipkinBaseUrl;

  @LocalServerPort
  private int port;

  @Autowired
  private RestTemplate restTemplate;

  @Test
  public void shouldImplementTracing() {
    String id = Long.toString(System.currentTimeMillis());
    ResponseEntity<String> result = restTemplate.postForEntity(String.format("http://localhost:%s/foo/%s",
            port, id),
            new TestMessage(port), String.class);
    assertEquals(HttpStatus.OK, result.getStatusCode());

    Eventually.eventually(() -> {
      assertTracesSendToZipkin(id);
    });
  }

  private void assertTracesSendToZipkin(String id)  {

    String url = String.format
            ("%sapi/v2/traces?annotationQuery=http.path=/foo/%s",
                    zipkinBaseUrl, id);

    logger.info("Retrieving traces {}", url);

    ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);

    assertEquals(HttpStatus.OK, result.getStatusCode());

    String jsonString = result.getBody();

    List<List<ZipkinSpan>> traces = OpenZipkinTraceDeserializer.deserializeTraces(jsonString);
    assertEquals(1, traces.size());

    List<ZipkinSpan> trace = traces.get(0);
    ZipkinSpan parentSpan = trace.stream().filter(s -> s.hasTag("http.path", "/foo/" + id)).findFirst().get();
    ZipkinSpan sendSpan = trace.stream().filter(s -> s.hasName("dosend testchannel")).findFirst().get();
    assertChildOf(parentSpan, sendSpan);
    ZipkinSpan receiveSpan = trace.stream().filter(s -> s.hasName("receive testchannel")).findFirst().get();
    assertChildOf(sendSpan, receiveSpan);
    ZipkinSpan barPostSpan = trace.stream().filter(s -> s.hasTag("http.path", "/bar")).findFirst().get();
    assertChildOf(receiveSpan, barPostSpan);

  }

  private void assertChildOf(ZipkinSpan parent, ZipkinSpan span) {
    if (!parent.isChild(span)) {
      fail(String.format("Expected the parent of %s to be %s but is %s", span, parent.getId(), span
              .getParentId()));
    }
  }
}
