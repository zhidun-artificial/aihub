package ai.zhidun.app.knowledge.documents.service;

import ai.zhidun.app.knowledge.documents.controller.DocumentController.ImportRequest;

public interface DocImportService {

    void importFromLocal(ImportRequest request);
}
