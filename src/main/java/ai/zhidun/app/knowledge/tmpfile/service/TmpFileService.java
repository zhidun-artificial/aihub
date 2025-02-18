package ai.zhidun.app.knowledge.tmpfile.service;

import ai.zhidun.app.knowledge.store.service.S3Service;
import ai.zhidun.app.knowledge.store.utils.FileParser;
import ai.zhidun.app.knowledge.store.utils.FileParser.Docx;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class TmpFileService {

    private final S3Service service;

    private final String bucket;

    public TmpFileService(S3Service service, @Value("${s3.buckets.tmpfile}") String bucket) {
        this.service = service;
        this.bucket = bucket;
    }

    @PostConstruct
    public void init() {
        service.createBucketIfNotExists(bucket);
    }

    public List<UploadResult> upload(MultipartFile[] files) {
        List<UploadResult> results = new ArrayList<>();
        for (MultipartFile file : files) {
            FileParser.ParsedResult result = FileParser.parse(file);

            String rawKey = result.newKey();
            service.put(bucket, rawKey, result);

            String rawUrl = service.url(bucket, rawKey);

            String url;
            String id;
            if (result instanceof Docx docx) {
                id = FileParser.toPdf(docx, (pdf) -> {
                    String pdfKey = pdf.newKey();
                    service.put(bucket, pdfKey, pdf);
                    return pdfKey;
                });
                url = service.url(bucket, id);
            } else {
                url = rawUrl;
                id = rawKey;
            }

            results.add(new UploadResult(url, rawUrl, id, result.fileName()));
        }
        return results;
    }
}
