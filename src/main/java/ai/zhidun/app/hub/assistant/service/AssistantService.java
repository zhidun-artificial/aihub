package ai.zhidun.app.hub.assistant.service;

import ai.zhidun.app.hub.assistant.AssistantApi;
import ai.zhidun.app.hub.assistant.controller.AssistantController;
import ai.zhidun.app.hub.assistant.model.AssistantDetailVo;
import ai.zhidun.app.hub.assistant.model.AssistantVo;
import ai.zhidun.app.hub.tmpfile.service.UploadResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public interface AssistantService {

  record AssistantCreateParam(
      String name,
      String llmModel,
      String systemPrompt,
      @Schema(description = "0-公开 1-个人 2-团队")
      Integer permit,
      @Schema(description = "团队ID，当permit=2时必填")
      String groupId,
      String description,
      List<String> baseIds,
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

  AssistantApi buildApi(String llmModel, String systemPrompt, List<String> baseIds, List<UploadResult> files);
}
