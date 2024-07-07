package apps.springai.vectordb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class VectorDbController {



    final VectorStore vectorStore;


    @PostMapping("/api/vectordb/document")
    public ResponseEntity<Document> saveDocumentToVectorStore(@RequestBody DocumentRequest documentRequest) {
        Document document = new Document(documentRequest.content());

        vectorStore.add(List.of(document));

        vectorStore.similaritySearch(document, 5);

        log.info("Document saved to vector store: {}", document);
        log.info("" + document.getEmbedding());
        return ResponseEntity.ok(document);
    }






}
