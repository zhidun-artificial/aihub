package ai.zhidun.app.hub.chat.service;

import ai.zhidun.app.hub.chat.model.QueryContext;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChatEvent.RetrievedContent.class, name = "rag"),
        @JsonSubTypes.Type(value = ChatEvent.PartialMessage.class, name = "partial_message"),
        @JsonSubTypes.Type(value = ChatEvent.Finish.class, name = "finish"),
})
public sealed interface ChatEvent {

    @Schema(name = "RetrievedContent")
    record RetrievedContent(String conversationId, QueryContext ctx) implements ChatEvent {

    }

    @Schema(name = "PartialMessage")
    record PartialMessage(String conversationId, String text) implements ChatEvent {

    }

    @Schema(name = "Finish")
    record Finish(String conversationId, ChatResponseMetadata metadata) implements ChatEvent {

    }
}
