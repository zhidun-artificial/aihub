package ai.zhidun.app.hub.assistant;

import ai.zhidun.app.hub.tmpfile.service.UploadResult;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;

import java.util.Map;

public record FileContent(UploadResult result, TextSegment segment) implements Content {

    private static final DocumentParser DEFAULT_DOCUMENT_PARSER = new ApacheTikaDocumentParser(true);

    public static FileContent from(UploadResult result) {
        Document document = UrlDocumentLoader.load(result.url(), DEFAULT_DOCUMENT_PARSER);
        TextSegment segment = TextSegment.from(document.text());
        return new FileContent(result, segment);
    }

    public static Document from(String url) {
        return UrlDocumentLoader.load(url, DEFAULT_DOCUMENT_PARSER);
    }

    @Override
    public TextSegment textSegment() {
        return segment;
    }

    @Override
    public Map<ContentMetadata, Object> metadata() {
        return Map.of();
    }
}
