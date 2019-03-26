package io.eventuate.tram.consumer.rabbitmq;

import io.eventuate.tram.consumer.common.coordinator.CommonLeaderSelector;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.CancelLeadershipException;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkLeaderSelector implements CommonLeaderSelector {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private LeaderSelector leaderSelector;
  private volatile boolean running = true;

  public ZkLeaderSelector(CuratorFramework curatorFramework,
                          String groupId,
                          Runnable leaderSelectedCallback,
                          Runnable leaderRemovedCallback) {

    String leaderPath = String.format("/eventuate-tram/rabbitmq/consumer-leaders/%s", groupId);

    leaderSelector = new LeaderSelector(curatorFramework, leaderPath, new LeaderSelectorListener() {
      @Override
      public void takeLeadership(CuratorFramework client) {
        leaderSelectedCallback.run();
        try {
          while (running) {
            try {
              Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
              break;
            }
          }
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
        } finally {
          leaderRemovedCallback.run();
        }
      }

      @Override
      public void stateChanged(CuratorFramework client, ConnectionState newState) {
        if (newState == ConnectionState.SUSPENDED || newState == ConnectionState.LOST) {
          throw new CancelLeadershipException();
        }
      }
    });

    leaderSelector.autoRequeue();

    leaderSelector.start();
  }

  @Override
  public void stop() {
    running = false;
    leaderSelector.close();
  }
}
