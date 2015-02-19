package ru.oorraa.backend.connectors.mqtt.eventbus;

import lombok.extern.slf4j.Slf4j;
import net.sf.xenqtt.client.PublishMessage;
import net.sf.xenqtt.message.QoS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.oorraa.backend.connectors.mqtt.mqtt.AsyncPublisher;
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

    public static final String CHAT_IN = "chat_in";
    public static final String CHAT_OUT = "chat_out";

    @Autowired
    private AsyncPublisher publisher;

    @Bean
    public ConsumerGroupBean<ChatMessage> chatInConsumer() {
        return new ConsumerGroupBean<>(CHAT_IN, ChatMessage.class, (msg, t) -> {
            try {
                publisher.getClient().publish(new PublishMessage(CHAT_IN, QoS.AT_MOST_ONCE, JsonUtil.toJson(msg)));
            } catch (JsonMapperException e) {
                ExcHandler.ex(e);
            }
        });
    }

}
