package io.eventuate.tram.spring.reactive.optimisticlocking;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OptimisticLockingDecoratorAutoConfigurationTest.Config.class)
public class OptimisticLockingDecoratorAutoConfigurationTest extends TestCase {

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