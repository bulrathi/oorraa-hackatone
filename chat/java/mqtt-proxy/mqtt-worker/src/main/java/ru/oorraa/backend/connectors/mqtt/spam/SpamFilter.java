package ru.oorraa.backend.connectors.mqtt.spam;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 20/02/15
 */
//@Service
@Slf4j
public class SpamFilter {

    @Value("${ru.oorraa.backend.connectors.mqtt.spam.maxConnections:3}")
    private int maxConnections = 3;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        HttpClient httpClient = HttpClientBuilder.create().setMaxConnPerRoute(maxConnections).build();
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate = new RestTemplate(requestFactory);
    }




}
