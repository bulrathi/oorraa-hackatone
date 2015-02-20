package ru.oorraa.common.eventbus.producer;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 19/02/15
 */
public interface KafkaProducer {

    <T> void send(String topic, T message);

}
