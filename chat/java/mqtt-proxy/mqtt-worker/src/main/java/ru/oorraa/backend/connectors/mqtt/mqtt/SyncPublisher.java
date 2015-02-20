package ru.oorraa.backend.connectors.mqtt.mqtt;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.xenqtt.client.MqttClient;
import net.sf.xenqtt.client.MqttClientListener;
import net.sf.xenqtt.client.PublishMessage;
import net.sf.xenqtt.client.SyncMqttClient;
import net.sf.xenqtt.message.ConnectReturnCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 20/02/15
 */
@Service
@Slf4j
public class SyncPublisher {

    @Value("${ru.oorraa.backend.connectors.mqtt.broker:188.166.32.82:1883}")
    private String broker;
    private MqttClientListener listener;
    @Getter
    private MqttClient client;

    @PostConstruct
    public void init() {

        listener = new MqttClientListener() {

            @Override
            public void publishReceived(MqttClient client, PublishMessage message) {
                log.warn("Received a message when no subscriptions were active. Check your broker ;)");
            }

            @Override
            public void disconnected(MqttClient client, Throwable cause, boolean reconnecting) {
                if (cause != null) {
                    log.error("Disconnected from the broker due to an exception.", cause);
                } else {
                    log.info("Disconnected from the broker.");
                }

                if (reconnecting) {
                    log.info("Attempting to reconnect to the broker.");
                }
            }
        };

        // Build your client. This client is a synchronous one so all interaction with the broker will block until said interaction completes.
        client = new SyncMqttClient("tcp://" + broker, listener, 5);
        ConnectReturnCode returnCode = client.connect("mqttSyncPublisher", true);
        if (returnCode != ConnectReturnCode.ACCEPTED) {
            log.error("Unable to connect to the broker. Reason: " + returnCode);
            return;
        }
    }

    @PreDestroy
    public void die() {
        // We are done. Disconnect.
        if (!client.isClosed()) {
            client.disconnect();
        }
    }

}
