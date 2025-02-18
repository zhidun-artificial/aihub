package ai.zhidun.app.knowledge.chat.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

@HttpExchange(url = "/v1", accept = MediaType.APPLICATION_JSON_VALUE)
public interface DifyConversationClient {

    record ListResponse<T>(int limit, boolean hasMore, List<T> data) {

    }

    record Success(String result) {

    }

    record Conversation(
            String id,
            String name,
            Map<String, Object> inputs,
            String status,
            String introduction,
            @JsonAlias("created_at")
            long createdAt,
            @JsonAlias("updated_at")
            long updatedAt
    ) {

    }



    @Getter
    enum SortBy {
        @Schema(description = "创建时间正序")
        CREATED_AT_ASC("created_at"),
        @Schema(description = "创建时间倒序")
        CREATED_AT_DESC("-created_at"),
        @Schema(description = "更新时间正序")
        UPDATED_AT_ASC("updated_at"),
        @Schema(description = "更新时间倒序")
        UPDATED_AT_DESC("-updated_at");

        private final String sort;

        SortBy(String sort) {

            this.sort = sort;
        }

    }

    @GetExchange("/conversations")
    ListResponse<Conversation> listConversation(
            @RequestParam String user,
            @RequestParam(required = false) String lastId,
            @RequestParam Integer limit,
            @RequestParam(name = "sort_by", required = false) String sort);

    record DeleteRequest(String user) {

    }

    record Message(
            String id,
            @JsonAlias("conversation_id ")
            String conversationId ,
            Map<String, Object> inputs,
            String query,
            List<MessageFile> files,
            String answer,
            @JsonAlias("created_at")
            long createdAt,
            @JsonAlias("updated_at")
            long updatedAt
    ) {

    }

    @GetExchange("/messages")
    ListResponse<Message> listMessages(
            @RequestParam("conversation_id") String conversationId,
            @RequestParam String user,
            @RequestParam(value = "first_id", required = false) String firstId,
            @RequestParam Integer limit);

    @DeleteExchange("/conversations/{id}")
    Success deleteConversation(@PathVariable String id, @RequestBody DeleteRequest request);

    record RenameRequest(String name, @JsonProperty("auto_generate") boolean autoGenerate, String user) {

    }

    record RenameResponse(
            String id,
            String name,
            Map<String, Object> inputs,
            String introduction,
            @JsonAlias("created_at")
            long createdAt,
            @JsonAlias("updated_at")
            long updatedAt

    ) {

    }

    @PostExchange("/conversations/{id}/name")
    RenameResponse renameConversation(@PathVariable String id, @RequestBody RenameRequest request);

    record MessageFile(
            String id,
            String type,
            @JsonAlias("belongs_to")
            String belongsTo,
            String url
    ) {

    }
}
