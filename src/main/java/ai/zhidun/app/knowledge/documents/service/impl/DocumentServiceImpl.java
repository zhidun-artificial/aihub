package ai.zhidun.app.knowledge.documents.service.impl;

import ai.zhidun.app.knowledge.common.BizError;
import ai.zhidun.app.knowledge.common.BizException;
import ai.zhidun.app.knowledge.documents.controller.DocumentController.SearchDocument;
import ai.zhidun.app.knowledge.documents.dao.Document;
import ai.zhidun.app.knowledge.documents.dao.DocumentMapper;
import ai.zhidun.app.knowledge.documents.model.DocumentVo;
import ai.zhidun.app.knowledge.documents.service.BlockChecker;
import ai.zhidun.app.knowledge.documents.service.BlockChecker.Blocked;
import ai.zhidun.app.knowledge.documents.service.BlockChecker.CheckResult;
import ai.zhidun.app.knowledge.documents.service.DocumentService;
import ai.zhidun.app.knowledge.security.auth.service.UserService;
import ai.zhidun.app.knowledge.store.service.S3Service;
import ai.zhidun.app.knowledge.store.utils.FileParser;
import ai.zhidun.app.knowledge.store.utils.FileParser.Docx;
import ai.zhidun.app.knowledge.store.utils.FileParser.ParsedResult;
import ai.zhidun.app.knowledge.store.utils.FileParser.Pdf;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    private final BlockChecker checker;

    private final String bucket;

    private final String blockedBucket;

    private final S3Service service;

    private final UserService userService;

    private final Integer coverDpi;

    public DocumentServiceImpl(BlockChecker checker,
                               @Value("${s3.buckets.document}") String bucket,
                               @Value("${s3.buckets.blocked}") String blockedBucket,
                               @Value("${s3.cover.dpi}") Integer coverDpi,
                               S3Service service, UserService userService) {
        this.checker = checker;
        this.bucket = bucket;
        this.blockedBucket = blockedBucket;
        this.service = service;
        this.userService = userService;
        this.coverDpi = coverDpi;
    }

    @PostConstruct
    public void init() {
        service.createBucketIfNotExists(bucket);
        service.createBucketIfNotExists(blockedBucket);
    }

    @Override
    public void rename(Integer id, String name) {
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
    public void delete(Integer id) {
        if (this.getById(id) instanceof Document document) {
            service.delete(document.getBucket(), document.getKey());
            this.removeById(id);
        }
    }

    @Override
    public IPage<DocumentVo> search(SearchDocument request) {
        return search(request, bucket);
    }

    @Override
    public IPage<DocumentVo> searchBlocked(SearchDocument request) {
        return search(request, blockedBucket);
    }

    private IPage<DocumentVo> search(SearchDocument request, String bucket) {
        PageDTO<Document> page = new PageDTO<>(request.pageNo(), request.pageSize());

        LambdaQueryWrapper<Document> query = Wrappers.lambdaQuery();
        query = query
                .eq(Document::getBucket, bucket)
                .and(StringUtils.isNotBlank(request.key()), c -> c.like(Document::getTitle, "%" + request.key() + "%"))
                .and(request.libraryId() != null, c -> c.eq(Document::getLibraryId, request.libraryId()));

        query = switch (request.sort()) {
            case UPDATED_AT_ASC -> query.orderByAsc(Document::getUpdateTime);
            case CREATED_AT_ASC -> query.orderByAsc(Document::getCreateTime);
            case CREATED_AT_DESC -> query.orderByDesc(Document::getCreateTime);
            case UPDATED_AT_DESC -> query.orderByDesc(Document::getUpdateTime);
        };
        return this
                .page(page, query)
                .convert(this::from);
    }

    @Override
    public ReplaceResult replace(Integer id, MultipartFile file) {
        if (this.getById(id) instanceof Document document) {
            ParsedResult result = FileParser.parse(file);
            return switch (result) {
                case Docx docx -> new ReplaceResult(replaceFile(docx, document, checker.check(docx)), null);
                case Pdf pdf -> new ReplaceResult(replaceFile(pdf, document, checker.check(pdf)), null);
                case FileParser.Unknown ignored -> new ReplaceResult(
                        null, new Unknown(file.getOriginalFilename(), file.getContentType()));
            };

        } else {
            throw new BizException(HttpStatus.BAD_REQUEST, BizError.error("未找到对应的记录"));
        }

    }

    record PutResult(String bucket, String key, String rawKey, String coverKey) {

    }

    PutResult put(String bucket, ParsedResult file) {
        String rawKey = file.newKey();

        service.put(bucket, rawKey, file);

        return switch (file) {
            case Docx docx -> FileParser.toPdf(docx, (pdf) -> {
                String pdfKey = pdf.newKey();
                service.put(bucket, pdfKey, pdf);
                return saveCover(bucket, pdfKey, rawKey, pdf);
            });
            case Pdf pdf -> saveCover(bucket, rawKey, rawKey, pdf);
            case FileParser.Unknown ignored -> new PutResult(bucket, rawKey, rawKey, null);
        };
    }

    private PutResult saveCover(String bucket, String key, String rawKey, Pdf pdf) {
        try {
            byte[] jpeg = pdf.renderFirstPageAsJpeg(coverDpi);
            String coverKey = key + ".cover.jpeg";
            service.putCover(bucket, coverKey, jpeg);
            return new PutResult(bucket, key, rawKey, coverKey);
        } catch (IOException e) {
            log.warn("get cover failed", e);
            return new PutResult(bucket, key, rawKey, null);
        }
    }

    private DocumentVo replaceFile(ParsedResult file, Document document, CheckResult result) {
        String bucket = bucket(result);
        PutResult putResult = put(bucket, file);

        document.setKey(putResult.key);
        document.setRawKey(putResult.rawKey);
        document.setCoverKey(putResult.coverKey);
        document.setBucket(bucket);
        document.setTitle(file.title());
        document.setFileName(file.fileName());
        if (result instanceof Blocked(String reason)) {
            document.setBlockedReason(reason);
        }
        this.saveOrUpdate(document);

        return from(document);
    }

    @Override
    public SaveResult save(MultipartFile[] files, Integer libraryId, Integer userId) {
        List<ParsedResult> results = Lists.newArrayList();
        for (MultipartFile file : files) {
            results.add(FileParser.parse(file));
        }
        return save(results, libraryId, userId);
    }

    @Override
    public SaveResult save(Collection<ParsedResult> files, Integer libraryId, Integer userId) {
        List<Unknown> unknowns = new ArrayList<>();
        List<DocumentVo> saved = new ArrayList<>();
        for (ParsedResult result : files) {
            switch (result) {
                case FileParser.Docx docx -> saved.add(saveFile(docx, libraryId, checker.check(docx), userId));
                case Pdf pdf -> saved.add(saveFile(pdf, libraryId, checker.check(pdf), userId));
                case FileParser.Unknown unknown -> unknowns
                        .add(new Unknown(unknown.fileName(), result.contentType()));
            }
        }
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

    private DocumentVo saveFile(ParsedResult file,
                                Integer libraryId,
                                CheckResult result,
                                Integer userId) {
        String bucket = bucket(result);
        PutResult putResult = put(bucket, file);

        Document entity = new Document();

        entity.setKey(putResult.key);
        entity.setRawKey(putResult.rawKey);
        entity.setCoverKey(putResult.coverKey);
        entity.setBucket(bucket);
        entity.setLibraryId(libraryId);
        entity.setCreator(userId);
        entity.setTitle(file.title());
        entity.setFileName(file.fileName());
        if (result instanceof Blocked(String reason)) {
            entity.setBlockedReason(reason);
        }
        this.save(entity);

        return from(this.getById(entity.getId()));
    }

    private DocumentVo from(Document entity) {
        String url = service.url(entity.getBucket(), entity.getKey());
        String rawUrl;
        if (entity.getRawKey() != null) {
            rawUrl = service.url(entity.getBucket(), entity.getRawKey());
        } else {
            rawUrl = url;
        }
        String coverUrl = null;
        if (entity.getCoverKey() != null) {
            coverUrl = service.url(entity.getBucket(), entity.getCoverKey());
        }
        return new DocumentVo(entity.getId(),
                entity.getLibraryId(),
                entity.getTitle(),
                entity.getFileName(),
                url,
                rawUrl,
                coverUrl,
                entity.getCreator(),
                userService.name(entity.getCreator()),
                entity.getBlockedReason(),
                entity.getCreateTime().getTime());
    }
}
