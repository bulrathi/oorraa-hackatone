package ru.oorraa.backend.connectors.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import ru.oorraa.backend.connectors.mqtt.eventbus.MQTTEBConfig;
import ru.oorraa.common.eventbus.EventBusConstants;
import ru.oorraa.common.profiles.DefaultProfile;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 19/02/15
 */
@Configuration
@Import({MQTTEBConfig.class})
@Slf4j
public class MQTTWorkerConfig {

    public static final String PACKAGE = "ru.oorraa.backend.connectors.mqtt";

    public static void main(String[] args) {
        log.info("Start {}", MQTTWorkerConfig.class.getSimpleName());
        SpringApplication.run(MQTTWorkerConfig.class, args);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @DefaultProfile
    @Configuration
    @ComponentScan({MQTTWorkerConfig.PACKAGE, EventBusConstants.PACKAGE})
    @PropertySource(value = "classpath:mqtt.properties")
    @PropertySource(value = "${propsFile:file:backend-local.properties}", ignoreResourceNotFound = true)
    @PropertySource(value = "${propsPath:file:}mqtt-worker-local.properties", ignoreResourceNotFound = true)
    public static class MQTTWorkerConfigDefault {
    }

}
