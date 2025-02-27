package ai.zhidun.app.hub.chat.service;

import ai.zhidun.app.hub.chat.model.QueryContext;
import ai.zhidun.app.hub.chat.service.ChatEvent.FinishedEvent;
import ai.zhidun.app.hub.chat.service.ChatEvent.PartialMessageEvent;
import ai.zhidun.app.hub.chat.service.ChatEvent.RetrievedContentEvent;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(discriminatorProperty = "event")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "event", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RetrievedContentEvent.class, name = "retrieved_ctx"),
        @JsonSubTypes.Type(value = PartialMessageEvent.class, name = "partial_message"),
        @JsonSubTypes.Type(value = FinishedEvent.class, name = "finished"),
})
public sealed interface ChatEvent {

    @Schema(name = "RetrievedContentEvent")
    record RetrievedContentEvent(String conversationId, QueryContext ctx) implements ChatEvent {

    }

    @Schema(name = "PartialMessageEvent")
    record PartialMessageEvent(String conversationId, String text) implements ChatEvent {

    }

    @Schema(name = "FinishedEvent")
    record FinishedEvent(String conversationId, String metadata) implements ChatEvent {

    }
}
