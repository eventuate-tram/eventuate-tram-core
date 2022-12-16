package io.eventuate.tram.spring.flyway;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ScriptExecutor {
  public static String readFileToString(String path) {
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource resource = resourceLoader.getResource(path);
    try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
      return FileCopyUtils.copyToString(reader);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void executeScript(Map<String, String> replacements, String script, SqlExecutor sqlExecutor) {

    String s = readFileToString(script);

    for (Map.Entry<String, String> entry : replacements.entrySet())
      s = s.replace("${" + entry.getKey() + "}", entry.getValue());

    String[] t = s.split(";");

    for (String statement : t) {
      if (!statement.startsWith("USE ") && statement.trim().length() > 0) {
        System.out.println(statement);
        try {
          sqlExecutor.execute(statement);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }
}
