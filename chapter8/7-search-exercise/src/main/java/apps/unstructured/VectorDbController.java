package apps.unstructured;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController

@Slf4j
public class VectorDbController {
    final VectorStore vectorStore;
    final VertexAiGeminiChatModel vertexAiGeminiChatModel;

    public VectorDbController(VectorStore vectorStore, VertexAiGeminiChatModel vertexAiGeminiChatModel) {
        this.vectorStore = vectorStore;
        this.vertexAiGeminiChatModel = vertexAiGeminiChatModel;
    }



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


    @PostMapping("/api/internaldata/chat")
    public ChatBotResponse answerQuestion(@RequestBody SimilaritySearchRequest similaritySearchRequest) {


        String question = similaritySearchRequest.question();

        SearchRequest searchRequest = similaritySearchRequest.getSearchRequest();

        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .build();

        var chatClientRequest = ChatClient.builder(vertexAiGeminiChatModel).build()
                .prompt()
                .advisors(questionAnswerAdvisor)
                .user(question);


        ChatResponse chatResponse = chatClientRequest
                .call()
                .chatResponse();

        String answer = chatResponse.getResult().getOutput().getText();

        return new ChatBotResponse(question, answer);

    }
}
