package ai.zhidun.app.knowledge.documents.service.impl;

import ai.zhidun.app.knowledge.documents.controller.DocumentController.ImportRequest;
import ai.zhidun.app.knowledge.documents.model.LibraryVo;
import ai.zhidun.app.knowledge.documents.service.DocImportService;
import ai.zhidun.app.knowledge.documents.service.DocumentService;
import ai.zhidun.app.knowledge.documents.service.DocumentService.SaveResult;
import ai.zhidun.app.knowledge.documents.service.LibraryService;
import ai.zhidun.app.knowledge.security.auth.service.UserService;
import ai.zhidun.app.knowledge.store.utils.FileParser;
import ai.zhidun.app.knowledge.store.utils.FileParser.ParsedResult;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Service
@Slf4j
public class DocImportServiceImpl implements DocImportService {

    private final LibraryService libraryService;

    private final DocumentService documentService;

    private final Path importRootPath;

    private final UserService userService;

    public DocImportServiceImpl(LibraryService libraryService,
                                DocumentService documentService,
                                @Value("${document.import-local-root}") Path importRootPath,
                                UserService userService) {
        this.libraryService = libraryService;
        this.documentService = documentService;
        this.importRootPath = importRootPath;
        this.userService = userService;
    }

    private static final JsonMapper jsonMapper = new JsonMapper();

    public record FileInfo(
            String id,
            @NonNull
            @JsonAlias("file_name")
            String fileName,
            String title,
            String group
    ) {

    }

    @Override
    @SneakyThrows
    public void importFromLocal(ImportRequest request) {
        Semaphore semaphore = new Semaphore(request.parallelism());

        try (ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread
                .ofVirtual()
                .inheritInheritableThreadLocals(true)
                .factory())) {

            Map<String, LibraryVo> library = new HashMap<>();

            try (BufferedReader reader = Files.newBufferedReader(importRootPath.resolve("records"), StandardCharsets.UTF_8)) {
                for (; ; ) {
                    String line = reader.readLine();
                    if (line == null)
                        break;
                    FileInfo fileInfo = jsonMapper.readValue(line, FileInfo.class);
                    LibraryVo libraryVo = library.computeIfAbsent(fileInfo.group, this::saveOrReuseLibrary);

                    semaphore.acquire();
                    executor.submit(() -> {
                        try {
                            SaveResult result = saveDoc(fileInfo, libraryVo);
                            log.info("Save document '{}' of group {}: {}", fileInfo.fileName, fileInfo.group(), result);
                        } catch (Exception e) {
                            log.warn("Save document '{}' of group {} failed!", fileInfo.fileName, fileInfo.group(), e);
                        } finally {
                            semaphore.release();
                        }
                    });
                }
            }

            try {
                semaphore.acquire(request.parallelism());
            } catch (InterruptedException e) {
                log.warn("await {}'s document save failed", importRootPath, e);
            }

        }

    }

    private LibraryVo saveOrReuseLibrary(String group) {
        log.info("Create Or Reuse library '{}'!", group);
        return libraryService
                .getFistByName(group)
                .orElseGet(() -> libraryService.create(group));
    }

    private SaveResult saveDoc(FileInfo fileInfo, LibraryVo libraryVo) {
        File file = importRootPath.resolve(fileInfo.id).toFile();
        ParsedResult result = FileParser.parse(file, fileInfo.title(), fileInfo.fileName());
        return documentService.save(List.of(result), libraryVo.id(), userService.adminUserId());
    }
}
