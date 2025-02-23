package ai.zhidun.app.hub.chat.model;

import ai.zhidun.app.hub.tmpfile.service.UploadResult;

import java.util.List;

public record QueryContext(
        List<RetrievalResource> resources,
        List<UploadResult> files
) {
}
