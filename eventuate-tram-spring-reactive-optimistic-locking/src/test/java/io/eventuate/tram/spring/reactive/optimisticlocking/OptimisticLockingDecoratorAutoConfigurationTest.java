package io.eventuate.tram.spring.reactive.optimisticlocking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.AutoConfiguration;

@SpringBootTest(classes = OptimisticLockingDecoratorAutoConfigurationTest.Config.class)
public class OptimisticLockingDecoratorAutoConfigurationTest {

    @AutoConfiguration
    @EnableAutoConfiguration(exclude= DataSourceAutoConfiguration.class)
    public static class Config {
    }

    @Autowired
    private OptimisticLockingDecorator optimisticLockingDecorator;

    @Test
    public void contextShouldLoad() {

    }
}