package ru.oorraa.backend.connectors.mqtt.mqtt;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.xenqtt.client.MqttClient;
import net.sf.xenqtt.client.MqttClientListener;
import net.sf.xenqtt.client.PublishMessage;
import org.springframework.beans.factory.annotation.Autowired;
import ru.oorraa.backend.connectors.mqtt.eventbus.MQTTEBConfig;
import ru.oorraa.backend.connectors.mqtt.spam.MessageQualityType;
import ru.oorraa.backend.connectors.mqtt.spam.StopWordsFilter;
import ru.oorraa.common.ExcHandler;
import ru.oorraa.common.eventbus.producer.KafkaProducer;
import ru.oorraa.common.json.JsonMapperException;
import ru.oorraa.common.json.JsonUtil;
import ru.oorraa.common.model.ChatMessage;

import javax.annotation.PostConstruct;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 20/02/15
 */
//@Service
@Slf4j
public class MQTTListener {

    @Getter
    private MqttClientListener listener;
    @Autowired
    private KafkaProducer producer;

    @PostConstruct
    public void init() {
        listener = new MqttClientListener() {

            @Override
            public void publishReceived(MqttClient client, PublishMessage message) {
                try {
                    ChatMessage msg = JsonUtil.fromJson(message.getPayloadString(), ChatMessage.class);
                    log.info("publishReceived > {}", msg);
                    message.ack();

                    MessageQualityType type = StopWordsFilter.check(msg.getText());
                    if(MessageQualityType.BAD_WORDS.equals(type)) {
                        msg.setText("В сообщении замечен МАТ");
                    }

                    producer.send(MQTTEBConfig.KAFKA_CHAT_IN, msg);
                } catch (JsonMapperException e) {
                    ExcHandler.ex(e);
                }
            }

            @Override
            public void disconnected(MqttClient client, Throwable cause, boolean reconnecting) {
                if (cause != null) {
                    log.error("Disconnected from the broker due to an exception.", cause);
                } else {
                    log.info("Disconnecting from the broker.");
                }

                if (reconnecting) {
                    log.info("Attempting to reconnect to the broker.");
                }
            }
        };
    }

}
