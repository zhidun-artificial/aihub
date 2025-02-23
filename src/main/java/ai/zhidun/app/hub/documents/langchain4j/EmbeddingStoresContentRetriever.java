package ai.zhidun.app.hub.documents.langchain4j;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.ArrayList;
import java.util.List;

public class EmbeddingStoresContentRetriever implements ContentRetriever {

    private final List<Content> directContents;

    private final List<EmbeddingStoreContentRetriever> retrievers;

    public EmbeddingStoresContentRetriever(List<Content> directContents, List<EmbeddingStoreContentRetriever> retrievers) {
        this.directContents = directContents;
        this.retrievers = retrievers;
    }

    @Override
    public List<Content> retrieve(Query query) {
        List<Content> contents = new ArrayList<>();

        //todo maybe we can parallelize this
        for (EmbeddingStoreContentRetriever retriever : retrievers) {
            contents.addAll(retriever.retrieve(query));
        }

        if (directContents != null) {
            contents.addAll(directContents);
        }

        return contents;
    }
}
