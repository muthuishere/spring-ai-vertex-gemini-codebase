package apps.springai.vectordb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorDbService {


    final VectorStore vectorStore;


    public void deleteAll() {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.defaults());

        vectorStore.delete(documents.stream().map(Document::getId).toList());
    }

    public void addDocuments(String... inputs) {

        List<Document> documentsToBeInserted = new ArrayList<>();

        for (String input : inputs) {
            documentsToBeInserted.add(new Document(input));
        }

        vectorStore.add(documentsToBeInserted);
    }


    public void findDocument() {



        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                new Document("Spring AI can be used in PGVector Store!!", Map.of("meta1", "meta1")),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Add the documents to PGVector
        vectorStore.add(documents);

        // Retrieve documents similar to a query
        List<Document> results = vectorStore.similaritySearch(SearchRequest.query("Spring").withTopK(2));
        log.info("Results: " + results);



    }
}
