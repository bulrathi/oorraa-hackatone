package ru.oorraa.backend.connectors.mqtt.mqtt;

import lombok.extern.slf4j.Slf4j;
import net.sf.xenqtt.client.MqttClientListener;
import net.sf.xenqtt.client.Subscription;
import net.sf.xenqtt.client.SyncMqttClient;
import net.sf.xenqtt.message.ConnectReturnCode;
import net.sf.xenqtt.message.QoS;
import ru.oorraa.backend.connectors.mqtt.eventbus.MQTTEBConfig;
import ru.oorraa.common.eventbus.producer.KafkaProducer;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 20/02/15
 */
@Slf4j
public class SyncSubscriber {

    private final List<Subscription> subscriptions = new ArrayList<>();
    private final SyncMqttClient client;


    public SyncSubscriber(String broker, MqttClientListener listener, KafkaProducer producer) {
//        listener = new MqttClientListener() {
//
//            @Override
//            public void publishReceived(MqttClient client, PublishMessage message) {
//                try {
//                    ChatMessage msg = JsonUtil.fromJson(message.getPayloadString(), ChatMessage.class);
//                    log.info("publishReceived > {}", msg);
//                    message.ack();
//
//                    MessageQualityType type = StopWordsFilter.check(msg.getText());
//                    if(MessageQualityType.BAD_WORDS.equals(type)) {
//                        msg.setText("АХТУНГ - МАТ!");
//                    } else if(MessageQualityType.SPAM.equals(type)) {
//                        msg.setText("АХТУНГ - СПАМ!");
//                    }
//
//                    producer.send(MQTTEBConfig.KAFKA_CHAT_IN, msg);
//                } catch (JsonMapperException e) {
//                    ExcHandler.ex(e);
//                }
//            }
//
//            @Override
//            public void disconnected(MqttClient client, Throwable cause, boolean reconnecting) {
//                if (cause != null) {
//                    log.error("Disconnected from the broker due to an exception.", cause);
//                } else {
//                    log.info("Disconnecting from the broker.");
//                }
//                if (reconnecting) {
//                    log.info("Attempting to reconnect to the broker.");
//                }
//            }
//
//        };

        // Build your client. This client is a synchronous one so all interaction with the broker will block until said interaction completes.
        client = new SyncMqttClient("tcp://" + broker, listener, 5);
        try {
            // Connect to the broker with a specific client ID. Only if the broker accepted the connection shall we proceed.
            ConnectReturnCode returnCode = client.connect("mqttSyncSubscriber", true);
            if (returnCode != ConnectReturnCode.ACCEPTED) {
                log.error("Unable to connect to the MQTT broker. Reason: " + returnCode);
                return;
            }

            // Create your subscriptions. In this case we want to build up a catalog of classic rock.
            List<Subscription> subscriptions = new ArrayList<Subscription>();
            subscriptions.add(new Subscription(MQTTEBConfig.MQTT_CHAT_OUT, QoS.AT_LEAST_ONCE));
            client.subscribe(subscriptions);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
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
