package ai.zhidun.app.hub.tmpfile.service;

import ai.zhidun.app.hub.common.CustomIdentifierGenerator;
import ai.zhidun.app.hub.store.service.S3Service;
import ai.zhidun.app.hub.store.utils.FileParser;
import ai.zhidun.app.hub.store.utils.FileParser.ParsedResult;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.Tag;

import java.util.ArrayList;
import java.util.List;

@Service
public class TmpFileService {

    private final S3Service service;

    private final String bucket;

    private final FileParser parser;

    public TmpFileService(S3Service service, @Value("${s3.buckets.tmpfile}") String bucket, FileParser parser) {
        this.service = service;
        this.bucket = bucket;
        this.parser = parser;
    }

    private final Tag used = Tag.builder().key("used").value("true").build();
    private final Tag unused = Tag.builder().key("used").value("true").build();

    @PostConstruct
    public void init() {
        service.createBucketIfNotExists(bucket);

        //todo: set ttl from config
        service.setFilter(bucket, 1, unused);
    }

    public void setUnused(String key) {
        service.setTags(bucket, key, unused);
    }

    public List<UploadResult> upload(MultipartFile[] files) {
        List<UploadResult> results = new ArrayList<>();
        for (MultipartFile file : files) {
            ParsedResult result = parser.parse(file);

            String key = CustomIdentifierGenerator.uuidV7();
            service.put(bucket, key, result, used);

            String url = service.url(bucket, key);

            results.add(new UploadResult(url, key, result.fileName()));
        }
        return results;
    }
}
