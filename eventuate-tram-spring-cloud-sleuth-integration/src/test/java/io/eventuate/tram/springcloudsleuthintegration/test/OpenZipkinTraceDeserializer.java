package io.eventuate.tram.springcloudsleuthintegration.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class OpenZipkinTraceDeserializer {
  static List<List<ZipkinSpan>> deserializeTraces(String jsonString) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      return objectMapper.readValue(jsonString, new TypeReference<List<List<ZipkinSpan>>>() { });

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
