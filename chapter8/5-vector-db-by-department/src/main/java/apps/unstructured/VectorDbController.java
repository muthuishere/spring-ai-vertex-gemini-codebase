package apps.unstructured;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class VectorDbController {
    final VectorStore vectorStore;

    @PostMapping("/api/vectordb/document")
    public Document saveDocumentToVectorStore(@RequestBody DocumentRequest documentRequest) {
        Document documentToBeSaved =documentRequest.getDocument();
        vectorStore.add(List.of(documentToBeSaved));
        return documentToBeSaved;
    }

    @PostMapping("/api/vectordb/documents")
    public List<Document> saveDocumentsToVectorStore(@RequestBody List<DocumentRequest> documentRequests)  {

        List<Document> documents= documentRequests.stream()
                .map(DocumentRequest::getDocument)
                .toList();
        vectorStore.add(documents);

        return documents;
    }

    @PostMapping("/api/vectordb/documents/search")
    public List<Document> handleSimilarSearchQuery(@RequestBody SimilaritySearchRequest similaritySearchRequest) {

        SearchRequest searchRequest = similaritySearchRequest.getSearchRequest();


        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        return documents;
    }
}
