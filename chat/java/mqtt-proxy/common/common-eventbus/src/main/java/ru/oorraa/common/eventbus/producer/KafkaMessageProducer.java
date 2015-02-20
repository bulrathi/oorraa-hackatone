package ru.oorraa.common.eventbus.producer;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.oorraa.common.eventbus.MessageEncoder;
import ru.oorraa.common.model.ChatMessage;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 19/02/15
 */
@Slf4j
@Service
public class KafkaMessageProducer implements KafkaProducer {

    @Value("${ru.oorraa.common.eventbus.producer.brokerList}")
    private String brokerList;
    @Value("${ru.oorraa.common.eventbus.zookeeper}")
    private String zookeeper;
    private ProducerConfig config;
    private Producer<String, ChatMessage> producer;

    @PostConstruct
    public void init() {

        Properties props = new Properties();
        props.put("metadata.broker.list", brokerList); // "broker1:9092,broker2:9092 "
        props.put("zookeeper.connect", zookeeper); // "hostname1:port1/dir"
        props.put("serializer.class", MessageEncoder.class.getName());
        props.put("request.required.acks", "1");
        props.put("producer.type", "sync");
        props.put("client.id", "mqtt_proxy_client");

        log.trace("kafka producer config: {}", props);

        config = new ProducerConfig(props);
        producer = new Producer<>(config);
    }

    @Override
    public <T> void send(String topic, T message) {
        log.info("Send message: [{}] ts: {}, {}, ", topic, System.currentTimeMillis(), message);
        KeyedMessage<String, ChatMessage> msg = new KeyedMessage<>(topic, (ChatMessage) message);
        producer.send(msg);
    }

    @PreDestroy
    public void destroy() throws Exception {
        log.info("closing kafka producer: clientId={}, brokers={}", config.clientId(), config.brokerList());
        producer.close();
    }

}
