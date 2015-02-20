package ru.oorraa.common.eventbus.consumer;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 19/02/15
 */
@FunctionalInterface
public interface DtoConsumer<D> {

    void consume(D dto, long timestamp);

}
