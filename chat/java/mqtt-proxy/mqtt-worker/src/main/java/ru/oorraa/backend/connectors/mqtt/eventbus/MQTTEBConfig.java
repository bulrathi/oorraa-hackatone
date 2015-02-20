package ru.oorraa.backend.connectors.mqtt.eventbus;

import lombok.extern.slf4j.Slf4j;
import net.sf.xenqtt.client.PublishMessage;
import net.sf.xenqtt.message.QoS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.oorraa.backend.connectors.mqtt.mqtt.SyncPublisher;
import ru.oorraa.backend.connectors.mqtt.spam.MessageQualityType;
import ru.oorraa.backend.connectors.mqtt.spam.StopWordsFilter;
import ru.oorraa.common.ExcHandler;
import ru.oorraa.common.eventbus.consumer.ConsumerGroupBean;
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
    @Autowired
    private SyncPublisher publisher;

    @Bean
    public ConsumerGroupBean<ChatMessage> chatInConsumer() {
        return new ConsumerGroupBean<>(zookeeper, KAFKA_CHAT_OUT, ChatMessage.class, (msg, t) -> {
            try {
                log.info("publising message to mqtt {}", msg);

                MessageQualityType type = StopWordsFilter.check(msg.getText());
                if(MessageQualityType.BAD_WORDS.equals(type)) {
                    msg.setText("АХТУНГ - МАТ!");
                }

                publisher.getClient().publish(new PublishMessage(MQTT_CHAT_IN, QoS.AT_MOST_ONCE, JsonUtil.toJson(msg)));
            } catch (JsonMapperException e) {
                ExcHandler.ex(e);
            }
        });
    }

}
