package ru.oorraa.backend.connectors.mqtt.mqtt;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.xenqtt.client.*;
import net.sf.xenqtt.message.ConnectReturnCode;
import ru.oorraa.common.ExcHandler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 19/02/15
 */
@Deprecated
//@Service
@Slf4j
public class AsyncPublisher {

    final CountDownLatch connectLatch = new CountDownLatch(1);
    final AtomicReference<ConnectReturnCode> connectReturnCode = new AtomicReference<ConnectReturnCode>();
    @Getter
    private MqttClient client;
    private AsyncClientListener listener;

    @PostConstruct
    public void init() {
        listener = new AsyncClientListener() {

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

            @Override
            public void connected(MqttClient client, ConnectReturnCode returnCode) {
                connectReturnCode.set(returnCode);
                connectLatch.countDown();
            }

            @Override
            public void subscribed(MqttClient client, Subscription[] requestedSubscriptions, Subscription[] grantedSubscriptions, boolean requestsGranted) {
            }

            @Override
            public void unsubscribed(MqttClient client, String[] topics) {
            }

            @Override
            public void published(MqttClient client, PublishMessage message) {
            }

        };

        // Build your client. This client is an asynchronous one so all interaction with the broker will be non-blocking.
        client = new AsyncMqttClient("tcp://188.166.32.82:1883", listener, 5);
        try {
            // Connect to the broker. We will await the return code so that we know whether or not we can even begin publishing.
            client.connect("musicProducerAsync", false, "music-user", "music-pass");
            connectLatch.await();

            ConnectReturnCode returnCode = connectReturnCode.get();
            if (returnCode == null || returnCode != ConnectReturnCode.ACCEPTED) {
                // The broker bounced us. We are done.
                log.error("The broker rejected our attempt to connect. Reason: " + returnCode);
                return;
            }
        } catch (InterruptedException ex) {
            ExcHandler.ex(ex);
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
