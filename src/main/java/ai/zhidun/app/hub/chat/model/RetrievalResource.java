package ai.zhidun.app.hub.chat.model;

public record RetrievalResource(
        String documentId,
        String fileName,
        String url
) {
}
