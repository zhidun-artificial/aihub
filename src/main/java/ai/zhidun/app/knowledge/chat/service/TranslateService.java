package ai.zhidun.app.knowledge.chat.service;

import ai.zhidun.app.knowledge.chat.client.OllamaClient;
import ai.zhidun.app.knowledge.chat.config.OllamaProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TranslateService {

    private final OllamaClient client;

    private final OllamaProperties properties;

    public TranslateService(OllamaClient client, OllamaProperties properties) {
        this.client = client;

        this.properties = properties;
    }

    public record TranslateRecord(
            int index,
            String source,
            String target
    ) {

    }

    public List<TranslateRecord> translate(String targetLang, List<String> sources) {
        ArrayList<TranslateRecord> list = new ArrayList<>();
        int index = 0;
        for (String source : sources) {
            String prompt = String.format(properties.getTranslateTemplate(), targetLang, source);
            try {
                OllamaClient.CompletionResponse response = client.completion(OllamaClient.Request
                        .builder()
                        .model(properties.getTranslateModel())
                        .system(properties.getTranslateSystem())
                        .stream(false)
                        .prompt(prompt)
                        .build()
                );
                String target = response.response();
                list.add(new TranslateRecord(index, source, target));
            } catch (RuntimeException e) {
                log.warn("翻译报错", e);
                list.add(new TranslateRecord(index, source, ""));
            }
            index++;
        }
        return list;
    }
}
