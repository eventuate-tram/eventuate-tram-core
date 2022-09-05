package io.eventuate.tram.broker.db.integrationtests;

import io.eventuate.common.testcontainers.EventuateMySqlContainer;
import io.eventuate.common.testcontainers.EventuateZookeeperContainer;
import io.eventuate.common.testcontainers.PropertyProvidingContainer;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaContainer;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TramIntegrationTestConfiguration.class, properties = {"eventuate.tram.outbox.partitioning.outbox.tables=8", "eventuate.tram.outbox.partitioning.message.partitions=4"})
public class MultipleOutboxTramIntegrationTest extends AbstractTramIntegrationTest {

    public static final int OUTBOX_TABLES = 8;
    public static Network network = Network.newNetwork();
    public static EventuateMySqlContainer mysql =
            new EventuateMySqlContainer()
                    .withNetwork(network)
                    .withNetworkAliases("mysql")
                    .withEnv("EVENTUATE_OUTBOX_TABLES", Integer.toString(OUTBOX_TABLES))
                    .withReuse(true);


    public static EventuateZookeeperContainer zookeeper = new EventuateZookeeperContainer().withReuse(true)
            .withNetwork(network)
            .withNetworkAliases("zookeeper");

    public static EventuateKafkaContainer kafka =
            new EventuateKafkaContainer("zookeeper:2181")
                    .waitingFor(new DockerHealthcheckWaitStrategy())
                    .withNetwork(network)
                    .withNetworkAliases("kafka")
                    .withReuse(true);

    public static EventuateCdcContainer cdc =
            new EventuateCdcContainer()
                    .withNetwork(network)
                    .withEnv("SPRING_DATASOURCE_URL", "jdbc:mysql://mysql/eventuate")
                    .withEnv("SPRING_DATASOURCE_USERNAME", "mysqluser")
                    .withEnv("SPRING_DATASOURCE_PASSWORD", "mysqlpw")
                    .withEnv("SPRING_DATASOURCE_DRIVER_CLASS_NAME", "com.mysql.cj.jdbc.Driver")
                    .withEnv("EVENTUATELOCAL_ZOOKEEPER_CONNECTION_STRING", "zookeeper:2181")
                    .withEnv("EVENTUATELOCAL_KAFKA_BOOTSTRAP_SERVERS", "kafka:29092")
                    .withEnv("EVENTUATELOCAL_CDC_DB_USER_NAME", "root")
                    .withEnv("EVENTUATELOCAL_CDC_DB_PASSWORD", "rootpassword")
                    .withEnv("EVENTUATELOCAL_CDC_READER_NAME", "MySqlReader")
                    .withEnv("EVENTUATE_OUTBOX_ID", "1")
                    .withEnv("EVENTUATELOCAL_CDC_MYSQL_BINLOG_CLIENT_UNIQUE_ID", "1234567890")
                    .withEnv("EVENTUATELOCAL_CDC_READ_OLD_DEBEZIUM_DB_OFFSET_STORAGE_TOPIC", "false")
                    .withEnv("SPRING_PROFILES_ACTIVE", "EventuatePolling")
                    .withEnv("EVENTUATE_CDC_OUTBOX_PARTITIONING_OUTBOX_TABLES", Integer.toString(OUTBOX_TABLES))

            ;

    @DynamicPropertySource
    static void registerMySqlProperties(DynamicPropertyRegistry registry) {
        PropertyProvidingContainer.startAndProvideProperties(registry, mysql, zookeeper, kafka, cdc);
    }


    @Override
    protected void preAssertCheck() {
        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(cdc.getLogs());
    }
}
