package io.eventuate.tram.spring.cloudsleuthintegration.test;

import org.junit.Test;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OpenZipkinTraceDeserializerTest {

  @Test
  public void test() throws IOException {
    String jsonString = StreamUtils.copyToString(getClass().getResourceAsStream("/trace.json"), Charset.defaultCharset());
    List<List<ZipkinSpan>> list = OpenZipkinTraceDeserializer.deserializeTraces(jsonString);
    System.out.println("trace=" + list);
    assertEquals(1, list.size());

    List<ZipkinSpan> trace = list.get(0);
    assertEquals(6, trace.size());
    System.out.println(trace.get(0));
  }
}