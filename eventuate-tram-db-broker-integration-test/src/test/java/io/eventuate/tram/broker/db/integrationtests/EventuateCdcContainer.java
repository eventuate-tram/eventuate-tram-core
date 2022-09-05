package io.eventuate.tram.broker.db.integrationtests;

import io.eventuate.common.testcontainers.PropertyProvidingContainer;
import org.testcontainers.containers.GenericContainer;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class EventuateCdcContainer extends GenericContainer<EventuateCdcContainer> implements PropertyProvidingContainer  {

    public EventuateCdcContainer() {
        super("eventuateio/eventuate-cdc-service:0.14.0-SNAPSHOT");
    }

    @Override
    public void registerProperties(BiConsumer<String, Supplier<Object>> registry) {

    }
}
