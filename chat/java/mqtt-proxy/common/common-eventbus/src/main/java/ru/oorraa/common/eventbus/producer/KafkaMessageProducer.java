package ru.oorraa.common.eventbus.producer;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.oorraa.common.eventbus.MessageEncoder;
import ru.oorraa.common.model.ChatMessage;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 19/02/15
 */
@Slf4j
@Service
public class KafkaMessageProducer implements KafkaProducer {

    private ProducerConfig config;
    private Producer<String, ChatMessage> producer;

    @PostConstruct
    public void init() {

        Properties props = new Properties();
        props.put("metadata.broker.list", "178.62.194.22:9092"); // "broker1:9092,broker2:9092 "
        props.put("zookeeper.connect", "178.62.194.22:2181/kafka"); // "hostname1:port1/dir"
        props.put("serializer.class", MessageEncoder.class.getName());
        props.put("request.required.acks", "1");
        props.put("producer.type", "sync");
        props.put("client.id", "mqtt-proxy");

        log.trace("kafka producer config: {}", props);

        config = new ProducerConfig(props);
        producer = new Producer<>(config);
    }

    @Override
    public <T> void send(String topic, T message) {
        log.trace("Send message: [{}] ts: {}, {}, ", topic, System.currentTimeMillis(), message);
        KeyedMessage<String, ChatMessage> msg = new KeyedMessage<>(topic, (ChatMessage) message);
        producer.send(msg);
    }

}
