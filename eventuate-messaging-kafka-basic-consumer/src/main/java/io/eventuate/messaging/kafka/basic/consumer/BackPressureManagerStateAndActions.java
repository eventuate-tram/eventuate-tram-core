package io.eventuate.messaging.kafka.basic.consumer;

public class BackPressureManagerStateAndActions {

  final BackPressureActions actions;
  final BackPressureManagerState state;

  public BackPressureManagerStateAndActions(BackPressureActions actions, BackPressureManagerState state) {
    this.actions = actions;
    this.state = state;
  }

  public BackPressureManagerStateAndActions(BackPressureManagerState state) {
    this(BackPressureActions.NONE, state);
  }

}
