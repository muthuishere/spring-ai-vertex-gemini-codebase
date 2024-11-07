package apps.unstructured;

import com.google.cloud.storage.Blob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Media;
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
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/agent")
@Slf4j
public class ImageEmbeddingChatController {
    private final StorageBucketService storageBucketService;
    private final VectorStore vectorStore;
    private final VertexAiGeminiChatModel vertexAiGeminiChatModel;
    private final EmbeddingMediaProcessor embeddingMediaProcessor;

//       TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
//        documents = tokenTextSplitter.split(documents);


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


            List<Document>   documents = embeddingMediaProcessor.processPdfToDocuments(resource,metaData);


            vectorStore.add(documents);
            return ResponseEntity.status(HttpStatus.CREATED).body(documents);
        } catch (IOException e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }



    @PostMapping("/chat")
    public ChatBotResponse answerQuestion(@RequestBody InternalSearchRequest internalSearchRequest) {
        String question = internalSearchRequest.question();
        SearchRequest searchRequest = internalSearchRequest.getSearchRequest();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);


        // Need to add the reference URL in the context so that llm can generate the answer based on the context and also where it can download
        String contextHistory = documents.stream()
                .map(document ->  document.getContent() +
                                  System.lineSeparator() + "  Reference Page Number:" +  document.getMetadata().get("page").toString() +
                                  System.lineSeparator() + "  Reference File URL: http://localhost:8080/api/internal/agent/file/" +  document.getMetadata().get("fileID").toString()
                )
                .collect(Collectors.joining(System.lineSeparator()));

        List<Media> medias=documents.stream()
                .map(doc-> embeddingMediaProcessor.getMedia(doc))
                .collect(Collectors.toList());

        var systemPrompt = "You are an AI assistant. Provide the answer based solely on the provided context.\n\n";
        var userPrompt1 = "Question: " + question + "\n\n" +
                         "Below is the context history:\n\n" + contextHistory + "\n\n" +
                         "Please provide a detailed answer to the question based on the context history. In your response, include:\n" +
                         "- The answer to the question.\n" +
                         "- Any relevant reference URL(s).\n" +
                         "- Page numbers if available.\n\n" +
                         "Ensure that your answer is clear and references are properly cited.";;

        var userPrompt = "Question: " + question + "\n\n" + "Context:\n" + contextHistory + "\n\n" + "Please provide the answer to the question based on context history \n  Also add a reference url pointing to it based on context history";
        ;

        var systemMessage = new SystemMessage(systemPrompt);
        var userMessage = new UserMessage(userPrompt,medias);

        var chatClientRequest = ChatClient.builder(vertexAiGeminiChatModel)
                .build()
                .prompt()
                .messages(List.of(systemMessage,userMessage));

        var chatResponse = chatClientRequest.call().chatResponse();
        String answer = chatResponse.getResult().getOutput().getContent();

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
