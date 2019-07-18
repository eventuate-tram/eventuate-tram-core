package io.eventuate.messaging.kafka.basic.consumer;

import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BackPressureManagerTest {

  private BackPressureConfig config = new BackPressureConfig();
  private BackPressureManager bpm;
  private Set<TopicPartition> twoTopicPartitions;

  @Before
  public void setUp() {
    this.config.setLow(5);
    this.config.setHigh(10);
    this.twoTopicPartitions = new HashSet<>();
    twoTopicPartitions.add(new TopicPartition("x", 0));
    twoTopicPartitions.add(new TopicPartition("x", 1));
    this.bpm = new BackPressureManager(config, twoTopicPartitions);

  }

  @Test
  public void shouldNotApplyBackPressure() {
    BackPressureActions actions = bpm.update(10);
    assertEmpty(actions);
  }

  private void assertEmpty(BackPressureActions actions) {
    assertEquals(emptySet(), actions.pause);
    assertEquals(emptySet(), actions.resume);
  }

  @Test
  public void shouldApplyBackPressure() {
    BackPressureActions actions = bpm.update(11);
    assertActionsEqual(new BackPressureActions(twoTopicPartitions, emptySet()), actions);

    actions = bpm.update(11);
    assertEmpty(actions);

    actions = bpm.update(6);
    assertEmpty(actions);

    actions = bpm.update(5);
    assertActionsEqual(new BackPressureActions(emptySet(), twoTopicPartitions), actions);

    actions = bpm.update(10);
    assertEmpty(actions);

  }

  private void assertActionsEqual(BackPressureActions expected, BackPressureActions actual) {
    assertEquals(expected.pause, actual.pause);
    assertEquals(expected.resume, actual.resume);
  }

}