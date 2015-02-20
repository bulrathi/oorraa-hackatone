package ru.oorraa.backend.connectors.mqtt.spam;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Callable;

import static ru.oorraa.backend.connectors.mqtt.spam.MessageQualityType.HAM;
import static ru.oorraa.backend.connectors.mqtt.spam.MessageQualityType.SPAM;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 20/02/15
 */
public class RedirectFollower implements Callable<MessageQualityType> {

    private static final int redirectsTreshHold = 3;
//    private static final String linksChecker = "http://www.google.com/safebrowsing/diagnostic?hl=ru&site=";

    private final String link;
    private final RestTemplate restTemplate;

    public RedirectFollower(String link, RestTemplate restTemplate) {
        this.link = link;
        this.restTemplate = restTemplate;
    }

    @Override
    public MessageQualityType call() throws Exception {
        MessageQualityType type = followLinksRecursively(link, 0);
        if(SPAM.equals(type)) {
            return type;
        }
        return HAM;
    }

    private MessageQualityType followLinksRecursively(String link, int redirectsCount) {

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(link, String.class);
        HttpStatus status = responseEntity.getStatusCode();
        if(status.is2xxSuccessful()) {
            // TODO check link in the base
            return HAM;
        } else if (redirectsCount < redirectsTreshHold && status.is3xxRedirection() && responseEntity.getHeaders().getLocation() != null) {
            return followLinksRecursively(responseEntity.getHeaders().getLocation().toString(), ++redirectsCount);
        } else {
            return SPAM;
        }
    }

}
