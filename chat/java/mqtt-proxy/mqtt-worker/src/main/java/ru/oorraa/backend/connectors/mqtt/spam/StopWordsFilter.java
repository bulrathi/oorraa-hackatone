package ru.oorraa.backend.connectors.mqtt.spam;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;

/**
 * @author s.meshkov <a href="mailto:s.meshkov@oorraa.net"/>
 * @since 20/02/15
 */
public class StopWordsFilter {

    private static final Splitter splitter = Splitter.on(" ");

    public static final Set<String> BAD_WORDS = ImmutableSet.of(
            "хуй",
            "хуйня",
            "пидар",
            "пидор",
            "пидрила",
            "жопа",
            "пизда",
            "гавно",
            "ебал",
            "ебать",
            "сука",
            "суки",
            "сукин",
            "сукины",
            "педик",
            "педрила",
            "соплежую",
            "соплежуй",
            "сучка",
            "опиздень",
            "ебонтяй",
            "охуевающий",
            "гондопляс",
            "опидоревший",
            "хуекрылый",
            "пидрастический",
            "склипиздень",
            "манда",
            "разъебись",
            "ебись"
    );

    public static MessageQualityType check(String text) {
        List<String> words = Lists.newArrayList(splitter.split(text));
        for (String w : words) {
            if(BAD_WORDS.contains(w.toLowerCase())) {
                return MessageQualityType.BAD_WORDS;
            }
        }
        return MessageQualityType.HAM;
    }

}
