package io.eventuate.tram.cdc.mysql.connector.configuration;

import io.eventuate.coordination.leadership.LeaderSelectorFactory;
import io.eventuate.coordination.leadership.zookeeper.ZkLeaderSelector;
import io.eventuate.local.common.EventuateLocalZookeperConfigurationProperties;
import io.eventuate.local.unified.cdc.pipeline.common.health.ZookeeperHealthCheck;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!Redis")
public class ZookeeperConfiguration {
  @Bean
  public EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties() {
    return new EventuateLocalZookeperConfigurationProperties();
  }

  @Bean(destroyMethod = "close")
  public CuratorFramework curatorFramework(EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties) {
    String connectionString = eventuateLocalZookeperConfigurationProperties.getConnectionString();
    return makeStartedCuratorClient(connectionString);
  }

  @Bean
  public LeaderSelectorFactory connectorLeaderSelectorFactory(CuratorFramework curatorFramework) {
    return (lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback) ->
            new ZkLeaderSelector(curatorFramework, lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback);
  }

  @Bean
  public ZookeeperHealthCheck zookeeperHealthCheck() {
    return new ZookeeperHealthCheck();
  }

  static CuratorFramework makeStartedCuratorClient(String connectionString) {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client = CuratorFrameworkFactory.
            builder().retryPolicy(retryPolicy)
            .connectString(connectionString)
            .build();
    client.start();
    return client;
  }
}
