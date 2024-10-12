package apps.unstructured;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.ChatClient;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/agent")
@Slf4j
public class InternalAgentController {
    private final StorageBucketService storageBucketService;
    private final VectorStore vectorStore;
    private final VertexAiGeminiChatModel vertexAiGeminiChatModel;

//       TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
//        documents = tokenTextSplitter.split(documents);
    private List<Document> readDocumentsWithTika(Resource resource) {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
        List<Document> documents = tikaDocumentReader.read();
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<Document> chunkedDocuments = tokenTextSplitter.split(documents);
        return chunkedDocuments;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestPart MultipartFile file) {


        try {
            Resource resource = file.getResource();
            String filename = file.getOriginalFilename();
            byte[] bytes = file.getBytes();
            String fileID = storageBucketService.uploadFile(filename, bytes);

            List<Document> documents = readDocumentsWithTika(resource);
            documents.forEach(document -> document.getMetadata().put("fileID", fileID));

            vectorStore.add(documents);
            return ResponseEntity.status(HttpStatus.CREATED).body("Succesfully uploaded");
        } catch (IOException e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    @PostMapping("/chat")
    public ChatBotResponse answerQuestion(@RequestBody InternalSearchRequest internalSearchRequest) {

         String question = internalSearchRequest.question();
        SearchRequest searchRequest = SearchRequest.query(question);


        QuestionAnswerAdvisor questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore, searchRequest);
        var chatClientRequest = ChatClient.builder(vertexAiGeminiChatModel).build()
                .prompt()
                .advisors(questionAnswerAdvisor)
                .user(question);




        var chatResponse = chatClientRequest
                .call()
                .chatResponse();

        String answer = chatResponse.getResult().getOutput().getContent();




        return new ChatBotResponse(question, answer);
    }
}
