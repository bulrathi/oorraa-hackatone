package ru.oorraa.backend.connectors.mqtt.eventbus;

import lombok.extern.slf4j.Slf4j;
import net.sf.xenqtt.client.MqttClientListener;
import net.sf.xenqtt.client.PublishMessage;
import net.sf.xenqtt.message.QoS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.oorraa.backend.connectors.mqtt.mqtt.MQTTListener;
import ru.oorraa.backend.connectors.mqtt.mqtt.SyncPublisher;
import ru.oorraa.backend.connectors.mqtt.mqtt.SyncSubscriber;
import ru.oorraa.backend.connectors.mqtt.spam.MessageQualityType;
import ru.oorraa.backend.connectors.mqtt.spam.StopWordsFilter;
import ru.oorraa.common.ExcHandler;
import ru.oorraa.common.eventbus.consumer.ConsumerGroupBean;
import ru.oorraa.common.eventbus.producer.KafkaProducer;
import ru.oorraa.common.json.JsonMapperException;
import ru.oorraa.common.json.JsonUtil;
import ru.oorraa.common.model.ChatMessage;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 19/02/15
 */
@Configuration
@Slf4j
public class MQTTEBConfig {

    public static final String KAFKA_CHAT_IN = "chat_in";
    public static final String KAFKA_CHAT_OUT = "chat_out";

    public static final String MQTT_CHAT_IN = "chat/in";
    public static final String MQTT_CHAT_OUT = "chat/out";

    @Value("${ru.oorraa.common.eventbus.zookeeper}")
    private String zookeeper;
    @Value("${ru.oorraa.backend.connectors.mqtt.broker:188.166.32.82:1883}")
    private String mqttBroker;
    @Autowired
    private SyncPublisher publisher;

    @Bean
    @Autowired
    public MqttClientListener mqqtListener(KafkaProducer producer) {
        return new MQTTListener(producer).getListener();
    }

    @Bean
    @Autowired
    public SyncSubscriber mqttSubscriber(KafkaProducer producer) {
        return new SyncSubscriber(mqttBroker, mqqtListener(producer), producer);
    }

    @Bean
    @Autowired
    public SyncPublisher mqttPublisher(KafkaProducer producer) {
        return new SyncPublisher(mqttBroker, mqqtListener(producer));
    }

    @Bean
    public ConsumerGroupBean<ChatMessage> chatInConsumer() {
        return new ConsumerGroupBean<>(zookeeper, KAFKA_CHAT_OUT, ChatMessage.class, (msg, t) -> {
            try {
                log.info("publising message to mqtt {}", msg);

                MessageQualityType type = StopWordsFilter.check(msg.getText());
                if(MessageQualityType.BAD_WORDS.equals(type)) {
                    msg.setText("АХТУНГ - МАТ!");
                } else if(MessageQualityType.SPAM.equals(type)) {
                    msg.setText("АХТУНГ - СПАМ!");
                }

                publisher.getClient().publish(new PublishMessage(MQTT_CHAT_IN, QoS.AT_LEAST_ONCE, JsonUtil.toJson(msg)));
            } catch (JsonMapperException e) {
                ExcHandler.ex(e);
            }
        });
    }

}
