package io.eventuate.tram.inmemory;

public interface EventuateTransactionSynchronizationManager {
  boolean isTransactionActive();
  void executeAfterTransaction(Runnable runnable);
}
