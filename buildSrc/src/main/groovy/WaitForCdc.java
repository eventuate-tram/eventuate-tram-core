import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.jayway.restassured.RestAssured;

public class WaitForCdc extends DefaultTask {

  private String getenv(String name, String defaultValue) {
    return Optional.ofNullable(System.getenv(name)).orElse(defaultValue);
  }

  @TaskAction
  public void waitForCdc() {

    String hostName = getenv("DOCKER_HOST_IP", "localhost");

    getLogger().info("Connected to CDC on hostname={}", hostName);

    long deadline = System.currentTimeMillis() + 1000 * 60;

    while (System.currentTimeMillis() <= deadline) {

      try {

        String detail = RestAssured.given().
                when().
                get(String.format("http://%s:8099/actuator/health", hostName)).
                then().
                statusCode(200)
                .extract().
                        path("details.binlogEntryReaderHealthCheck.details[\"detail-1\"]");

        if (detail != null && detail.length() > 0 && !detail.endsWith("is not the leader")) {
          getLogger().info("CDC is up");
          return;
        }

        getLogger().info("CDC is not ready. Detail={}", detail);
      } catch (Exception | AssertionError e) {
        getLogger().error("Got error connecting to CDC {}", e.getMessage());
      }


      try {
        TimeUnit.SECONDS.sleep(4);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    throw new RuntimeException("CDC failed to start");

  }
}