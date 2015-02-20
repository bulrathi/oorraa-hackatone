package ru.oorraa.backend.connectors.mqtt.mqtt;

import lombok.extern.slf4j.Slf4j;
import net.sf.xenqtt.client.*;
import net.sf.xenqtt.message.ConnectReturnCode;
import net.sf.xenqtt.message.QoS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.oorraa.backend.connectors.mqtt.eventbus.MQTTEBConfig;
import ru.oorraa.common.ExcHandler;
import ru.oorraa.common.eventbus.producer.KafkaProducer;
import ru.oorraa.common.json.JsonMapperException;
import ru.oorraa.common.json.JsonUtil;
import ru.oorraa.common.model.ChatMessage;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 20/02/15
 */
@Service
@Slf4j
public class SyncSubscriber {

    final List<Subscription> subscriptions = new ArrayList<>();
    @Autowired
    private KafkaProducer producer;
    private MqttClientListener listener;
    private SyncMqttClient client;

    @PostConstruct
    public void init() {

        listener = new MqttClientListener() {

            @Override
            public void publishReceived(MqttClient client, PublishMessage message) {
                try {
                    producer.send(MQTTEBConfig.KAFKA_CHAT_IN, JsonUtil.fromJson(message.getPayloadString(), ChatMessage.class));
                } catch (JsonMapperException e) {
                    ExcHandler.ex(e);
                }
                message.ack();
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

        // Build your client. This client is a synchronous one so all interaction with the broker will block until said interaction completes.
        client = new SyncMqttClient("tcp://188.166.32.82:1883", listener, 5);
        try {
            // Connect to the broker with a specific client ID. Only if the broker accepted the connection shall we proceed.
            ConnectReturnCode returnCode = client.connect("musicLover", true);
            if (returnCode != ConnectReturnCode.ACCEPTED) {
                log.error("Unable to connect to the MQTT broker. Reason: " + returnCode);
                return;
            }

            // Create your subscriptions. In this case we want to build up a catalog of classic rock.
            List<Subscription> subscriptions = new ArrayList<Subscription>();
            subscriptions.add(new Subscription(MQTTEBConfig.MQTT_CHAT_OUT, QoS.AT_MOST_ONCE));
            client.subscribe(subscriptions);

        } catch (Exception ex) {
            log.error("An unexpected exception has occurred.", ex);
        }

    }


    @PreDestroy
    public void die() {
        // We are done. Unsubscribe at this time.
        List<String> topics = new ArrayList<String>();
        for (Subscription subscription : subscriptions) {
            topics.add(subscription.getTopic());
        }
        client.unsubscribe(topics);

        if (!client.isClosed()) {
            client.disconnect();
        }
    }

}
