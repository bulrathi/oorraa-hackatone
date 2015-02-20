package ru.oorraa.backend.connectors.mqtt.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import ru.oorraa.backend.connectors.mqtt.MQTTWorkerConfig;
import ru.oorraa.common.eventbus.EventBusConstants;
import ru.oorraa.common.profiles.IntegrationTestProfile;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 20/02/15
 */
@Configuration
@IntegrationTestProfile
@ComponentScan({MQTTWorkerConfig.PACKAGE, EventBusConstants.PACKAGE})
@PropertySource(value = "classpath:mqtt-it.properties")
@PropertySource(value = "${propsFile:file:backend-it-local.properties}", ignoreResourceNotFound = true)
@PropertySource(value = "${propsPath:file:}mqtt-worker-it-local.properties", ignoreResourceNotFound = true)
public class MQTTWorkerITConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
