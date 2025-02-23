package ai.zhidun.app.hub.chat.controller;

import ai.zhidun.app.hub.chat.common.Utf8SseEmitter;
import ai.zhidun.app.hub.chat.service.ChatEvent;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {
    private final ChatLanguageModel chatLanguageModel;

    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final Assistant assistant;
    private final InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();


    public TestController(ChatLanguageModel chatLanguageModel, StreamingChatLanguageModel streamingChatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
        this.streamingChatLanguageModel = streamingChatLanguageModel;
        this.assistant = AiServices
                .builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(3))
                .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))
                .build();

        Document document = FileSystemDocumentLoader.loadDocument("paper.pdf");
        document.metadata().put("id", "1");
        List<Document> documents = List.of(document);

        EmbeddingStoreIngestor.ingest(documents, embeddingStore);
    }

    interface Assistant {

        TokenStream chat2(@MemoryId int memoryId, @UserMessage String userMessage);

        AiMessage chat(@MemoryId int memoryId, @UserMessage String userMessage);
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String query) {


        AiMessage message = assistant.chat(1, query);
        return message.text();
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam String query) {
        Utf8SseEmitter emitter = new Utf8SseEmitter();

        TokenStream stream = assistant
                .chat2(2, query)
                .onRetrieved(contents -> {
                    try {
                        for (Content content : contents) {
                            TextSegment segment = content.textSegment();
                            log.info("Content: {}", segment);
                            content.metadata().forEach((k, v) -> log.info("Metadata: {}={}", k, v));

                            emitter.send("插到知识:" + segment.metadata().getString("id") + " " + segment.metadata().getString("file_name"));
                        }
                    } catch (IOException e) {
                        log.warn("Failed to send contents", e);
                    }
                })
                .onPartialResponse(text -> {
                    try {
                        emitter.send("Partial response: " + text);
                    } catch (IOException e) {
                        log.warn("Failed to send partial response", e);
                    }
                })
                .onCompleteResponse((response) -> {
                    log.info("Complete response: {}", response.aiMessage().text());
                    emitter.complete();
                })
                .onError(emitter::completeWithError);

        Thread.ofVirtual().start(stream::start);

        return emitter;
    }
}
