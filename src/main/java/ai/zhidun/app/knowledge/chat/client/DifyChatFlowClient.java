package ai.zhidun.app.knowledge.chat.client;


import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@HttpExchange(url = "/v1", contentType = MediaType.APPLICATION_JSON_VALUE)
public interface DifyChatFlowClient {

    enum ResponseMode {
        @JsonProperty("streaming")
        STREAMING,
        @JsonProperty("blocking")
        BLOCKING
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "transfer_method")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = DifyChatFlowClient.RemoteUrl.class, name = "remote_url"),
            @JsonSubTypes.Type(value = DifyChatFlowClient.UploadFile.class, name = "local_file"),
    })
    sealed interface File permits RemoteUrl, UploadFile {

        @JsonProperty("transfer_method")
        String transferMethod();
    }

    @JsonTypeName("remote_url")
    record RemoteUrl(String type, String url) implements File {

        @JsonProperty("transfer_method")
        @Override
        public String transferMethod() {
            return "remote_url";
        }
    }

    @JsonTypeName("local_file")
    record UploadFile(String type, @JsonProperty("upload_file_id") String fileId) implements File {

        @JsonProperty("transfer_method")
        @Override
        public String transferMethod() {
            return "local_file";
        }

    }

    record RetrieverResource(
            String content,
            @JsonAlias("dataset_id")
            String datasetId,
            @JsonAlias("dataset_name")
            String datasetName,
            @JsonAlias("document_id")
            String documentId,
            @JsonAlias("document_name")
            String documentName,
            Integer position,
            Double score,
            @JsonAlias("segment_id")
            String segmentId
    ) {

    }

    record MetaData(
            @JsonAlias("retriever_resources")
            List<RetrieverResource> retrieverResources,
            Map<String, String> usage) {

    }

    record WorkflowStarted(
            String id,
            @JsonAlias("created_at")
            long createdAt,
            @JsonAlias("sequence_number")
            String sequenceNumber,
            @JsonAlias("workflow_id")
            String workflowId
    ) {

    }

    record WorkflowFinished(
            @JsonAlias("created_at")
            long createdAt,
            @JsonAlias("elapsed_time")
            double elapsedTime,
            String error,
            @JsonAlias("finished_at")
            long finishedAt,
            String id,
            ObjectNode outputs,
            String status,
            @JsonAlias("total_steps")
            int totalSteps,
            @JsonAlias("total_tokens")
            Integer totalTokens,
            @JsonAlias("workflow_id")
            String workflowId
    ) {

        private static final ObjectMapper MAPPER = new ObjectMapper();

        @SuppressWarnings("unused")
        @JsonIgnore
        public <T> T convertOutputs(Class<T> resultType) {
            try {
                return MAPPER.readerFor(resultType).readValue(outputs);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Data
    final class Other implements ChunkChatCompletionEvent {
        private String id;
        @JsonUnwrapped
        @JsonAnySetter
        private ObjectNode all;
    }


    record NodeStarted(
            String id,
            @JsonAlias("node_id")
            String nodeId,
            @JsonAlias("node_type")
            String nodeType,
            String title,
            Integer index,
            @JsonAlias("predecessor_node_id")
            String predecessorNodeId,
            Map<String, Object> inputs,
            @JsonAlias("created_at")
            long createdAt
    ) {

    }

    record NodeFinished(
            String id,
            @JsonAlias("node_id")
            String nodeId,
            int index,
            @JsonAlias("predecessor_node_id")
            String predecessorNodeId,
            Map<String, String> inputs,
            @JsonAlias("process_data")
            Map<String, String> processData,
            Map<String, String> outputs,
            String status,
            String error,
            @JsonAlias("elapsed_time")
            Double elapsedTime,
            @JsonAlias("execution_metadata")
            Map<String, String> execution_metadata,
            @JsonAlias("created_at")
            long createdAt
    ) {

    }

    @JsonTypeName("message")
    record ChunkChatMessageEvent(
            @JsonAlias("task_id")
            String taskId,
            @JsonAlias("message_id")
            String messageId,
            @JsonAlias("conversation_id")
            String conversationId,
            String answer,
            @JsonAlias("created_at")
            long createdAt
    ) implements ChunkChatCompletionEvent {
    }

    @JsonTypeName("message_file")
    record ChunkChatMessageFileEvent(
            String id,
            String type,
            @JsonAlias("belongs_to")
            String belongsTo,
            String url,
            @JsonAlias("conversation_id")
            String conversationId
    ) implements ChunkChatCompletionEvent {

    }

    @JsonTypeName("message_end")
    record ChunkChatMessageEndEvent(
            @JsonAlias("task_id")
            String taskId,
            @JsonAlias("message_id")
            String messageId,
            @JsonAlias("conversation_id")
            String conversationId,
            MetaData metadata

    ) implements ChunkChatCompletionEvent {

    }

    @JsonTypeName("message_replace")
    record ChunkChatMessageReplaceEvent(
            @JsonAlias("task_id")
            String taskId,
            @JsonAlias("message_id")
            String messageId,
            @JsonAlias("conversation_id")
            String conversationId,
            String answer,
            @JsonAlias("created_at")
            long createdAt
    ) implements ChunkChatCompletionEvent {

    }

    @JsonTypeName("workflow_started")
    record ChunkChatWorkflowStartedEvent(
            @JsonAlias("task_id")
            String taskId,
            @JsonAlias("workflow_run_id")
            String workflowRunId,
            WorkflowStarted data
    ) implements ChunkChatCompletionEvent {

    }

    @JsonTypeName("workflow_finished")
    record ChunkChatWorkflowFinishedEvent(
            @JsonAlias("task_id")
            String taskId,
            @JsonAlias("workflow_run_id")
            String workflowRunId,
            WorkflowFinished data
    ) implements ChunkChatCompletionEvent {

    }

    @JsonTypeName("node_started")
    record ChunkChatNodeStartedEvent(
            @JsonAlias("task_id")
            String taskId,
            @JsonAlias("workflow_run_id")
            String workflowRunId,
            NodeStarted data
    ) implements ChunkChatCompletionEvent {

    }

    @JsonTypeName("node_finished")
    record ChunkChatNodeFinishedEvent(
            @JsonAlias("task_id")
            String taskId,
            @JsonAlias("workflow_run_id")
            String workflowRunId,
            NodeFinished data
    ) implements ChunkChatCompletionEvent {

    }

    @JsonTypeName("error")
    record ChunkChatErrorEvent(
            @JsonAlias("task_id")
            String taskId,
            @JsonAlias("message_id")
            String messageId,
            Integer status,
            String code,
            String message
    ) implements ChunkChatCompletionEvent {

    }

    @JsonTypeName("tts_message")
    record ChunkTTSMessageEvent(
            @JsonAlias("task_id")
            String taskId,
            @JsonAlias("message_id")
            String messageId,
            String audio,
            @JsonAlias("created_at")
            long createdAt
    ) implements ChunkChatCompletionEvent {

    }

    @JsonTypeName("tts_message_end")
    record ChunkTTSMessageEndEvent(
            @JsonAlias("task_id")
            String taskId,
            @JsonAlias("message_id")
            String messageId,
            String audio,
            @JsonAlias("created_at")
            long createdAt
    ) implements ChunkChatCompletionEvent {

    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "event")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ChunkChatMessageEvent.class, name = "message"),
            @JsonSubTypes.Type(value = ChunkChatMessageFileEvent.class, name = "message_file"),
            @JsonSubTypes.Type(value = ChunkChatMessageEndEvent.class, name = "message_end"),
            @JsonSubTypes.Type(value = ChunkChatMessageReplaceEvent.class, name = "message_replace"),
            @JsonSubTypes.Type(value = ChunkChatWorkflowStartedEvent.class, name = "workflow_started"),
            @JsonSubTypes.Type(value = ChunkChatWorkflowFinishedEvent.class, name = "workflow_finished"),
            @JsonSubTypes.Type(value = ChunkChatNodeFinishedEvent.class, name = "node_started"),
            @JsonSubTypes.Type(value = ChunkChatMessageEndEvent.class, name = "node_finished"),
            @JsonSubTypes.Type(value = ChunkChatErrorEvent.class, name = "error"),
            @JsonSubTypes.Type(value = ChunkTTSMessageEvent.class, name = "tts_message"),
            @JsonSubTypes.Type(value = ChunkTTSMessageEndEvent.class, name = "tts_message_end"),
            @JsonSubTypes.Type(value = Other.class, names = {"iteration_started", "iteration_next", "iteration_completed"}),
    })
    sealed interface ChunkChatCompletionEvent permits
            ChunkChatErrorEvent,
            ChunkChatMessageEndEvent,
            ChunkChatMessageEvent,
            ChunkChatMessageFileEvent,
            ChunkChatMessageReplaceEvent,
            ChunkChatNodeFinishedEvent,
            ChunkChatNodeStartedEvent,
            ChunkChatWorkflowFinishedEvent,
            ChunkChatWorkflowStartedEvent,
            ChunkTTSMessageEndEvent,
            ChunkTTSMessageEvent,
            Other {
    }

    @PostExchange(value = "/chat-messages", accept = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<ServerSentEvent<ChunkChatCompletionEvent>> chat(@RequestBody ChatRequest request);
}
