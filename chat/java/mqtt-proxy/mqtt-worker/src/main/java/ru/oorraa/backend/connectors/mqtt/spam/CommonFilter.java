package ru.oorraa.backend.connectors.mqtt.spam;

import ru.oorraa.common.model.ChatMessage;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 20/02/15
 */
public class CommonFilter {


    public static MessageQualityType check(ChatMessage msg) {

        MessageQualityType type = StopWordsFilter.check(msg.getText());
        if(MessageQualityType.BAD_WORDS.equals(type)) {
            msg.setText("АХТУНГ - МАТ!");
        }



        return type;
    }

}
