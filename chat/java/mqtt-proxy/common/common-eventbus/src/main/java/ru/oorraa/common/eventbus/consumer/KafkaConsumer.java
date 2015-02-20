package ru.oorraa.common.eventbus.consumer;

import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;
import lombok.extern.slf4j.Slf4j;
import ru.oorraa.common.ExcHandler;
import ru.oorraa.common.eventbus.SerializeUtil;
import ru.oorraa.common.json.JsonMapperException;
import ru.oorraa.common.json.JsonUtil;

import java.io.UnsupportedEncodingException;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 19/02/15
 */
@Slf4j
public class KafkaConsumer<D> implements Runnable {

    private final KafkaStream<byte[], byte[]> stream;
    private final Class<D> dtoClass;
    private final DtoConsumer<D> consumer;

    public KafkaConsumer(KafkaStream<byte[], byte[]> stream, Class<D> dtoClass, DtoConsumer<D> consumer) {
        this.stream = stream;
        this.dtoClass = dtoClass;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        log.debug("Kafka consumer is started");

        for (MessageAndMetadata<byte[], byte[]> aStream : stream) {
            byte[] bytes = aStream.message();
            log.trace("Consumed {} bytes", bytes.length);
            try {
                D msg = JsonUtil.fromJson(SerializeUtil.bytes2String(bytes), dtoClass);
                log.info("Decoded msg: {}", msg);
                consumer.consume(msg, System.currentTimeMillis());
            } catch (JsonMapperException | UnsupportedEncodingException e) {
                ExcHandler.ex(e);
            }
        }
        log.debug("Consumer is shutting down");
    }
}
