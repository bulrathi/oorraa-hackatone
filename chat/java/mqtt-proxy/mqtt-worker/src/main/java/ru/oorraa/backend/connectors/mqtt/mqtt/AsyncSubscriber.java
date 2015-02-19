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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 19/02/15
 */
@Service
@Slf4j
public class AsyncSubscriber {

    final CountDownLatch connectLatch = new CountDownLatch(1);
    final AtomicReference<ConnectReturnCode> connectReturnCode = new AtomicReference<ConnectReturnCode>();
    final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();
    @Autowired
    private KafkaProducer producer;
    private AsyncClientListener listener;
    private AsyncMqttClient client;

    @PostConstruct
    public void init() {
        listener = new AsyncClientListener() {

            @Override
            public void publishReceived(MqttClient client, PublishMessage message) {
                try {
                    producer.send(MQTTEBConfig.KAFKA_CHAT_OUT, JsonUtil.fromJson(message.getPayloadString(), ChatMessage.class));
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

            @Override
            public void connected(MqttClient client, ConnectReturnCode returnCode) {
                connectReturnCode.set(returnCode);
                connectLatch.countDown();
            }

            @Override
            public void published(MqttClient client, PublishMessage message) {
                // We do not publish so this should never be called, in theory ;).
            }

            @Override
            public void subscribed(MqttClient client, Subscription[] requestedSubscriptions, Subscription[] grantedSubscriptions, boolean requestsGranted) {
                if (!requestsGranted) {
                    log.error("Unable to subscribe to the following subscriptions: " + Arrays.toString(requestedSubscriptions));
                }

                log.debug("Granted subscriptions: " + Arrays.toString(grantedSubscriptions));
            }

            @Override
            public void unsubscribed(MqttClient client, String[] topics) {
                log.debug("Unsubscribed from the following topics: " + Arrays.toString(topics));
            }

        };


        // Build your client. This client is an asynchronous one so all interaction with the broker will be non-blocking.
        client = new AsyncMqttClient("tcp://188.166.32.82:1883", listener, 5);
//        try {
            // Connect to the broker with a specific client ID. Only if the broker accepted the connection shall we proceed.
            client.connect("musicLover", true);
            ConnectReturnCode returnCode = connectReturnCode.get();
            if (returnCode == null || returnCode != ConnectReturnCode.ACCEPTED) {
                log.error("Unable to connect to the MQTT broker. Reason: " + returnCode);
                return;
            }

            // Create your subscriptions. In this case we want to build up a catalog of classic rock.
            subscriptions.add(new Subscription(MQTTEBConfig.MQTT_CHAT_OUT, QoS.AT_MOST_ONCE));
                    client.subscribe(subscriptions);
//        } catch (Exception ex) {
//            log.error("An unexpected exception has occurred.", ex);
//        }
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
