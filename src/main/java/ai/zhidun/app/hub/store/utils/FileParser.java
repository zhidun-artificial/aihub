package ai.zhidun.app.hub.store.utils;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

@Service
public class FileParser {

    private final ApacheTikaDocumentParser parser;

    public FileParser() {
        // todo maybe extract some config?
        parser = new ApacheTikaDocumentParser(true);
    }

    public sealed interface ParsedResult permits Success, Failure {

        String fileName();

        String contentType();

        InputStream getInputStream();
    }

    public record Success(Document document,
                          String contentType,
                          String fileName,
                          Supplier<InputStream> source) implements ParsedResult {

        public String content() {
            return document.text();
        }

        @Override
        public InputStream getInputStream() {
            return this.source.get();
        }
    }

    public record Failure(String message,
                          Throwable e,
                          String contentType,
                          String fileName,
                          Supplier<InputStream> source
    ) implements ParsedResult {

        @Override
        public InputStream getInputStream() {
            return this.source.get();
        }
    }

    @SneakyThrows
    public ParsedResult parse(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        try {
            Document document = this.parser.parse(file.getInputStream());
            return new Success(document, contentType, fileName, file::getInputStream);
        } catch (RuntimeException | IOException e) {
            return new Failure(e.getMessage(), e, contentType, fileName, file::getInputStream);
        }
    }

}
