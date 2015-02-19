package ru.oorraa.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 19/02/15
 */
@Accessors(chain = true)
@Data
public class ChatMessage {

    private final String author;
    private final String text;

}
