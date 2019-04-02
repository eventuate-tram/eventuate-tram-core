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

  private String groupId;
  private String memberId;
  private LeaderSelector leaderSelector;
  private volatile boolean running = true;

  public ZkLeaderSelector(CuratorFramework curatorFramework,
                          String groupId,
                          String memberId,
                          Runnable leaderSelectedCallback,
                          Runnable leaderRemovedCallback) {

    this.groupId = groupId;
    this.memberId = memberId;

    String leaderPath = String.format("/eventuate-tram/rabbitmq/consumer-leaders/%s", groupId);

    leaderSelector = new LeaderSelector(curatorFramework, leaderPath, new LeaderSelectorListener() {
      @Override
      public void takeLeadership(CuratorFramework client) {
        logger.info("Calling leaderSelectedCallback, groupId : {}, memberId: {}", groupId, memberId);
        leaderSelectedCallback.run();
        logger.info("Called leaderSelectedCallback, groupId : {}, memberId: {}", groupId, memberId);
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
          try {
            logger.info("Calling leaderRemovedCallback, groupId : {}, memberId: {}", groupId, memberId);
            leaderRemovedCallback.run();
            logger.info("Called leaderRemovedCallback, groupId : {}, memberId: {}", groupId, memberId);
          } catch (Exception e) {
            logger.error(e.getMessage(), e);
          }
        }
      }

      @Override
      public void stateChanged(CuratorFramework client, ConnectionState newState) {
        logger.info("StateChanged, state : {}, groupId : {}, memberId: {}", newState, groupId, memberId);
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
    logger.info("Closing leader, groupId : {}, memberId : {}", groupId, memberId);
    running = false;
    leaderSelector.close();
    logger.info("Closed leader, groupId : {}, memberId : {}", groupId, memberId);
  }
}
