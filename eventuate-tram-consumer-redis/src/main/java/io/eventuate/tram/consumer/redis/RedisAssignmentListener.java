package io.eventuate.tram.consumer.redis;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.coordinator.Assignment;
import io.eventuate.tram.consumer.common.coordinator.AssignmentListener;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class RedisAssignmentListener implements AssignmentListener {
  private RedisTemplate<String, String> redisTemplate;
  private Consumer<Assignment> assignmentUpdatedCallback;
  private long assignmentListenerInterval;

  private String assignmentKey;
  private Optional<Assignment> lastAssignment;
  private Timer timer = new Timer();

  public RedisAssignmentListener(RedisTemplate<String, String> redisTemplate,
                                 String groupId,
                                 String memberId,
                                 long assignmentListenerInterval,
                                 Consumer<Assignment> assignmentUpdatedCallback) {

    this.redisTemplate = redisTemplate;
    this.assignmentListenerInterval = assignmentListenerInterval;
    this.assignmentUpdatedCallback = assignmentUpdatedCallback;

    assignmentKey = RedisKeyUtil.keyForAssignment(groupId, memberId);

    lastAssignment = readAssignment();
    lastAssignment.ifPresent(assignmentUpdatedCallback);

    scheduleAssignmentCheck();
  }

  private void scheduleAssignmentCheck() {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        checkAssignmentUpdate();
      }
    }, 0, assignmentListenerInterval);
  }

  private void checkAssignmentUpdate() {
    Optional<Assignment> currentAssignment = readAssignment();

    if (!currentAssignment.equals(lastAssignment)) {
      currentAssignment.ifPresent(assignmentUpdatedCallback);
      lastAssignment = currentAssignment;
    }
  }

  private Optional<Assignment> readAssignment() {
    return Optional
            .ofNullable(redisTemplate.opsForValue().get(assignmentKey))
            .map(jsonAssignment -> JSonMapper.fromJson(jsonAssignment, Assignment.class));
  }

  public void remove() {
    timer.cancel();
  }
}
