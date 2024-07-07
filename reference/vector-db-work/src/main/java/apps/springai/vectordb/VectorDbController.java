package apps.springai.vectordb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<Document> saveDocumentsToVectorStore(@RequestBody List<DocumentRequest> documentRequests)  throws Exception{

        List<Document> documents= documentRequests.stream().map(DocumentRequest::content).map(Document::new).toList();
       vectorStore.add(documents);

        return documents;
    }

    @DeleteMapping("/api/vectordb/documents")
    public ResponseEntity<String> deleteAllDocuments()  throws Exception{
        String sql = "DELETE FROM vector_store";
        int rowsAffected = jdbcTemplate.update(sql);
        return ResponseEntity.ok("Deleted "+rowsAffected+" rows");
    }


    @GetMapping("/api/vectordb/documents/search")
    public List<Document> handleQuery(@RequestParam(name = "question", required = true) String question) {
        SearchRequest searchRequest = SearchRequest.query(question);
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        //return documents;
        return documents.stream().map(document -> {
            document.setEmbedding(document.getEmbedding().subList(0, 2));
            return document;
        }).toList();
    }




}
