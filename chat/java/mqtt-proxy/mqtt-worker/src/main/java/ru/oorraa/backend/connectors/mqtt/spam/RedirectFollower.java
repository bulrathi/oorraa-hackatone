package ru.oorraa.backend.connectors.mqtt.spam;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Callable;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 20/02/15
 */
public class RedirectFollower implements Callable<MessageQualityType> {

    private final String link;
    private final RestTemplate restTemplate;

    public RedirectFollower(String link, RestTemplate restTemplate) {
        this.link = link;
        this.restTemplate = restTemplate;
    }

    @Override
    public ru.oorraa.backend.connectors.mqtt.spam.MessageQualityType call() throws Exception {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(link, String.class);
        HttpStatus status = responseEntity.getStatusCode();
        if(status.is2xxSuccessful()) {
            return ru.oorraa.backend.connectors.mqtt.spam.MessageQualityType.HAM;
        } else {

        }
        return null;
    }

    private ru.oorraa.backend.connectors.mqtt.spam.MessageQualityType followLinksRecursively(String link) {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(link, String.class);
        HttpStatus status = responseEntity.getStatusCode();
        if(status.is2xxSuccessful()) {
            return ru.oorraa.backend.connectors.mqtt.spam.MessageQualityType.HAM;
        } else {

        }
        return null;
    }

//    @Override
//    public MessageQualityType call() throws Exception {
//        ResponseEntity<String> responseEntity = restTemplate.getForEntity(link, String.class);
//        HttpStatus status = responseEntity.getStatusCode();
//        if(status)
//        return null;
//    }

    public static enum MessageQualityType {
        SPAM, HAM;
    }

}
