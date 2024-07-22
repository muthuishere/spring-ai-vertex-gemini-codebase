package apps.springai;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import apps.springai.SimilaritySearchRequest;
import apps.springai.DocumentRequest;

@RestController
@RequiredArgsConstructor
@Slf4j
public class VectorDbController {



    final VectorStore vectorStore;

    final JdbcTemplate jdbcTemplate;

    @PostMapping("/api/vectordb/document")
    public Document saveDocumentToVectorStore(@RequestBody DocumentRequest documentRequest) {
        Document document = new Document(documentRequest.content());
        vectorStore.add(List.of(document));
        return document;
    }


    @PostMapping("/api/vectordb/documents")
    public List<Document> saveDocumentsToVectorStore(@RequestBody List<DocumentRequest> documentRequests)  {

        List<Document> documents= documentRequests.stream()
                .map(DocumentRequest::content)
                .map(Document::new)
                .toList();
        vectorStore.add(documents);

        return documents;
    }


    @DeleteMapping("/api/vectordb/documents")
    public ResponseEntity<String> deleteAllDocuments() {
        jdbcTemplate.execute("DELETE FROM vector_store");;
        return ResponseEntity.ok("All documents deleted");

    }


    @PostMapping("/api/vectordb/documents/search")
    public List<Document> handleSimilarSearchQuery(@RequestBody SimilaritySearchRequest similaritySearchRequest) {


        Double threshold = 1- similaritySearchRequest.maximumDistance();

        SearchRequest searchRequest = SearchRequest
                                        .query(similaritySearchRequest.question())
                                        .withTopK(similaritySearchRequest.limit())
                                        .withSimilarityThreshold(threshold);
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        return formatResults(documents);
    }

    private static List<Document> formatResults(List<Document> documents) {

        //return documents;

        return documents.stream().map(document -> {
            document.setEmbedding(document.getEmbedding().subList(0, 1));
            return document;
        }).collect(Collectors.toList());
    }


}