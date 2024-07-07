package apps.springai;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

record DocumentRequest(String content) {  }

@RestController
@RequiredArgsConstructor
@Slf4j
public class VectorDbController {



    final VectorStore vectorStore;

    @PostMapping("/api/vectordb/document")
    public Document saveDocumentToVectorStore(@RequestBody DocumentRequest documentRequest) {
        Document document = new Document(documentRequest.content());
        vectorStore.add(List.of(document));
        return document;
    }

}