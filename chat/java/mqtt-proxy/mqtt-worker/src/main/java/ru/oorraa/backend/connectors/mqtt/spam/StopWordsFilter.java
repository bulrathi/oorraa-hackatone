package ru.oorraa.backend.connectors.mqtt.spam;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 20/02/15
 */
public class StopWordsFilter {

    public static final Set<String> BAD_WORDS = ImmutableSet.of(
            "жопа"
    );
    private static final Splitter splitter = Splitter.on(" ");
    private static HttpClient httpClient = HttpClientBuilder.create().setMaxConnPerRoute(3).build();
    private static ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
    private static RestTemplate restTemplate = new RestTemplate(requestFactory);

    public static MessageQualityType check(String text) {
        List<String> words = Lists.newArrayList(splitter.split(text));
        try {
            for (String w : words) {
                String word = w.toLowerCase();
                if (BAD_WORDS.contains(word)) {
                    return MessageQualityType.BAD_WORDS;
                }
                if (word.contains("http://") || word.contains("https://")) {
                    MessageQualityType type = new RedirectFollower(word, restTemplate).call();
                    if (MessageQualityType.SPAM.equals(type)) {
                        return type;
                    }
                }
            }
        } catch (Exception e) {
        }
        return MessageQualityType.HAM;
    }

}
