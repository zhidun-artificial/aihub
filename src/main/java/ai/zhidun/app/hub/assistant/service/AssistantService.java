package ai.zhidun.app.hub.assistant.service;

import ai.zhidun.app.hub.assistant.AssistantApi;
import ai.zhidun.app.hub.assistant.controller.AssistantController;
import ai.zhidun.app.hub.assistant.model.AssistantDetailVo;
import ai.zhidun.app.hub.assistant.model.AssistantVo;
import ai.zhidun.app.hub.documents.controller.KnowledgeBaseController;
import ai.zhidun.app.hub.documents.model.KnowledgeBaseVo;
import ai.zhidun.app.hub.tmpfile.service.UploadResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface AssistantService {

    record AssistantCreateParam(
            String name,
            String llmModel,
            String systemPrompt,
            Integer permit,
            String description,
            List<String> baseIds,
            String creator,
            JsonNode ext
    ) {
    }

    AssistantVo create(AssistantCreateParam param);

    record AssistantUpdateParam(
            String id,
            String name,
            String llmModel,
            String systemPrompt,
            String description,
            List<String> baseIds,
            JsonNode ext
    ) {
    }

    AssistantVo update(AssistantUpdateParam param);

    void delete(String id);

    IPage<AssistantVo> search(AssistantController.SearchAssistant search);

    AssistantDetailVo detail(String id);

    AssistantApi buildApi(String id, List<UploadResult> files);

    AssistantApi buildApi(String llmModel, List<String> baseIds, List<UploadResult> files);
}
