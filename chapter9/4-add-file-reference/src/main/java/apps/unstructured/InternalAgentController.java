package apps.unstructured;

import com.google.cloud.storage.Blob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.client.ChatClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private List<Document> readDocumentsWithTika(Resource resource, Map<String,Object> metaData) {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
        List<Document> documents = tikaDocumentReader.read();
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<Document> chunkedDocuments = tokenTextSplitter.split(documents);
        chunkedDocuments.forEach(document -> {

            document.getMetadata().putAll(metaData);
        });
        return chunkedDocuments;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestPart String category ,@RequestPart MultipartFile file) {


        try {
            Resource resource = file.getResource();
            String filename = file.getOriginalFilename();
            String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

            byte[] bytes = file.getBytes();
            String fileID = storageBucketService.uploadFile(filename, bytes);

            Map<String, Object> metaData = new HashMap<>();
            metaData.put("fileID", fileID);
            metaData.put("extension", extension);
            metaData.put("filename", filename);
            metaData.put("category", category);


            List<Document> documents = readDocumentsWithTika(resource,metaData);


            vectorStore.add(documents);
            return ResponseEntity.status(HttpStatus.CREATED).body(documents);
        } catch (IOException e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    @PostMapping("/chat-with-advisor")
    public ChatBotResponse answerQuestionWithAdvisor(@RequestBody InternalSearchRequest internalSearchRequest) {

        String question = internalSearchRequest.question();

        SearchRequest searchRequest = internalSearchRequest.getSearchRequest();

        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .build();

        var chatClientRequest = ChatClient.builder(vertexAiGeminiChatModel).build()
                .prompt()
                .advisors(questionAnswerAdvisor)
                .user(question);

        var chatResponse = chatClientRequest
                .call()
                .chatResponse();

        String answer = chatResponse.getResult().getOutput().getText();
        return new ChatBotResponse(question, answer);
    }
    @PostMapping("/chat")
    public ChatBotResponse answerQuestionWithoutAdvisor(@RequestBody InternalSearchRequest internalSearchRequest) {
        String question = internalSearchRequest.question();
        SearchRequest searchRequest = internalSearchRequest.getSearchRequest();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);


        // Need to add the reference URL in the context so that llm can generate the answer based on the context and also where it can download
        String contextHistory = documents.stream()
                .map(document ->  document.getText() + System.lineSeparator() + "  Reference URL: http://localhost:8080/api/internal/agent/file/" +  document.getMetadata().get("fileID").toString())
                .collect(Collectors.joining(System.lineSeparator()));

        var systemPrompt = "You are an AI assistant. Provide the answer based solely on the provided context.\n\n";
        var userPrompt = "Question: " + question + "\n\n" + "Context:\n" + contextHistory + "\n\n" + "Please provide the answer to the question based on context history \n  Also add a reference url pointing to it based on context history";
        ;

        var chatClientRequest = ChatClient.builder(vertexAiGeminiChatModel)
                .build()
                .prompt()
                .system(systemPrompt)
                .user(userPrompt);

        var chatResponse = chatClientRequest.call().chatResponse();
        String answer = chatResponse.getResult().getOutput().getText();

        return new ChatBotResponse(question, answer);
    }

    @GetMapping("/file/{fileID}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileID) {
        Blob blob = storageBucketService.getFile(fileID);

        if(blob == null){
            return ResponseEntity.notFound().build();
        }
        byte[] content = blob.getContent();
        ByteArrayResource resource = new ByteArrayResource(content);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileID + "\"")
                .body(resource);
    }
}
