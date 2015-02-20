package ru.oorraa.common.eventbus.consumer;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import lombok.extern.slf4j.Slf4j;
import ru.oorraa.common.ConcurrencyUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 19/02/15
 */
@Slf4j
public class ConsumerGroupBean<D> {

    private final Properties props;
    private final int numThreads;
    private final String route;
    private final DtoConsumer<D> consumer;
    private final ConsumerConnector connector;
    private final ExecutorService pool;
    private final Class<D> dtoClass;

    public ConsumerGroupBean(String zookeeper, String route, Class<D> dtoClass, DtoConsumer<D> consumer) {

        props = new Properties();
        props.put("zookeeper.connect", zookeeper);
        props.put("group.id", UUID.randomUUID().toString());
        props.put("client.id", "mqtt-proxy");
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");

        log.trace("kafka consumer config: {}", props);

        this.route = route;
        this.numThreads = ConcurrencyUtil.numThreads();
        this.consumer = consumer;
        this.connector = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
        this.pool = Executors.newFixedThreadPool(numThreads);
        this.dtoClass = dtoClass;
    }

    @PostConstruct
    public void postConstruct() {
        log.info("Init ConsumerGroupBean: props={}", props);

        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(route, numThreads);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = connector.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(route);

        // now create an object to consume the messages
        for (final KafkaStream<byte[], byte[]> stream : streams) {
            pool.execute(new KafkaConsumer<D>(stream, dtoClass, consumer));
        }
    }


    @PreDestroy
    public void preDestroy() {
        log.info("destroying ConsumerGroupBean: props={}", props);
        if (connector != null) {
            connector.shutdown();
        }
        ConcurrencyUtil.stopExecutor(pool);
    }

}
