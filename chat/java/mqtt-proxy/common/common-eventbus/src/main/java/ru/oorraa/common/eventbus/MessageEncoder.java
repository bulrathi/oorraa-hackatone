package ru.oorraa.common.eventbus;

import kafka.serializer.Encoder;
import ru.oorraa.common.ExcHandler;
import ru.oorraa.common.json.JsonMapperException;
import ru.oorraa.common.json.JsonUtil;
import ru.oorraa.common.model.ChatMessage;

import java.io.UnsupportedEncodingException;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 19/02/15
 */
public class MessageEncoder implements Encoder<ChatMessage> {

    public MessageEncoder(kafka.utils.VerifiableProperties props) {}

    @Override
    public byte[] toBytes(ChatMessage message) {
        byte[] bytes = null;
        try {
            bytes = SerializeUtil.string2Bytes(JsonUtil.toJson(message));
        } catch (UnsupportedEncodingException | JsonMapperException e) {
            ExcHandler.ex(e);
        }
        return bytes;
    }
}
