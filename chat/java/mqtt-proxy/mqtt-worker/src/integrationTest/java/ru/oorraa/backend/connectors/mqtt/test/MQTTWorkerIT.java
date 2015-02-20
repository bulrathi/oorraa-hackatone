package ru.oorraa.backend.connectors.mqtt.test;

import net.sf.xenqtt.client.PublishMessage;
import net.sf.xenqtt.message.QoS;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.oorraa.backend.connectors.mqtt.eventbus.MQTTEBConfig;
import ru.oorraa.backend.connectors.mqtt.mqtt.AsyncPublisher;
import ru.oorraa.common.eventbus.producer.KafkaProducer;
import ru.oorraa.common.json.JsonMapperException;
import ru.oorraa.common.json.JsonUtil;
import ru.oorraa.common.model.ChatMessage;
import ru.oorraa.common.profiles.IntegrationTestProfile;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 20/02/15
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(IntegrationTestProfile.NAME)
@ContextConfiguration(classes = MQTTWorkerITConfig.class)
@DirtiesContext
public class MQTTWorkerIT {

    @Autowired
    private KafkaProducer producer;
    @Autowired
    private AsyncPublisher publisher;

    @Test
    public void testKafka2MQTT() {
        producer.send(MQTTEBConfig.KAFKA_CHAT_OUT, createMessage());
    }

    @Test
    public void testMQTT2Kafka() throws JsonMapperException {
        publisher.getClient().publish(new PublishMessage(MQTTEBConfig.MQTT_CHAT_OUT, QoS.AT_MOST_ONCE, JsonUtil.toJson(createMessage())));
    }

    // ---------------------------------------------- PRIVATES --------------------------------------------

    private ChatMessage createMessage() {
        long t = System.currentTimeMillis();
        return new ChatMessage("T. Testov " + t, "JUnit text message " + t);
    }

}
