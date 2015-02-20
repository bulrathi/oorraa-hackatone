package ru.oorraa.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 19/02/15
 */
@Accessors(chain = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {

    private String author;
    private String text;

}
