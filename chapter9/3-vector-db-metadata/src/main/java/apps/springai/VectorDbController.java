package apps.springai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    final JdbcTemplate jdbcTemplate;

    private static List<Document> formatResults(List<Document> documents) {

        //return documents;

        return documents.stream().map(document -> {

            float[] trimmed  = new float[2];
            trimmed[0] = document.getEmbedding()[0];
            trimmed[1] = document.getEmbedding()[1];
            document.setEmbedding(trimmed);
            return document;
        }).collect(Collectors.toList());
    }

    @PostMapping("/api/vectordb/document")
    public Document saveDocumentToVectorStore(@RequestBody DocumentRequest documentRequest) {
        Document document = documentRequest.getDocument();
        vectorStore.add(List.of(document));
        return document;
    }

    @PostMapping("/api/vectordb/documents")
    public List<Document> saveDocumentsToVectorStore(@RequestBody List<DocumentRequest> documentRequests) {

        List<Document> documents = documentRequests.stream()
                .map(DocumentRequest::getDocument)
                .toList();
        vectorStore.add(documents);

        return documents;
    }

    @DeleteMapping("/api/vectordb/documents")
    public ResponseEntity<String> deleteAllDocuments() {
        jdbcTemplate.execute("DELETE FROM vector_store");
        ;
        return ResponseEntity.ok("All documents deleted");

    }

    @PostMapping("/api/vectordb/documents/search")
    public List<Document> handleSimilarSearchQuery(@RequestBody SimilaritySearchRequest similaritySearchRequest) {


        Filter.Expression expression =
                new FilterExpressionBuilder()
                        .eq("department", similaritySearchRequest.department())
                        .build();


//        SearchRequest searchRequest = SearchRequest
//                .query(similaritySearchRequest.question())
//                .withFilterExpression("department == 'IT'")
//                .withTopK(similaritySearchRequest.limit())
//                 .withSimilarityThreshold(similaritySearchRequest.getThreshold());


        SearchRequest searchRequest = SearchRequest
                .query(similaritySearchRequest.question())
                .withFilterExpression(expression)
                .withTopK(similaritySearchRequest.limit())
                .withSimilarityThreshold(similaritySearchRequest.getThreshold());


        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        return formatResults(documents);
    }


}