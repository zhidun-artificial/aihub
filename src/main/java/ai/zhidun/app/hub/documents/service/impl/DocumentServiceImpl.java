package ai.zhidun.app.hub.documents.service.impl;

import ai.zhidun.app.hub.assistant.FileContent;
import ai.zhidun.app.hub.auth.service.UserService;
import ai.zhidun.app.hub.common.BizError;
import ai.zhidun.app.hub.common.BizException;
import ai.zhidun.app.hub.common.CustomIdentifierGenerator;
import ai.zhidun.app.hub.documents.controller.DocumentController.SearchDocument;
import ai.zhidun.app.hub.documents.dao.Document;
import ai.zhidun.app.hub.documents.dao.DocumentMapper;
import ai.zhidun.app.hub.documents.model.DocumentVo;
import ai.zhidun.app.hub.documents.service.BlockChecker;
import ai.zhidun.app.hub.documents.service.BlockChecker.Blocked;
import ai.zhidun.app.hub.documents.service.BlockChecker.CheckResult;
import ai.zhidun.app.hub.documents.service.DocumentService;
import ai.zhidun.app.hub.store.service.S3Service;
import ai.zhidun.app.hub.store.utils.FileParser;
import ai.zhidun.app.hub.store.utils.FileParser.Failure;
import ai.zhidun.app.hub.store.utils.FileParser.ParsedResult;
import ai.zhidun.app.hub.store.utils.FileParser.Success;
import co.elastic.clients.json.JsonData;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements
    DocumentService {

  private final BlockChecker checker;

  private final String bucket;

  private final String blockedBucket;

  private final S3Service service;

  private final UserService userService;

  private final FileParser parser;

  private final VectorStoreService storeService;

  public DocumentServiceImpl(BlockChecker checker,
      @Value("${s3.buckets.document}") String bucket,
      @Value("${s3.buckets.blocked}") String blockedBucket,
      S3Service service,
      UserService userService,
      FileParser parser,
      VectorStoreService storeService) {
    this.checker = checker;
    this.bucket = bucket;
    this.blockedBucket = blockedBucket;
    this.service = service;
    this.userService = userService;
    this.parser = parser;
    this.storeService = storeService;
  }

  @PostConstruct
  public void init() {
    service.createBucketIfNotExists(bucket);
    service.createBucketIfNotExists(blockedBucket);
  }

  @Override
  public void rename(String id, String name) {
    if (this.getById(id) instanceof Document document) {
      document.setTitle(name);
      this.saveOrUpdate(document);
      String extension = FilenameUtils.getExtension(document.getFileName());

      String newName;
      if (name.endsWith(extension)) {
        newName = name;
      } else {
        newName = name + "." + extension;
      }
      service.rename(document.getBucket(), document.getKey(), newName);
    } else {
      throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("未找到对应的记录"));
    }
  }

  @Override
  public void delete(String id) {
    if (this.getById(id) instanceof Document document) {
      // 删除文件
      service.delete(document.getBucket(), document.getKey());
      if (storeService.exists(document.getBaseId())) {
        storeService
            .build(document.getBaseId())
            .removeAll(new IsEqualTo("documentId", JsonData.of(document.getId())));
      }
      this.removeById(id);
    }
  }

  @Override
  public void batchDelete(List<String> ids) {
    if (ids == null || ids.isEmpty()) {
      return;
    }
    LambdaQueryWrapper<Document> queryWrapper = Wrappers
        .lambdaQuery(Document.class)
        .in(Document::getId, ids);

    this.list(queryWrapper)
        .forEach(document -> {
          service.delete(document.getBucket(), document.getKey());
          storeService
              .build(document.getBaseId())
              .removeAll(new IsEqualTo("documentId", JsonData.of(document.getId())));
        });

    this.remove(queryWrapper);
  }

  private final AtomicInteger counter = new AtomicInteger(0);

  @Override
  public void triggerIngest() {
    int idx = counter.getAndIncrement();
    if (idx == 0) {
      Thread.ofVirtual()
          .name("ingest")
          .start(() -> {
            while (counter.get() > 0) {
              try {
                this.ingest();
              } catch (Exception e) {
                log.warn("ingest error", e);
              } finally {
                counter.decrementAndGet();
              }
            }
          });
    }
  }

  private final CopyOnWriteArraySet<String> ingestingIds = new CopyOnWriteArraySet<>();

  public void ingest() {
    LambdaQueryWrapper<Document> query = Wrappers.lambdaQuery(Document.class)
        .eq(Document::getStatus, STATUS_PENDING);

    List<Document> documents = this.list(query);

    log.info("ingest documents: {}", documents.size());

    Map<String, EmbeddingStoreIngestor> injestsMap = new HashMap<>(16);
    //todo concurrent ingest maybe
    for (Document document : documents) {
      String documentId = document.getId();
      log.info("ingest document: {}", documentId);
      ingestingIds.add(documentId);
      try {
        String url = service.url(document.getBucket(), document.getKey());
        String localUrl = service.localUrl(url);
        var doc = FileContent.from(localUrl);

        doc.metadata().put("documentId", documentId);
        doc.metadata().put("fileName", document.getFileName());
        doc.metadata().put("url", url);

        injestsMap
            .computeIfAbsent(document.getBaseId(), storeService::ingest)
            .ingest(doc);

        this.lambdaUpdate()
            .eq(Document::getId, documentId)
            .set(Document::getStatus, STATUS_FINISHED)
            .update();
      } catch (Exception e) {
        log.warn("ingest document {} error", documentId, e);
        this.lambdaUpdate()
            .eq(Document::getId, documentId)
            .set(Document::getStatus, STATUS_ERROR)
            .update();
      } finally {
        ingestingIds.remove(documentId);
      }
    }

    storeService.refresh();

    log.info("ingest finished");
  }

  @Scheduled(fixedDelayString = "${ingest.interval:1m}")
  public void scheduledIngest() {
    log.info("scheduled ingest");
    this.triggerIngest();
  }

  @Override
  public IPage<DocumentVo> search(SearchDocument request) {
    return search(request, bucket);
  }

  @Override
  public IPage<DocumentVo> searchBlocked(SearchDocument request) {
    return search(request, blockedBucket);
  }

  @Override
  public void retryIngest() {
    this.lambdaUpdate()
        .eq(Document::getStatus, STATUS_ERROR)
        .set(Document::getStatus, STATUS_PENDING)
        .update();
    this.triggerIngest();
  }

  private IPage<DocumentVo> search(SearchDocument request, String bucket) {
    PageDTO<Document> page = new PageDTO<>(request.pageNo(), request.pageSize());

    LambdaQueryWrapper<Document> query = Wrappers.lambdaQuery();
    query = query
        .eq(Document::getBucket, bucket)
        .and(StringUtils.isNotBlank(request.key()),
            c -> c.like(Document::getTitle, "%" + request.key() + "%"))
        .and(request.baseId() != null, c -> c.eq(Document::getBaseId, request.baseId()));

    query = request.sort().sort(query, Document::getCreateTime, Document::getUpdateTime);

    return this
        .page(page, query)
        .convert(this::from);
  }

  @Override
  public ReplaceResult replace(String id, MultipartFile file) {
    if (this.getById(id) instanceof Document document) {
      ParsedResult result = parser.parse(file);
      return switch (result) {
        case Success success -> new ReplaceResult(
            replaceFile(success, document, checker.check(success.content())), null);
        case Failure ignored -> new ReplaceResult(
            null, new Unknown(file.getOriginalFilename(), file.getContentType()));
      };

    } else {
      throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("未找到对应的记录"));
    }

  }

  record PutResult(String bucket, String key) {

  }

  @SneakyThrows
  PutResult put(String bucket, Success result) {
    String key = CustomIdentifierGenerator.uuidV7();
    service.put(bucket, key, result);
    return new PutResult(bucket, key);
  }

  private DocumentVo replaceFile(Success file, Document document, CheckResult result) {
    String bucket = bucket(result);
    PutResult putResult = put(bucket, file);

    document.setKey(putResult.key);
    document.setBucket(bucket);
    document.setTitle(file.fileName());
    document.setFileName(file.fileName());
    document.setStatus(STATUS_PENDING);
    if (result instanceof Blocked(String reason)) {
      document.setBlockedReason(reason);
    }
    this.saveOrUpdate(document);

    // 删除文件
    service.delete(document.getBucket(), document.getKey());
    if (storeService.exists(document.getBaseId())) {
      storeService
          .build(document.getBaseId())
          .removeAll(new IsEqualTo("documentId", JsonData.of(document.getId())));
    }

    this.triggerIngest();

    return from(document);
  }

  @Override
  public SaveResult save(MultipartFile[] files, String baseId, String userId) {
    List<ParsedResult> results = Lists.newArrayList();
    for (MultipartFile file : files) {
      results.add(parser.parse(file));
    }
    return save(results, baseId, userId);
  }

  @Override
  public SaveResult save(Collection<ParsedResult> files, String baseId, String userId) {
    List<Unknown> unknowns = new ArrayList<>();
    List<DocumentVo> saved = new ArrayList<>();
    for (ParsedResult result : files) {
      switch (result) {
        case Success success -> saved
            .add(saveFile(success, baseId, checker.check(success.content()), userId));
        case Failure failure -> unknowns
            .add(new Unknown(failure.fileName(), failure.contentType()));
      }
    }
    this.triggerIngest();
    return new SaveResult(saved, unknowns);
  }

  private String bucket(CheckResult result) {
    switch (result) {
      case Blocked ignored -> {
        return blockedBucket;
      }
      case BlockChecker.Passed ignored -> {
        return bucket;
      }
    }
  }

  private DocumentVo saveFile(Success file,
      String baseId,
      CheckResult result,
      String userId) {
    String bucket = bucket(result);
    PutResult putResult = put(bucket, file);

    Document entity = new Document();

    entity.setKey(putResult.key);
    entity.setBucket(bucket);
    entity.setBaseId(baseId);
    entity.setCreator(userId);
    entity.setTitle(file.fileName());
    entity.setFileName(file.fileName());
    if (result instanceof Blocked(String reason)) {
      entity.setBlockedReason(reason);
    }
    this.save(entity);

    return from(this.getById(entity.getId()));
  }

  private DocumentVo from(Document entity) {
    String url = service.url(entity.getBucket(), entity.getKey());
    Integer status = entity.getStatus();
    if (status == STATUS_PENDING) {
      if (ingestingIds.contains(entity.getId())) {
        status = STATUS_INGESTING;
      }
    }
    return new DocumentVo(entity.getId(),
        entity.getBaseId(),
        entity.getTitle(),
        entity.getFileName(),
        status,
        url,
        entity.getCreator(),
        userService.name(entity.getCreator()),
        entity.getBlockedReason(),
        entity.getCreateTime().getTime(),
        entity.getUpdateTime().getTime());
  }
}
